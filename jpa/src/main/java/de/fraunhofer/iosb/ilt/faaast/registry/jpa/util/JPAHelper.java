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
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.DescriptionDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.EndpointDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.IdentifierKeyValuePairDescriptor;
import java.util.ArrayList;
import java.util.List;


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
    public static List<EndpointDescriptor> createJPAEndpoints(List<EndpointDescriptor> endpoints) {
        List<EndpointDescriptor> retval = new ArrayList<>();
        endpoints.forEach((e) -> {
            retval.add(new JPAEndpointDescriptor(e));
        });
        return retval;
    }


    /**
     * Create a list of JPA descriptions from a list of default endpoints.
     *
     * @param descriptions The desired descriptions.
     * @return The list of JPA descriptions.
     */
    public static List<DescriptionDescriptor> createJPADescriptions(List<DescriptionDescriptor> descriptions) {
        List<DescriptionDescriptor> retval = new ArrayList<>();
        descriptions.forEach((e) -> {
            retval.add(new JPADescriptionDescriptor(e));
        });
        return retval;
    }


    /**
     * Create a list of JPA IdentifierKeyValuePairs from a list of default endpoints.
     *
     * @param pairs The desired IdentifierKeyValuePairs.
     * @return The list of JPA IdentifierKeyValuePairs.
     */
    public static List<IdentifierKeyValuePairDescriptor> createJPAIdentifierKeyValuePair(List<IdentifierKeyValuePairDescriptor> pairs) {
        List<IdentifierKeyValuePairDescriptor> retval = new ArrayList<>();
        pairs.forEach((e) -> {
            retval.add(new JPAIdentifierKeyValuePairDescriptor(e));
        });
        return retval;
    }
}
