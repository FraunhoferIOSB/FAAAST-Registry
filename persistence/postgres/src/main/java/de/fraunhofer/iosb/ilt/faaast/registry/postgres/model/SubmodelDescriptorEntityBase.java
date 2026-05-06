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
package de.fraunhofer.iosb.ilt.faaast.registry.postgres.model;

import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


/**
 * Base Database entity for SubmodelDescriptor.
 */
public class SubmodelDescriptorEntityBase {

    private String idShort;

    @JdbcTypeCode(SqlTypes.JSON)
    private String administration;

    @JdbcTypeCode(SqlTypes.JSON)
    private String endpoints;

    @JdbcTypeCode(SqlTypes.JSON)
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    private String displayName;

    @JdbcTypeCode(SqlTypes.JSON)
    private String extensions;

    @JdbcTypeCode(SqlTypes.JSON)
    private String semanticId;

    @JdbcTypeCode(SqlTypes.JSON)
    private String supplementalSemanticId;

    public String getIdShort() {
        return idShort;
    }


    public void setIdShort(String idShort) {
        this.idShort = idShort;
    }


    @JdbcTypeCode(SqlTypes.JSON)
    public String getAdministration() {
        return administration;
    }


    @JdbcTypeCode(SqlTypes.JSON)
    public void setAdministration(String administration) {
        this.administration = administration;
    }


    @JdbcTypeCode(SqlTypes.JSON)
    public String getEndpoints() {
        return endpoints;
    }


    @JdbcTypeCode(SqlTypes.JSON)
    public void setEndpoints(String endpoints) {
        this.endpoints = endpoints;
    }


    @JdbcTypeCode(SqlTypes.JSON)
    public String getDescription() {
        return description;
    }


    @JdbcTypeCode(SqlTypes.JSON)
    public void setDescription(String description) {
        this.description = description;
    }


    @JdbcTypeCode(SqlTypes.JSON)
    public String getDisplayName() {
        return displayName;
    }


    @JdbcTypeCode(SqlTypes.JSON)
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    @JdbcTypeCode(SqlTypes.JSON)
    public String getExtensions() {
        return extensions;
    }


    @JdbcTypeCode(SqlTypes.JSON)
    public void setExtensions(String extensions) {
        this.extensions = extensions;
    }


    @JdbcTypeCode(SqlTypes.JSON)
    public String getSupplementalSemanticId() {
        return supplementalSemanticId;
    }


    @JdbcTypeCode(SqlTypes.JSON)
    public void setSemanticId(String semanticId) {
        this.semanticId = semanticId;
    }


    @JdbcTypeCode(SqlTypes.JSON)
    public String getSemanticId() {
        return semanticId;
    }


    @JdbcTypeCode(SqlTypes.JSON)
    public void setSupplementalSemanticId(String supplementalSemanticId) {
        this.supplementalSemanticId = supplementalSemanticId;
    }

    public abstract static class AbstractBuilder<T extends SubmodelDescriptorEntityBase, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B idShort(String value) {
            getBuildingInstance().setIdShort(value);
            return getSelf();
        }


        public B administration(String value) {
            getBuildingInstance().setAdministration(value);
            return getSelf();
        }


        public B endpoints(String value) {
            getBuildingInstance().setEndpoints(value);
            return getSelf();
        }


        public B description(String value) {
            getBuildingInstance().setDescription(value);
            return getSelf();
        }


        public B displayName(String value) {
            getBuildingInstance().setDisplayName(value);
            return getSelf();
        }


        public B extensions(String value) {
            getBuildingInstance().setExtensions(value);
            return getSelf();
        }


        public B semanticId(String value) {
            getBuildingInstance().setSemanticId(value);
            return getSelf();
        }


        public B supplementalSemanticId(String value) {
            getBuildingInstance().setSupplementalSemanticId(value);
            return getSelf();
        }
    }

    //public static class Builder extends AbstractBuilder<SubmodelDescriptorEntityBase, Builder> {

    //    @Override
    //    protected Builder getSelf() {
    //        return this;
    //    }

    //    @Override
    //    protected SubmodelDescriptorEntityBase newBuildingInstance() {
    //        return new SubmodelDescriptorEntityBase();
    //    }
    //}
}
