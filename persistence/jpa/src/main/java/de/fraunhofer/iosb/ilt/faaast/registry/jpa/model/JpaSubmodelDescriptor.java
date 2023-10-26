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

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.util.ModelTransformationHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultSubmodelDescriptor;
import java.util.Objects;


/**
 * Registry Descriptor JPA implementation for Submodel.
 */
public class JpaSubmodelDescriptor extends DefaultSubmodelDescriptor {

    @JsonIgnore
    private String id;
    @JsonIgnore
    private boolean standalone;

    public JpaSubmodelDescriptor() {
        id = null;
        standalone = false;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public boolean getStandalone() {
        return standalone;
    }


    public void setStandalone(boolean value) {
        standalone = value;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, standalone);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (obj == null) {
            return false;
        }
        else if (this.getClass() != obj.getClass()) {
            return false;
        }
        else {
            JpaSubmodelDescriptor other = (JpaSubmodelDescriptor) obj;
            return super.equals(obj)
                    && Objects.equals(this.id, other.id)
                    && Objects.equals(this.standalone, other.standalone);
        }
    }

    public abstract static class AbstractBuilder<T extends JpaSubmodelDescriptor, B extends AbstractBuilder<T, B>>
            extends DefaultSubmodelDescriptor.AbstractBuilder<JpaSubmodelDescriptor, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B standalone(boolean value) {
            getBuildingInstance().setStandalone(value);
            return getSelf();
        }


        @Override
        public B from(SubmodelDescriptor other) {
            if (other != null) {
                id(other.getIdentification().getIdentifier());
                idShort(other.getIdShort());
                endpoints(ModelTransformationHelper.convertEndpoints(other.getEndpoints()));
                administration(ModelTransformationHelper.convertAdministrativeInformation(other.getAdministration()));
                descriptions(ModelTransformationHelper.convertDescriptions(other.getDescriptions()));
                displayNames(ModelTransformationHelper.convertDescriptions(other.getDisplayNames()));
                identification(ModelTransformationHelper.convertIdentifier(other.getIdentification()));
                semanticId(ModelTransformationHelper.convertReference(other.getSemanticId()));
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<JpaSubmodelDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected JpaSubmodelDescriptor newBuildingInstance() {
            return new JpaSubmodelDescriptor();
        }
    }
}
