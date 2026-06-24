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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Resolves field identifier strings from the jsonschema2pojo Value.$field property
 * to their corresponding PostgreSQL SQL expressions.
 *
 * <p>Handles $aasdesc and $smdesc field identifiers against the actual DB schema:
 * - aas_descriptors (with JSONB columns: endpoints, specific_asset_ids)
 * - submodel_descriptors (linked via aas_id FK, JSONB: endpoints, semantic_id)
 * - submodel_descriptors_standalone (JSONB: endpoints, semantic_id)
 */
public class FieldIdentifierResolver {

    // --- Table constants matching schema.dmp ---
    private static final String TBL_AAS_DESC = "aas_descriptors";
    private static final String TBL_AAS_DESC_ALIAS = "ad";
    private static final String TBL_SM_DESC = "submodel_descriptors";
    private static final String TBL_SM_DESC_ALIAS = "sd";
    private static final String TBL_SM_DESC_STANDALONE = "submodel_descriptors_standalone";
    private static final String TBL_SM_DESC_STANDALONE_ALIAS = "sds";

    // --- Regex patterns for $aasdesc ---
    private static final Pattern AASDESC_PATTERN = Pattern.compile("^\\$aasdesc#(.+)$");
    private static final Pattern SMDESC_PATTERN = Pattern.compile("^\\$smdesc#(.+)$");

    // Sub-patterns for field paths
    private static final Pattern SPECIFIC_ASSET_IDS_PATTERN = Pattern.compile("^specificAssetIds\\[(\\d*)\\]\\.(.+)$");
    private static final Pattern ENDPOINTS_PATTERN = Pattern.compile("^endpoints\\[(\\d*)\\]\\.(.+)$");
    private static final Pattern SM_DESCRIPTORS_PATTERN = Pattern.compile("^submodelDescriptors\\[(\\d*)\\]\\.(.+)$");
    private static final Pattern SEMANTIC_ID_KEYS_PATTERN = Pattern.compile("^semanticId\\.keys\\[(\\d*)\\]\\.(type|value)$");
    private static final Pattern EXTERNAL_SUBJECT_ID_KEYS_PATTERN = Pattern.compile("^externalSubjectId\\.keys\\[(\\d*)\\]\\.(type|value)$");

    /**
     * Main entry point: resolves a $field string from a Value object to a FieldMapping.
     *
     * @param fieldIdentifier the value of Value.getField(), e.g. "$aasdesc#idShort"
     * @return the FieldMapping describing how to query this field in PostgreSQL
     */
    public FieldMapping resolve(String fieldIdentifier) {
        Matcher aasMatcher = AASDESC_PATTERN.matcher(fieldIdentifier);
        if (aasMatcher.matches()) {
            return resolveAasDesc(aasMatcher.group(1));
        }

        Matcher smMatcher = SMDESC_PATTERN.matcher(fieldIdentifier);
        if (smMatcher.matches()) {
            return resolveSmDesc(smMatcher.group(1));
        }

        throw new UnsupportedOperationException(
                "Field identifier not supported for DB mapping: " + fieldIdentifier);
    }

    // =========================================================================
    // $aasdesc resolution
    // =========================================================================


    private FieldMapping resolveAasDesc(String path) {
        // Direct columns on aas_descriptors
        switch (path) {
            case "id" -> {
                return directAasDesc("id");
            }
            case "idShort" -> {
                return directAasDesc("id_short");
            }
            case "assetKind" -> {
                return FieldMapping.builder()
                        .baseTable(TBL_AAS_DESC)
                        .baseTableAlias(TBL_AAS_DESC_ALIAS)
                        .sqlExpression(TBL_AAS_DESC_ALIAS + ".asset_kind")
                        .accessType(FieldMapping.AccessType.DIRECT)
                        .requiresCast(true)
                        .castType("ASSET_KIND_ENUM")
                        .build();
            }
            case "assetType" -> {
                return directAasDesc("asset_type");
            }
            case "globalAssetId" -> {
                return directAasDesc("global_asset_id");
            }
        }

        // specificAssetIds[<idx>].<attr>
        Matcher specificMatcher = SPECIFIC_ASSET_IDS_PATTERN.matcher(path);
        if (specificMatcher.matches()) {
            return resolveAasDescSpecificAssetIds(specificMatcher.group(1), specificMatcher.group(2));
        }

        // endpoints[<idx>].<attr>
        Matcher endpointMatcher = ENDPOINTS_PATTERN.matcher(path);
        if (endpointMatcher.matches()) {
            return resolveAasDescEndpoints(endpointMatcher.group(1), endpointMatcher.group(2));
        }

        // submodelDescriptors[<idx>].<attr>
        Matcher smDescMatcher = SM_DESCRIPTORS_PATTERN.matcher(path);
        if (smDescMatcher.matches()) {
            return resolveAasDescSubmodelDescriptors(smDescMatcher.group(2));
        }

        throw new IllegalArgumentException("Unsupported $aasdesc field path: " + path);
    }


