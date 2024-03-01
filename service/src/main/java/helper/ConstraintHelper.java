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

import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.BadRequestException;
import de.fraunhofer.iosb.ilt.faaast.registry.service.RegistryService;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.List;
import java.util.regex.Pattern;
import org.eclipse.digitaltwin.aas4j.v3.model.AdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.DataSpecificationContent;
import org.eclipse.digitaltwin.aas4j.v3.model.DataSpecificationIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.EmbeddedDataSpecification;
import org.eclipse.digitaltwin.aas4j.v3.model.Extension;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringDefinitionTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringPreferredNameTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringShortNameTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ValueList;
import org.eclipse.digitaltwin.aas4j.v3.model.ValueReferencePair;


/**
 * Helper class for constraint validation.
 */
public class ConstraintHelper {

    //private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintHelper.class);
    private static final Pattern LANG_LANGUAGE_PATTERN = Pattern.compile(
            "^(([a-zA-Z]{2,3}(-[a-zA-Z]{3}(-[a-zA-Z]{3}){2})?|[a-zA-Z]{4}|[a-zA-Z]{5,8})(-[a-zA-Z]{4})?(-([a-zA-Z]{2}|[0-9]{3}))?(-(([a-zA-Z0-9]){5,8}|[0-9]([a-zA-Z0-9]){3}))*(-[0-9A-WY-Za-wy-z](-([a-zA-Z0-9]){2,8})+)*(-[xX](-([a-zA-Z0-9]){1,8})+)?|[xX](-([a-zA-Z0-9]){1,8})+|((en-GB-oed|i-ami|i-bnn|i-default|i-enochian|i-hak|i-klingon|i-lux|i-mingo|i-navajo|i-pwn|i-tao|i-tay|i-tsu|sgn-BE-FR|sgn-BE-NL|sgn-CH-DE)|(art-lojban|cel-gaulish|no-bok|no-nyn|zh-guoyu|zh-hakka|zh-min|zh-min-nan|zh-xiang)))$");
    private static final Pattern TEXT_PATTERN = Pattern
            .compile("^([\\t\\n\\r -퟿-�]|\\ud800[\\udc00-\\udfff]|[\\ud801-\\udbfe][\\udc00-\\udfff]|\\udbff[\\udc00-\\udfff])*$");
    private static final Pattern VERSION_PATTERN = Pattern.compile("^(0|[1-9][0-9]*)$");
    private static final int MAX_ID_LENGTH = 2000;
    private static final int MAX_IDSHORT_LENGTH = 128;
    private static final int MAX_DESCRIPTION_TEXT_LENGTH = 1023;
    private static final int MAX_NAME_TEXT_LENGTH = 128;
    private static final int MAX_IEC61360_NAME_TEXT_LENGTH = 255;
    private static final int MAX_IEC61360_SHORT_NAME_TEXT_LENGTH = 18;
    private static final int MAX_VALUE_LENGTH = 2000;
    private static final int MAX_VERSION_LENGTH = 4;

    private ConstraintHelper() {}


    /**
     * Validate the given AAS Descriptor.
     *
     * @param aas The desired AAS Descriptor.
     */
    public static void validate(AssetAdministrationShellDescriptor aas) {
        Ensure.requireNonNull(aas, RegistryService.AAS_NOT_NULL_TXT);
        checkId(aas.getId());
        checkIdShort(aas.getIdShort());
        checkDescriptions(aas.getDescriptions());
        checkDisplayNames(aas.getDisplayNames());
        checkExtensions(aas.getExtensions());
        checkAdministrativeInformation(aas.getAdministration());
    }


    private static void checkId(String id) {
        if ((id == null) || (id.length() == 0)) {
            raiseConstraintViolatedException("no Id provided");
        }
        else if (id.length() > MAX_ID_LENGTH) {
            raiseConstraintViolatedException("ID too long.");
        }
    }


    private static void checkIdShort(String idShort) {
        if ((idShort != null) && (idShort.length() > MAX_IDSHORT_LENGTH)) {
            raiseConstraintViolatedException("IdShort too long.");
        }
    }


    private static void checkDescriptions(List<LangStringTextType> descriptions) {
        if (descriptions != null) {
            for (var d: descriptions) {
                checkDescription(d);
            }
        }
    }


