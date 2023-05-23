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
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.builder.KeyBuilder;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import java.util.Objects;


/**
 * Registry Descriptor JPA implementation for Key.
 */
public class JPAKey extends DefaultKey {

    @JsonIgnore
    private String id;

    public JPAKey() {
        id = null;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }

    public abstract static class AbstractBuilder<T extends JPAKey, B extends AbstractBuilder<T, B>>
            extends KeyBuilder<JPAKey, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B from(Key other) {
            if (Objects.nonNull(other)) {
                idType(other.getIdType());
                type(other.getType());
                value(other.getValue());
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<JPAKey, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected JPAKey newBuildingInstance() {
            return new JPAKey();
        }
    }
}
