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
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringDefinitionTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.LangStringDefinitionTypeIec61360Builder;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringDefinitionTypeIec61360;


/**
 * Registry Descriptor JPA implementation for LangStringDefinitionTypeIec61360.
 */
public class JpaLangStringDefinitionTypeIec61360 extends DefaultLangStringDefinitionTypeIec61360 {

    @JsonIgnore
    private String id;

    public JpaLangStringDefinitionTypeIec61360() {
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
            JpaLangStringDefinitionTypeIec61360 other = (JpaLangStringDefinitionTypeIec61360) obj;
            return super.equals(obj)
                    && Objects.equals(this.id, other.id);
        }
    }

    public abstract static class AbstractBuilder<T extends JpaLangStringDefinitionTypeIec61360, B extends AbstractBuilder<T, B>>
            extends LangStringDefinitionTypeIec61360Builder<JpaLangStringDefinitionTypeIec61360, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B from(LangStringDefinitionTypeIec61360 other) {
            if (Objects.nonNull(other)) {
                text(other.getText());
                language(other.getLanguage());
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<JpaLangStringDefinitionTypeIec61360, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected JpaLangStringDefinitionTypeIec61360 newBuildingInstance() {
            return new JpaLangStringDefinitionTypeIec61360();
        }
    }
}
