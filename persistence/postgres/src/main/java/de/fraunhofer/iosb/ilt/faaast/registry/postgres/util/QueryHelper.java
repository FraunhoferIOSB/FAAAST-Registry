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
package de.fraunhofer.iosb.ilt.faaast.registry.postgres.util;

import static de.fraunhofer.iosb.ilt.faaast.registry.core.query.QueryEvaluator.ValueKind.BOOL;
import static de.fraunhofer.iosb.ilt.faaast.registry.core.query.QueryEvaluator.ValueKind.DATETIME;
import static de.fraunhofer.iosb.ilt.faaast.registry.core.query.QueryEvaluator.ValueKind.FIELD;
import static de.fraunhofer.iosb.ilt.faaast.registry.core.query.QueryEvaluator.ValueKind.HEX;
import static de.fraunhofer.iosb.ilt.faaast.registry.core.query.QueryEvaluator.ValueKind.NUM;
import static de.fraunhofer.iosb.ilt.faaast.registry.core.query.QueryEvaluator.ValueKind.STR;
import static de.fraunhofer.iosb.ilt.faaast.registry.core.query.QueryEvaluator.ValueKind.TIME;

import de.fraunhofer.iosb.ilt.faaast.registry.core.query.QueryEvaluator;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.Value;
import java.util.ArrayList;
import java.util.List;


/**
 * Helper class for query Lanuage operations.
 */
public class QueryHelper {

    private static final QueryEvaluator evaluator = new QueryEvaluator();

    private enum LogicalMode {
        UNUSED,
        AND,
        OR
    }

    /**
     * Constructs the where clause for a database query based on the given condition.
     * 
     * @param condition The condition of the AAS Query.
     * @param parameters the database parameters.
     * @return The text for the where clause.
     */
    public static String getAasQueryWhereClauses(LogicalExpression condition, List<Object> parameters) {
        StringBuilder builder = new StringBuilder();
        LogicalMode mode = getLogicalOperatorMode(condition);
        if (mode != LogicalMode.UNUSED) {
            String txt = processLogicalOperator(condition, parameters, LogicalMode.AND);
            if (!txt.isEmpty()) {
                builder.append(txt);
            }
        }
        //if ((condition.get$and() != null) && (!condition.get$and().isEmpty())) {
        //    List<LogicalExpression> andList = condition.get$and();
        //    String andTxt = "";
        //    for (var expr: andList) {
        //        String txt = getAasQueryWhereClauses(expr, parameters);
        //        if (!txt.isEmpty()) {
        //            if (!andTxt.isEmpty()) {
        //                andTxt += " AND ";
        //            }
        //            andTxt += txt;
        //        }
        //    }
        //    if (!andTxt.isEmpty()) {
        //        builder.append(andTxt);
        //    }
        //}
        if (isComparisonOperator(condition)) {
            List<Value> list = getComparisonValues(condition);
            if (list.size() != 2) {
                throw new IllegalArgumentException("Equals must contain exactly 2 operators");
            }
            Value left = list.get(0);
            Value right = list.get(1);
            builder.append(valueToString(left, parameters));
            builder.append(getComparisonString(condition));
            builder.append(valueToString(right, parameters));
        }

        //if (!builder.isEmpty()) {
        //    builder.insert(0, " where ");
        //}

        return builder.toString();
    }


    private static String processLogicalOperator(LogicalExpression condition, List<Object> parameters, LogicalMode mode) {
        //List<LogicalExpression> values = getLogicalValues(condition, mode);
        String retval;
        //if ((condition.get$and() != null) && (!condition.get$and().isEmpty())) {
        if (mode != LogicalMode.UNUSED) {
            List<LogicalExpression> values = getLogicalValues(condition, mode);
            String fullTxt = "";
            for (var expr: values) {
                String txt = getAasQueryWhereClauses(expr, parameters);
                if (!txt.isEmpty()) {
                    if (!fullTxt.isEmpty()) {
                        fullTxt += getLogicalOperator(mode);
                    }
                    fullTxt += txt;
                }
            }
            retval = fullTxt;
            //if (!fullTxt.isEmpty()) {
            //    builder.append(fullTxt);
            //}
        }
        else {
            retval = "";
        }
        return retval;
    }


    private static List<LogicalExpression> getLogicalValues(LogicalExpression condition, LogicalMode mode) {
        List<LogicalExpression> retval;
        retval = switch (mode) {
            case AND -> condition.get$and();
            case OR -> condition.get$or();
            default -> new ArrayList<>();
        };
        return retval;
    }


    private static String getLogicalOperator(LogicalMode mode) {
        String retval = switch (mode) {
            case AND -> " AND ";
            case OR -> " OR ";
            default -> "";
        };
        return retval;
    }


