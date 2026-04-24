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
import de.fraunhofer.iosb.ilt.faaast.registry.postgres.model.AssetAdministrationShellDescriptorEntity;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;


/**
 * Helper class for PostgreQL objects.
 */
public class EntityManagerHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityManagerHelper.class);
    private static final ObjectMapper mapper = new ObjectMapper();

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
        return doPaging(entityManager, limit, cursor, queryCriteria);
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
        //queryTxt.append("select aas from AssetAdministrationShellDescriptorEntity aas where ");
        queryTxt.append("select * from aas_descriptors where ");
        if (specificAssetIds != null && !specificAssetIds.isEmpty()) {
            queryTxt.append("specific_asset_ids @> CAST (? AS JSONB) ");

            //queryTxt.append("CAST (? AS JSONB) ");
            //queryTxt.append("' ");
            //// queryTxt.append("]' ");
            if (globalAssetId != null) {
                queryTxt.append("AND ");
            }
        }

        if (globalAssetId != null) {
            queryTxt.append("global_asset_id = ?");
        }

        //String queryTxt2 = queryTxt.toString();
        //queryTxt2 = queryTxt2.replace("AssetAdministrationShellDescriptorEntity", "aas_descriptors").replace("specificAssetIds", "specific_asset_ids").replace("globalAssetId",
        //        "global_asset_id");

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


    private static Page<AssetAdministrationShellDescriptor> doPaging(EntityManager entityManager, int limit, int cursor,
                                                                     CriteriaQuery<AssetAdministrationShellDescriptorEntity> queryCriteria)
            throws DeserializationException {
        var query = entityManager.createQuery(queryCriteria).setFirstResult(cursor).setMaxResults(limit + 1);
        List<AssetAdministrationShellDescriptor> list = query.getResultList().stream()
                .map(LambdaExceptionHelper.rethrowFunction(x -> ModelTransformationHelper.convertAAS(x)))
                .toList();
        String nextCursor = null;
        if (list.size() > limit) {
            nextCursor = Integer.toString(cursor + limit);
        }
        return Page.<AssetAdministrationShellDescriptor> builder()
                .result(list.stream().limit(limit).toList())
                .metadata(PagingMetadata.builder()
                        .cursor(nextCursor)
                        .build())
                .build();
    }
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
