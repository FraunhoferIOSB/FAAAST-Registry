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
 * Main mapper class that translates AAS Query Language expressions into SQL WHERE clauses.
 * Supports $aasdesc and $smdesc field identifiers.
 *
 * <p>Usage example:
 * QueryLanguageToSqlMapper mapper = new QueryLanguageToSqlMapper();
 * SqlQueryResult result = mapper.mapComparison("$smdesc#semanticId.keys[0].value", "$eq", "0173-1#01-AHD205#001");
 * // result.toSql() -> full SQL SELECT statement
 */
public class QueryLanguageToSqlMapper {

    private static final Pattern FIELD_IDENTIFIER_PATTERN = Pattern.compile("\\$(aasdesc|smdesc)#(.+)");

    private final AasDescriptorFieldMapper aasDescMapper;
    private final SmDescriptorFieldMapper smDescMapper;

    public QueryLanguageToSqlMapper() {
        this.aasDescMapper = new AasDescriptorFieldMapper();
        this.smDescMapper = new SmDescriptorFieldMapper();
    }


    /**
     * Parses a field identifier string and resolves it to an SQL mapping.
     *
     * @param fieldIdentifier e.g., "$aasdesc#idShort" or "$smdesc#semanticId.keys[0].value"
     * @return the resolved SqlMapping
     */
    public SqlMapping resolveField(String fieldIdentifier) {
        Matcher matcher = FIELD_IDENTIFIER_PATTERN.matcher(fieldIdentifier);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid field identifier: " + fieldIdentifier);
        }

        String root = matcher.group(1); // "aasdesc" or "smdesc"
        String path = matcher.group(2); // everything after "#"

        return switch (root) {
            case "aasdesc" -> aasDescMapper.resolve(path);
            case "smdesc" -> smDescMapper.resolve(path);
            default -> throw new IllegalArgumentException("Unsupported root: $" + root);
        };
    }


    /**
     * Maps a single comparison expression to a full SQL query.
     *
     * @param fieldIdentifier the left-hand operand (field identifier)
     * @param operator the comparison operator token (e.g., "$eq", "$contains")
     * @param value the literal value to compare against
     * @return a SqlQueryResult containing the complete SQL statement
     */
    public SqlQueryResult mapComparison(String fieldIdentifier, String operator, String value) {
        SqlMapping mapping = resolveField(fieldIdentifier);
        ComparisonOperator op = ComparisonOperator.fromToken(operator);

        String qualifiedColumn = mapping.getQualifiedColumn();
        String sqlOperator = op.getSqlOperator();
        String paramValue = isStringOperator(op) ? op.wrapValueForLike(value) : value;

        // Build WHERE clause
        String whereClause = qualifiedColumn + " " + sqlOperator + " ?";

        return new SqlQueryResult(mapping, whereClause, List.of(paramValue));
    }


    /**
     * Maps a logical AND expression with multiple comparisons.
     *
     * @param comparisons The list of and comparisons.
     * @return The corresponding SQL query result.
     */
    public SqlQueryResult mapAndExpression(List<ComparisonInput> comparisons) {
        List<SqlMapping> mappings = new ArrayList<>();
        List<String> whereClauses = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        for (ComparisonInput comp: comparisons) {
            SqlQueryResult partial = mapComparison(comp.fieldIdentifier(), comp.operator(), comp.value());
            mappings.add(partial.getMapping());
            whereClauses.add(partial.getWhereClause());
            parameters.addAll(partial.getParameters());
        }

        String combinedWhere = String.join(" AND ", whereClauses);

        // Use first mapping as base, merge joins from others
        SqlMapping baseMapping = mappings.get(0);
        List<JoinClause> allJoins = new ArrayList<>(baseMapping.getJoins());
        for (int i = 1; i < mappings.size(); i++) {
            allJoins.addAll(mappings.get(i).getJoins());
        }

        SqlMapping merged = new SqlMapping(
                baseMapping.getBaseTable(),
                baseMapping.getBaseTableAlias(),
                "*",
                allJoins,
                null);

        return new SqlQueryResult(merged, combinedWhere, parameters);
    }


    /**
     * Maps a logical OR expression with multiple comparisons.
     *
     * @param comparisons The list of or comparisons.
     * @return The corresponding SQL query result.
     */
    public SqlQueryResult mapOrExpression(List<ComparisonInput> comparisons) {
        List<SqlMapping> mappings = new ArrayList<>();
        List<String> whereClauses = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        for (ComparisonInput comp: comparisons) {
            SqlQueryResult partial = mapComparison(comp.fieldIdentifier(), comp.operator(), comp.value());
            mappings.add(partial.getMapping());
            whereClauses.add("(" + partial.getWhereClause() + ")");
            parameters.addAll(partial.getParameters());
        }

        String combinedWhere = String.join(" OR ", whereClauses);

        SqlMapping baseMapping = mappings.get(0);
        List<JoinClause> allJoins = new ArrayList<>(baseMapping.getJoins());
        for (int i = 1; i < mappings.size(); i++) {
            allJoins.addAll(mappings.get(i).getJoins());
        }

        SqlMapping merged = new SqlMapping(
                baseMapping.getBaseTable(),
                baseMapping.getBaseTableAlias(),
                "*",
                allJoins,
                null);

        return new SqlQueryResult(merged, combinedWhere, parameters);
    }


    private boolean isStringOperator(ComparisonOperator op) {
        return op == ComparisonOperator.CONTAINS ||
                op == ComparisonOperator.STARTS_WITH ||
                op == ComparisonOperator.ENDS_WITH;
    }

    /**
     * Record for comparison input.
     */
    public record ComparisonInput(String fieldIdentifier, String operator, String value) {}
}
