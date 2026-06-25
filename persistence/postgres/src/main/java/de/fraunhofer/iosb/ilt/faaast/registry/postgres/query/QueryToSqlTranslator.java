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

import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.MatchExpression;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.Query;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.StringValue;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.Value;
import de.fraunhofer.iosb.ilt.faaast.registry.postgres.model.AssetAdministrationShellDescriptorEntity;
import de.fraunhofer.iosb.ilt.faaast.registry.postgres.model.SubmodelDescriptorEntityStandalone;
import java.util.ArrayList;
import java.util.List;


/**
 * Translates the jsonschema2pojo-generated Query object tree
 * into a PostgreSQL SQL query string for $aasdesc and $smdesc contexts.
 */
public class QueryToSqlTranslator {

    private final FieldIdentifierResolver fieldResolver;

    private class ValueCast {
        private String valueCast;
        private String field;

        public String getValueCast() {
            return valueCast;
        }


        public void setValueCast(String value) {
            valueCast = value;
        }


        public String getField() {
            return field;
        }


        public void setField(String value) {
            field = value;
        }
    }

    public QueryToSqlTranslator() {
        this.fieldResolver = new FieldIdentifierResolver();
    }


    /**
     * Translates a full Query object into a SQL SELECT statement.
     *
     * @param query the parsed Query object (from JSON deserialization)
     * @param targetRoot "$aasdesc" or "$smdesc" - determines base table
     * @return complete SQL query string with parameter placeholders
     */
    public SqlTranslationResult translate(Query query, String targetRoot) {
        SqlTranslationResult result = new SqlTranslationResult();

        // Determine base table
        if ("$aasdesc".equals(targetRoot)) {
            result.setBaseTable("aas_descriptors");
            result.setBaseAlias("ad");
            result.setEntityClass(AssetAdministrationShellDescriptorEntity.class);
        }
        else if ("$smdesc".equals(targetRoot)) {
            result.setBaseTable("submodel_descriptors_standalone");
            result.setBaseAlias("sds");
            result.setEntityClass(SubmodelDescriptorEntityStandalone.class);
        }
        else {
            throw new IllegalArgumentException("Unsupported target root: " + targetRoot);
        }

        // Determine SELECT clause
        boolean selectIdOnly = query.get$select() != null && "id".equals(query.get$select());
        result.setSelectIdOnly(selectIdOnly);

        // Translate condition
        LogicalExpression condition = query.get$condition();
        TranslationContext ctx = new TranslationContext();
        String whereClause = translateLogicalExpression(condition, ctx, new ValueCast());

        result.setWhereClause(whereClause);
        result.setJoinClauses(ctx.getJoins());
        result.setLateralClauses(ctx.getLaterals());
        result.setParameters(ctx.getParameters());

        return result;
    }

    // =========================================================================
    // Logical expression translation
    // =========================================================================


