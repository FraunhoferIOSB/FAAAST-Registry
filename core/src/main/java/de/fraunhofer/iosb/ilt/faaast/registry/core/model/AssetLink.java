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
package de.fraunhofer.iosb.ilt.faaast.registry.core.model;

import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Class representing an AssetLink.
 */
public class AssetLink {

    private String name;
    private String value;

    public AssetLink() {}


    public AssetLink(String name, String value) {
        this.name = name;
        this.value = value;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getValue() {
        return value;
    }


    public void setValue(String value) {
        this.value = value;
    }

    public abstract static class AbstractBuilder<T extends AssetLink, B extends AbstractBuilder<T, B>>
            extends ExtendableBuilder<T, B> {

        /**
         * This function allows setting a value for name.
         *
         * @param name desired value to be set
         * @return Builder object with new value for name
         */
        public B name(String name) {
            getBuildingInstance().setName(name);
            return getSelf();
        }


        /**
         * This function allows setting a value for value.
         *
         * @param value desired value to be set
         * @return Builder object with new value for value
         */
        public B value(String value) {
            getBuildingInstance().setValue(value);
            return getSelf();
        }


        public B from(SpecificAssetId specificAssetId) {
            name(specificAssetId.getName());
            value(specificAssetId.getValue());
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<AssetLink, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected AssetLink newBuildingInstance() {
            return new AssetLink();
        }
    }
}
