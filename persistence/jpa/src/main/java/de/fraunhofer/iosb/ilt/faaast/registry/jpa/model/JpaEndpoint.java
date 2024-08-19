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
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEndpoint;


/**
 * Registry Descriptor JPA implementation for Endpoint.
 */
public class JpaEndpoint extends DefaultEndpoint {

    @JsonIgnore
    private String id;

    public JpaEndpoint() {
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
            JpaEndpoint other = (JpaEndpoint) obj;
            return super.equals(obj)
                    && Objects.equals(this.id, other.id);
        }
    }

    public abstract static class AbstractBuilder<T extends JpaEndpoint, B extends AbstractBuilder<T, B>> extends DefaultEndpoint.Builder {
        public DefaultEndpoint.Builder from(Endpoint other) {
            if (other != null) {
                _interface(other.get_interface());
                protocolInformation(other.getProtocolInformation());
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<JpaEndpoint, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected JpaEndpoint newBuildingInstance() {
            return new JpaEndpoint();
        }
    }
}
