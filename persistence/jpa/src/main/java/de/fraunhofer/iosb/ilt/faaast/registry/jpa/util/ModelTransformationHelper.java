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
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaDataSpecificationIec61360;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaDescription;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaDisplayName;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaEndpoint;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaExtension;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaKey;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaLangStringDefinitionTypeIec61360;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaLangStringPreferredNameTypeIec61360;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaLangStringShortNameTypeIec61360;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaLevelType;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaProtocolInformation;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaReference;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaSpecificAssetId;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaSubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaSubmodelDescriptorStandalone;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaValueList;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaValueReferencePair;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.Endpoint;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.ProtocolInformation;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.AdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.DataSpecificationIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.Extension;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringDefinitionTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringPreferredNameTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringShortNameTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.LevelType;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.ValueList;
import org.eclipse.digitaltwin.aas4j.v3.model.ValueReferencePair;


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
     * Converts a list of LangStringTextType to a list of JPALangStringTextType.
     *
     * @param descriptions The list of descriptions.
     * @return The converted list of descriptions.
     */
    public static List<LangStringTextType> convertDescriptions(List<LangStringTextType> descriptions) {
        return descriptions.stream()
                .map(x -> new JpaDescription.Builder().from(x).build())
                .collect(Collectors.toList());
    }


    /**
     * Converts a list of LangStringNameType to a list of JPALangStringNameType.
     *
     * @param names The list of names.
     * @return The converted list of names.
     */
    public static List<LangStringNameType> convertDisplayNames(List<LangStringNameType> names) {
        return names.stream()
                .map(x -> new JpaDisplayName.Builder().from(x).build())
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

    //    /**
    //     * Converts Identifier to JPAIdentifier.
    //     *
    //     * @param identifier The Identifier.
    //     * @return The converted JPAIdentifier.
    //     */
    //    public static Identifier convertIdentifier(Identifier identifier) {
    //        return new JpaIdentifier.Builder().from(identifier).build();
    //    }


    /**
     * Converts a list of SpecificAssetIds to a list of JPASpecificAssetId.
     *
     * @param pairs The list of SpecificAssetId.
     * @return The converted list of JPASpecificAssetId.
     */
    public static List<SpecificAssetId> convertSpecificAssetIds(List<SpecificAssetId> pairs) {
        if (Objects.isNull(pairs)) {
            return null;
        }
        return pairs.stream()
                .map(x -> new JpaSpecificAssetId.Builder().from(x).build())
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
     * Converts Reference to JPAReference.
     *
     * @param references The list of References.
     * @return The converted list of References.
     */
    public static List<Reference> convertReferences(List<Reference> references) {
        if (Objects.isNull(references)) {
            return null;
        }
        return references.stream()
                .map(x -> new JpaReference.Builder().from(x).build())
                .collect(Collectors.toList());
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


    /**
     * Converts Extension to JPAExtension.
     *
     * @param extension The Extension.
     * @return The converted JPAExtension.
     */
    public static JpaExtension convertExtension(Extension extension) {
        return new JpaExtension.Builder()
                .from(extension)
                .build();
    }


    /**
     * Converts Extension to JPAExtension.
     *
     * @param extensions The list of Extensions.
     * @return The converted list of Extensions.
     */
    public static List<Extension> convertExtensions(List<Extension> extensions) {
        if (Objects.isNull(extensions)) {
            return null;
        }
        return extensions.stream()
                .map(x -> new JpaExtension.Builder().from(x).build())
                .collect(Collectors.toList());
    }


    /**
     * Converts a list of LangStringPreferredNameTypeIec61360 to a list of JpaLangStringPreferredNameTypeIec61360.
     *
     * @param names The list of names.
     * @return The converted list of names.
     */
    public static List<LangStringPreferredNameTypeIec61360> convertPreferredNameIec61360(List<LangStringPreferredNameTypeIec61360> names) {
        return names.stream()
                .map(x -> new JpaLangStringPreferredNameTypeIec61360.Builder().from(x).build())
                .collect(Collectors.toList());
    }


    /**
     * Converts a list of LangStringShortNameTypeIec61360 to a list of JpaLangStringShortNameTypeIec61360.
     *
     * @param names The list of names.
     * @return The converted list of names.
     */
    public static List<LangStringShortNameTypeIec61360> convertShortNameIec61360(List<LangStringShortNameTypeIec61360> names) {
        return names.stream()
                .map(x -> new JpaLangStringShortNameTypeIec61360.Builder().from(x).build())
                .collect(Collectors.toList());
    }


    /**
     * Converts a list of LangStringDefinitionTypeIec61360 to a list of JpaLangStringDefinitionTypeIec61360.
     *
     * @param names The list of names.
     * @return The converted list of names.
     */
    public static List<LangStringDefinitionTypeIec61360> convertDefinitionIec61360(List<LangStringDefinitionTypeIec61360> names) {
        return names.stream()
                .map(x -> new JpaLangStringDefinitionTypeIec61360.Builder().from(x).build())
                .collect(Collectors.toList());
    }


    /**
     * Converts a list of ValueReferencePair to a list of JpaValueReferencePair.
     *
     * @param pairs The list of pairs.
     * @return The converted list of pairs.
     */
    public static List<ValueReferencePair> convertValueReferencePairs(List<ValueReferencePair> pairs) {
        return pairs.stream()
                .map(x -> new JpaValueReferencePair.Builder().from(x).build())
                .collect(Collectors.toList());
    }


    /**
     * Converts a ValueList to a JpaValueList.
     *
     * @param value the desired ValueList.
     * @return The converted ValueList.
     */
    public static ValueList convertValueList(ValueList value) {
        return new JpaValueList.Builder()
                .from(value)
                .build();
    }


    /**
     * Converts a DataSpecificationIec61360 into JpaDataSpecificationIec61360.
     *
     * @param iec The desired DataSpecificationIec61360.
     * @return The converted JPA DataSpecificationIec61360.
     */
    public static DataSpecificationIec61360 convertDataSpecificationIec61360(DataSpecificationIec61360 iec) {
        return new JpaDataSpecificationIec61360.Builder().from(iec).build();
    }


    /**
     * Converts LevelType to JpaLevelType.
     *
     * @param levelType The LevelType.
     * @return The converted LevelType.
     */
    public static JpaLevelType convertLevelType(LevelType levelType) {
        return new JpaLevelType.Builder()
                .from(levelType)
                .build();
    }
}
