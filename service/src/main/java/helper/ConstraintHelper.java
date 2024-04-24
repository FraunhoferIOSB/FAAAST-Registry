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

import de.fraunhofer.iosb.ilt.faaast.registry.service.RegistryService;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.List;
import java.util.regex.Pattern;
import org.eclipse.digitaltwin.aas4j.v3.model.AdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.Extension;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;


/**
 * Helper class for constraint validation.
 */
public class ConstraintHelper {

    //private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintHelper.class);
    public static final Pattern TEXT_PATTERN = Pattern
            .compile("^([\\t\\n\\r -퟿-�]|\\ud800[\\udc00-\\udfff]|[\\ud801-\\udbfe][\\udc00-\\udfff]|\\udbff[\\udc00-\\udfff])*$");
    public static final Pattern LANG_LANGUAGE_PATTERN = Pattern.compile(
            "^(([a-zA-Z]{2,3}(-[a-zA-Z]{3}(-[a-zA-Z]{3}){2})?|[a-zA-Z]{4}|[a-zA-Z]{5,8})(-[a-zA-Z]{4})?(-([a-zA-Z]{2}|[0-9]{3}))?(-(([a-zA-Z0-9]){5,8}|[0-9]([a-zA-Z0-9]){3}))*(-[0-9A-WY-Za-wy-z](-([a-zA-Z0-9]){2,8})+)*(-[xX](-([a-zA-Z0-9]){1,8})+)?|[xX](-([a-zA-Z0-9]){1,8})+|((en-GB-oed|i-ami|i-bnn|i-default|i-enochian|i-hak|i-klingon|i-lux|i-mingo|i-navajo|i-pwn|i-tao|i-tay|i-tsu|sgn-BE-FR|sgn-BE-NL|sgn-CH-DE)|(art-lojban|cel-gaulish|no-bok|no-nyn|zh-guoyu|zh-hakka|zh-min|zh-min-nan|zh-xiang)))$");
    private static final Pattern VERSION_PATTERN = Pattern.compile("^(0|[1-9][0-9]*)$");
    public static final int MAX_IDENTIFIER_LENGTH = 2000;
    public static final int MAX_IDSHORT_LENGTH = 128;
    public static final int MAX_DESCRIPTION_TEXT_LENGTH = 1023;
    public static final int MAX_IEC61360_NAME_TEXT_LENGTH = 255;
    public static final int MAX_IEC61360_SHORT_NAME_TEXT_LENGTH = 18;
    private static final int MAX_VERSION_LENGTH = 4;
    public static final int MAX_STRING_2048_LENGTH = 2048;
    private static final int MAX_LABEL_LENGTH = 64;

    private ConstraintHelper() {}


    /**
     * Validate the given AAS Descriptor.
     *
     * @param aas The desired AAS Descriptor.
     */
    public static void validate(AssetAdministrationShellDescriptor aas) {
        Ensure.requireNonNull(aas, RegistryService.AAS_NOT_NULL_TXT);
        CommonConstraintHelper.checkId(aas.getId());
        CommonConstraintHelper.checkIdShort(aas.getIdShort());
        checkDescriptions(aas.getDescriptions());
        checkDisplayNames(aas.getDisplayNames());
        checkExtensions(aas.getExtensions());
        checkAdministrativeInformation(aas.getAdministration());
        CommonConstraintHelper.checkText(aas.getAssetType(), MAX_IDENTIFIER_LENGTH, false, "Asset Type");
        EndpointConstraintHelper.checkEndpoints(aas.getEndpoints());
        CommonConstraintHelper.checkText(aas.getGlobalAssetId(), MAX_IDENTIFIER_LENGTH, false, "Global Asset ID");
        checkSpecificAssetIds(aas.getSpecificAssetIds());
        checkSubmodels(aas.getSubmodels());
    }


    /**
     * Validate the given AAS Descriptor.
     *
     * @param submodel The desired Submodel Descriptor.
     */
    public static void validate(SubmodelDescriptor submodel) {
        checkSubmodel(submodel);
    }


    private static void checkSubmodels(List<SubmodelDescriptor> submodels) {
        if (submodels != null) {
            submodels.stream().forEach(ConstraintHelper::checkSubmodel);
        }
    }