    private String translateLogicalExpression(LogicalExpression expr, TranslationContext ctx, ValueCast valueCast) {
        // $and
        if (expr.get$and() != null && !expr.get$and().isEmpty()) {
            List<String> parts = new ArrayList<>();
            for (LogicalExpression sub: expr.get$and()) {
                parts.add("(" + translateLogicalExpression(sub, ctx, valueCast) + ")");
            }
            return String.join(" AND ", parts);
        }

        // $or
        if (expr.get$or() != null && !expr.get$or().isEmpty()) {
            List<String> parts = new ArrayList<>();
            for (LogicalExpression sub: expr.get$or()) {
                parts.add("(" + translateLogicalExpression(sub, ctx, valueCast) + ")");
            }
            return String.join(" OR ", parts);
        }

        // $not
        if (expr.get$not() != null) {
            return "NOT (" + translateLogicalExpression(expr.get$not(), ctx, valueCast) + ")";
        }

        // $match (translates like $and but with correlated element identity)
        if (expr.get$match() != null && !expr.get$match().isEmpty()) {
            return translateMatchExpression(expr.get$match(), ctx, valueCast);
        }

        // $boolean literal
        if (expr.get$boolean() != null) {
            return expr.get$boolean() ? "TRUE" : "FALSE";
        }

        // Comparison operators
        if (listIsNotEmpty(expr.get$eq())) {
            return translateComparison(expr.get$eq(), "=", ctx);
        }
        if (listIsNotEmpty(expr.get$ne())) {
            return translateComparison(expr.get$ne(), "!=", ctx);
        }
        if (listIsNotEmpty(expr.get$gt())) {
            return translateComparison(expr.get$gt(), ">", ctx);
        }
        if (listIsNotEmpty(expr.get$ge())) {
            return translateComparison(expr.get$ge(), ">=", ctx);
        }
        if (listIsNotEmpty(expr.get$lt())) {
            return translateComparison(expr.get$lt(), "<", ctx);
        }
        if (listIsNotEmpty(expr.get$le())) {
            return translateComparison(expr.get$le(), "<=", ctx);
        }

        // String operators
        if (listIsNotEmpty(expr.get$contains())) {
            return translateStringOp(expr.get$contains(), "LIKE", "%", "%", ctx, valueCast);
        }
        if (listIsNotEmpty(expr.get$startsWith())) {
            return translateStringOp(expr.get$startsWith(), "LIKE", "", "%", ctx, valueCast);
        }
        if (listIsNotEmpty(expr.get$endsWith())) {
            return translateStringOp(expr.get$endsWith(), "LIKE", "%", "", ctx, valueCast);
        }
        if (listIsNotEmpty(expr.get$regex())) {
            return translateStringRegex(expr.get$regex(), ctx, valueCast);
        }

        throw new IllegalArgumentException("Cannot translate logical expression: no recognized operator");
    }

    // =========================================================================
    // Comparison translation
    // =========================================================================


    private String translateComparison(List<Value> operands, String sqlOp, TranslationContext ctx) {
        if (operands.size() != 2) {
            throw new IllegalArgumentException("Comparison requires exactly 2 operands");
        }
        ValueCast valueCast = new ValueCast();
        String left = translateValue(operands.get(0), ctx, valueCast);
        String right = translateValue(operands.get(1), ctx, valueCast);
        return left + " " + sqlOp + " " + right;
    }


    private String translateStringOp(List<StringValue> operands, String op,
                                     String prefix, String suffix, TranslationContext ctx, ValueCast valueCast) {
        if (operands.size() != 2) {
            throw new IllegalArgumentException("String operation requires exactly 2 operands");
        }
        String left = translateStringValue(operands.get(0), ctx, valueCast);
        String right = translateStringValueLiteral(operands.get(1), ctx, prefix, suffix, valueCast);
        return left + " " + op + " " + right;
    }


    private String translateStringRegex(List<StringValue> operands, TranslationContext ctx, ValueCast valueCast) {
        if (operands.size() != 2) {
            throw new IllegalArgumentException("Regex operation requires exactly 2 operands");
        }
        String left = translateStringValue(operands.get(0), ctx, valueCast);
        String right = translateStringValue(operands.get(1), ctx, valueCast);
        // PostgreSQL regex operator
        return left + " ~ " + right;
    }

    // =========================================================================
    // Value translation
    // =========================================================================


