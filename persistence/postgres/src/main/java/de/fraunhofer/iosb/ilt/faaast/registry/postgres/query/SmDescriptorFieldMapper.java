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
 * Maps $smdesc FieldIdentifiers to SQL constructs.
 *
 * <p>Grammar:
 * <FieldIdentifierSmDescriptor> ::= "$smdesc#" <SmDescriptorClause>
 * <SmDescriptorClause> ::= ( <SemanticIdClause> | "idShort" | "id" |
 * "endpoints" ("[" [0-9]* "]") "." <EndpointClause> )
 * <EndpointClause> ::= "interface" | "protocolinformation.href"
 * <SemanticIdClause> ::= ( "semanticId" | "semanticId." <ReferenceClause> )
 * <ReferenceClause> ::= ( "type" | "keys" ("[" [0-9]* "]") (".type" | ".value") )
 */
public class SmDescriptorFieldMapper {

    private static final String BASE_TABLE = "submodel_descriptor";
    private static final String BASE_ALIAS = "smd";

    private static final Pattern ENDPOINT_PATTERN = Pattern.compile("endpoints\\[(\\d*)\\]\\.(interface|protocolinformation\\.href)");

    private static final Pattern SEMANTIC_ID_KEYS_PATTERN = Pattern.compile("semanticId\\.keys\\[(\\d*)\\]\\.(type|value)");

    private static final Pattern SEMANTIC_ID_TYPE_PATTERN = Pattern.compile("semanticId\\.type");

    /**
     * Resolves a $smdesc field identifier path to its SQL mapping.
     *
     * @param fieldPath the path after "$smdesc#", e.g., "id", "semanticId.keys[0].value"
     * @return the resolved SqlMapping
     */
    public SqlMapping resolve(String fieldPath) {
        if (fieldPath == null || fieldPath.isBlank()) {
            throw new IllegalArgumentException("Field path must not be empty for $smdesc");
        }

        // Direct attributes
        switch (fieldPath) {
            case "id" -> {
                return directMapping("id");
            }
            case "idShort" -> {
                return directMapping("id_short");
            }
            case "semanticId" -> {
                // Comparing semanticId as a whole (serialized reference)
                return resolveSemanticIdDirect();
            }
        }

        // semanticId.type
        if (SEMANTIC_ID_TYPE_PATTERN.matcher(fieldPath).matches()) {
            return resolveSemanticIdType();
        }

        // semanticId.keys[<idx>].(type|value)
        Matcher semanticKeysMatcher = SEMANTIC_ID_KEYS_PATTERN.matcher(fieldPath);
        if (semanticKeysMatcher.matches()) {
            return resolveSemanticIdKeys(semanticKeysMatcher);
        }

        // endpoints[<idx>].(interface|protocolinformation.href)
        Matcher endpointMatcher = ENDPOINT_PATTERN.matcher(fieldPath);
        if (endpointMatcher.matches()) {
            return resolveEndpoint(endpointMatcher);
        }

        throw new IllegalArgumentException("Unsupported $smdesc field path: " + fieldPath);
    }


    private SqlMapping directMapping(String column) {
        return new SqlMapping(BASE_TABLE, BASE_ALIAS, column, new ArrayList<>(), null);
    }


    private SqlMapping resolveSemanticIdDirect() {
        // Join to semantic_id table; compare full serialized reference
        List<JoinClause> joins = new ArrayList<>();
        String semAlias = "sem";
        joins.add(new JoinClause(JoinClause.JoinType.LEFT,
                "semantic_id", semAlias,
                semAlias + ".submodel_descriptor_id = " + BASE_ALIAS + ".id"));
        return new SqlMapping(BASE_TABLE, BASE_ALIAS, "id", joins, null);
    }


    private SqlMapping resolveSemanticIdType() {
        List<JoinClause> joins = new ArrayList<>();
        String semAlias = "sem";
        joins.add(new JoinClause(JoinClause.JoinType.INNER,
                "semantic_id", semAlias,
                semAlias + ".submodel_descriptor_id = " + BASE_ALIAS + ".id"));
        return new SqlMapping(BASE_TABLE, BASE_ALIAS, "type", joins, null);
    }


    private SqlMapping resolveSemanticIdKeys(Matcher matcher) {
        String indexStr = matcher.group(1);
        String attribute = matcher.group(2);
        Integer index = indexStr.isEmpty() ? null : Integer.valueOf(indexStr);

        List<JoinClause> joins = new ArrayList<>();

        // Join to semantic_id
        String semAlias = "sem";
        joins.add(new JoinClause(JoinClause.JoinType.INNER,
                "semantic_id", semAlias,
                semAlias + ".submodel_descriptor_id = " + BASE_ALIAS + ".id"));

        // Join to semantic_id_key (reference_key)
        String keyAlias = "semk";
        String keyOn = keyAlias + ".semantic_id_id = " + semAlias + ".id";
        String keyAdditional = index != null ? keyAlias + ".key_index = " + index : null;
        joins.add(new JoinClause(JoinClause.JoinType.INNER,
                "semantic_id_key", keyAlias, keyOn, keyAdditional));

        String column = attribute.equals("type") ? "type" : "value";
        return new SqlMapping(BASE_TABLE, BASE_ALIAS, column, joins, index);
    }


    private SqlMapping resolveEndpoint(Matcher matcher) {
        String indexStr = matcher.group(1);
        String attribute = matcher.group(2);
        Integer index = indexStr.isEmpty() ? null : Integer.valueOf(indexStr);

        List<JoinClause> joins = new ArrayList<>();
        String joinAlias = "ep_sm";
        String onCondition = joinAlias + ".submodel_descriptor_id = " + BASE_ALIAS + ".id";
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
}
