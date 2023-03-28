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
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.util.JPAHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultSubmodelDescriptor;


/**
 * Registry Descriptor JPA implementation for Submodel.
 */
public class JPASubmodelDescriptor extends DefaultSubmodelDescriptor {

    @JsonIgnore
    private String id;

    public JPASubmodelDescriptor() {
        id = null;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }

    public abstract static class AbstractBuilder<T extends JPASubmodelDescriptor, B extends AbstractBuilder<T, B>>
            extends DefaultSubmodelDescriptor.AbstractBuilder<JPASubmodelDescriptor, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        @Override
        public B from(SubmodelDescriptor other) {
            super.from(other);
            if (other != null) {
                id(other.getIdentification().getIdentifier());
                endpoints(JPAHelper.createJPAEndpoints(other.getEndpoints()));
                administration(new JPAAdministrativeInformation(other.getAdministration()));
                descriptions(JPAHelper.createJPADescriptions(other.getDescriptions()));
                identification(new JPAIdentifier(other.getIdentification()));
                semanticId(new JPAReference(other.getSemanticId()));
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<JPASubmodelDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected JPASubmodelDescriptor newBuildingInstance() {
            return new JPASubmodelDescriptor();
        }
    }
}