    private String translateValue(Value value, TranslationContext ctx, ValueCast valueCast) {
        // $field → resolve to SQL expression
        if (value.get$field() != null) {
            FieldMapping mapping = fieldResolver.resolve(value.get$field());
            registerMapping(mapping, ctx);

            // assetKind special handling: compare as integer
            if (mapping.isRequiresCast() && "ASSET_KIND_ENUM".equals(mapping.getCastType())) {
                valueCast.setValueCast(mapping.getCastType());
                valueCast.setField(value.get$field());
                //Object v = AssetKindParameterHandler.convertParameterValue(value.get$field(), value.)
                return mapping.getSqlExpression();
            }
            return mapping.getSqlExpression();
        }

        // $strVal → string literal parameter
        if (value.get$strVal() != null) {
            if (valueCast.getValueCast().equals("ASSET_KIND_ENUM")) {
                ctx.addParameter(AssetKindParameterHandler.convertParameterValue(valueCast.getField(), value.get$strVal()));
            }
            else {
                ctx.addParameter(value.get$strVal());
            }
            return "?";
        }

        // $numVal → numeric literal parameter
        if (value.get$numVal() != null) {
            ctx.addParameter(value.get$numVal());
            return "?";
        }

        // $hexVal → hex literal
        if (value.get$hexVal() != null) {
            // Strip "16#" prefix and pass as parameter
            String hex = value.get$hexVal().substring(3);
            ctx.addParameter(hex);
            return "decode(?, 'hex')";
        }

        // $boolean → boolean literal
        if (value.get$boolean() != null) {
            return value.get$boolean() ? "TRUE" : "FALSE";
        }

        // $dateTimeVal → timestamp literal
        if (value.get$dateTimeVal() != null) {
            ctx.addParameter(value.get$dateTimeVal());
            return "?::TIMESTAMPTZ";
        }

        // $timeVal → time literal
        if (value.get$timeVal() != null) {
            ctx.addParameter(value.get$timeVal());
            return "?::TIME";
        }

        // $strCast → explicit cast to text
        if (value.get$strCast() != null) {
            return "(" + translateValue(value.get$strCast(), ctx, valueCast) + ")::TEXT";
        }

        // $numCast → explicit cast to numeric
        if (value.get$numCast() != null) {
            return "(" + translateValue(value.get$numCast(), ctx, valueCast) + ")::NUMERIC";
        }

        // $boolCast → explicit cast to boolean
        if (value.get$boolCast() != null) {
            return "(" + translateValue(value.get$boolCast(), ctx, valueCast) + ")::BOOLEAN";
        }

        // $dateTimeCast
        if (value.get$dateTimeCast() != null) {
            return "(" + translateValue(value.get$dateTimeCast(), ctx, valueCast) + ")::TIMESTAMPTZ";
        }

        // $timeCast
        if (value.get$timeCast() != null) {
            return "(" + translateValue(value.get$timeCast(), ctx, valueCast) + ")::TIME";
        }

        // $dayOfWeek, $dayOfMonth, $month, $year (date part extraction)
        if (value.get$dayOfWeek() != null) {
            ctx.addParameter(value.get$dayOfWeek());
            return "EXTRACT(DOW FROM ?::TIMESTAMPTZ)";
        }
        if (value.get$dayOfMonth() != null) {
            ctx.addParameter(value.get$dayOfMonth());
            return "EXTRACT(DAY FROM ?::TIMESTAMPTZ)";
        }
        if (value.get$month() != null) {
            ctx.addParameter(value.get$month());
            return "EXTRACT(MONTH FROM ?::TIMESTAMPTZ)";
        }
        if (value.get$year() != null) {
            ctx.addParameter(value.get$year());
            return "EXTRACT(YEAR FROM ?::TIMESTAMPTZ)";
        }

        throw new IllegalArgumentException("Cannot translate Value: no recognized field set");
    }


    private String translateStringValue(StringValue sv, TranslationContext ctx, ValueCast valueCast) {
        if (sv.get$field() != null) {
            FieldMapping mapping = fieldResolver.resolve(sv.get$field());
            registerMapping(mapping, ctx);
            return mapping.getSqlExpression();
        }
        if (sv.get$strVal() != null) {
            ctx.addParameter(sv.get$strVal());
            return "?";
        }
        if (sv.get$strCast() != null) {
            return "(" + translateValue(sv.get$strCast(), ctx, valueCast) + ")::TEXT";
        }
        throw new IllegalArgumentException("Cannot translate StringValue");
    }


    private String translateStringValueLiteral(StringValue sv, TranslationContext ctx,
                                               String prefix, String suffix, ValueCast valueCast) {
        if (sv.get$strVal() != null) {
            // Wrap for LIKE: e.g. '%value%'
            String likeValue = prefix + escapeLike(sv.get$strVal()) + suffix;
            ctx.addParameter(likeValue);
            return "?";
        }
        // If it's a field reference, need CONCAT with wildcards
        if (sv.get$field() != null) {
            FieldMapping mapping = fieldResolver.resolve(sv.get$field());
            registerMapping(mapping, ctx);
            if (!prefix.isEmpty() && !suffix.isEmpty()) {
                return "'%' || " + mapping.getSqlExpression() + " || '%'";
            }
            else if (!suffix.isEmpty()) {
                return mapping.getSqlExpression() + " || '%'";
            }
            else if (!prefix.isEmpty()) {
                return "'%' || " + mapping.getSqlExpression();
            }
            return mapping.getSqlExpression();
        }
        return translateStringValue(sv, ctx, valueCast);
    }