    private static void checkSubmodel(SubmodelDescriptor submodel) {
        Ensure.requireNonNull(submodel, RegistryService.SUBMODEL_NOT_NULL_TXT);
        CommonConstraintHelper.checkId(submodel.getId());
        CommonConstraintHelper.checkIdShort(submodel.getIdShort());
        checkDescriptions(submodel.getDescriptions());
        checkDisplayNames(submodel.getDisplayNames());
        checkExtensions(submodel.getExtensions());
        checkAdministrativeInformation(submodel.getAdministration());
        EndpointConstraintHelper.checkEndpoints(submodel.getEndpoints());
        // Submodel must have at least one endpoint
        if ((submodel.getEndpoints() == null) || (submodel.getEndpoints().isEmpty())) {
            CommonConstraintHelper.raiseConstraintViolatedException("submodel doesn't have an endpoint");
        }
        CommonConstraintHelper.checkReference(submodel.getSemanticId());
        CommonConstraintHelper.checkReferences(submodel.getSupplementalSemanticIds());
    }


    private static void checkDescriptions(List<LangStringTextType> descriptions) {
        if (descriptions != null) {
            descriptions.stream().forEach(ConstraintHelper::checkDescription);
        }
    }


    private static void checkDescription(LangStringTextType description) {
        if (description != null) {
            CommonConstraintHelper.checkLanguage(description.getLanguage(), "description language");
            CommonConstraintHelper.checkText(description.getText(), MAX_DESCRIPTION_TEXT_LENGTH, true, "description text");
        }
    }


    private static void checkDisplayNames(List<LangStringNameType> names) {
        if (names != null) {
            names.stream().forEach(ConstraintHelper::checkDisplayName);
        }
    }


    private static void checkDisplayName(LangStringNameType name) {
        if (name != null) {
            CommonConstraintHelper.checkLanguage(name.getLanguage(), "display name language");
            CommonConstraintHelper.checkText(name.getText(), MAX_IDSHORT_LENGTH, true, "display name text");
        }
    }


    private static void checkExtensions(List<Extension> extensions) {
        if (extensions != null) {
            extensions.stream().forEach(ConstraintHelper::checkExtension);
        }
    }


    private static void checkExtension(Extension extension) {
        if (extension != null) {
            CommonConstraintHelper.checkText(extension.getName(), MAX_IDSHORT_LENGTH, true, "extension name");
            CommonConstraintHelper.checkReference(extension.getSemanticId());
            CommonConstraintHelper.checkReferences(extension.getSupplementalSemanticIds());
            CommonConstraintHelper.checkReferences(extension.getRefersTo());
        }
    }


    private static void checkAdministrativeInformation(AdministrativeInformation adminInfo) {
        if (adminInfo != null) {
            DataSpecificationConstraintHelper.checkEmbeddedDataSpecifications(adminInfo.getEmbeddedDataSpecifications());
            checkVersion(adminInfo.getVersion(), "Version");
            checkVersion(adminInfo.getRevision(), "Revision");
            CommonConstraintHelper.checkReference(adminInfo.getCreator());
            CommonConstraintHelper.checkText(adminInfo.getTemplateId(), MAX_IDENTIFIER_LENGTH, false, "templateId");
        }
    }


    private static void checkVersion(String version, String msg) {
        if (version != null) {
            if (version.isEmpty()) {
                CommonConstraintHelper.raiseConstraintViolatedException(String.format("%s is empty", msg));
            }
            else if (version.length() > MAX_VERSION_LENGTH) {
                CommonConstraintHelper.raiseConstraintViolatedException(String.format("%s too long", msg));
            }
            else if (!VERSION_PATTERN.matcher(version).matches()) {
                CommonConstraintHelper.raiseConstraintViolatedException(String.format("%s doesn't match the pattern", msg));
            }
        }
    }


    private static void checkSpecificAssetIds(List<SpecificAssetId> specificAssetIds) {
        if (specificAssetIds != null) {
            specificAssetIds.stream().forEach(ConstraintHelper::checkSpecificAssetId);
        }
    }


    private static void checkSpecificAssetId(SpecificAssetId specificAssetId) {
        if (specificAssetId != null) {
            CommonConstraintHelper.checkReference(specificAssetId.getSemanticId());
            CommonConstraintHelper.checkReferences(specificAssetId.getSupplementalSemanticIds());
            CommonConstraintHelper.checkText(specificAssetId.getName(), MAX_LABEL_LENGTH, true, "Specific Asset ID Name");
            CommonConstraintHelper.checkText(specificAssetId.getValue(), MAX_IDENTIFIER_LENGTH, true, "Specific Asset ID value");
            CommonConstraintHelper.checkReference(specificAssetId.getExternalSubjectId());
        }
    }

}
