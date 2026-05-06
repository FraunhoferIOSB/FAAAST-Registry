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
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
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
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
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
     * @throws DeserializationException If a deserialization error occurs.
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
     * @throws SerializationException If a serialization error occurs.
     */
    public static SubmodelDescriptorEntity convertSubmodel(SubmodelDescriptor submodel) throws SerializationException {
        if (submodel == null) {
            return null;
        }

        return new SubmodelDescriptorEntity.Builder()
                .id(submodel.getId())
                .idShort(submodel.getIdShort())
                .administration(jsonSerializer.write(submodel.getAdministration()))
                .endpoints(jsonSerializer.write(submodel.getEndpoints()))
                .description(jsonSerializer.write(submodel.getDescription()))
                .displayName(jsonSerializer.write(submodel.getDisplayName()))
                .extensions(jsonSerializer.write(submodel.getExtensions()))
                .semanticId(jsonSerializer.write(submodel.getSemanticId()))
                .supplementalSemanticId(jsonSerializer.write(submodel.getSupplementalSemanticId()))
                .build();
    }


    /**
     * Generates a SubmodelDescriptor from a SubmodelDescriptorEntity.
     *
     * @param submodel The desired SubmodelDescriptorEntity.
     * @return The converted SubmodelDescriptorEntity.
     * @throws DeserializationException If a serialization error occurs.
     */
    public static SubmodelDescriptor convertSubmodel(SubmodelDescriptorEntity submodel) throws DeserializationException {
        if (submodel == null) {
            return null;
        }

        DefaultSubmodelDescriptor.Builder builder = new DefaultSubmodelDescriptor.Builder()
                .id(submodel.getId())
                .idShort(submodel.getIdShort());

        if ((submodel.getAdministration() != null) && (!submodel.getAdministration().isEmpty())) {
            builder.administration(jsonDeserializer.read(submodel.getAdministration(), AdministrativeInformation.class));
        }
        if ((submodel.getEndpoints() != null) && (!submodel.getEndpoints().isEmpty())) {
            builder.endpoints(jsonDeserializer.readList(submodel.getEndpoints(), Endpoint.class));
        }
        if ((submodel.getDescription() != null) && (!submodel.getDescription().isEmpty())) {
            builder.description(jsonDeserializer.readList(submodel.getDescription(), LangStringTextType.class));
        }
        if ((submodel.getDisplayName() != null) && (!submodel.getDisplayName().isEmpty())) {
            builder.displayName(jsonDeserializer.readList(submodel.getDisplayName(), LangStringNameType.class));
        }
        if ((submodel.getExtensions() != null) && (!submodel.getExtensions().isEmpty())) {
            builder.extensions(jsonDeserializer.readList(submodel.getExtensions(), Extension.class));
        }
        if ((submodel.getSemanticId() != null) && (!submodel.getSemanticId().isEmpty())) {
            builder.semanticId(jsonDeserializer.read(submodel.getSemanticId(), Reference.class));
        }
        if ((submodel.getSupplementalSemanticId() != null) && (!submodel.getSupplementalSemanticId().isEmpty())) {
            builder.supplementalSemanticId(jsonDeserializer.readList(submodel.getSupplementalSemanticId(), Reference.class));
        }
        return builder.build();
    }


    /**
     * Generates a list of SubmodelDescriptorEntities from the corresponding SubmodelDescriptors.
     *
     * @param submodels The desired list of SubmodelDescriptor.
     * @return The converted list of SubmodelDescriptorEntity.
     * @throws SerializationException If a serialization error occurs.
     */
    public static List<SubmodelDescriptorEntity> convertSubmodels(List<SubmodelDescriptor> submodels) throws SerializationException {
        if (submodels == null) {
            return null;
        }

        return submodels.stream()
                .map(LambdaExceptionHelper.rethrowFunction(s -> convertSubmodel(s)))
                .toList();
    }


    /**
     * Generates a list of SubmodelDescriptors from the corresponding SubmodelDescriptorEntities.
     *
     * @param submodels The desired list of SubmodelDescriptorEntities.
     * @return The converted list of SubmodelDescriptors.
     * @throws DeserializationException If a deserialization error occurs.
     */
    public static List<SubmodelDescriptor> convertSubmodelsEntity(List<SubmodelDescriptorEntity> submodels) throws DeserializationException {
        if (submodels == null) {
            return null;
        }

        return submodels.stream()
                .map(LambdaExceptionHelper.rethrowFunction(s -> convertSubmodel(s)))
                .toList();
    }
}
