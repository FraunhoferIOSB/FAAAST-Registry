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

import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaAdministrativeInformation;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaAssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaDescription;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaEndpoint;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaIdentifier;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaIdentifierKeyValuePair;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaKey;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaProtocolInformation;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaReference;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaSubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaSubmodelDescriptorStandalone;
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

    private ModelTransformationHelper() {}


    /**
     * Converts AssetAdministrationShellDescriptor to JPAAssetAdministrationShellDescriptor.
     *
     * @param aas The AssetAdministrationShellDescriptor.
     * @return The converted JPAAssetAdministrationShellDescriptor.
     */
    public static JpaAssetAdministrationShellDescriptor convertAAS(AssetAdministrationShellDescriptor aas) {
        return new JpaAssetAdministrationShellDescriptor.Builder()
                .from(aas)
                .build();
    }


    /**
     * Converts AdministrativeInformation to JPAAdministrativeInformation.
     *
     * @param administrativeInformation The AdministrativeInformation.
     * @return The converted JPAAdministrativeInformation.
     */
    public static JpaAdministrativeInformation convertAdministrativeInformation(AdministrativeInformation administrativeInformation) {
        return new JpaAdministrativeInformation.Builder()
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
                .map(x -> new JpaDescription.Builder().from(x).build())
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
                .map(x -> new JpaEndpoint.Builder().from(x).build())
                .collect(Collectors.toList());
    }


    /**
     * Converts Identifier to JPAIdentifier.
     *
     * @param identifier The Identifier.
     * @return The converted JPAIdentifier.
     */
    public static Identifier convertIdentifier(Identifier identifier) {
        return new JpaIdentifier.Builder().from(identifier).build();
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
                .map(x -> new JpaIdentifierKeyValuePair.Builder().from(x).build())
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
                .map(x -> new JpaKey.Builder().from(x).build())
                .collect(Collectors.toList());
    }


    /**
     * Converts ProtocolInformation to JPAProtocolInformation.
     *
     * @param protocolInformation The ProtocolInformation.
     * @return The converted JPAProtocolInformation.
     */
    public static JpaProtocolInformation convertProtocolInformation(ProtocolInformation protocolInformation) {
        return new JpaProtocolInformation.Builder().from(protocolInformation).build();
    }


    /**
     * Converts Reference to JPAReference.
     *
     * @param reference The Reference.
     * @return The converted JPAReference.
     */
    public static JpaReference convertReference(Reference reference) {
        return new JpaReference.Builder()
                .from(reference)
                .build();
    }


    /**
     * Converts SubmodelDescriptor to JPASubmodelDescriptor.
     *
     * @param submodel The SubmodelDescriptor.
     * @return The converted JPASubmodelDescriptor.
     */
    public static JpaSubmodelDescriptor convertSubmodel(SubmodelDescriptor submodel) {
        return new JpaSubmodelDescriptor.Builder().from(submodel).build();
    }


    /**
     * Converts SubmodelDescriptor to JPASubmodelDescriptorStandalone.
     *
     * @param submodel The SubmodelDescriptor.
     * @return The converted JPASubmodelDescriptor.
     */
    public static JpaSubmodelDescriptorStandalone convertSubmodelStandalone(SubmodelDescriptor submodel) {
        return new JpaSubmodelDescriptorStandalone.Builder().from(submodel).build();
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
                .map(x -> new JpaSubmodelDescriptor.Builder().from(x).build())
                .collect(Collectors.toList());
    }

}
