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
package de.fraunhofer.iosb.ilt.faaast.registry.jpa.model;

import de.fraunhofer.iosb.ilt.faaast.registry.jpa.util.ModelTransformationHelper;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.AssetAdministrationShellDescriptorBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShellDescriptor;


/**
 * Registry Descriptor JPA implementation for AssetAdministrationShell.
 */
public class JpaAssetAdministrationShellDescriptor extends DefaultAssetAdministrationShellDescriptor {

    public abstract static class AbstractBuilder<T extends JpaAssetAdministrationShellDescriptor, B extends AbstractBuilder<T, B>>
            extends AssetAdministrationShellDescriptorBuilder<T, B> {

        public B from(AssetAdministrationShellDescriptor other) {
            if (Objects.nonNull(other)) {
                id(other.getId());
                idShort(other.getIdShort());
                assetKind(other.getAssetKind());
                assetType(other.getAssetType());
                endpoints(ModelTransformationHelper.convertEndpoints(other.getEndpoints()));
                administration(ModelTransformationHelper.convertAdministrativeInformation(other.getAdministration()));
                description(ModelTransformationHelper.convertDescriptions(other.getDescription()));
                displayName(ModelTransformationHelper.convertDisplayNames(other.getDisplayName()));
                globalAssetId(other.getGlobalAssetId());
                specificAssetIds(ModelTransformationHelper.convertSpecificAssetIds(other.getSpecificAssetIds()));
                extensions(ModelTransformationHelper.convertExtensions(other.getExtensions()));
                submodelDescriptors(ModelTransformationHelper.convertSubmodels(other.getSubmodelDescriptors()));
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<JpaAssetAdministrationShellDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected JpaAssetAdministrationShellDescriptor newBuildingInstance() {
            return new JpaAssetAdministrationShellDescriptor();
        }

    }
}
