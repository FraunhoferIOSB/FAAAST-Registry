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

import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JPAAdministrativeInformation;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JPAAssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JPADescription;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JPAEndpoint;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JPAIdentifier;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JPAIdentifierKeyValuePair;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JPAKey;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JPAProtocolInformation;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JPAReference;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JPASubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.Endpoint;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.ProtocolInformation;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import io.adminshell.aas.v3.model.AdministrativeInformation;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.Reference;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Helper class to transform AAS model classes to JPA model classes.
 */
public class ModelTransformationHelper {

    /**
     * Converts AssetAdministrationShellDescriptor to JPAAssetAdministrationShellDescriptor.
     *
     * @param aas The AssetAdministrationShellDescriptor.
     * @return The converted JPAAssetAdministrationShellDescriptor.
     */
    public static JPAAssetAdministrationShellDescriptor convertAAS(AssetAdministrationShellDescriptor aas) {
        return new JPAAssetAdministrationShellDescriptor.Builder()
                .from(aas)
                .build();
    }


    /**
     * Converts AdministrativeInformation to JPAAdministrativeInformation.
     *
     * @param administrativeInformation The AdministrativeInformation.
     * @return The converted JPAAdministrativeInformation.
     */
    public static JPAAdministrativeInformation convertAdministrativeInformation(AdministrativeInformation administrativeInformation) {
        return new JPAAdministrativeInformation.Builder()
                .from(administrativeInformation)
                .build();
    }


    /**
     * Converts a list of LangString to a list of JPADescription.
     *
     * @param descriptions The list of LangString.
     * @return The converted list of JPADescription.
     */
    public static List<LangString> convertDescriptions(List<LangString> descriptions) {
        return descriptions.stream()
                .map(x -> new JPADescription.Builder().from(x).build())
                .collect(Collectors.toList());
    }


    /**
     * Converts a list of Endpoint to a list of JPAEndpoint.
     *
     * @param endpoints The list of Endpoint.
     * @return The converted list of JPAEndpoint.
     */
    public static List<Endpoint> convertEndpoints(List<Endpoint> endpoints) {
        if (Objects.isNull(endpoints)) {
            return null;
        }
        return endpoints.stream()
                .map(x -> new JPAEndpoint.Builder().from(x).build())
                .collect(Collectors.toList());
    }


    /**
     * Converts Identifier to JPAIdentifier.
     *
     * @param identifier The Identifier.
     * @return The converted JPAIdentifier.
     */
    public static Identifier convertIdentifier(Identifier identifier) {
        return new JPAIdentifier.Builder().from(identifier).build();
    }


    /**
     * Converts a list of IdentifierKeyValuePair to a list of JPAIdentifierKeyValuePair.
     *
     * @param pairs The list of IdentifierKeyValuePair.
     * @return The converted list of JPAIdentifierKeyValuePair.
     */
    public static List<IdentifierKeyValuePair> convertIdentifierKeyValuePairs(List<IdentifierKeyValuePair> pairs) {
        if (Objects.isNull(pairs)) {
            return null;
        }
        return pairs.stream()
                .map(x -> new JPAIdentifierKeyValuePair.Builder().from(x).build())
                .collect(Collectors.toList());
    }


    /**
     * Converts a list of Key to a list of JPAKey.
     *
     * @param keys The list of Key.
     * @return The converted list of JPAKey.
     */
    public static List<Key> convertKeys(List<Key> keys) {
        if (Objects.isNull(keys)) {
            return null;
        }
        return keys.stream()
                .map(x -> new JPAKey.Builder().from(x).build())
                .collect(Collectors.toList());
    }


    /**
     * Converts ProtocolInformation to JPAProtocolInformation.
     *
     * @param protocolInformation The ProtocolInformation.
     * @return The converted JPAProtocolInformation.
     */
    public static JPAProtocolInformation convertProtocolInformation(ProtocolInformation protocolInformation) {
        return new JPAProtocolInformation.Builder().from(protocolInformation).build();
    }


    /**
     * Converts Reference to JPAReference.
     *
     * @param reference The Reference.
     * @return The converted JPAReference.
     */
    public static JPAReference convertReference(Reference reference) {
        return new JPAReference.Builder()
                .from(reference)
                .build();
    }


    /**
     * Converts SubmodelDescriptor to JPASubmodelDescriptor.
     *
     * @param submodel The SubmodelDescriptor.
     * @return The converted JPASubmodelDescriptor.
     */
    public static JPASubmodelDescriptor convertSubmodel(SubmodelDescriptor submodel) {
        return new JPASubmodelDescriptor.Builder().from(submodel).build();
    }


    /**
     * Converts a list of SubmodelDescriptor to a list of JPASubmodelDescriptor.
     *
     * @param submodels The list of SubmodelDescriptor.
     * @return The converted list of JPASubmodelDescriptor.
     */
    public static List<SubmodelDescriptor> convertSubmodels(List<SubmodelDescriptor> submodels) {
        if (Objects.isNull(submodels)) {
            return null;
        }
        return submodels.stream()
                .map(x -> new JPASubmodelDescriptor.Builder().from(x).build())
                .collect(Collectors.toList());
    }

}
