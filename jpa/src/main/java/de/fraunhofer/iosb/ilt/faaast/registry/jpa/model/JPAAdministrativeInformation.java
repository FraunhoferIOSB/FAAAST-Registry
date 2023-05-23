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
import io.adminshell.aas.v3.model.AdministrativeInformation;
import io.adminshell.aas.v3.model.builder.AdministrativeInformationBuilder;
import io.adminshell.aas.v3.model.impl.DefaultAdministrativeInformation;
import java.util.Objects;


/**
 * Registry Descriptor JPA implementation for AdministrativeInformation.
 */
public class JPAAdministrativeInformation extends DefaultAdministrativeInformation {

    @JsonIgnore
    private String adminId;

    public JPAAdministrativeInformation() {
        adminId = null;
    }


    public String getAdminId() {
        return adminId;
    }


    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public abstract static class AbstractBuilder<T extends JPAAdministrativeInformation, B extends AbstractBuilder<T, B>>
            extends AdministrativeInformationBuilder<JPAAdministrativeInformation, B> {

        public B from(AdministrativeInformation other) {
            if (Objects.nonNull(other)) {
                version(other.getVersion());
                revision(other.getRevision());
                embeddedDataSpecifications(other.getEmbeddedDataSpecifications());
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<JPAAdministrativeInformation, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected JPAAdministrativeInformation newBuildingInstance() {
            return new JPAAdministrativeInformation();
        }
    }
}
