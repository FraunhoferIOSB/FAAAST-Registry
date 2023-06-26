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
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.ProtocolInformation;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultProtocolInformation;


/**
 * Registry Descriptor JPA implementation for ProtocolInformation.
 */
public class JPAProtocolInformation extends DefaultProtocolInformation {

    @JsonIgnore
    private String id;

    public JPAProtocolInformation() {
        id = null;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }

    public abstract static class AbstractBuilder<T extends JPAProtocolInformation, B extends AbstractBuilder<T, B>>
            extends DefaultProtocolInformation.AbstractBuilder<JPAProtocolInformation, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        @Override
        public B from(ProtocolInformation other) {
            super.from(other);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<JPAProtocolInformation, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected JPAProtocolInformation newBuildingInstance() {
            return new JPAProtocolInformation();
        }
    }
}
