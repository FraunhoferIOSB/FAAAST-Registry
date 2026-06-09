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

import de.fraunhofer.iosb.ilt.faaast.registry.core.model.AssetLink;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.QueryEvaluator;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.Query;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.Value;
import de.fraunhofer.iosb.ilt.faaast.registry.postgres.model.AssetAdministrationShellDescriptorEntity;
import de.fraunhofer.iosb.ilt.faaast.registry.postgres.model.SubmodelDescriptorEntityStandalone;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;


/**
 * Helper class for PostgreQL objects.
 */
public class EntityManagerHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityManagerHelper.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final QueryEvaluator evaluator = new QueryEvaluator();

    private EntityManagerHelper() {}


    /**
     * Fetches all instances of AssetAdministrationShellDescriptor, matching the given criteria.
     *
     * @param entityManager The entityManager to use.
     * @param assetType The desired assetType.
     * @param assetKind The desired assetKind.
     * @param limit The desired limit.
     * @param cursor The desired cursor.
     * @return All instances matching the given criteria.
     * @throws DeserializationException If a deserialization error occurs.
     */
    public static Page<AssetAdministrationShellDescriptor> getPagedAas(EntityManager entityManager, String assetType, AssetKind assetKind, int limit, int cursor)
            throws DeserializationException {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AssetAdministrationShellDescriptorEntity> queryCriteria = builder.createQuery(AssetAdministrationShellDescriptorEntity.class);
        Root<AssetAdministrationShellDescriptorEntity> root = queryCriteria.from(AssetAdministrationShellDescriptorEntity.class);
        List<Predicate> predicates = new ArrayList<>();
        if (assetType != null) {
            predicates.add(builder.equal(root.get("assetType"), assetType));
        }
        if (assetKind != null) {
            predicates.add(builder.equal(root.get("assetKind"), assetKind));
        }
        queryCriteria.select(root);
        if (!predicates.isEmpty()) {
            queryCriteria.where(predicates.toArray(Predicate[]::new));
        }
        queryCriteria.orderBy(builder.asc(root.get("id")));
        //return doPaging(entityManager, limit, cursor, queryCriteria);
        return getPagedAas(entityManager, limit, cursor, queryCriteria);
    }


    /**
     * Fetches all instances of SubmodelDescriptor.
     *
     * @param entityManager The entityManager to use.
     * @param limit The desired limit.
     * @param cursor The desired cursor.
     * @return All instances.
     * @throws DeserializationException If a deserialization error occurs.
     */
    public static Page<SubmodelDescriptor> getPagedSubmodelStandalone(EntityManager entityManager, int limit, int cursor) throws DeserializationException {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SubmodelDescriptorEntityStandalone> queryCriteria = builder.createQuery(SubmodelDescriptorEntityStandalone.class);
        queryCriteria.select(queryCriteria.from(SubmodelDescriptorEntityStandalone.class));

        //var query = entityManager.createQuery(queryCriteria).setFirstResult(cursor).setMaxResults(limit + 1);
        return getPagedSubmodelStandalone(entityManager, limit, cursor, queryCriteria);
    }


    /**
     * Returns a list of AAS descriptors filtered by the given globalAssetId and SpecificAssetId's "name" and "value"
     * fields.
     *
     * @param entityManager The entityManager to use.
     * @param specificAssetIds The desired specificAssetIds "name" and "value" fields.
     * @param globalAssetId The desired globalAssetId.
     * @return All AAS descriptors matching the given criteria.
     * @throws DeserializationException If a deserialization error occurs.
     */
    public static List<AssetAdministrationShellDescriptor> getAas(EntityManager entityManager, List<AssetLink> specificAssetIds, String globalAssetId)
            throws DeserializationException {

        StringBuilder queryTxt = new StringBuilder();
        queryTxt.append("select * from aas_descriptors where ");
        if (specificAssetIds != null && !specificAssetIds.isEmpty()) {
            queryTxt.append("specific_asset_ids @> CAST (? AS JSONB) ");

            if (globalAssetId != null) {
                queryTxt.append("AND ");
            }
        }

        if (globalAssetId != null) {
            queryTxt.append("global_asset_id = ?");
        }

        var query = entityManager.createNativeQuery(queryTxt.toString(), AssetAdministrationShellDescriptorEntity.class);
        int index = 1;
        if (specificAssetIds != null && !specificAssetIds.isEmpty()) {
            query.setParameter(index++, mapper.writeValueAsString(specificAssetIds));
        }
        if (globalAssetId != null) {
            query.setParameter(index++, globalAssetId);
        }

        List<AssetAdministrationShellDescriptorEntity> resultList = query.getResultList();
        LOGGER.debug("getAas: found {} entities", resultList.size());

        //LOGGER.debug("getAas: results: {}", query2.getResultList().size());
        // Return default AAS descriptor implementation objects
        return resultList.stream()
                .map(LambdaExceptionHelper.rethrowFunction(x -> ModelTransformationHelper.convertAAS(x)))
                .toList();
    }


    /**
     * Fetches all instances of SubmodelDescriptor.
     *
     * @param entityManager The entityManager to use.
     * @param limit The desired limit.
     * @param cursor The desired cursor.
     * @param aasQuery The desired AAS query.
     * @return All instances.
     * @throws DeserializationException If a deserialization error occurs.
     */
    public static Page<AssetAdministrationShellDescriptor> getPagedAasQuery(EntityManager entityManager, int limit, int cursor, Query aasQuery) throws DeserializationException {
        //CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        //CriteriaQuery<AssetAdministrationShellDescriptorEntity> queryCriteria = builder.createQuery(AssetAdministrationShellDescriptorEntity.class);
        //queryCriteria.select(queryCriteria.from(AssetAdministrationShellDescriptorEntity.class));

        //var query = entityManager.createQuery(queryCriteria).setFirstResult(cursor).setMaxResults(limit + 1);
        return getPagedAasQueryInternNative(entityManager, limit, cursor, aasQuery);
    }


    /**
     * Fetches all instances of SubmodelDescriptor.
     *
     * @param entityManager The entityManager to use.
     * @param limit The desired limit.
     * @param cursor The desired cursor.
     * @param aasQuery The desired AAS query.
     * @return All instances.
     * @throws DeserializationException If a deserialization error occurs.
     */
    public static Page<SubmodelDescriptor> getPagedSubmodelQuery(EntityManager entityManager, int limit, int cursor, Query aasQuery) throws DeserializationException {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SubmodelDescriptorEntityStandalone> queryCriteria = builder.createQuery(SubmodelDescriptorEntityStandalone.class);
        queryCriteria.select(queryCriteria.from(SubmodelDescriptorEntityStandalone.class));

        //var query = entityManager.createQuery(queryCriteria).setFirstResult(cursor).setMaxResults(limit + 1);
        return getPagedSubmodelQuery(entityManager, limit, cursor, queryCriteria, aasQuery);
    }


    /**
     * Fetches all instances of a given type from the entityManager as a list of a desired return type.
     *
     * @param <R> the return type
     * @param <T> the type to fetch
     * @param entityManager the entityManager to use
     * @param type the type to fetch
     * @param returnType the type to return
     * @param limit The desired limit.
     * @param cursor The desired cursor.
     * @return all instances of given type cast to return type
     */
    //public static <R, T> Page<R> getAllPaged(EntityManager entityManager, Class<T> type, Class<R> returnType, int limit, int cursor) {
    //    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    //    CriteriaQuery<T> queryCriteria = builder.createQuery(type);
    //    queryCriteria.select(queryCriteria.from(type));

    //    var query = entityManager.createQuery(queryCriteria).setFirstResult(cursor).setMaxResults(limit + 1);
    //    return doPaging(entityManager, returnType, limit, cursor, queryCriteria);
    //}

    private static Page<AssetAdministrationShellDescriptor> getPagedAas(EntityManager entityManager, int limit, int cursor,
                                                                        CriteriaQuery<AssetAdministrationShellDescriptorEntity> queryCriteria)
            throws DeserializationException {
        var query = entityManager.createQuery(queryCriteria).setFirstResult(cursor).setMaxResults(limit + 1);
        List<AssetAdministrationShellDescriptor> list = query.getResultList().stream()
                .map(LambdaExceptionHelper.rethrowFunction(x -> ModelTransformationHelper.convertAAS(x)))
                .toList();
        return doPaging(limit, cursor, list);
    }

    //    private static Page<SubmodelDescriptor> getPagedSubmodel(EntityManager entityManager, int limit, int cursor,
    //                                                             CriteriaQuery<SubmodelDescriptorEntity> queryCriteria)
    //            throws DeserializationException {
    //        var query = entityManager.createQuery(queryCriteria).setFirstResult(cursor).setMaxResults(limit + 1);
    //        List<SubmodelDescriptor> list = query.getResultList().stream()
    //                .map(LambdaExceptionHelper.rethrowFunction(x -> ModelTransformationHelper.convertSubmodel(x)))
    //                .toList();
    //        return doPaging(limit, cursor, list);
    //    }


    private static Page<SubmodelDescriptor> getPagedSubmodelStandalone(EntityManager entityManager, int limit, int cursor,
                                                                       CriteriaQuery<SubmodelDescriptorEntityStandalone> queryCriteria)
            throws DeserializationException {
        var query = entityManager.createQuery(queryCriteria).setFirstResult(cursor).setMaxResults(limit + 1);
        List<SubmodelDescriptor> list = query.getResultList().stream()
                .map(LambdaExceptionHelper.rethrowFunction(x -> ModelTransformationHelper.convertSubmodel(x)))
                .toList();
        return doPaging(limit, cursor, list);
    }

    //public static void getAllPaged(EntityManager entityManager) {
    //    Page<AssetAdministrationShellDescriptor> 
    //}


    //private static <R, T> Page<R> doPaging(EntityManager entityManager, Class<R> returnType, int limit, int cursor, CriteriaQuery<T> queryCriteria) {
    private static <R, T> Page<R> doPaging(int limit, int cursor, List<R> list) {
        //var query = entityManager.createQuery(queryCriteria).setFirstResult(cursor).setMaxResults(limit + 1);
        //List<R> list = query.getResultList().stream()
        //        .map(x -> converter.apply(x))
        //        .toList();
        String nextCursor = null;
        if (list.size() > limit) {
            nextCursor = Integer.toString(cursor + limit);
        }
        return Page.<R> builder()
                .result(list.stream().limit(limit).toList())
                .metadata(PagingMetadata.builder()
                        .cursor(nextCursor)
                        .build())
                .build();
    }

    //    private static <R extends Descriptor, T> Page<R> doQueryPaging(int limit, int cursor, List<R> list, Query aasQuery) {
    //        String nextCursor = null;
    //        if (list.size() > limit) {
    //            nextCursor = Integer.toString(cursor + limit);
    //        }
    //        return Page.<R> builder()
    //                .result(list.stream()
    //                        .filter(aas -> evaluator.matches(aasQuery.get$condition(), aas))
    //                        .limit(limit)
    //                        .toList())
    //                .metadata(PagingMetadata.builder()
    //                        .cursor(nextCursor)
    //                        .build())
    //                .build();
    //    }


    private static Page<AssetAdministrationShellDescriptor> getPagedAasQueryInternNative(EntityManager entityManager, int limit, int cursor, Query aasQuery)
            throws DeserializationException {

        StringBuilder queryTxt = new StringBuilder();
        queryTxt.append("select * from aas_descriptors");
        List<Object> parameters = new ArrayList<>();
        queryTxt.append(getAasQueryWhereClauses(aasQuery.get$condition(), parameters));

        LOGGER.debug("getPagedAasQueryIntern: Query: {}", queryTxt);
        var query = entityManager.createNativeQuery(queryTxt.toString(), AssetAdministrationShellDescriptorEntity.class);
        int index = 1;
        for (var param: parameters) {
            query.setParameter(index++, param);
        }
        List<AssetAdministrationShellDescriptorEntity> entityList = query.getResultList();

        LOGGER.debug("getPagedAasQueryIntern: found {} entities", entityList.size());
        List<AssetAdministrationShellDescriptor> list = entityList.stream()
                .map(LambdaExceptionHelper.rethrowFunction(x -> ModelTransformationHelper.convertAAS(x)))
                .filter(aas -> evaluator.matches(aasQuery.get$condition(), aas))
                .toList();
        return doPaging(limit, cursor, list);
    }

    //    private static Page<AssetAdministrationShellDescriptor> getPagedAasQueryIntern(EntityManager entityManager, int limit, int cursor, Query aasQuery)
    //            throws DeserializationException {
    //
    //        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    //        CriteriaQuery<AssetAdministrationShellDescriptorEntity> queryCriteria = builder.createQuery(AssetAdministrationShellDescriptorEntity.class);
    //        queryCriteria.select(queryCriteria.from(AssetAdministrationShellDescriptorEntity.class));
    //
    //        List<Predicate> predicates = addAasQueryCriteria(entityManager, queryCriteria.from(AssetAdministrationShellDescriptorEntity.class), aasQuery);
    //        if (!predicates.isEmpty()) {
    //            queryCriteria.where(predicates.toArray(Predicate[]::new));
    //        }
    //
    //        var query = entityManager.createQuery(queryCriteria).setFirstResult(cursor).setMaxResults(limit + 1);
    //        List<AssetAdministrationShellDescriptorEntity> entityList = query.getResultList();
    //        List<AssetAdministrationShellDescriptor> list = entityList.stream()
    //                .map(LambdaExceptionHelper.rethrowFunction(x -> ModelTransformationHelper.convertAAS(x)))
    //                .filter(aas -> evaluator.matches(aasQuery.get$condition(), aas))
    //                .toList();
    //        return doPaging(limit, cursor, list);
    //    }


    private static Page<SubmodelDescriptor> getPagedSubmodelQuery(EntityManager entityManager, int limit, int cursor,
                                                                  CriteriaQuery<SubmodelDescriptorEntityStandalone> queryCriteria, Query aasQuery)
            throws DeserializationException {
        var query = entityManager.createQuery(queryCriteria).setFirstResult(cursor).setMaxResults(limit + 1);
        List<SubmodelDescriptor> list = query.getResultList().stream()
                .map(LambdaExceptionHelper.rethrowFunction(x -> ModelTransformationHelper.convertSubmodel(x)))
                .filter(aas -> evaluator.matches(aasQuery.get$condition(), aas))
                .toList();
        return doPaging(limit, cursor, list);
    }


    private static String getAasQueryWhereClauses(LogicalExpression condition, List<Object> parameters) {
        StringBuilder builder = new StringBuilder();
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

        if (!builder.isEmpty()) {
            builder.insert(0, " where ");
        }

        return builder.toString();
    }

    //    private static List<Predicate> addAasQueryCriteria(EntityManager entityManager, Root<AssetAdministrationShellDescriptorEntity> root, Query aasQuery) {
    //        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    //        //Root<AssetAdministrationShellDescriptorEntity> root = queryCriteria.from(AssetAdministrationShellDescriptorEntity.class);
    //        List<Predicate> predicates = new ArrayList<>();
    //        LogicalExpression condition = aasQuery.get$condition();
    //        if (condition.get$eq() != null) {
    //            List<Value> list = condition.get$eq();
    //            if (list.size() != 2) {
    //                throw new IllegalArgumentException("Equals must contain exactly 2 operators");
    //            }
    //            Value left = list.get(0);
    //            Value right = list.get(1);
    //            var leftKind = evaluator.determineValueKind(left);
    //            var rightKind = evaluator.determineValueKind(right);
    //            Expression<?> expression = null;
    //            Object object = null;
    //            if (leftKind == FIELD) {
    //                expression = root.get(left.get$field().substring(QueryEvaluator.PREFIX_AAS_DESC.length()));
    //            }
    //            else {
    //                object = valueToObject(left);
    //            }
    //            if (rightKind == FIELD) {
    //                expression = root.get(right.get$field().substring(QueryEvaluator.PREFIX_AAS_DESC.length()));
    //            }
    //            else {
    //                object = valueToObject(right);
    //            }
    //            predicates.add(builder.equal(expression, object));
    //        }
    //        return predicates;
    //    }


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
            default -> throw new IllegalArgumentException(String.format("Field %s not supported", field));
        }
        return retval;
    }


    private static boolean isComparisonOperator(LogicalExpression condition) {
        if ((condition.get$eq() != null) && (!condition.get$eq().isEmpty())) {
            return true;
        }
        else if ((condition.get$ne() != null) && (!condition.get$ne().isEmpty())) {
            return true;
        }
        else if ((condition.get$gt() != null) && (!condition.get$gt().isEmpty())) {
            return true;
        }
        else if ((condition.get$ge() != null) && (!condition.get$ge().isEmpty())) {
            return true;
        }
        else if ((condition.get$lt() != null) && (!condition.get$lt().isEmpty())) {
            return true;
        }
        else if ((condition.get$le() != null) && (!condition.get$le().isEmpty())) {
            return true;
        }
        return false;
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

    //    private static Object valueToObject(Value value) {
    //        Object retval;
    //        var kind = evaluator.determineValueKind(value);
    //        switch (kind) {
    //            case FIELD -> retval = value.get$field();
    //            case STR -> retval = value.get$strVal();
    //            case NUM -> retval = value.get$numVal();
    //            case HEX -> retval = value.get$hexVal();
    //            case DATETIME -> retval = value.get$dateTimeVal();
    //            case TIME -> retval = value.get$timeVal();
    //            case BOOL -> retval = value.get$boolean();
    //            default -> throw new UnsupportedOperationException(String.format("Value Kind %s not supported", kind));
    //        }
    //        return retval;
    //    }

    //private static Page<AssetAdministrationShellDescriptor> doPaging(EntityManager entityManager, int limit, int cursor,
    //                                                                 CriteriaQuery<AssetAdministrationShellDescriptorEntity> queryCriteria)
    //        throws DeserializationException {
    //    var query = entityManager.createQuery(queryCriteria).setFirstResult(cursor).setMaxResults(limit + 1);
    //    List<AssetAdministrationShellDescriptor> list = query.getResultList().stream()
    //            .map(LambdaExceptionHelper.rethrowFunction(x -> ModelTransformationHelper.convertAAS(x)))
    //            .toList();
    //    String nextCursor = null;
    //    if (list.size() > limit) {
    //        nextCursor = Integer.toString(cursor + limit);
    //    }
    //    return Page.<AssetAdministrationShellDescriptor> builder()
    //            .result(list.stream().limit(limit).toList())
    //            .metadata(PagingMetadata.builder()
    //                    .cursor(nextCursor)
    //                    .build())
    //            .build();
    //}
    //
    //
    //    private static Predicate createSpecificAssetIdSubquery(
    //                                                           Root<AssetAdministrationShellDescriptorEntity> root,
    //                                                           Map<String, String> specificAssetIdNamesValues,
    //                                                           CriteriaQuery<AssetAdministrationShellDescriptor> queryCriteria,
    //                                                           CriteriaBuilder cb) {
    //
    //        Subquery<Long> subquery = queryCriteria.subquery(Long.class);
    //        Root<AssetAdministrationShellDescriptorEntity> subRoot = subquery.from(AssetAdministrationShellDescriptorEntity.class);
    //        Join<AssetAdministrationShellDescriptorEntity, JpaSpecificAssetId> join = subRoot.join("specificAssetIds");
    //
    //        // correlate
    //        Predicate correlate = cb.equal(subRoot, root);
    //
    //        // OR of all key/value combinations to count matched pairs
    //        List<Predicate> anyMatch = new ArrayList<>();
    //        for (Map.Entry<String, String> specificAssetIdNameValuePair: specificAssetIdNamesValues.entrySet()) {
    //            anyMatch.add(cb.and(
    //                    cb.equal(join.get("name"), specificAssetIdNameValuePair.getKey()),
    //                    cb.equal(join.get("value"), specificAssetIdNameValuePair.getValue())));
    //        }
    //
    //        // JOIN ->  | Descriptor.id | Descriptor.idShort | ... | SpecificAssetId.name | SpecificAssetId.value | ... |
    //        // WHERE -> Filter all rows where SpecificAssetId.name and SpecificAssetId.value match one of the arguments SpecificAssetIds
    //        // WHERE -> Filter all rows with the other clauses from the root query
    //        // COUNT all rows, grouping by Descriptor.id -> For each Descriptor, get "how many specific asset ids matched".
    //        //          The "distinct" part prohibits any duplicate SpecificAssetIds to count as multiple matches -> If a Descriptor has duplicate SpecificAssetIds, they count as one.
    //        // SELECT all ids from the Descriptors which have exactly as many filtered SpecificAssetIds as the argument.
    //
    //        subquery.select(cb.countDistinct(join.get("id")))
    //                .where(cb.and(correlate, cb.or(anyMatch.toArray(Predicate[]::new))));
    //
    //        // ensure at least all provided pairs are present
    //        return cb.equal(subquery, (long) specificAssetIdNamesValues.size());
    //    }
}
