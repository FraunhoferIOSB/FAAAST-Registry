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
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.impl.DefaultReference;


/**
 * Registry Descriptor JPA implementation for Reference.
 */
public class JPAReferenceDescriptor extends DefaultReference {

    @JsonIgnore
    private String id;

    public JPAReferenceDescriptor() {
        id = null;
    }


    public JPAReferenceDescriptor(Reference source) {
        id = null;
        setKeys(JPAHelper.createJPAKeys(source.getKeys()));
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }
}
