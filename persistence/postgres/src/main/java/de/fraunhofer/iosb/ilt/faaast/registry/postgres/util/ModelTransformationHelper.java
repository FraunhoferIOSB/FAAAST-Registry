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

import de.fraunhofer.iosb.ilt.faaast.registry.postgres.model.AssetAdministrationShellDescriptorEntity;
import de.fraunhofer.iosb.ilt.faaast.registry.postgres.model.SubmodelDescriptorEntity;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.DeserializerWrapper;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.Extension;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelDescriptor;


/**
 * Helper class to transform AAS model classes to Postgres model classes.
 */
public class ModelTransformationHelper {

    private static final JsonSerializer jsonSerializer = new JsonSerializer();
    private static final DeserializerWrapper jsonDeserializer = new DeserializerWrapper();

    private ModelTransformationHelper() {}


    /**
     * Generates AssetAdministrationShellDescriptorEntity from AssetAdministrationShellDescriptor.
     *
     * @param aas The AssetAdministrationShellDescriptor.
     * @return The converted AssetAdministrationShellDescriptorEntity entity.
     * @throws SerializationException If a serialization error occurs.
     */
    public static AssetAdministrationShellDescriptorEntity convertAAS(AssetAdministrationShellDescriptor aas) throws SerializationException {
        if (aas == null) {
            return null;
        }

        return new AssetAdministrationShellDescriptorEntity.Builder()
                .id(aas.getId())
                .idShort(aas.getIdShort())
                .administration(jsonSerializer.write(aas.getAdministration()))
                .assetKind(aas.getAssetKind())
                .assetType(aas.getAssetType())
                .globalAssetId(aas.getGlobalAssetId())
                .endpoints(jsonSerializer.write(aas.getEndpoints()))
                .specificAssetIds(jsonSerializer.write(aas.getSpecificAssetIds()))
                .description(jsonSerializer.write(aas.getDescription()))
                .displayName(jsonSerializer.write(aas.getDisplayName()))
                .extensions(jsonSerializer.write(aas.getExtensions()))
                //.submodelDescriptors(jsonSerializer.write(aas.getSubmodelDescriptors()))
                .submodelDescriptors(convertSubmodels(aas.getSubmodelDescriptors()))
                .build();
    }


    /**
     * Generates AssetAdministrationShellDescriptor from AssetAdministrationShellDescriptorEntity.
     *
     * @param aas The AssetAdministrationShellDescriptorEntity.
     * @return The converted AssetAdministrationShellDescriptor.
     * @throws DeserializationException If a serialization error occurs.
     */
    public static AssetAdministrationShellDescriptor convertAAS(AssetAdministrationShellDescriptorEntity aas) throws DeserializationException {
        if (aas == null) {
            return null;
        }

        DefaultAssetAdministrationShellDescriptor.Builder builder = new DefaultAssetAdministrationShellDescriptor.Builder()
                .id(aas.getId())
                .idShort(aas.getIdShort())
                .assetKind(aas.getAssetKind())
                .assetType(aas.getAssetType())
                .globalAssetId(aas.getGlobalAssetId());

        if ((aas.getAdministration() != null) && (!aas.getAdministration().isEmpty())) {
            builder.administration(jsonDeserializer.read(aas.getAdministration(), AdministrativeInformation.class));
        }
        if ((aas.getEndpoints() != null) && (!aas.getEndpoints().isEmpty())) {
            builder.endpoints(jsonDeserializer.readList(aas.getEndpoints(), Endpoint.class));
        }
        if ((aas.getSpecificAssetIds() != null) && (!aas.getSpecificAssetIds().isEmpty())) {
            builder.specificAssetIds(jsonDeserializer.readList(aas.getSpecificAssetIds(), SpecificAssetId.class));
        }
        if ((aas.getDescription() != null) && (!aas.getDescription().isEmpty())) {
            builder.description(jsonDeserializer.readList(aas.getDescription(), LangStringTextType.class));
        }
        if ((aas.getDisplayName() != null) && (!aas.getDisplayName().isEmpty())) {
            builder.displayName(jsonDeserializer.readList(aas.getDisplayName(), LangStringNameType.class));
        }
        if ((aas.getExtensions() != null) && (!aas.getExtensions().isEmpty())) {
            builder.extensions(jsonDeserializer.readList(aas.getExtensions(), Extension.class));
        }
        if ((aas.getSubmodelDescriptors() != null) && (!aas.getSubmodelDescriptors().isEmpty())) {
            //builder.submodelDescriptors(jsonDeserializer.readList(aas.getSubmodelDescriptors(), SubmodelDescriptor.class));
            builder.submodelDescriptors(convertSubmodelsEntity(aas.getSubmodelDescriptors()));
        }

        return builder.build();
    }


    /**
     * Generates a SubmodelDescriptorEntity from SubmodelDescriptor.
     *
     * @param submodel The desired SubmodelDescriptor.
     * @return The converted SubmodelDescriptorEntity.
     */
    public static SubmodelDescriptorEntity convertSubmodel(SubmodelDescriptor submodel) {
        if (submodel == null) {
            return null;
        }

        return new SubmodelDescriptorEntity.Builder()
                .id(submodel.getId())
                .idShort(submodel.getIdShort())
                .build();
    }


    /**
     * Generates a SubmodelDescriptor from a SubmodelDescriptorEntity.
     *
     * @param submodel The desired SubmodelDescriptorEntity.
     * @return The converted SubmodelDescriptorEntity.
     */
    public static SubmodelDescriptor convertSubmodel(SubmodelDescriptorEntity submodel) {
        if (submodel == null) {
            return null;
        }

        return new DefaultSubmodelDescriptor.Builder()
                .id(submodel.getId())
                .idShort(submodel.getIdShort())
                .build();
    }


    /**
     * Generates a list of SubmodelDescriptorEntities from the corresponding SubmodelDescriptors.
     *
     * @param submodels The desired list of SubmodelDescriptor.
     * @return The converted list of SubmodelDescriptorEntity.
     */
    public static List<SubmodelDescriptorEntity> convertSubmodels(List<SubmodelDescriptor> submodels) {
        if (submodels == null) {
            return null;
        }

        return submodels.stream()
                .map(s -> convertSubmodel(s))
                .toList();
    }


    /**
     * Generates a list of SubmodelDescriptors from the corresponding SubmodelDescriptorEntities.
     *
     * @param submodels The desired list of SubmodelDescriptorEntities.
     * @return The converted list of SubmodelDescriptors.
     */
    public static List<SubmodelDescriptor> convertSubmodelsEntity(List<SubmodelDescriptorEntity> submodels) {
        if (submodels == null) {
            return null;
        }

        return submodels.stream()
                .map(s -> convertSubmodel(s))
                .toList();
    }
}