    private FieldMapping directAasDesc(String column) {
        return FieldMapping.builder()
                .baseTable(TBL_AAS_DESC)
                .baseTableAlias(TBL_AAS_DESC_ALIAS)
                .sqlExpression(TBL_AAS_DESC_ALIAS + "." + column)
                .accessType(FieldMapping.AccessType.DIRECT)
                .build();
    }


    /**
     * Resolves specificAssetIds[idx].attrPath against JSONB column specific_asset_ids.
     *
     * <p>JSONB structure: [{"name":"...", "value":"...", "externalSubjectId": {"type":"...", "keys":[...]}}]
     */
    private FieldMapping resolveAasDescSpecificAssetIds(String indexStr, String attrPath) {
        String col = TBL_AAS_DESC_ALIAS + ".specific_asset_ids";

        if (indexStr.isEmpty()) {
            // "Any element" notation [] → use jsonb_array_elements
            String lateralAlias = "said_elem";
            String lateral = "CROSS JOIN LATERAL jsonb_array_elements(" + col + ") AS " + lateralAlias;
            String expr = resolveSpecificAssetIdAttr(lateralAlias, attrPath);
            return FieldMapping.builder()
                    .baseTable(TBL_AAS_DESC)
                    .baseTableAlias(TBL_AAS_DESC_ALIAS)
                    .sqlExpression(expr)
                    .lateralClause(lateral)
                    .accessType(FieldMapping.AccessType.JSONB_ARRAY_ANY)
                    .build();
        }
        else {
            // Specific index
            int idx = Integer.parseInt(indexStr);
            String base = col + "->" + idx;
            String expr = resolveSpecificAssetIdAttrFromBase(base, attrPath);
            return FieldMapping.builder()
                    .baseTable(TBL_AAS_DESC)
                    .baseTableAlias(TBL_AAS_DESC_ALIAS)
                    .sqlExpression(expr)
                    .accessType(FieldMapping.AccessType.JSONB_TEXT)
                    .build();
        }
    }


    private String resolveSpecificAssetIdAttr(String elemAlias, String attrPath) {
        switch (attrPath) {
            case "name" -> {
                return elemAlias + "->>'name'";
            }
            case "value" -> {
                return elemAlias + "->>'value'";
            }
            case "externalSubjectId" -> {
                return elemAlias + "->'externalSubjectId'->'keys'->0->>'value'";
            }
            case "externalSubjectId.type" -> {
                return elemAlias + "->'externalSubjectId'->>'type'";
            }
        }
        // externalSubjectId.keys[<idx>].(type|value)
        Matcher m = EXTERNAL_SUBJECT_ID_KEYS_PATTERN.matcher(attrPath);
        if (m.matches()) {
            String keyIdx = m.group(1);
            String keyAttr = m.group(2);
            if (keyIdx.isEmpty()) {
                // need nested lateral - simplified: use jsonb_path_query
                return "jsonb_path_query_first(" + elemAlias
                        + "->'externalSubjectId'->'keys', '$[*]." + keyAttr + "')#>>'{}'";
            }
            return elemAlias + "->'externalSubjectId'->'keys'->" + keyIdx + "->>'" + keyAttr + "'";
        }
        throw new IllegalArgumentException("Unknown specificAssetIds attribute: " + attrPath);
    }


    private String resolveSpecificAssetIdAttrFromBase(String base, String attrPath) {
        switch (attrPath) {
            case "name" -> {
                return base + "->>'name'";
            }
            case "value" -> {
                return base + "->>'value'";
            }
            case "externalSubjectId" -> {
                return base + "->'externalSubjectId'->'keys'->0->>'value'";
            }
            case "externalSubjectId.type" -> {
                return base + "->'externalSubjectId'->>'type'";
            }
        }
        Matcher m = EXTERNAL_SUBJECT_ID_KEYS_PATTERN.matcher(attrPath);
        if (m.matches()) {
            String keyIdx = m.group(1);
            String keyAttr = m.group(2);
            if (keyIdx.isEmpty()) {
                return "jsonb_path_query_first(" + base
                        + "->'externalSubjectId'->'keys', '$[*]." + keyAttr + "')#>>'{}'";
            }
            return base + "->'externalSubjectId'->'keys'->" + keyIdx + "->>'" + keyAttr + "'";
        }
        throw new IllegalArgumentException("Unknown specificAssetIds attribute: " + attrPath);
    }