    private static boolean isComparisonOperator(LogicalExpression condition) {
        boolean retval;
        if ((condition.get$eq() != null) && (!condition.get$eq().isEmpty())) {
            retval = true;
        }
        else if ((condition.get$ne() != null) && (!condition.get$ne().isEmpty())) {
            retval = true;
        }
        else if ((condition.get$gt() != null) && (!condition.get$gt().isEmpty())) {
            retval = true;
        }
        else if ((condition.get$ge() != null) && (!condition.get$ge().isEmpty())) {
            retval = true;
        }
        else if ((condition.get$lt() != null) && (!condition.get$lt().isEmpty())) {
            retval = true;
        }
        else if ((condition.get$le() != null) && (!condition.get$le().isEmpty())) {
            retval = true;
        }
        else {
            retval = false;
        }
        return retval;
    }


    private static String getComparisonString(LogicalExpression condition) {
        String retval;
        if ((condition.get$eq() != null) && (!condition.get$eq().isEmpty())) {
            retval = "=";
        }
        else if ((condition.get$ne() != null) && (!condition.get$ne().isEmpty())) {
            retval = "!=";
        }
        else if ((condition.get$gt() != null) && (!condition.get$gt().isEmpty())) {
            retval = ">";
        }
        else if ((condition.get$ge() != null) && (!condition.get$ge().isEmpty())) {
            retval = ">=";
        }
        else if ((condition.get$lt() != null) && (!condition.get$lt().isEmpty())) {
            retval = "<";
        }
        else if ((condition.get$le() != null) && (!condition.get$le().isEmpty())) {
            retval = "<=";
        }
        else {
            throw new IllegalArgumentException("Illegal Comparison Condition");
        }

        return retval;
    }


    private static List<Value> getComparisonValues(LogicalExpression condition) {
        List<Value> retval;
        if ((condition.get$eq() != null) && (!condition.get$eq().isEmpty())) {
            retval = condition.get$eq();
        }
        else if ((condition.get$ne() != null) && (!condition.get$ne().isEmpty())) {
            retval = condition.get$ne();
        }
        else if ((condition.get$gt() != null) && (!condition.get$gt().isEmpty())) {
            retval = condition.get$gt();
        }
        else if ((condition.get$ge() != null) && (!condition.get$ge().isEmpty())) {
            retval = condition.get$ge();
        }
        else if ((condition.get$lt() != null) && (!condition.get$lt().isEmpty())) {
            retval = condition.get$lt();
        }
        else if ((condition.get$le() != null) && (!condition.get$le().isEmpty())) {
            retval = condition.get$le();
        }
        else {
            throw new IllegalArgumentException("Illegal Comparison Condition");
        }
        return retval;
    }


    private static LogicalMode getLogicalOperatorMode(LogicalExpression condition) {
        LogicalMode retval;
        if ((condition.get$and() != null) && (!condition.get$and().isEmpty())) {
            retval = LogicalMode.AND;
        }
        else if ((condition.get$or() != null) && (!condition.get$or().isEmpty())) {
            retval = LogicalMode.OR;
        }
        else {
            retval = LogicalMode.UNUSED;
        }
        return retval;
    }


    private static String valueToString(Value value, List<Object> parameters) {
        String retval;
        var kind = evaluator.determineValueKind(value);
        switch (kind) {
            case FIELD -> {
                retval = value.get$field();
                if (retval.startsWith(QueryEvaluator.PREFIX_AAS_DESC)) {
                    retval = retval.substring(QueryEvaluator.PREFIX_AAS_DESC.length());
                }
                else if (retval.startsWith(QueryEvaluator.PREFIX_SM_DESC)) {
                    retval = retval.substring(QueryEvaluator.PREFIX_SM_DESC.length());
                }
                retval = convertFieldToColumnName(retval);
            }

            case STR -> {
                retval = "?";
                parameters.add(value.get$strVal());
            }

            case NUM -> {
                retval = "?";
                parameters.add(value.get$numVal());
            }

            case HEX -> {
                retval = "?";
                parameters.add(value.get$hexVal());
            }

            case DATETIME -> {
                retval = "?";
                parameters.add(value.get$dateTimeVal());
            }

            case TIME -> {
                retval = "?";
                parameters.add(value.get$timeVal());
            }

            case BOOL -> {
                retval = "?";
                parameters.add(value.get$boolean());
            }

            default -> throw new UnsupportedOperationException(String.format("Value Kind %s not supported", kind));
        }
        return retval;
    }


    private static String convertFieldToColumnName(String field) {
        String retval;
        switch (field) {
            case "idShort" -> retval = "id_short";
            case "id" -> retval = "id";
            case "assetKind" -> retval = "asset_kind";
            case "assetType" -> retval = "asset_type";
            case "globalAssetId" -> retval = "global_asset_id";
            default -> throw new IllegalArgumentException(String.format("Field %s not supported", field));
        }
        return retval;
    }

}
