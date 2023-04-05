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

import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JPADescription;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JPAEndpoint;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JPAIdentifierKeyValuePair;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JPAKey;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.Endpoint;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.LangString;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;


/**
 * Helper class for JPA objects.
 */
public class JPAHelper {

    /**
     * Create a list of JPA endpoints from a list of default endpoints.
     *
     * @param endpoints The desired endpoints.
     * @return The list of JPA endpoints.
     */
    public static List<Endpoint> createJPAEndpoints(List<Endpoint> endpoints) {
        List<Endpoint> retval = new ArrayList<>();
        endpoints.forEach((e) -> {
            retval.add(new JPAEndpoint.Builder().from(e).build());
        });
        return retval;
    }


    /**
     * Create a list of JPA descriptions from a list of default descriptions.
     *
     * @param descriptions The desired descriptions.
     * @return The list of JPA descriptions.
     */
    public static List<LangString> createJPADescriptions(List<LangString> descriptions) {
        List<LangString> retval = new ArrayList<>();
        descriptions.forEach((e) -> {
            retval.add(new JPADescription(e));
        });
        return retval;
    }


    /**
     * Create a list of JPA IdentifierKeyValuePairs from a list of default IdentifierKeyValuePairs.
     *
     * @param pairs The desired IdentifierKeyValuePairs.
     * @return The list of JPA IdentifierKeyValuePairs.
     */
    public static List<IdentifierKeyValuePair> createJPAIdentifierKeyValuePair(List<IdentifierKeyValuePair> pairs) {
        List<IdentifierKeyValuePair> retval = new ArrayList<>();
        pairs.forEach((e) -> {
            retval.add(new JPAIdentifierKeyValuePair(e));
        });
        return retval;
    }


    /**
     * Create a list of JPA Keys from a list of default Keys.
     *
     * @param keys The desired keys.
     * @return The list of JPA Keys.
     */
    public static List<Key> createJPAKeys(List<Key> keys) {
        List<Key> retval = new ArrayList<>();
        keys.forEach((e) -> {
            retval.add(new JPAKey(e));
        });
        return retval;
    }


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
