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
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Registry Descriptor JPA implementation for Endpoint.
 */
public class JpaEndpointProtocolVersion {

    @JsonIgnore
    private String id;

    private String value;

    public JpaEndpointProtocolVersion() {
        id = null;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getValue() {
        return value;
    }


    public void setValue(String value) {
        this.value = value;
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, value);
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
            JpaEndpointProtocolVersion other = (JpaEndpointProtocolVersion) obj;
            return Objects.equals(this.id, other.id)
                    && Objects.equals(this.value, other.value);
        }
    }

    public abstract static class AbstractBuilder<T extends JpaEndpointProtocolVersion, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B value(String value) {
            getBuildingInstance().setValue(value);
            return getSelf();
        }


        public B from(JpaEndpointProtocolVersion other) {
            if (other != null) {
                id(other.getId());
                value(other.getValue());
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<JpaEndpointProtocolVersion, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected JpaEndpointProtocolVersion newBuildingInstance() {
            return new JpaEndpointProtocolVersion();
        }
    }

}
