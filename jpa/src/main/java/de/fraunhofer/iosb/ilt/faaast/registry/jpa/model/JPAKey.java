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
import io.adminshell.aas.v3.model.impl.DefaultKey;


/**
 * Registry Descriptor JPA implementation for Key.
 */
public class JPAKey extends DefaultKey {

    @JsonIgnore
    private String id;

    public JPAKey() {
        id = null;
    }


    public JPAKey(Key source) {
        id = null;
        setIdType(source.getIdType());
        setType(source.getType());
        setValue(source.getValue());
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }
}