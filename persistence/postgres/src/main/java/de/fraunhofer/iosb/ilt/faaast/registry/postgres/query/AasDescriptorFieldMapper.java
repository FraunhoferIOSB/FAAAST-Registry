/*
 * Copyright (c) 2021 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.faaast.registry.postgres.query;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Maps $aasdesc FieldIdentifiers to SQL constructs.
 *
 * <p>Grammar:
 * <FieldIdentifierAasDescriptor> ::= "$aasdesc#" ( "idShort" | "id" | "assetKind" |
 * "assetType" | "globalAssetId" | <SpecificAssetIdsClause> |
 * "endpoints" ("[" [0-9]* "]") "." <EndpointClause> |
 * "submodelDescriptors" ("[" [0-9]* "]") "." <SmDescriptorClause> )
 */
public class AasDescriptorFieldMapper {

    private static final String BASE_TABLE = "aas_descriptor";
    private static final String BASE_ALIAS = "ad";

    // Regex patterns for parsing
    private static final Pattern SPECIFIC_ASSET_ID_PATTERN = Pattern.compile("specificAssetIds\\[(\\d*)\\]\\.(name|value|externalSubjectId(?:\\.(.+))?)");

    private static final Pattern ENDPOINT_PATTERN = Pattern.compile("endpoints\\[(\\d*)\\]\\.(interface|protocolinformation\\.href)");

    private static final Pattern SM_DESCRIPTOR_PATTERN = Pattern.compile("submodelDescriptors\\[(\\d*)\\]\\.(.+)");

    private final SmDescriptorFieldMapper smDescriptorMapper;

    public AasDescriptorFieldMapper() {
        this.smDescriptorMapper = new SmDescriptorFieldMapper();
    }


    /**
     * Resolves a $aasdesc field identifier path to its SQL mapping.
     *
     * @param fieldPath the path after "$aasdesc#", e.g., "idShort", "endpoints[0].interface"
     * @return the resolved SqlMapping
     */
    public SqlMapping resolve(String fieldPath) {
        if (fieldPath == null || fieldPath.isBlank()) {
            throw new IllegalArgumentException("Field path must not be empty for $aasdesc");
        }

        // Direct attributes on aas_descriptor table
        switch (fieldPath) {
            case "id":
                return directMapping("id");
            case "idShort":
                return directMapping("id_short");
            case "assetKind":
                return directMapping("asset_kind");
            case "assetType":
                return directMapping("asset_type");
            case "globalAssetId":
                return directMapping("global_asset_id");
        }

        // specificAssetIds[<idx>].(name|value|externalSubjectId)
        Matcher specificMatcher = SPECIFIC_ASSET_ID_PATTERN.matcher(fieldPath);
        if (specificMatcher.matches()) {
            return resolveSpecificAssetId(specificMatcher);
        }

        // endpoints[<idx>].(interface|protocolinformation.href)
        Matcher endpointMatcher = ENDPOINT_PATTERN.matcher(fieldPath);
        if (endpointMatcher.matches()) {
            return resolveEndpoint(endpointMatcher);
        }

        // submodelDescriptors[<idx>].<SmDescriptorClause>
        Matcher smDescMatcher = SM_DESCRIPTOR_PATTERN.matcher(fieldPath);
        if (smDescMatcher.matches()) {
            return resolveSubmodelDescriptor(smDescMatcher);
        }

        throw new IllegalArgumentException("Unsupported $aasdesc field path: " + fieldPath);
    }


    private SqlMapping directMapping(String column) {
        return new SqlMapping(BASE_TABLE, BASE_ALIAS, column, new ArrayList<>(), null);
    }


    private SqlMapping resolveSpecificAssetId(Matcher matcher) {
        String indexStr = matcher.group(1);
        String attribute = matcher.group(2);
        Integer index = indexStr.isEmpty() ? null : Integer.valueOf(indexStr);

        List<JoinClause> joins = new ArrayList<>();
        String joinAlias = "said";
        String onCondition = joinAlias + ".aas_descriptor_id = " + BASE_ALIAS + ".id";
        String additionalCond = index != null ? joinAlias + ".order_index = " + index : null;

        joins.add(new JoinClause(JoinClause.JoinType.INNER,
                "specific_asset_id", joinAlias, onCondition, additionalCond));

        String column;
        if (attribute.equals("name")) {
            column = "name";
        }
        else if (attribute.equals("value")) {
            column = "value";
        }
        else if (attribute.startsWith("externalSubjectId")) {
            // externalSubjectId or externalSubjectId.<ReferenceClause>
            String refPart = matcher.group(3);
            if (refPart == null || refPart.isEmpty()) {
                column = "external_subject_id";
            }
            else {
                // Join to reference/keys table for externalSubjectId
                return resolveExternalSubjectIdReference(joins, joinAlias, refPart, index);
            }
        }
        else {
            throw new IllegalArgumentException("Unknown specificAssetIds attribute: " + attribute);
        }

        return new SqlMapping(BASE_TABLE, BASE_ALIAS, column, joins, index);
    }


