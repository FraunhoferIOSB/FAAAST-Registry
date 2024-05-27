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
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultSubmodelDescriptor;


/**
 * Registry Descriptor JPA implementation Base class for Submodel.
 */
public abstract class JpaSubmodelDescriptorBase extends DefaultSubmodelDescriptor {

    protected JpaSubmodelDescriptorBase() {}

    public abstract static class AbstractBuilder<T extends JpaSubmodelDescriptorBase, B extends AbstractBuilder<T, B>>
            extends DefaultSubmodelDescriptor.AbstractBuilder<T, B> {

        @Override
        public B from(SubmodelDescriptor other) {
            if (other != null) {
                id(other.getId());
                idShort(other.getIdShort());
                endpoints(ModelTransformationHelper.convertEndpoints(other.getEndpoints()));
                administration(ModelTransformationHelper.convertAdministrativeInformation(other.getAdministration()));
                descriptions(ModelTransformationHelper.convertDescriptions(other.getDescriptions()));
                displayNames(ModelTransformationHelper.convertDisplayNames(other.getDisplayNames()));
                semanticId(ModelTransformationHelper.convertReference(other.getSemanticId()));
                extensions(ModelTransformationHelper.convertExtensions(other.getExtensions()));
                supplementalSemanticIds(ModelTransformationHelper.convertReferences(other.getSupplementalSemanticIds()));
            }
            return getSelf();
        }
    }
}
