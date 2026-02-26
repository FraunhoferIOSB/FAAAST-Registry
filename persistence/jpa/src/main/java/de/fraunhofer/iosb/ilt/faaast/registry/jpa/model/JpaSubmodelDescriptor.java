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
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;


/**
 * Registry Descriptor JPA implementation for Submodel of an AAS.
 */
public class JpaSubmodelDescriptor extends JpaSubmodelDescriptorBase {

    @JsonIgnore
    private String aasId;

    public JpaSubmodelDescriptor() {
        aasId = null;
    }


    public String getAasId() {
        return aasId;
    }


    public void setAasId(String value) {
        aasId = value;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), aasId);
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
            JpaSubmodelDescriptor other = (JpaSubmodelDescriptor) obj;
            return super.equals(obj)
                    && Objects.equals(this.aasId, other.aasId);
        }
    }

    public abstract static class AbstractBuilder<T extends JpaSubmodelDescriptor, B extends AbstractBuilder<T, B>>
            extends JpaSubmodelDescriptorBase.AbstractBuilder<T, B> {

        public B aasId(String value) {
            getBuildingInstance().setAasId(value);
            return getSelf();
        }


        public B fromAas(SubmodelDescriptor other, String aasId) {
            if (Objects.nonNull(other)) {
                from(other);
                aasId(aasId);
            }

            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<JpaSubmodelDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected JpaSubmodelDescriptor newBuildingInstance() {
            return new JpaSubmodelDescriptor();
        }
    }
}
