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
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.Endpoint;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultEndpoint;


/**
 * Registry Descriptor JPA implementation for Endpoint.
 */
public class JPAEndpointDescriptor extends DefaultEndpoint {

    @JsonIgnore
    private String id;

    public JPAEndpointDescriptor() {
        id = null;
    }

    //public JPAEndpointDescriptor(Endpoint source) {
    //    id = null;
    //    setInterfaceInformation(source.getInterfaceInformation());
    //    setProtocolInformation(source.getProtocolInformation());
    //}


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }

    public abstract static class AbstractBuilder<T extends JPAEndpointDescriptor, B extends AbstractBuilder<T, B>> extends DefaultEndpoint.AbstractBuilder<T, B> {

        @Override
        public B from(Endpoint other) {
            super.from(other);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<JPAEndpointDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected JPAEndpointDescriptor newBuildingInstance() {
            return new JPAEndpointDescriptor();
        }
    }
}
