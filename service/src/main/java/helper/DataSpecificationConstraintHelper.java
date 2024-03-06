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
package helper;

import static helper.ConstraintHelper.MAX_IDENTIFIER_LENGTH;

import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.DataSpecificationContent;
import org.eclipse.digitaltwin.aas4j.v3.model.DataSpecificationIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.EmbeddedDataSpecification;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringDefinitionTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringPreferredNameTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringShortNameTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.ValueList;
import org.eclipse.digitaltwin.aas4j.v3.model.ValueReferencePair;


/**
 * Helper class for validating DataSpecification constraints.
 */
public class DataSpecificationConstraintHelper {

    private DataSpecificationConstraintHelper() {}


    /**
     * Checks the constraints of the given Embedded Data Specification.
     *
     * @param specs The desired Embedded Data Specification.
     */
    public static void checkEmbeddedDataSpecifications(List<EmbeddedDataSpecification> specs) {
        if (specs != null) {
            specs.stream().forEach(DataSpecificationConstraintHelper::checkEmbeddedDataSpecification);
        }
    }


    private static void checkEmbeddedDataSpecification(EmbeddedDataSpecification data) {
        if (data != null) {
            if (data.getDataSpecification() == null) {
                CommonConstraintHelper.raiseConstraintViolatedException("no DataSpecification provided");
            }
            CommonConstraintHelper.checkReference(data.getDataSpecification());
            checkDataSpecificationContent(data.getDataSpecificationContent());
        }
    }


    private static void checkDataSpecificationContent(DataSpecificationContent content) {
        if (content == null) {
            CommonConstraintHelper.raiseConstraintViolatedException("no DataSpecificationContent provided");
        }
        else if (content instanceof DataSpecificationIec61360 dataSpecificationIec61360) {
            checkDataSpecificationIec61360(dataSpecificationIec61360);
        }
    }


    private static void checkDataSpecificationIec61360(DataSpecificationIec61360 dataSpec) {
        checkIec61360Names(dataSpec.getPreferredName());
        checkIec61360ShortNames(dataSpec.getShortName());
        CommonConstraintHelper.checkText(dataSpec.getUnit(), 0, false, "IEC 61360: unit");
        CommonConstraintHelper.checkReference(dataSpec.getUnitId());
        CommonConstraintHelper.checkText(dataSpec.getSourceOfDefinition(), 0, false, "IEC 61360: source of definition");
        CommonConstraintHelper.checkText(dataSpec.getSymbol(), 0, false, "IEC 61360: symbol");
        checkIec61360Definitions(dataSpec.getDefinition());
        CommonConstraintHelper.checkText(dataSpec.getValueFormat(), 0, false, "IEC 61360: value format");
        checkValueList(dataSpec.getValueList());
        CommonConstraintHelper.checkText(dataSpec.getValue(), ConstraintHelper.MAX_IDENTIFIER_LENGTH, false, "IEC 61360: value");
    }


    private static void checkIec61360Names(List<LangStringPreferredNameTypeIec61360> names) {
        if ((names == null) || names.isEmpty()) {
            CommonConstraintHelper.raiseConstraintViolatedException("no IEC 61360 Preferred Name provided");
        }
        else {
            names.stream().forEach(DataSpecificationConstraintHelper::checkIec61360Name);
        }
    }


    private static void checkIec61360Name(LangStringPreferredNameTypeIec61360 name) {
        if (name != null) {
            CommonConstraintHelper.checkLanguage(name.getLanguage(), "IEC 61360 PreferredName language");
            CommonConstraintHelper.checkText(name.getText(), ConstraintHelper.MAX_IEC61360_NAME_TEXT_LENGTH, true, "IEC 61360 PreferredName text");
        }
        else {
            CommonConstraintHelper.raiseConstraintViolatedException("IEC 61360 PreferredName not provided");
        }
    }


    private static void checkIec61360ShortNames(List<LangStringShortNameTypeIec61360> names) {
        if ((names == null) || names.isEmpty()) {
            CommonConstraintHelper.raiseConstraintViolatedException("no IEC 61360 Short Name provided");
        }
        else {
            names.stream().forEach(DataSpecificationConstraintHelper::checkIec61360ShortName);
        }
    }


    private static void checkIec61360ShortName(LangStringShortNameTypeIec61360 name) {
        if (name != null) {
            CommonConstraintHelper.checkLanguage(name.getLanguage(), "IEC 61360 ShortName language");
            CommonConstraintHelper.checkText(name.getText(), ConstraintHelper.MAX_IEC61360_SHORT_NAME_TEXT_LENGTH, true, "IEC 61360 ShortName");
        }
    }


    private static void checkIec61360Definitions(List<LangStringDefinitionTypeIec61360> definitions) {
        if (definitions != null) {
            definitions.stream().forEach(DataSpecificationConstraintHelper::checkIec61360Definition);
        }
    }


    private static void checkIec61360Definition(LangStringDefinitionTypeIec61360 definition) {
        if (definition != null) {
            CommonConstraintHelper.checkLanguage(definition.getLanguage(), "IEC 61360 definition language");
            CommonConstraintHelper.checkText(definition.getText(), ConstraintHelper.MAX_DESCRIPTION_TEXT_LENGTH, true, "IEC 61360 definition text");
        }
    }


    private static void checkValueList(ValueList values) {
        if (values != null) {
            checkValueReferencePairs(values.getValueReferencePairs());
        }
    }


    private static void checkValueReferencePairs(List<ValueReferencePair> pairs) {
        if (pairs != null) {
            pairs.stream().forEach(DataSpecificationConstraintHelper::checkValueReferencePair);
        }
    }


    private static void checkValueReferencePair(ValueReferencePair pair) {
        if (pair != null) {
            CommonConstraintHelper.checkText(pair.getValue(), MAX_IDENTIFIER_LENGTH, true, "ValueReferencePair value");
            CommonConstraintHelper.checkReference(pair.getValueId());
        }
    }

}
