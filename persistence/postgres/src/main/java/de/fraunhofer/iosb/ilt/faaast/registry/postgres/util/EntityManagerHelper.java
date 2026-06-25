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
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.Query;
import de.fraunhofer.iosb.ilt.faaast.registry.postgres.model.AssetAdministrationShellDescriptorEntity;
import de.fraunhofer.iosb.ilt.faaast.registry.postgres.model.SubmodelDescriptorEntityStandalone;
import de.fraunhofer.iosb.ilt.faaast.registry.postgres.query.QueryToSqlTranslator;
import de.fraunhofer.iosb.ilt.faaast.registry.postgres.query.SqlTranslationResult;
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
    //private static final QueryEvaluator evaluator = new QueryEvaluator();
    private static final QueryToSqlTranslator translator = new QueryToSqlTranslator();

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
        return getPagedAasQueryNative(entityManager, limit, cursor, aasQuery);
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
        //CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        //CriteriaQuery<SubmodelDescriptorEntityStandalone> queryCriteria = builder.createQuery(SubmodelDescriptorEntityStandalone.class);
        //queryCriteria.select(queryCriteria.from(SubmodelDescriptorEntityStandalone.class));

        //var query = entityManager.createQuery(queryCriteria).setFirstResult(cursor).setMaxResults(limit + 1);
        return getPagedSubmodelQueryNative(entityManager, limit, cursor, aasQuery);
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


    //private static Page<AssetAdministrationShellDescriptor> getPagedAasQueryInternNative(EntityManager entityManager, int limit, int cursor, Query aasQuery)
    private static Page<AssetAdministrationShellDescriptor> getPagedAasQueryNative(EntityManager entityManager, int limit, int cursor, Query aasQuery)
            throws DeserializationException {

        //SqlTranslationResult result = translator.translate(aasQuery, "$aasdesc");

        //LOGGER.atInfo().log("getPagedAasQueryInternNative: {}", result.toSql());
        //LOGGER.atInfo().log("Parameters: {}", result.getParameters());

        //int index = 1;
        //var query = entityManager.createNativeQuery(result.toSql(), result.getEntityClass());
        //for (var param: result.getParameters()) {
        //    query.setParameter(index++, param);
        //}

        var query = createDatabaseQuery(entityManager, aasQuery, "$aasdesc");

        //StringBuilder queryTxt = new StringBuilder();
        //queryTxt.append("select * from aas_descriptors");
        //List<Object> parameters = new ArrayList<>();
        //String whereTxt = QueryHelper.getAasQueryWhereClauses(aasQuery.get$condition(), parameters);
        //if (!whereTxt.isEmpty()) {
        //    whereTxt = " where " + whereTxt;
        //}

        //queryTxt.append(whereTxt);

        //LOGGER.debug("getPagedAasQueryIntern: Query: {}", queryTxt);
        //var query = entityManager.createNativeQuery(queryTxt.toString(), AssetAdministrationShellDescriptorEntity.class);
        //int index = 1;
        //for (var param: parameters) {
        //    query.setParameter(index++, param);
        //}
        List<AssetAdministrationShellDescriptorEntity> entityList = query.getResultList();

        LOGGER.debug("getPagedAasQueryIntern: found {} entities", entityList.size());
        List<AssetAdministrationShellDescriptor> list = entityList.stream()
                .map(LambdaExceptionHelper.rethrowFunction(x -> ModelTransformationHelper.convertAAS(x)))
                //        .filter(aas -> evaluator.matches(aasQuery.get$condition(), aas))
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


    private static jakarta.persistence.Query createDatabaseQuery(EntityManager entityManager, Query aasQuery, String targetRoot) {
        SqlTranslationResult result = translator.translate(aasQuery, targetRoot);

        LOGGER.atInfo().log("getPagedAasQueryInternNative: {}", result.toSql());
        LOGGER.atInfo().log("Parameters: {}", result.getParameters());

        int index = 1;
        var query = entityManager.createNativeQuery(result.toSql(), result.getEntityClass());
        for (var param: result.getParameters()) {
            query.setParameter(index++, param);
        }
        return query;
    }


    private static Page<SubmodelDescriptor> getPagedSubmodelQueryNative(EntityManager entityManager, int limit, int cursor, Query aasQuery)
            throws DeserializationException {
        var query = createDatabaseQuery(entityManager, aasQuery, "$smdesc");
        List<SubmodelDescriptorEntityStandalone> entityList = query.getResultList();

        List<SubmodelDescriptor> list = entityList.stream()
                .map(LambdaExceptionHelper.rethrowFunction(x -> ModelTransformationHelper.convertSubmodel(x)))
                //        .filter(aas -> evaluator.matches(aasQuery.get$condition(), aas))
                .toList();
        return doPaging(limit, cursor, list);
    }

}
