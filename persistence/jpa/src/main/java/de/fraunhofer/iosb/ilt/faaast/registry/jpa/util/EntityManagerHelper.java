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

import de.fraunhofer.iosb.ilt.faaast.registry.core.AasRepository;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaAssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;


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
     * Fetches all instances of AssetAdministrationShellDescriptor, matching the given criteria.
     *
     * @param entityManager The entityManager to use.
     * @param assetType The desired assetType.
     * @param assetKind The desired assetKind.
     * @return All instances matching the given criteria.
     */
    public static List<AssetAdministrationShellDescriptor> getAllAas(EntityManager entityManager, String assetType, AssetKind assetKind) {
        //if ((assetKind == null) && (assetType == null)) {
        //    return getAll(entityManager, JpaAssetAdministrationShellDescriptor.class, AssetAdministrationShellDescriptor.class);
        //}

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JpaAssetAdministrationShellDescriptor> cq = builder.createQuery(JpaAssetAdministrationShellDescriptor.class);
        //var queryCriteria = builder.createQuery(JpaAssetAdministrationShellDescriptor.class);
        Root<JpaAssetAdministrationShellDescriptor> root = cq.from(JpaAssetAdministrationShellDescriptor.class);
        List<Predicate> predicates = new ArrayList<>();
        if (assetType != null) {
            predicates.add(builder.equal(root.get("assetType"), assetType));
        }
        if (assetKind != null) {
            predicates.add(builder.equal(root.get("assetKind"), assetKind));
        }
        //Metamodel m = entityManager.getMetamodel();
        //var aas_ = m.entity(JpaAssetAdministrationShellDescriptor.class);
        //EntityType<JpaAssetAdministrationShellDescriptor> Aas_ = root.getModel();
        cq.select(root);
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(Predicate[]::new));
        }
        //cq.orderBy(builder.asc(root.get(Aas_.getId(JpaAssetAdministrationShellDescriptor.class))));
        cq.orderBy(builder.asc(root.get("id")));
        var query = entityManager.createQuery(cq);
        return query.getResultList().stream()
                .map(AssetAdministrationShellDescriptor.class::cast)
                .toList();
    }


    /**
     * Fetches all instances of AssetAdministrationShellDescriptor, matching the given criteria.
     *
     * @param entityManager The entityManager to use.
     * @param assetType The desired assetType.
     * @param assetKind The desired assetKind.
     * @param paging The desired Paging info.
     * @return All instances matching the given criteria.
     */
    public static Page<AssetAdministrationShellDescriptor> getPagedAas(EntityManager entityManager, String assetType, AssetKind assetKind, PagingInfo paging) {
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
        int limit = Long.valueOf(paging.getLimit()).intValue();
        if ((limit < 0) || (limit > AasRepository.DEFAULT_LIMIT)) {
            limit = AasRepository.DEFAULT_LIMIT;
        }
        int cursor = 0;
        if (paging.getCursor() != null) {
            cursor = Integer.parseInt(paging.getCursor());
        }
        var query = entityManager.createQuery(queryCriteria).setFirstResult(cursor).setMaxResults(limit);
        List<AssetAdministrationShellDescriptor> retval = query.getResultList().stream()
                .map(AssetAdministrationShellDescriptor.class::cast)
                .toList();
        String nextCursor = null;
        if (retval.size() >= limit) {
            nextCursor = Integer.toString(cursor + retval.size());
        }
        return Page.<AssetAdministrationShellDescriptor> builder()
                .result(retval)
                .metadata(PagingMetadata.builder()
                        .cursor(nextCursor)
                        .build())
                .build();
    }
}
