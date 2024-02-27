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
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ReferenceBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;


/**
 * Registry Descriptor JPA implementation for Reference.
 */
public class JpaReference extends DefaultReference {

    @JsonIgnore
    private String id;

    public JpaReference() {
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
            JpaReference other = (JpaReference) obj;
            return super.equals(obj)
                    && Objects.equals(this.id, other.id);
        }
    }

    public abstract static class AbstractBuilder<T extends JpaReference, B extends AbstractBuilder<T, B>>
            extends ReferenceBuilder<JpaReference, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B from(Reference other) {
            if (Objects.nonNull(other)) {
                keys(ModelTransformationHelper.convertKeys(other.getKeys()));
                type(other.getType());
                referredSemanticId(ModelTransformationHelper.convertReference(other.getReferredSemanticId()));
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<JpaReference, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected JpaReference newBuildingInstance() {
            return new JpaReference();
        }
    }
}