    /**
     * Resolves endpoints[idx].attrPath against JSONB column endpoints.
     *
     * <p>JSONB structure: [{"interface":"...", "protocolInformation":{"href":"..."}}]
     */
    private FieldMapping resolveAasDescEndpoints(String indexStr, String attrPath) {
        String col = TBL_AAS_DESC_ALIAS + ".endpoints";
        return resolveEndpointJsonb(col, indexStr, attrPath,
                TBL_AAS_DESC, TBL_AAS_DESC_ALIAS, "ep_aas_elem");
    }


    private FieldMapping resolveEndpointJsonb(String col, String indexStr, String attrPath,
                                              String baseTable, String baseAlias,
                                              String lateralAlias) {
        String leafExpr = endpointAttrExpression(attrPath);

        if (indexStr.isEmpty()) {
            // Any element → lateral
            String lateral = "CROSS JOIN LATERAL jsonb_array_elements(" + col + ") AS " + lateralAlias;
            String expr = lateralAlias + leafExpr;
            return FieldMapping.builder()
                    .baseTable(baseTable)
                    .baseTableAlias(baseAlias)
                    .sqlExpression(expr)
                    .lateralClause(lateral)
                    .accessType(FieldMapping.AccessType.JSONB_ARRAY_ANY)
                    .build();
        }
        else {
            int idx = Integer.parseInt(indexStr);
            String expr = col + "->" + idx + leafExpr;
            return FieldMapping.builder()
                    .baseTable(baseTable)
                    .baseTableAlias(baseAlias)
                    .sqlExpression(expr)
                    .accessType(FieldMapping.AccessType.JSONB_TEXT)
                    .build();
        }
    }


    private String endpointAttrExpression(String attrPath) {
        return switch (attrPath) {
            case "interface" -> "->>'interface'";
            case "protocolinformation.href" -> "->'protocolInformation'->>'href'";
            default -> throw new IllegalArgumentException("Unknown endpoint attribute: " + attrPath);
        };
    }


    /**
     * Resolves submodelDescriptors[idx].attrPath.
     * This requires a JOIN from aas_descriptors to submodel_descriptors table.
     */
    private FieldMapping resolveAasDescSubmodelDescriptors(String innerPath) {
        // JOIN clause from aas_descriptors → submodel_descriptors
        String joinClause = "JOIN " + TBL_SM_DESC + " " + TBL_SM_DESC_ALIAS
                + " ON " + TBL_SM_DESC_ALIAS + ".aas_id = " + TBL_AAS_DESC_ALIAS + ".id";

        // Resolve the inner path against submodel_descriptors columns
        String sqlExpr = resolveSmDescInnerPath(innerPath, TBL_SM_DESC_ALIAS);

        // If the inner path itself requires a lateral (e.g., endpoints[])
        String lateral = resolveSmDescInnerLateral(innerPath, TBL_SM_DESC_ALIAS);

        return FieldMapping.builder()
                .baseTable(TBL_AAS_DESC)
                .baseTableAlias(TBL_AAS_DESC_ALIAS)
                .sqlExpression(sqlExpr)
                .joinClause(joinClause)
                .lateralClause(lateral)
                .accessType(lateral != null
                        ? FieldMapping.AccessType.JSONB_ARRAY_ANY
                        : FieldMapping.AccessType.JOIN_REQUIRED)
                .build();
    }

    // =========================================================================
    // $smdesc resolution (submodel_descriptors_standalone)
    // =========================================================================


    private FieldMapping resolveSmDesc(String path) {
        // Direct columns
        switch (path) {
            case "id" -> {
                return directSmDescStandalone("id");
            }
            case "idShort" -> {
                return directSmDescStandalone("id_short");
            }
            case "semanticId" -> {
                // Shortcut: semanticId → keys[0].value
                return FieldMapping.builder()
                        .baseTable(TBL_SM_DESC_STANDALONE)
                        .baseTableAlias(TBL_SM_DESC_STANDALONE_ALIAS)
                        .sqlExpression(TBL_SM_DESC_STANDALONE_ALIAS
                                + ".semantic_id->'keys'->0->>'value'")
                        .accessType(FieldMapping.AccessType.JSONB_TEXT)
                        .build();
            }
            case "semanticId.type" -> {
                return FieldMapping.builder()
                        .baseTable(TBL_SM_DESC_STANDALONE)
                        .baseTableAlias(TBL_SM_DESC_STANDALONE_ALIAS)
                        .sqlExpression(TBL_SM_DESC_STANDALONE_ALIAS
                                + ".semantic_id->>'type'")
                        .accessType(FieldMapping.AccessType.JSONB_TEXT)
                        .build();
            }
        }

        // semanticId.keys[<idx>].(type|value)
        Matcher semKeysMatcher = SEMANTIC_ID_KEYS_PATTERN.matcher(path);
        if (semKeysMatcher.matches()) {
            return resolveSmDescSemanticIdKeys(semKeysMatcher.group(1), semKeysMatcher.group(2));
        }

        // endpoints[<idx>].<attr>
        Matcher endpointMatcher = ENDPOINTS_PATTERN.matcher(path);
        if (endpointMatcher.matches()) {
            String col = TBL_SM_DESC_STANDALONE_ALIAS + ".endpoints";
            return resolveEndpointJsonb(col, endpointMatcher.group(1), endpointMatcher.group(2),
                    TBL_SM_DESC_STANDALONE, TBL_SM_DESC_STANDALONE_ALIAS, "ep_sds_elem");
        }

        throw new IllegalArgumentException("Unsupported $smdesc field path: " + path);
    }


