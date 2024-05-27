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
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.LevelType;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.LevelTypeBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLevelType;


/**
 * Registry Descriptor JPA implementation for LevelType.
 */
public class JpaLevelType extends DefaultLevelType {

    @JsonIgnore
    private String id;

    public JpaLevelType() {
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
            JpaLevelType other = (JpaLevelType) obj;
            return super.equals(obj)
                    && Objects.equals(this.id, other.id);
        }
    }

    public abstract static class AbstractBuilder<T extends JpaLevelType, B extends AbstractBuilder<T, B>>
            extends LevelTypeBuilder<JpaLevelType, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B from(LevelType other) {
            if (Objects.nonNull(other)) {
                min(other.getMin());
                max(other.getMax());
                nom(other.getNom());
                typ(other.getTyp());
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<JpaLevelType, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected JpaLevelType newBuildingInstance() {
            return new JpaLevelType();
        }
    }
}
