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
package de.fraunhofer.iosb.ilt.faaast.registry.core.query;

import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.MatchExpression;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.StringValue;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.Descriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.ProtocolInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Evaluates queries for descriptors sent to /query endpoints.
 */
public class QueryEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryEvaluator.class);
    private static final String PREFIX_AAS_DESC = "$aasdesc#";
    private static final String PREFIX_SM_DESC = "$smdesc#";

    public QueryEvaluator() {}

    private enum ComparisonOperator {
        EQ,
        NE,
        GT,
        GE,
        LT,
        LE,
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH,
        REGEX;

        boolean isStringOperator() {
            return this == CONTAINS || this == STARTS_WITH || this == ENDS_WITH || this == REGEX;
        }
    }

    private enum ValueKind {
        NONE,
        FIELD,
        STR,
        NUM,
        HEX,
        DATETIME,
        TIME,
        BOOL,
        STR_CAST,
        NUM_CAST
    }

    private enum StringValueKind {
        NONE,
        FIELD,
        STR,
        STR_CAST}

    /**
     * @param suffix e.g., ".name", "Sub.Path#value"
     */
    private record Condition(String suffix, ComparisonOperator operator, List<Object> rightVals) {private Condition(String suffix, ComparisonOperator operator, List<Object> rightVals) {
            this.suffix = suffix;
            this.operator = operator;
            this.rightVals = rightVals != null ? rightVals : Collections.emptyList();
        }}

    private record MatchOperation(ComparisonOperator operator, List<Value> args) {}


    private record MatchEvaluationContext(String commonPrefix, List<Condition> itemConditions, boolean directMismatch) {}


    private record IndexSelection(boolean selectAll, Integer index, String remainingSuffix) {}

    /**
     * @param argumentProvider provides arguments for this operator
     */
    private record OperationSpec<T>(ComparisonOperator operator, Supplier<List<T>>argumentProvider)
    {}

    /**
     * Used to decide whether to filter out the Descriptor.
     *
     * @param expr logical expression (tree)
     * @param descriptor AAS or Submodel Descriptor.
     * @return true if expression matches the descriptor
     * 
     */
    public boolean matches(LogicalExpression expr, Descriptor descriptor) {
        if (expr == null || descriptor == null) {
            return false;
        }

        // boolean
        if (expr.get$boolean() != null) {
            return expr.get$boolean();
        }

        // logical
        if (expr.get$and() != null && !expr.get$and().isEmpty()) {
            return expr.get$and().stream().allMatch(e -> matches(e, descriptor));
        }
        if (expr.get$or() != null && !expr.get$or().isEmpty()) {
            return expr.get$or().stream().anyMatch(e -> matches(e, descriptor));
        }
        if (expr.get$not() != null) {
            return !matches(expr.get$not(), descriptor);
        }

        // match operator
        if (expr.get$match() != null && !expr.get$match().isEmpty()) {
            return evaluateMatch(expr.get$match(), descriptor);
        }

        // numeric/boolean/string comparisons
        boolean evaluated = evaluateFirstValueOperator(expr, descriptor);
        if (evaluated) {
            return true;
        }

        // string binary operators
        return evaluateFirstStringOperator(expr, descriptor);
    }


    private boolean evaluateMatch(List<MatchExpression> matches, Descriptor descriptor) {
        if (matches == null || matches.isEmpty()) {
            return true;
        }
        MatchEvaluationContext ctx = buildMatchEvaluationContext(matches, descriptor);
        if (ctx.directMismatch) {
            return false;
        }
        if (ctx.commonPrefix == null) {
            return true;
        }
        return evaluateListMatch(ctx.commonPrefix, ctx.itemConditions, descriptor);
    }


    private MatchEvaluationContext buildMatchEvaluationContext(List<MatchExpression> matches, Descriptor descriptor) {
        String commonPrefix = null;
        List<Condition> itemConditions = new ArrayList<>();
        boolean directMismatch = false;

        for (MatchExpression m: matches) {
            MatchOperation mo = getMatchOperation(m);
            if (mo == null) {
                LOGGER.error("Unsupported operator in match");
                directMismatch = true;
                break;
            }
            if (mo.args.size() < 2) {
                LOGGER.error("$match operator {} requires two arguments", mo.operator);
                directMismatch = true;
                break;
            }

            Value left = mo.args.get(0);
            Value right = mo.args.get(1);
            if (left.get$field() == null) {
                LOGGER.error("Left side in $match must be a field: {}", left);
                directMismatch = true;
                break;
            }

            String field = left.get$field();
            List<Object> rightVals = evaluateValue(right, descriptor);

            int listMarker = field.indexOf("[]");
            if (listMarker == -1) {
                //                if (field.startsWith(PREFIX_SME + "#")) {
                //                    String prefix = PREFIX_SME;
                //                    if (commonPrefix != null && !commonPrefix.equals(prefix)) {
                //                        LOGGER.error("Non-common prefix in match: {} vs {}", commonPrefix, prefix);
                //                        directMismatch = true;
                //                        break;
                //                    }
                //                    commonPrefix = prefix;
                //                    String suffix = field.substring((PREFIX_SME + "#").length());
                //                    itemConditions.add(new Condition(suffix, mo.operator, rightVals));
                //                }
                //                else {
                // evaluate parent condition immediately
                List<Object> leftVals = evaluateValue(left, descriptor);
                if (!anyPairSatisfies(leftVals, rightVals, mo.operator)) {
                    directMismatch = true;
                    break;
                }
                //}
            }
            else {
                String prefix = field.substring(0, listMarker);
                if (commonPrefix != null && !commonPrefix.equals(prefix)) {
                    LOGGER.error("Non-common prefix in match: {} vs {}", commonPrefix, prefix);
                    directMismatch = true;
                    break;
                }
                commonPrefix = prefix;
                String suffix = field.substring(listMarker + 2);
                itemConditions.add(new Condition(suffix, mo.operator, rightVals));
            }
        }

        return new MatchEvaluationContext(commonPrefix, itemConditions, directMismatch);
    }


    private MatchOperation getMatchOperation(MatchExpression m) {
        List<MatchOperation> candidates = Arrays.asList(
                new MatchOperation(ComparisonOperator.EQ, m.get$eq()),
                new MatchOperation(ComparisonOperator.NE, m.get$ne()),
                new MatchOperation(ComparisonOperator.GT, m.get$gt()),
                new MatchOperation(ComparisonOperator.GE, m.get$ge()),
                new MatchOperation(ComparisonOperator.LT, m.get$lt()),
                new MatchOperation(ComparisonOperator.LE, m.get$le()));
        for (MatchOperation mo: candidates) {
            if (mo.args != null && !mo.args.isEmpty()) {
                return mo;
            }
        }
        return null;
    }


    private List<Object> evaluateValue(Value v, Descriptor descriptor) {
        return switch (determineValueKind(v)) {
            case FIELD -> nonNull(getFieldValues(v.get$field(), descriptor));
            case STR -> Collections.singletonList(v.get$strVal());
            case NUM -> Collections.singletonList(v.get$numVal());
            case HEX -> Collections.singletonList(v.get$hexVal());
            case DATETIME -> Collections.singletonList(v.get$dateTimeVal());
            case TIME -> Collections.singletonList(v.get$timeVal());
            case BOOL -> Collections.singletonList(v.get$boolean());
            case STR_CAST -> evaluateValue(v.get$strCast(), descriptor).stream()
                    .map(String::valueOf).collect(Collectors.toList());
            case NUM_CAST -> evaluateValue(v.get$numCast(), descriptor).stream()
                    .map(String::valueOf)
                    .map(this::parseDoubleOrNull)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            default -> Collections.emptyList();
        };
    }


    private ValueKind determineValueKind(Value v) {
        if (v == null)
            return ValueKind.NONE;
        if (v.get$field() != null)
            return ValueKind.FIELD;
        if (v.get$strVal() != null)
            return ValueKind.STR;
        if (v.get$numVal() != null)
            return ValueKind.NUM;
        if (v.get$hexVal() != null)
            return ValueKind.HEX;
        if (v.get$dateTimeVal() != null)
            return ValueKind.DATETIME;
        if (v.get$timeVal() != null)
            return ValueKind.TIME;
        if (v.get$boolean() != null)
            return ValueKind.BOOL;
        if (v.get$strCast() != null)
            return ValueKind.STR_CAST;
        if (v.get$numCast() != null)
            return ValueKind.NUM_CAST;
        return ValueKind.NONE;
    }


    private List<Object> getFieldValues(String field, Descriptor descriptor) {
        if (field == null || descriptor == null)
            return Collections.emptyList();

        if (field.startsWith(PREFIX_AAS_DESC)) {
            if (!(descriptor instanceof AssetAdministrationShellDescriptor))
                return Collections.emptyList();
            return getAasDescriptorFieldValues((AssetAdministrationShellDescriptor) descriptor, field.substring(PREFIX_AAS_DESC.length()));
        }

        if (field.startsWith(PREFIX_SM_DESC)) {
            if (!(descriptor instanceof SubmodelDescriptor))
                return Collections.emptyList();
            return new ArrayList<>(getSubmodelDescriptorFieldValues((SubmodelDescriptor) descriptor, field.substring(PREFIX_SM_DESC.length())));
        }

        LOGGER.error("Unsupported field: {}", field);
        return Collections.emptyList();
    }


    private static <T> List<T> nonNull(List<T> in) {
        return in != null ? in : Collections.emptyList();
    }


    private List<Object> getAasDescriptorFieldValues(AssetAdministrationShellDescriptor aas, String attr) {
        switch (attr) {
            case "idShort":
                return Collections.singletonList(aas.getIdShort());
            case "id":
                return Collections.singletonList(aas.getId());
            case "assetKind":
                return (aas.getAssetKind() == null)
                        ? Collections.emptyList()
                        : Collections.singletonList(aas.getAssetKind().name());
            case "assetType":
                String assetType = aas.getAssetType();
                return assetType == null ? Collections.emptyList() : Collections.singletonList(assetType);
            case "globalAssetId":
                String globalAssetId = aas.getGlobalAssetId();
                return globalAssetId == null ? Collections.emptyList() : Collections.singletonList(globalAssetId);
            default:
                if (attr.startsWith("specificAssetIds")) {
                    if (aas.getSpecificAssetIds() == null) {
                        return Collections.emptyList();
                    }
                    String remaining = attr.substring("specificAssetIds".length());
                    IndexSelection indexSelection = parseIndexSelection(remaining);
                    List<SpecificAssetId> sais = aas.getSpecificAssetIds();
                    List<SpecificAssetId> selectedItems = selectByIndex(sais, indexSelection);
                    List<Object> values = new ArrayList<>();
                    for (SpecificAssetId sai: selectedItems) {
                        values.add(getSpecificAssetIdAttribute(sai, indexSelection.remainingSuffix));
                    }
                    return values;
                }
                else if (attr.startsWith("endpoints")) {
                    if (aas.getEndpoints() == null) {
                        return Collections.emptyList();
                    }
                    String remaining = attr.substring("endpoints".length());
                    IndexSelection indexSelection = parseIndexSelection(remaining);
                    List<Endpoint> endpoints = aas.getEndpoints();
                    List<Endpoint> selectedEndpoints = selectByIndex(endpoints, indexSelection);
                    List<Object> values = new ArrayList<>();
                    for (Endpoint ep: selectedEndpoints) {
                        values.add(getEndpointAttribute(ep, indexSelection.remainingSuffix));
                    }
                    return values;
                }
                LOGGER.error("getAasDescriptorFieldValues: Unsupported AAS attribute: {}", attr);
                return Collections.emptyList();
        }
    }


    private List<String> getSubmodelDescriptorFieldValues(SubmodelDescriptor sm, String attr) {
        switch (attr) {
            case "idShort":
                return Collections.singletonList(sm.getIdShort());
            case "id":
                return Collections.singletonList(sm.getId());
            case "semanticId": {
                Reference ref = sm.getSemanticId();
                if (ref == null || ref.getKeys() == null || ref.getKeys().isEmpty()) {
                    return Collections.emptyList();
                }
                return Collections.singletonList(ref.getKeys().get(0).getValue());
            }
            default:
                if (attr.startsWith("semanticId.type")) {
                    Reference ref = sm.getSemanticId();
                    if (ref == null || ref.getType() == null) {
                        return Collections.emptyList();
                    }
                    return Collections.singletonList(ref.getType().name());
                }
                else if (attr.startsWith("semanticId.keys")) {
                    Reference ref = sm.getSemanticId();
                    if (ref == null || ref.getKeys() == null) {
                        return Collections.emptyList();
                    }

                    String remaining = attr.substring("semanticId.keys".length());
                    IndexSelection indexSelection = parseIndexSelection(remaining);

                    List<Key> selectedItems = selectByIndex(ref.getKeys(), indexSelection);
                    return extractKeyAttributeValues(selectedItems, indexSelection);
                }
                else if (attr.startsWith("endpoints")) {
                    if (sm.getEndpoints() == null) {
                        return Collections.emptyList();
                    }
                    String remaining = attr.substring("endpoints".length());
                    IndexSelection indexSelection = parseIndexSelection(remaining);
                    List<Endpoint> endpoints = sm.getEndpoints();
                    List<Endpoint> selectedEndpoints = selectByIndex(endpoints, indexSelection);
                    List<String> values = new ArrayList<>();
                    for (Endpoint ep: selectedEndpoints) {
                        values.add(getEndpointAttribute(ep, indexSelection.remainingSuffix));
                    }
                    return values;
                }
                LOGGER.error("Unsupported SM attribute: {}", attr);
                return Collections.emptyList();
        }
    }


    private IndexSelection parseIndexSelection(String s) {
        if (s == null || s.isEmpty()) {
            return new IndexSelection(true, null, "");
        }
        String rem = s;
        boolean selectAll = false;
        Integer idx = null;

        if (rem.startsWith("[]")) {
            selectAll = true;
            rem = rem.substring(2);
        }
        else if (rem.startsWith("[")) {
            int end = rem.indexOf(']');
            if (end > 1) {
                String idxStr = rem.substring(1, end);
                try {
                    idx = Integer.valueOf(idxStr);
                }
                catch (NumberFormatException e) {
                    LOGGER.error("Invalid index in path: {}", s);
                    return new IndexSelection(true, null, rem.substring(end + 1));
                }
                rem = rem.substring(end + 1);
            }
        }
        return new IndexSelection(selectAll, idx, rem);
    }


    private <T> List<T> selectByIndex(List<T> list, IndexSelection selector) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        if (selector.selectAll || selector.index == null) {
            return list;
        }
        int i = selector.index;
        return (i >= 0 && i < list.size()) ? Collections.singletonList(list.get(i)) : Collections.emptyList();
    }


    private String getSpecificAssetIdAttribute(SpecificAssetId sai, String path) {
        if ((sai == null) || (path == null)) {
            LOGGER.error("getSpecificAssetIdAttribute: Unsupported property {} for object {}", path, sai);
            return null;
        }
        switch (path) {
            case ".name":
                return sai.getName();
            case ".value":
                return sai.getValue();
            default:
                if (path.startsWith(".externalSubjectId") && sai.getExternalSubjectId() != null) {
                    return String.valueOf(sai.getExternalSubjectId());
                }
        }
        LOGGER.error("Unsupported property: {}", path);
        return null;
    }


    private String getEndpointAttribute(Endpoint endpoint, String path) {
        if ((endpoint == null) || (path == null)) {
            LOGGER.error("getEndpointAttribute: Unsupported property {} for object {}", path, endpoint);
            return null;
        }

        switch (path) {
            case "interface":
                return endpoint.get_interface();
            case "protocolinformation.href":
                ProtocolInformation pi = endpoint.getProtocolInformation();
                if (pi == null) {
                    LOGGER.warn("getEndpointAttribute: ProtocolInformation is null");
                    return null;
                }
                return pi.getHref();
        }
        LOGGER.error("getEndpointAttribute: Unsupported property: {}", path);
        return null;
    }


    private boolean anyPairSatisfies(List<Object> left, List<Object> right, ComparisonOperator operator) {
        if (left == null || right == null) {
            return false;
        }
        for (Object l: left) {
            for (Object r: right) {
                if (compareValues(l, r, operator)) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean compareValues(Object a, Object b, ComparisonOperator operator) {
        if (operator == null)
            return false;

        if (operator.isStringOperator()) {
            return compareUsingStringOperator(a, b, operator);
        }
        return compareUsingGeneralComparison(a, b, operator);
    }


    private boolean compareUsingStringOperator(Object a, Object b, ComparisonOperator operator) {
        if (a == null || b == null)
            return false;
        String left = String.valueOf(a);
        String right = String.valueOf(b);

        return switch (operator) {
            case CONTAINS -> left.contains(right);
            case STARTS_WITH -> left.startsWith(right);
            case ENDS_WITH -> left.endsWith(right);
            case REGEX -> Pattern.compile(right).matcher(left).matches();
            default -> false;
        };
    }


    private boolean compareUsingGeneralComparison(Object a, Object b, ComparisonOperator operator) {
        if (a == null || b == null) {
            return (operator == ComparisonOperator.EQ)
                    ? Objects.equals(a, b)
                    : (operator == ComparisonOperator.NE) && !Objects.equals(a, b);
        }

        // try numeric
        Double d1 = toDouble(a);
        Double d2 = toDouble(b);
        if (d1 != null && d2 != null) {
            return switch (operator) {
                case EQ -> Double.compare(d1, d2) == 0;
                case NE -> Double.compare(d1, d2) != 0;
                case GT -> d1 > d2;
                case GE -> d1 >= d2;
                case LT -> d1 < d2;
                case LE -> d1 <= d2;
                default -> false;
            };
        }

        // try boolean
        String sa = String.valueOf(a).trim();
        String sb = String.valueOf(b).trim();
        Boolean ba = parseBooleanStrict(sa);
        Boolean bb = parseBooleanStrict(sb);
        if (ba != null && bb != null) {
            return switch (operator) {
                case EQ -> Objects.equals(ba, bb);
                case NE -> !Objects.equals(ba, bb);
                default -> false;
            };
        }

        // string comparison
        int cmp = sa.compareTo(sb);
        return switch (operator) {
            case EQ -> cmp == 0;
            case NE -> cmp != 0;
            case GT -> cmp > 0;
            case GE -> cmp >= 0;
            case LT -> cmp < 0;
            case LE -> cmp <= 0;
            default -> false;
        };
    }


    private static Boolean parseBooleanStrict(String s) {
        if ("true".equalsIgnoreCase(s))
            return Boolean.TRUE;
        if ("false".equalsIgnoreCase(s))
            return Boolean.FALSE;
        return null;
    }


    private Double parseDoubleOrNull(String s) {
        try {
            return Double.valueOf(s);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }


    private Double toDouble(Object o) {
        if (o instanceof Number number) {
            return number.doubleValue();
        }
        return parseDoubleOrNull(String.valueOf(o));
    }


    private boolean evaluateListMatch(String commonPrefix, List<Condition> itemConditions, Descriptor descriptor) {
        switch (commonPrefix) {
            case "$aasdesc#specificAssetIds":
                if (!(descriptor instanceof AssetAdministrationShellDescriptor aas))
                    return false;
                if (aas.getSpecificAssetIds() == null)
                    return false;

                for (SpecificAssetId item: aas.getSpecificAssetIds()) {
                    if (doAllItemConditionsMatch(itemConditions, cond -> {
                        String s = getSpecificAssetIdAttribute(item, cond.suffix);
                        return s == null ? Collections.emptyList() : Collections.singletonList(s);
                    })) {
                        return true;
                    }
                }
                return false;

//            case PREFIX_SME:
//                if (!(identifiable instanceof Submodel sm))
//                    return false;
//                List<SubmodelElement> topLevel = sm.getSubmodelElements();
//                if (topLevel == null)
//                    return false;
//
//                for (SubmodelElement item: topLevel) {
//                    if (doAllItemConditionsMatch(itemConditions, cond -> getPropertyValuesFromSuffix(item, cond.suffix))) {
//                        return true;
//                    }
//                }
//                return false;

            default:
//                if (commonPrefix.startsWith(PREFIX_SME + ".")) {
//                    if (!(identifiable instanceof Submodel sm2))
//                        return false;
//                    String path = commonPrefix.substring((PREFIX_SME + ".").length());
//                    SubmodelElement listElem = getSubmodelElementByPath(sm2, path);
//                    if (!(listElem instanceof SubmodelElementList))
//                        return false;
//
//                    List<SubmodelElement> items = ((SubmodelElementList) listElem).getValue();
//                    if (items == null)
//                        return false;
//
//                    for (SubmodelElement item: items) {
//                        if (doAllItemConditionsMatch(itemConditions, cond -> getPropertyValuesFromSuffix(item, cond.suffix))) {
//                            return true;
//                        }
//                    }
//                    return false;
//                }
                LOGGER.error("evaluateListMatch: Unsupported prefix for $match: {}", commonPrefix);
                return false;
        }
    }


    private boolean doAllItemConditionsMatch(List<Condition> conditions,
                                             java.util.function.Function<Condition, List<Object>> leftValueExtractor) {
        for (Condition cond: conditions) {
            List<Object> leftVals = nonNull(leftValueExtractor.apply(cond));
            if (!anyPairSatisfies(leftVals, cond.rightVals, cond.operator)) {
                return false;
            }
        }
        return true;
    }


    private static List<String> extractKeyAttributeValues(List<Key> selectedItems, IndexSelection selector) {
        List<String> results = new ArrayList<>();
        for (Key key: selectedItems) {
            switch (selector.remainingSuffix) {
                case ".type":
                    results.add(key.getType().name());
                    break;
                case ".value":
                    results.add(key.getValue());
                    break;
                default:
                    break;
            }
        }
        return results;
    }


    private boolean evaluateFirstValueOperator(LogicalExpression expr, Descriptor descriptor) {
        List<OperationSpec<Value>> operations = Arrays.asList(
                new OperationSpec<>(ComparisonOperator.EQ, expr::get$eq),
                new OperationSpec<>(ComparisonOperator.NE, expr::get$ne),
                new OperationSpec<>(ComparisonOperator.GT, expr::get$gt),
                new OperationSpec<>(ComparisonOperator.GE, expr::get$ge),
                new OperationSpec<>(ComparisonOperator.LT, expr::get$lt),
                new OperationSpec<>(ComparisonOperator.LE, expr::get$le));
        for (OperationSpec<Value> spec: operations) {
            List<Value> args = spec.argumentProvider.get();
            if (args != null && !args.isEmpty()) {
                return evaluateBinaryComparison(args, descriptor, spec.operator);
            }
        }
        return false;
    }


    private boolean evaluateBinaryComparison(List<Value> args, Descriptor descriptor, ComparisonOperator operator) {
        if (args.size() < 2) {
            LOGGER.error("Operator {} requires two arguments", operator);
            return false;
        }
        List<Object> left = evaluateValue(args.get(0), descriptor);
        List<Object> right = evaluateValue(args.get(1), descriptor);
        return anyPairSatisfies(left, right, operator);
    }


    private boolean evaluateFirstStringOperator(LogicalExpression expr, Descriptor descriptor) {
        List<OperationSpec<StringValue>> operations = Arrays.asList(
                new OperationSpec<>(ComparisonOperator.CONTAINS, expr::get$contains),
                new OperationSpec<>(ComparisonOperator.STARTS_WITH, expr::get$startsWith),
                new OperationSpec<>(ComparisonOperator.ENDS_WITH, expr::get$endsWith),
                new OperationSpec<>(ComparisonOperator.REGEX, expr::get$regex));
        for (OperationSpec<StringValue> spec: operations) {
            List<StringValue> args = spec.argumentProvider.get();
            if (args != null && !args.isEmpty()) {
                return evaluateBinaryStringOperator(args, descriptor, spec.operator);
            }
        }
        return false;
    }


    private boolean evaluateBinaryStringOperator(List<StringValue> args, Descriptor descriptor, ComparisonOperator operator) {
        if (args.size() < 2) {
            LOGGER.error("String operator {} requires two arguments", operator);
            return false;
        }
        List<Object> left = evaluateStringValue(args.get(0), descriptor);
        List<Object> right = evaluateStringValue(args.get(1), descriptor);
        return anyPairSatisfies(left, right, operator);
    }


    private List<Object> evaluateStringValue(StringValue sv, Descriptor descriptor) {
        return switch (determineStringValueKind(sv)) {
            case FIELD -> nonNull(getFieldValues(sv.get$field(), descriptor));
            case STR -> Collections.singletonList(sv.get$strVal());
            case STR_CAST -> evaluateValue(sv.get$strCast(), descriptor).stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());
            default -> {
                LOGGER.error("Invalid string value: {}", sv);
                yield Collections.emptyList();
            }
        };
    }


    private StringValueKind determineStringValueKind(StringValue sv) {
        if (sv == null)
            return StringValueKind.NONE;
        if (sv.get$field() != null)
            return StringValueKind.FIELD;
        if (sv.get$strVal() != null)
            return StringValueKind.STR;
        if (sv.get$strCast() != null)
            return StringValueKind.STR_CAST;
        return StringValueKind.NONE;
    }
}