    private SqlMapping resolveExternalSubjectIdReference(List<JoinClause> existingJoins,
                                                         String parentAlias,
                                                         String refClause,
                                                         Integer index) {
        // externalSubjectId.type or externalSubjectId.keys[<idx>].(type|value)
        String refAlias = "esid_ref";
        existingJoins.add(new JoinClause(JoinClause.JoinType.INNER,
                "reference", refAlias,
                refAlias + ".id = " + parentAlias + ".external_subject_id_ref_id"));

        if (refClause.equals("type")) {
            return new SqlMapping(BASE_TABLE, BASE_ALIAS, "type", existingJoins, index);
        }

        // keys[<idx>].(type|value)
        Pattern keysPattern = Pattern.compile("keys\\[(\\d*)\\]\\.(type|value)");
        Matcher keysMatcher = keysPattern.matcher(refClause);
        if (keysMatcher.matches()) {
            String keyIndexStr = keysMatcher.group(1);
            String keyAttr = keysMatcher.group(2);
            Integer keyIndex = keyIndexStr.isEmpty() ? null : Integer.valueOf(keyIndexStr);

            String keyAlias = "esid_key";
            String keyOn = keyAlias + ".reference_id = " + refAlias + ".id";
            String keyAdditional = keyIndex != null ? keyAlias + ".key_index = " + keyIndex : null;
            existingJoins.add(new JoinClause(JoinClause.JoinType.INNER,
                    "reference_key", keyAlias, keyOn, keyAdditional));

            String column = keyAttr.equals("type") ? "type" : "value";
            return new SqlMapping(BASE_TABLE, BASE_ALIAS, column, existingJoins, index);
        }

        throw new IllegalArgumentException("Unsupported externalSubjectId reference clause: " + refClause);
    }


    private SqlMapping resolveEndpoint(Matcher matcher) {
        String indexStr = matcher.group(1);
        String attribute = matcher.group(2);
        Integer index = indexStr.isEmpty() ? null : Integer.valueOf(indexStr);

        List<JoinClause> joins = new ArrayList<>();
        String joinAlias = "ep_aas";
        String onCondition = joinAlias + ".aas_descriptor_id = " + BASE_ALIAS + ".id";
        String additionalCond = index != null ? joinAlias + ".order_index = " + index : null;

        joins.add(new JoinClause(JoinClause.JoinType.INNER,
                "endpoint", joinAlias, onCondition, additionalCond));

        String column = switch (attribute) {
            case "interface" -> "interface_name";
            case "protocolinformation.href" -> "href";
            default -> throw new IllegalArgumentException("Unknown endpoint attribute: " + attribute);
        };

        return new SqlMapping(BASE_TABLE, BASE_ALIAS, column, joins, index);
    }


    private SqlMapping resolveSubmodelDescriptor(Matcher matcher) {
        String indexStr = matcher.group(1);
        String smFieldPath = matcher.group(2);
        Integer index = indexStr.isEmpty() ? null : Integer.valueOf(indexStr);

        // Resolve the inner SmDescriptorClause via SmDescriptorFieldMapper,
        // then prepend a join from aas_descriptor -> submodel_descriptor
        SqlMapping innerMapping = smDescriptorMapper.resolve(smFieldPath);

        List<JoinClause> joins = new ArrayList<>();
        String smAlias = "sd";
        String onCondition = smAlias + ".aas_descriptor_id = " + BASE_ALIAS + ".id";
        String additionalCond = index != null ? smAlias + ".order_index = " + index : null;

        joins.add(new JoinClause(JoinClause.JoinType.INNER,
                "submodel_descriptor", smAlias, onCondition, additionalCond));

        // Append inner joins (adjusting references)
        for (JoinClause innerJoin: innerMapping.getJoins()) {
            joins.add(remapJoin(innerJoin, "smd", smAlias));
        }

        String column = innerMapping.getColumn();
        if (innerMapping.getJoins().isEmpty()) {
            // Column is directly on submodel_descriptor
            return new SqlMapping(BASE_TABLE, BASE_ALIAS, column, joins, index);
        }

        return new SqlMapping(BASE_TABLE, BASE_ALIAS, column, joins, index);
    }


    private JoinClause remapJoin(JoinClause original, String oldBaseAlias, String newBaseAlias) {
        String remappedOn = original.getOnCondition().replace(oldBaseAlias + ".", newBaseAlias + ".");
        return new JoinClause(original.getJoinType(), original.getTable(),
                original.getAlias() + "_nested", remappedOn, original.getAdditionalCondition());
    }
}
