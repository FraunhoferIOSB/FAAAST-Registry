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
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.util.ModelTransformationHelper;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.SpecificAssetIdBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;


/**
 * Registry Descriptor JPA implementation for SpecificAssetId.
 */
public class JpaSpecificAssetId extends DefaultSpecificAssetId {

    @JsonIgnore
    private String id;

    public JpaSpecificAssetId() {
        id = null;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (obj == null) {
            return false;
        }
        else if (this.getClass() != obj.getClass()) {
            return false;
        }
        else {
            JpaSpecificAssetId other = (JpaSpecificAssetId) obj;
            return super.equals(obj)
                    && Objects.equals(this.id, other.id);
        }
    }

    public abstract static class AbstractBuilder<T extends JpaSpecificAssetId, B extends AbstractBuilder<T, B>>
            extends SpecificAssetIdBuilder<JpaSpecificAssetId, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B from(SpecificAssetId other) {
            if (Objects.nonNull(other)) {
                semanticId(ModelTransformationHelper.convertReference(other.getSemanticId()));
                supplementalSemanticIds(ModelTransformationHelper.convertReferences(other.getSupplementalSemanticIds()));
                name(other.getName());
                value(other.getValue());
                externalSubjectId(ModelTransformationHelper.convertReference(other.getExternalSubjectId()));
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<JpaSpecificAssetId, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected JpaSpecificAssetId newBuildingInstance() {
            return new JpaSpecificAssetId();
        }
    }
}
