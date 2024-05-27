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
import org.eclipse.digitaltwin.aas4j.v3.model.AdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.AdministrativeInformationBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAdministrativeInformation;


/**
 * Registry Descriptor JPA implementation for AdministrativeInformation.
 */
public class JpaAdministrativeInformation extends DefaultAdministrativeInformation {

    @JsonIgnore
    private String adminId;

    public JpaAdministrativeInformation() {
        adminId = null;
    }


    public String getAdminId() {
        return adminId;
    }


    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), adminId);
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
            JpaAdministrativeInformation other = (JpaAdministrativeInformation) obj;
            return super.equals(obj)
                    && Objects.equals(this.adminId, other.adminId);
        }
    }

    public abstract static class AbstractBuilder<T extends JpaAdministrativeInformation, B extends AbstractBuilder<T, B>>
            extends AdministrativeInformationBuilder<JpaAdministrativeInformation, B> {

        public B from(AdministrativeInformation other) {
            if (Objects.nonNull(other)) {
                version(other.getVersion());
                revision(other.getRevision());
                creator(ModelTransformationHelper.convertReference(other.getCreator()));
                templateId(other.getTemplateId());
                embeddedDataSpecifications(ModelTransformationHelper.convertEmbeddedDataSpecifications(other.getEmbeddedDataSpecifications()));
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<JpaAdministrativeInformation, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected JpaAdministrativeInformation newBuildingInstance() {
            return new JpaAdministrativeInformation();
        }
    }
}
