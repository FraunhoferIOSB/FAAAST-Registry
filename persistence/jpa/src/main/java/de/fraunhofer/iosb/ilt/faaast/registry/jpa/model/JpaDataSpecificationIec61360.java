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
import org.eclipse.digitaltwin.aas4j.v3.model.DataSpecificationIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.DataSpecificationIec61360Builder;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultDataSpecificationIec61360;


/**
 * Registry Descriptor JPA implementation for DataSpecificationIec61360.
 */
public class JpaDataSpecificationIec61360 extends DefaultDataSpecificationIec61360 {

    @JsonIgnore
    private String id;

    public JpaDataSpecificationIec61360() {
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
            JpaDataSpecificationIec61360 other = (JpaDataSpecificationIec61360) obj;
            return super.equals(obj)
                    && Objects.equals(this.id, other.id);
        }
    }

    public abstract static class AbstractBuilder<T extends JpaDataSpecificationIec61360, B extends AbstractBuilder<T, B>>
            extends DataSpecificationIec61360Builder<JpaDataSpecificationIec61360, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B from(DataSpecificationIec61360 other) {
            if (Objects.nonNull(other)) {
                preferredName(ModelTransformationHelper.convertPreferredNameIec61360(other.getPreferredName()));
                shortName(ModelTransformationHelper.convertShortNameIec61360(other.getShortName()));
                unit(other.getUnit());
                unitId(ModelTransformationHelper.convertReference(other.getUnitId()));
                sourceOfDefinition(other.getSourceOfDefinition());
                symbol(other.getSymbol());
                dataType(other.getDataType());
                definition(ModelTransformationHelper.convertDefinitionIec61360(other.getDefinition()));
                valueFormat(other.getValueFormat());
                valueList(ModelTransformationHelper.convertValueList(other.getValueList()));
                value(other.getValue());
                levelType(ModelTransformationHelper.convertLevelType(other.getLevelType()));
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<JpaDataSpecificationIec61360, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected JpaDataSpecificationIec61360 newBuildingInstance() {
            return new JpaDataSpecificationIec61360();
        }
    }
}
