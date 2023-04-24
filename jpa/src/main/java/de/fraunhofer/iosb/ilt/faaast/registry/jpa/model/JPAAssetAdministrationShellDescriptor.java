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
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultAssetAdministrationShellDescriptor;


/**
 * Registry Descriptor JPA implementation for AssetAdministrationShell.
 */
public class JPAAssetAdministrationShellDescriptor extends DefaultAssetAdministrationShellDescriptor {

    @JsonIgnore
    private String id;

    public JPAAssetAdministrationShellDescriptor() {
        id = null;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }

    public abstract static class AbstractBuilder<T extends JPAAssetAdministrationShellDescriptor, B extends AbstractBuilder<T, B>>
            extends DefaultAssetAdministrationShellDescriptor.AbstractBuilder<JPAAssetAdministrationShellDescriptor, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        @Override
        public B from(AssetAdministrationShellDescriptor other) {
            if (other != null) {
                id(other.getIdentification().getIdentifier());
                idShort(other.getIdShort());
                endpoints(JPAHelper.createJPAEndpoints(other.getEndpoints()));
                administration(new JPAAdministrativeInformationDescriptor(other.getAdministration()));
                descriptions(JPAHelper.createJPADescriptions(other.getDescriptions()));
                identification(other.getIdentification());
                globalAssetId(other.getGlobalAssetId());
                specificAssetIds(JPAHelper.createJPAIdentifierKeyValuePair(other.getSpecificAssetIds()));
                other.getSubmodels().forEach((s) -> {
                    submodel(new JPASubmodelDescriptor.Builder().from(s).build());
                });
                //submodels(other.getSubmodels());
            }
            //super.from(other);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<JPAAssetAdministrationShellDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected JPAAssetAdministrationShellDescriptor newBuildingInstance() {
            return new JPAAssetAdministrationShellDescriptor();
        }
    }
}