    private static void checkDescription(LangStringTextType description) {
        if (description != null) {
            checkLanguage(description.getLanguage(), "description language");
            checkText(description.getText(), MAX_DESCRIPTION_TEXT_LENGTH, true, "description text");
        }
    }


    private static void checkDisplayNames(List<LangStringNameType> names) {
        if (names != null) {
            for (var n: names) {
                checkDisplayName(n);
            }
        }
    }


    private static void checkDisplayName(LangStringNameType name) {
        if (name != null) {
            checkLanguage(name.getLanguage(), "display name language");
            checkText(name.getText(), MAX_NAME_TEXT_LENGTH, true, "isplay name text");
        }
    }


    private static void checkExtensions(List<Extension> extensions) {
        if (extensions != null) {
            for (var e: extensions) {
                checkExtension(e);
            }
        }
    }


    private static void checkExtension(Extension extension) {
        if (extension != null) {
            checkText(extension.getName(), MAX_NAME_TEXT_LENGTH, true, "extension name");
            checkReference(extension.getSemanticId());
            checkReferences(extension.getSupplementalSemanticIds());
            checkReferences(extension.getRefersTo());
        }
    }


    private static void checkReferenceParent(Reference reference) {
        if (reference != null) {
            if (reference.getType() == null) {
                raiseConstraintViolatedException("no reference type provided");
            }
            if ((reference.getKeys() == null) || reference.getKeys().isEmpty()) {
                raiseConstraintViolatedException("no keys provided");
            }

            for (var k: reference.getKeys()) {
                checkKey(k);
            }
        }
    }


    private static void checkReferences(List<Reference> references) {
        if (references != null) {
            for (var r: references) {
                checkReference(r);
            }
        }
    }


    private static void checkReference(Reference reference) {
        if (reference != null) {
            checkReferenceParent(reference);
            checkReferenceParent(reference.getReferredSemanticId());
        }
    }


    private static void checkKey(Key key) {
        if (key.getType() == null) {
            raiseConstraintViolatedException("no key type provided");
        }
        checkText(key.getValue(), MAX_VALUE_LENGTH, true, "key value");
    }


    private static void checkAdministrativeInformation(AdministrativeInformation adminInfo) {
        if (adminInfo != null) {
            checkEmbeddedDataSpecifications(adminInfo.getEmbeddedDataSpecifications());
            checkVersion(adminInfo.getVersion(), "Version");
            checkVersion(adminInfo.getRevision(), "Revision");
            checkReference(adminInfo.getCreator());
            checkText(adminInfo.getTemplateId(), MAX_VALUE_LENGTH, false, "templateId");
        }
    }


    private static void checkEmbeddedDataSpecifications(List<EmbeddedDataSpecification> specs) {
        if (specs != null) {
            for (var s: specs) {
                checkEmbeddedDataSpecification(s);
            }
        }
    }


    private static void checkEmbeddedDataSpecification(EmbeddedDataSpecification data) {
        if (data != null) {
            if (data.getDataSpecification() == null) {
                raiseConstraintViolatedException("no DataSpecification provided");
            }
            checkReference(data.getDataSpecification());
            checkDataSpecificationContent(data.getDataSpecificationContent());
        }
    }


    private static void checkDataSpecificationContent(DataSpecificationContent content) {
        if (content == null) {
            raiseConstraintViolatedException("no DataSpecificationContent provided");
        }
        else if (content instanceof DataSpecificationIec61360 dataSpecificationIec61360) {
            checkDataSpecificationIec61360(dataSpecificationIec61360);
        }
    }


    private static void checkDataSpecificationIec61360(DataSpecificationIec61360 dataSpec) {
        // modelType missing in model
        checkIec61360Names(dataSpec.getPreferredName());
        checkIec61360ShortNames(dataSpec.getShortName());
        checkText(dataSpec.getUnit(), 0, false, "IEC 61360: unit");
        checkReference(dataSpec.getUnitId());
        checkText(dataSpec.getSourceOfDefinition(), 0, false, "IEC 61360: source of definition");
        checkText(dataSpec.getSymbol(), 0, false, "IEC 61360: symbol");
        checkIec61360Definitions(dataSpec.getDefinition());
        checkText(dataSpec.getValueFormat(), 0, false, "IEC 61360: value format");
        checkValueList(dataSpec.getValueList());
        checkText(dataSpec.getValue(), MAX_VALUE_LENGTH, false, "IEC 61360: value");
    }


