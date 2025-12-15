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
package de.fraunhofer.iosb.ilt.faaast.registry.jpa.util;

import de.fraunhofer.iosb.ilt.faaast.registry.core.query.QueryEvaluator;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.Query;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaAssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaSpecificAssetId;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.Descriptor;


/**
 * Helper class for JPA objects.
 */
public class EntityManagerHelper {

    private EntityManagerHelper() {}


    /**
     * Fetches all instances of a given type from the entityManager.
     *
     * @param <T> the type to fetch
     * @param entityManager the entityManager to use
     * @param type the type to fetch
     * @return all instances of given type
     */
    public static <T> List<T> getAll(EntityManager entityManager, Class<T> type) {
        return getAll(entityManager, type, type);
    }


    /**
     * Fetches all instances of a given type from the entityManager as a list of a desired return type.
     *
     * @param <R> the return type
     * @param <T> the type to fetch
     * @param entityManager the entityManager to use
     * @param type the type to fetch
     * @param returnType the type to return
     * @return all instances of given type cast to return type
     */
    public static <R, T extends R> List<R> getAll(EntityManager entityManager, Class<T> type, Class<R> returnType) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        var queryCriteria = builder.createQuery(type);
        queryCriteria.select(queryCriteria.from(type));
        var query = entityManager.createQuery(queryCriteria);
        return query.getResultList().stream()
                .map(returnType::cast)
                .collect(Collectors.toList());
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
    public static <R, T extends R> Page<R> getAllPaged(EntityManager entityManager, Class<T> type, Class<R> returnType, int limit, int cursor) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> queryCriteria = builder.createQuery(type);
        queryCriteria.select(queryCriteria.from(type));
        return doPaging(entityManager, returnType, limit, cursor, queryCriteria);
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
     * @param query The desired AAS query.
     * @return all instances of given type matching the query cast to return type
     */
    public static <R extends Descriptor, T extends R> Page<R> getAllQueryPaged(EntityManager entityManager, Class<T> type, Class<R> returnType, int limit, int cursor,
                                                                               Query query) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> queryCriteria = builder.createQuery(type);
        queryCriteria.select(queryCriteria.from(type));
        return doQueryPaging(entityManager, returnType, limit, cursor, queryCriteria, query);
    }


    /**
     * Fetches all instances of AssetAdministrationShellDescriptor, matching the given criteria.
     *
     * @param entityManager The entityManager to use.
     * @param assetType The desired assetType.
     * @param assetKind The desired assetKind.
     * @param limit The desired limit.
     * @param cursor The desired cursor.
     * @return All instances matching the given criteria.
     */
    public static Page<AssetAdministrationShellDescriptor> getPagedAas(EntityManager entityManager, String assetType, AssetKind assetKind, int limit, int cursor) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JpaAssetAdministrationShellDescriptor> queryCriteria = builder.createQuery(JpaAssetAdministrationShellDescriptor.class);
        Root<JpaAssetAdministrationShellDescriptor> root = queryCriteria.from(JpaAssetAdministrationShellDescriptor.class);
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
        return doPaging(entityManager, AssetAdministrationShellDescriptor.class, limit, cursor, queryCriteria);
    }


    /**
     * Returns a list of AAS descriptors filtered by the given globalAssetId and SpecificAssetId's "name" and "value"
     * fields.
     *
     * @param entityManager The entityManager to use.
     * @param specificAssetIdNamesValues The desired specificAssetIds "name" and "value" fields.
     * @param globalAssetId The desired globalAssetId.
     * @return All AAS descriptors matching the given criteria.
     */
    public static List<AssetAdministrationShellDescriptor> getAas(EntityManager entityManager, Map<String, String> specificAssetIdNamesValues, String globalAssetId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AssetAdministrationShellDescriptor> queryCriteria = builder.createQuery(AssetAdministrationShellDescriptor.class);
        Root<JpaAssetAdministrationShellDescriptor> root = queryCriteria.from(JpaAssetAdministrationShellDescriptor.class);
        List<Predicate> predicates = new ArrayList<>();

        if (specificAssetIdNamesValues != null && !specificAssetIdNamesValues.isEmpty()) {
            predicates.add(createSpecificAssetIdSubquery(root, specificAssetIdNamesValues, queryCriteria, builder));
        }

        if (globalAssetId != null) {
            predicates.add(builder.equal(root.get("globalAssetId"), globalAssetId));
        }
        queryCriteria.select(root);
        if (!predicates.isEmpty()) {
            queryCriteria.where(predicates.toArray(Predicate[]::new));
        }
        queryCriteria.orderBy(builder.asc(root));
        var query = entityManager.createQuery(queryCriteria);

        // Return default AAS descriptor implementation objects
        return query.getResultList();
    }


    private static Predicate createSpecificAssetIdSubquery(
                                                           Root<JpaAssetAdministrationShellDescriptor> root,
                                                           Map<String, String> specificAssetIdNamesValues,
                                                           CriteriaQuery<AssetAdministrationShellDescriptor> queryCriteria,
                                                           CriteriaBuilder cb) {

        Subquery<Long> subquery = queryCriteria.subquery(Long.class);
        Root<JpaAssetAdministrationShellDescriptor> subRoot = subquery.from(JpaAssetAdministrationShellDescriptor.class);
        Join<JpaAssetAdministrationShellDescriptor, JpaSpecificAssetId> join = subRoot.join("specificAssetIds");

        // correlate
        Predicate correlate = cb.equal(subRoot, root);

        // OR of all key/value combinations to count matched pairs
        List<Predicate> anyMatch = new ArrayList<>();
        for (Map.Entry<String, String> specificAssetIdNameValuePair: specificAssetIdNamesValues.entrySet()) {
            anyMatch.add(cb.and(
                    cb.equal(join.get("name"), specificAssetIdNameValuePair.getKey()),
                    cb.equal(join.get("value"), specificAssetIdNameValuePair.getValue())));
        }

        // JOIN ->  | Descriptor.id | Descriptor.idShort | ... | SpecificAssetId.name | SpecificAssetId.value | ... |
        // WHERE -> Filter all rows where SpecificAssetId.name and SpecificAssetId.value match one of the arguments SpecificAssetIds
        // WHERE -> Filter all rows with the other clauses from the root query
        // COUNT all rows, grouping by Descriptor.id -> For each Descriptor, get "how many specific asset ids matched".
        //          The "distinct" part prohibits any duplicate SpecificAssetIds to count as multiple matches -> If a Descriptor has duplicate SpecificAssetIds, they count as one.
        // SELECT all ids from the Descriptors which have exactly as many filtered SpecificAssetIds as the argument.

        subquery.select(cb.countDistinct(join.get("id")))
                .where(cb.and(correlate, cb.or(anyMatch.toArray(Predicate[]::new))));

        // ensure at least all provided pairs are present
        return cb.equal(subquery, (long) specificAssetIdNamesValues.size());
    }


    private static <R, T extends R> Page<R> doPaging(EntityManager entityManager, Class<R> returnType, int limit, int cursor, CriteriaQuery<T> queryCriteria) {
        var query = entityManager.createQuery(queryCriteria).setFirstResult(cursor).setMaxResults(limit + 1);
        List<R> list = query.getResultList().stream()
                .map(returnType::cast)
                .toList();
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


    private static <R extends Descriptor, T extends R> Page<R> doQueryPaging(EntityManager entityManager, Class<R> returnType, int limit, int cursor,
                                                                             CriteriaQuery<T> queryCriteria, Query aasQuery) {
        var entityQuery = entityManager.createQuery(queryCriteria).setFirstResult(cursor).setMaxResults(limit + 1);
        Stream<R> list = entityQuery.getResultList().stream()
                .map(returnType::cast);
        //.toList();
        String nextCursor = null;
        if (list.count() > limit) {
            nextCursor = Integer.toString(cursor + limit);
        }
        QueryEvaluator evaluator = new QueryEvaluator();
        return Page.<R> builder()
                .result(list
                        .filter(aas -> evaluator.matches(aasQuery.get$condition(), aas))
                        .limit(limit)
                        .toList())
                .metadata(PagingMetadata.builder()
                        .cursor(nextCursor)
                        .build())
                .build();
    }
}
