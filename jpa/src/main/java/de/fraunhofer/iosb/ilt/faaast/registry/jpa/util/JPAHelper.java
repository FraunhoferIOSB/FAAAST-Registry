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

import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JPADescriptionDescriptor;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JPAEndpointDescriptor;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JPAIdentifierKeyValuePairDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.Endpoint;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.LangString;
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
        return endpoints.stream()
                .map(x -> new JPAEndpointDescriptor.Builder().from(x).build())
                .collect(Collectors.toList());
    }


    /**
     * Create a list of JPA descriptions from a list of default endpoints.
     *
     * @param descriptions The desired descriptions.
     * @return The list of JPA descriptions.
     */
    public static List<LangString> createJPADescriptions(List<LangString> descriptions) {
        return descriptions.stream()
                .map(JPADescriptionDescriptor::new)
                .collect(Collectors.toList());
    }


    /**
     * Create a list of JPA IdentifierKeyValuePairs from a list of default endpoints.
     *
     * @param pairs The desired IdentifierKeyValuePairs.
     * @return The list of JPA IdentifierKeyValuePairs.
     */
    public static List<IdentifierKeyValuePair> createJPAIdentifierKeyValuePair(List<IdentifierKeyValuePair> pairs) {
        return pairs.stream()
                .map(JPAIdentifierKeyValuePairDescriptor::new)
                .collect(Collectors.toList());
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