    private String escapeLike(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    // =========================================================================
    // $match translation
    // =========================================================================


    private String translateMatchExpression(List<MatchExpression> matchItems, TranslationContext ctx, ValueCast valueCast) {
        // $match ensures all conditions apply to the SAME array element.
        // We detect the common array (lateral) and ensure a single lateral alias.

        // Collect all field identifiers to find the common array path
        String commonLateralAlias = "match_elem_" + ctx.nextMatchId();

        // For $match, we use a shared lateral so that all conditions reference the same element
        MatchTranslationContext matchCtx = new MatchTranslationContext(ctx, commonLateralAlias);

        List<String> conditions = new ArrayList<>();
        for (MatchExpression item: matchItems) {
            conditions.add(translateMatchExpressionItem(item, matchCtx, valueCast));
        }

        // If a lateral was introduced by match, wrap in EXISTS subquery
        if (matchCtx.hasLateral()) {
            String lateralFrom = matchCtx.getLateralFrom();
            String where = String.join(" AND ", conditions);
            return "EXISTS (SELECT 1 FROM " + lateralFrom + " WHERE " + where + ")";
        }

        // Otherwise, simple AND
        return String.join(" AND ", conditions);
    }


    private String translateMatchExpressionItem(MatchExpression item, MatchTranslationContext ctx, ValueCast valueCast) {
        // Nested $match
        if (item.get$match() != null && !item.get$match().isEmpty()) {
            List<String> parts = new ArrayList<>();
            for (MatchExpression sub: item.get$match()) {
                parts.add(translateMatchExpressionItem(sub, ctx, valueCast));
            }
            return "(" + String.join(" AND ", parts) + ")";
        }

        // Comparisons
        if (listIsNotEmpty(item.get$eq())) {
            return translateComparison(item.get$eq(), "=", ctx.getParent());
        }
        if (listIsNotEmpty(item.get$ne())) {
            return translateComparison(item.get$ne(), "!=", ctx.getParent());
        }
        if (listIsNotEmpty(item.get$gt())) {
            return translateComparison(item.get$gt(), ">", ctx.getParent());
        }
        if (listIsNotEmpty(item.get$ge())) {
            return translateComparison(item.get$ge(), ">=", ctx.getParent());
        }
        if (listIsNotEmpty(item.get$lt())) {
            return translateComparison(item.get$lt(), "<", ctx.getParent());
        }
        if (listIsNotEmpty(item.get$le())) {
            return translateComparison(item.get$le(), "<=", ctx.getParent());
        }
        if (listIsNotEmpty(item.get$contains())) {
            return translateStringOp(item.get$contains(), "LIKE", "%", "%", ctx.getParent(), valueCast);
        }
        if (listIsNotEmpty(item.get$startsWith())) {
            return translateStringOp(item.get$startsWith(), "LIKE", "", "%", ctx.getParent(), valueCast);
        }
        if (listIsNotEmpty(item.get$endsWith())) {
            return translateStringOp(item.get$endsWith(), "LIKE", "%", "", ctx.getParent(), valueCast);
        }
        if (listIsNotEmpty(item.get$regex())) {
            return translateStringRegex(item.get$regex(), ctx.getParent(), valueCast);
        }
        if (item.get$boolean() != null) {
            return item.get$boolean() ? "TRUE" : "FALSE";
        }

        throw new IllegalArgumentException("Cannot translate match expression item");
    }

    // =========================================================================
    // Context management
    // =========================================================================


    private void registerMapping(FieldMapping mapping, TranslationContext ctx) {
        if (mapping.hasJoin()) {
            ctx.addJoin(mapping.getJoinClause());
        }
        if (mapping.hasLateral()) {
            ctx.addLateral(mapping.getLateralClause());
        }
    }


    private static boolean listIsNotEmpty(List<?> list) {
        return (list != null) && (!list.isEmpty());
    }
}
