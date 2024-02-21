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

    //@JsonIgnore
    //private String id;

    protected JpaSubmodelDescriptorBase() {
        //    id = null;
    }

    //public String getId() {
    //    return id;
    //}

    //public void setId(String id) {
    //    this.id = id;
    //}

    //    @Override
    //    public int hashCode() {
    //        return Objects.hash(super.hashCode(), id);
    //    }
    //
    //
    //    @Override
    //    public boolean equals(Object obj) {
    //        if (this == obj) {
    //            return true;
    //        }
    //        else if (obj == null) {
    //            return false;
    //        }
    //        else if (this.getClass() != obj.getClass()) {
    //            return false;
    //        }
    //        else {
    //            JpaSubmodelDescriptorBase other = (JpaSubmodelDescriptorBase) obj;
    //            return super.equals(obj)
    //                    && Objects.equals(this.id, other.id);
    //        }
    //    }

    public abstract static class AbstractBuilder<T extends JpaSubmodelDescriptorBase, B extends AbstractBuilder<T, B>>
            extends DefaultSubmodelDescriptor.AbstractBuilder<T, B> {

        //public B id(String value) {
        //    getBuildingInstance().setId(value);
        //    return getSelf();
        //}

        @Override
        public B from(SubmodelDescriptor other) {
            if (other != null) {
                //id(other.getIdentification().getIdentifier());
                id(other.getId());
                idShort(other.getIdShort());
                endpoints(ModelTransformationHelper.convertEndpoints(other.getEndpoints()));
                administration(ModelTransformationHelper.convertAdministrativeInformation(other.getAdministration()));
                descriptions(ModelTransformationHelper.convertDescriptions(other.getDescriptions()));
                displayNames(ModelTransformationHelper.convertDisplayNames(other.getDisplayNames()));
                //identification(ModelTransformationHelper.convertIdentifier(other.getIdentification()));
                semanticId(ModelTransformationHelper.convertReference(other.getSemanticId()));
            }
            return getSelf();
        }
    }
}