    private static void checkIec61360Names(List<LangStringPreferredNameTypeIec61360> names) {
        if ((names == null) || names.isEmpty()) {
            raiseConstraintViolatedException("no IEC 61360 Preferred Name provided");
        }
        else {
            for (var n: names) {
                checkIec61360Name(n);
            }
        }
    }


    private static void checkIec61360Name(LangStringPreferredNameTypeIec61360 name) {
        if (name != null) {
            checkLanguage(name.getLanguage(), "IEC 61360 PreferredName language");
            checkText(name.getText(), MAX_IEC61360_NAME_TEXT_LENGTH, true, "IEC 61360 PreferredName text");
        }
        else {
            raiseConstraintViolatedException("IEC 61360 PreferredName not provided");
        }
    }


    private static void checkIec61360ShortNames(List<LangStringShortNameTypeIec61360> names) {
        if ((names == null) || names.isEmpty()) {
            raiseConstraintViolatedException("no IEC 61360 Short Name provided");
        }
        else {
            for (var n: names) {
                checkIec61360ShortName(n);
            }
        }
    }


    private static void checkIec61360ShortName(LangStringShortNameTypeIec61360 name) {
        if (name != null) {
            checkLanguage(name.getLanguage(), "IEC 61360 ShortName language");
            checkText(name.getText(), MAX_IEC61360_SHORT_NAME_TEXT_LENGTH, true, "IEC 61360 ShortName");
        }
    }


    private static void checkIec61360Definitions(List<LangStringDefinitionTypeIec61360> definitions) {
        if (definitions != null) {
            for (var d: definitions) {
                checkIec61360Definition(d);
            }
        }
    }


    private static void checkIec61360Definition(LangStringDefinitionTypeIec61360 definition) {
        if (definition != null) {
            checkLanguage(definition.getLanguage(), "IEC 61360 definition language");
            checkText(definition.getText(), MAX_DESCRIPTION_TEXT_LENGTH, true, "IEC 61360 definition text");
        }
    }


    private static void checkText(String txt, int maxLength, boolean notNull, String msg) {
        if (notNull && (txt == null)) {
            raiseConstraintViolatedException(String.format("%s is null", msg));
        }
        if (txt != null) {
            if (txt.isEmpty()) {
                raiseConstraintViolatedException(String.format("%s is empty", msg));
            }
            else if ((maxLength > 0) && (txt.length() > maxLength)) {
                raiseConstraintViolatedException(String.format("%s too long", msg));
            }
            else if (!TEXT_PATTERN.matcher(txt).matches()) {
                raiseConstraintViolatedException(String.format("%s doesn't match the pattern", msg));
            }
        }
    }


    private static void checkLanguage(String language, String msg) {
        if (language == null) {
            raiseConstraintViolatedException(String.format("no %s provided", msg));
        }
        else if (!LANG_LANGUAGE_PATTERN.matcher(language).matches()) {
            raiseConstraintViolatedException(String.format("%s doesn't match the pattern", msg));
        }
    }


    private static void checkValueList(ValueList values) {
        if (values != null) {
            checkValueReferencePairs(values.getValueReferencePairs());
        }
    }


    private static void checkValueReferencePairs(List<ValueReferencePair> pairs) {
        if (pairs != null) {
            for (var p: pairs) {
                checkValueReferencePair(p);
            }
        }
    }


    private static void checkValueReferencePair(ValueReferencePair pair) {
        if (pair != null) {
            checkText(pair.getValue(), MAX_VALUE_LENGTH, true, "ValueReferencePair value");
            checkReference(pair.getValueId());
        }
    }


    private static void checkVersion(String version, String msg) {
        if (version != null) {
            if (version.isEmpty()) {
                raiseConstraintViolatedException(String.format("%s is empty", msg));
            }
            else if (version.length() > MAX_VERSION_LENGTH) {
                raiseConstraintViolatedException(String.format("%s too long", msg));
            }
            else if (!VERSION_PATTERN.matcher(version).matches()) {
                raiseConstraintViolatedException(String.format("%s doesn't match the pattern", msg));
            }
        }
    }


    private static void raiseConstraintViolatedException(String txt) {
        throw new BadRequestException(txt);
    }
}
