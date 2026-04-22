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

import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


/**
 * Database entity for AssetAdministrationShellDescriptor.
 */
public class AssetAdministrationShellDescriptorEntity {

    private String id;

    private String idShort;
    private AssetKind assetKind;
    private String assetType;
    private String globalAssetId;

    //@JdbcTypeCode(SqlTypes.JSON)
    //@Column(name = "administration", columnDefinition = "jsonb")
    //private AdministrativeInformation administration;
    @JdbcTypeCode(SqlTypes.JSON)
    private String administration;

    @JdbcTypeCode(SqlTypes.JSON)
    private String endpoints;

    @JdbcTypeCode(SqlTypes.JSON)
    private String specificAssetIds;

    @JdbcTypeCode(SqlTypes.JSON)
    private String descriptions;

    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


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


    public AssetKind getAssetKind() {
        return assetKind;
    }


    public void setAssetKind(AssetKind assetKind) {
        this.assetKind = assetKind;
    }


    public String getAssetType() {
        return assetType;
    }


    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }


    public String getGlobalAssetId() {
        return globalAssetId;
    }


    public void setGlobalAssetId(String globalAssetId) {
        this.globalAssetId = globalAssetId;
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
    public String getSpecificAssetIds() {
        return specificAssetIds;
    }


    @JdbcTypeCode(SqlTypes.JSON)
    public void setSpecificAssetIds(String specificAssetIds) {
        this.specificAssetIds = specificAssetIds;
    }


    @JdbcTypeCode(SqlTypes.JSON)
    public String getDescriptions() {
        return descriptions;
    }


    @JdbcTypeCode(SqlTypes.JSON)
    public void setDescriptions(String descriptions) {
        this.descriptions = descriptions;
    }

    public abstract static class AbstractBuilder<T extends AssetAdministrationShellDescriptorEntity, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B idShort(String value) {
            getBuildingInstance().setIdShort(value);
            return getSelf();
        }


        public B administration(String value) {
            getBuildingInstance().setAdministration(value);
            return getSelf();
        }


        public B assetKind(AssetKind value) {
            getBuildingInstance().setAssetKind(value);
            return getSelf();
        }


        public B assetType(String value) {
            getBuildingInstance().setAssetType(value);
            return getSelf();
        }


        public B globalAssetId(String value) {
            getBuildingInstance().setGlobalAssetId(value);
            return getSelf();
        }


        public B endpoints(String value) {
            getBuildingInstance().setEndpoints(value);
            return getSelf();
        }


        public B specificAssetIds(String value) {
            getBuildingInstance().setSpecificAssetIds(value);
            return getSelf();
        }


        public B descriptions(String value) {
            getBuildingInstance().setDescriptions(value);
            return getSelf();
        }

        //public B from(AssetAdministrationShellDescriptor other) {
        //    if (Objects.nonNull(other)) {
        //        id(other.getId());
        //        idShort(other.getIdShort());
        //        //administration(other.getAdministration());
        //    }
        //    return getSelf();
        //}
    }

    public static class Builder extends AbstractBuilder<AssetAdministrationShellDescriptorEntity, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected AssetAdministrationShellDescriptorEntity newBuildingInstance() {
            return new AssetAdministrationShellDescriptorEntity();
        }
    }
}
