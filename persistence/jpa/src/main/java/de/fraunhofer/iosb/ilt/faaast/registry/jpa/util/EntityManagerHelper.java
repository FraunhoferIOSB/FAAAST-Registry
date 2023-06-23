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

import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;


/**
 * Helper class for JPA objects.
 */
public class EntityManagerHelper {

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
        //var query = entityManager.createQuery(String.format("SELECT x FROM %s x", type.getSimpleName()));
        var query = entityManager.createQuery(queryCriteria);
        return query.getResultList().stream()
                .map(returnType::cast)
                .collect(Collectors.toList());
    }
}
