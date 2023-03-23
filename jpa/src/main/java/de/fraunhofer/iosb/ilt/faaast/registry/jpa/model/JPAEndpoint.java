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
public class JPAEndpoint extends DefaultEndpoint {

    @JsonIgnore
    private String id;

    public JPAEndpoint() {
        id = null;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }

    public abstract static class AbstractBuilder<T extends JPAEndpoint, B extends AbstractBuilder<T, B>> extends DefaultEndpoint.AbstractBuilder<T, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        @Override
        public B from(Endpoint other) {
            super.from(other);
            if (other != null) {
                protocolInformation(new JPAProtocolInformation.Builder().from(other.getProtocolInformation()).build());
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<JPAEndpoint, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected JPAEndpoint newBuildingInstance() {
            return new JPAEndpoint();
        }
    }
}