    private FieldMapping directSmDescStandalone(String column) {
        return FieldMapping.builder()
                .baseTable(TBL_SM_DESC_STANDALONE)
                .baseTableAlias(TBL_SM_DESC_STANDALONE_ALIAS)
                .sqlExpression(TBL_SM_DESC_STANDALONE_ALIAS + "." + column)
                .accessType(FieldMapping.AccessType.DIRECT)
                .build();
    }


    private FieldMapping resolveSmDescSemanticIdKeys(String indexStr, String attr) {
        String base = TBL_SM_DESC_STANDALONE_ALIAS + ".semantic_id->'keys'";

        if (indexStr.isEmpty()) {
            // Any key → lateral
            String lateralAlias = "semkey_elem";
            String lateral = "CROSS JOIN LATERAL jsonb_array_elements("
                    + base + ") AS " + lateralAlias;
            String expr = lateralAlias + "->>'" + attr + "'";
            return FieldMapping.builder()
                    .baseTable(TBL_SM_DESC_STANDALONE)
                    .baseTableAlias(TBL_SM_DESC_STANDALONE_ALIAS)
                    .sqlExpression(expr)
                    .lateralClause(lateral)
                    .accessType(FieldMapping.AccessType.JSONB_ARRAY_ANY)
                    .build();
        }
        else {
            int idx = Integer.parseInt(indexStr);
            String expr = base + "->" + idx + "->>'" + attr + "'";
            return FieldMapping.builder()
                    .baseTable(TBL_SM_DESC_STANDALONE)
                    .baseTableAlias(TBL_SM_DESC_STANDALONE_ALIAS)
                    .sqlExpression(expr)
                    .accessType(FieldMapping.AccessType.JSONB_TEXT)
                    .build();
        }
    }

    // =========================================================================
    // Helper: resolve inner submodel descriptor paths (used for both contexts)
    // =========================================================================


    private String resolveSmDescInnerPath(String path, String alias) {
        switch (path) {
            case "id" -> {
                return alias + ".id";
            }
            case "idShort" -> {
                return alias + ".id_short";
            }
            case "semanticId" -> {
                return alias + ".semantic_id->'keys'->0->>'value'";
            }
            case "semanticId.type" -> {
                return alias + ".semantic_id->>'type'";
            }
        }
        Matcher semKeysMatcher = SEMANTIC_ID_KEYS_PATTERN.matcher(path);
        if (semKeysMatcher.matches()) {
            String idxStr = semKeysMatcher.group(1);
            String attr = semKeysMatcher.group(2);
            if (idxStr.isEmpty()) {
                // handled via lateral
                return "sd_semkey_elem->>'" + attr + "'";
            }
            return alias + ".semantic_id->'keys'->" + idxStr + "->>'" + attr + "'";
        }
        Matcher epMatcher = ENDPOINTS_PATTERN.matcher(path);
        if (epMatcher.matches()) {
            String idxStr = epMatcher.group(1);
            String attrPath = epMatcher.group(2);
            String leaf = endpointAttrExpression(attrPath);
            if (idxStr.isEmpty()) {
                return "sd_ep_elem" + leaf;
            }
            return alias + ".endpoints->" + idxStr + leaf;
        }
        throw new IllegalArgumentException("Unknown submodelDescriptor inner path: " + path);
    }


    private String resolveSmDescInnerLateral(String path, String alias) {
        Matcher semKeysMatcher = SEMANTIC_ID_KEYS_PATTERN.matcher(path);
        if (semKeysMatcher.matches() && semKeysMatcher.group(1).isEmpty()) {
            return "CROSS JOIN LATERAL jsonb_array_elements("
                    + alias + ".semantic_id->'keys') AS sd_semkey_elem";
        }
        Matcher epMatcher = ENDPOINTS_PATTERN.matcher(path);
        if (epMatcher.matches() && epMatcher.group(1).isEmpty()) {
            return "CROSS JOIN LATERAL jsonb_array_elements("
                    + alias + ".endpoints) AS sd_ep_elem";
        }
        return null;
    }
}
