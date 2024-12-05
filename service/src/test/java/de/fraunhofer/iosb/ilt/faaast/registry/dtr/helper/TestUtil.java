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
package de.fraunhofer.iosb.ilt.faaast.registry.dtr.helper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.ProtocolInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SecurityTypeEnum;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEndpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProtocolInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSecurityAttributeObject;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelDescriptor;


public class TestUtil {

    public static AssetAdministrationShellDescriptor createCompleteAasDescriptor() {
        return createCompleteAasDescriptor("semanticIdExample", "http://endpoint-address");
    }


    public static AssetAdministrationShellDescriptor createCompleteAasDescriptor(String semanticId, String endpointUrl) {
        DefaultAssetAdministrationShellDescriptor assetAdministrationShellDescriptor = new DefaultAssetAdministrationShellDescriptor();
        DefaultLangStringNameType displayName = new DefaultLangStringNameType();
        displayName.setLanguage("de");
        displayName.setText("this is an example description1");
        assetAdministrationShellDescriptor.setDisplayName(List.of(displayName));
        assetAdministrationShellDescriptor.setGlobalAssetId("globalAssetId example");
        assetAdministrationShellDescriptor.setAssetType("AssetType");
        assetAdministrationShellDescriptor.setAssetKind(AssetKind.INSTANCE);
        assetAdministrationShellDescriptor.setId("fb7ebcc2-5731-4948-aeaa-c9e9692decf5");
        assetAdministrationShellDescriptor.setIdShort(RandomStringUtils.random(10, true, true));

        DefaultReference DefaultSpecificAssetIdReference = new DefaultReference();
        DefaultSpecificAssetIdReference.setType(ReferenceTypes.MODEL_REFERENCE);
        DefaultKey defaultSpecificAssetIdKey = new DefaultKey();
        defaultSpecificAssetIdKey.setType(KeyTypes.ASSET_ADMINISTRATION_SHELL);
        defaultSpecificAssetIdKey.setValue("DefaultSpecificAssetIdReference key");
        DefaultSpecificAssetIdReference.setKeys(List.of(defaultSpecificAssetIdKey));

        DefaultReference externalSubjectIdReference = new DefaultReference();
        externalSubjectIdReference.setType(ReferenceTypes.EXTERNAL_REFERENCE);
        Key subjectIdKey = new DefaultKey();
        subjectIdKey.setType(KeyTypes.ASSET_ADMINISTRATION_SHELL);
        subjectIdKey.setValue("ExternalSubject key value");
        externalSubjectIdReference.setKeys(List.of(subjectIdKey));

        DefaultKey assetIdKey = new DefaultKey();
        assetIdKey.setType(KeyTypes.BASIC_EVENT_ELEMENT);
        assetIdKey.setValue("assetIdKey value");

        DefaultReference assetIdReference = new DefaultReference();
        assetIdReference.setType(ReferenceTypes.EXTERNAL_REFERENCE);
        assetIdReference.setKeys(List.of(assetIdKey));

        DefaultSpecificAssetId DefaultSpecificAssetId1 = new DefaultSpecificAssetId();
        DefaultSpecificAssetId1.setName("identifier1KeyExample");
        DefaultSpecificAssetId1.setValue("identifier1ValueExample");
        DefaultSpecificAssetId1.setSemanticId(DefaultSpecificAssetIdReference);
        DefaultSpecificAssetId1.setSupplementalSemanticIds(List.of(assetIdReference));
        DefaultSpecificAssetId1.setExternalSubjectId(externalSubjectIdReference);

        DefaultSpecificAssetId DefaultSpecificAssetId2 = new DefaultSpecificAssetId();
        DefaultSpecificAssetId2.setName("identifier2KeyExample");
        DefaultSpecificAssetId2.setValue("identifier2ValueExample");
        DefaultSpecificAssetId2.setSemanticId(DefaultSpecificAssetIdReference);
        DefaultSpecificAssetId2.setSupplementalSemanticIds(List.of(assetIdReference));
        DefaultSpecificAssetId2.setExternalSubjectId(externalSubjectIdReference);
        assetAdministrationShellDescriptor.setSpecificAssetIds(List.of(DefaultSpecificAssetId1, DefaultSpecificAssetId2));

        DefaultLangStringTextType description1 = new DefaultLangStringTextType();
        description1.setLanguage("de");
        description1.setText("hello text");
        DefaultLangStringTextType description2 = new DefaultLangStringTextType();
        description2.setLanguage("en");
        description2.setText("hello s");
        assetAdministrationShellDescriptor.setDescription(List.of(description1, description2));

        DefaultProtocolInformation protocolInformation = new DefaultProtocolInformation();
        protocolInformation.setEndpointProtocol("endpointProtocolExample");
        protocolInformation.setHref(endpointUrl);
        protocolInformation.setEndpointProtocolVersion(List.of("e"));
        protocolInformation.setSubprotocol("subprotocolExample");
        protocolInformation.setSubprotocolBody("subprotocolBodyExample");
        protocolInformation.setSubprotocolBodyEncoding("subprotocolBodyExample");

        DefaultSecurityAttributeObject securityAttribute = new DefaultSecurityAttributeObject();
        securityAttribute.setType(SecurityTypeEnum.NONE);
        securityAttribute.setKey("Security Attribute key");
        securityAttribute.setValue("Security Attribute value");
        protocolInformation.setSecurityAttributes(List.of(securityAttribute));

        Endpoint endpoint = new DefaultEndpoint();
        endpoint.set_interface("interfaceNameExample");
        endpoint.setProtocolInformation(protocolInformation);

        Reference submodelSemanticReference = new DefaultReference();
        submodelSemanticReference.setType(ReferenceTypes.EXTERNAL_REFERENCE);
        Key key = new DefaultKey();
        key.setType(KeyTypes.SUBMODEL);
        key.setValue(semanticId);
        submodelSemanticReference.setKeys(List.of(key));

        Reference submodelSupplemSemanticIdReference = new DefaultReference();
        submodelSupplemSemanticIdReference.setType(ReferenceTypes.EXTERNAL_REFERENCE);
        Key submodelSupplemSemanticIdkey = new DefaultKey();
        submodelSupplemSemanticIdkey.setType(KeyTypes.SUBMODEL);
        submodelSupplemSemanticIdkey.setValue("supplementalsemanticIdExample value");
        submodelSupplemSemanticIdReference.setKeys(List.of(submodelSupplemSemanticIdkey));

        SubmodelDescriptor submodelDescriptor = new DefaultSubmodelDescriptor();
        submodelDescriptor.setId(UUID.randomUUID().toString());
        submodelDescriptor.setDisplayName(List.of(displayName));
        submodelDescriptor.setIdShort(RandomStringUtils.random(10, true, true));
        submodelDescriptor.setSemanticId(submodelSemanticReference);
        submodelDescriptor.setSupplementalSemanticId(List.of(submodelSupplemSemanticIdReference));
        submodelDescriptor.setDescription(List.of(description1, description2));
        submodelDescriptor.setEndpoints(List.of(endpoint));
        List<SubmodelDescriptor> submodelDescriptors = new ArrayList<>();
        submodelDescriptors.add(submodelDescriptor);
        assetAdministrationShellDescriptor.setSubmodelDescriptors(submodelDescriptors);
        return assetAdministrationShellDescriptor;
    }


    public static SubmodelDescriptor createSubmodel() {
        return createSubmodel("semanticIdExample", "http://endpoint-address");
    }


    public static SubmodelDescriptor createSubmodel(String semanticId, String endpointUrl) {
        SubmodelDescriptor submodelDescriptor = new DefaultSubmodelDescriptor();
        submodelDescriptor.setId(UUID.randomUUID().toString());
        submodelDescriptor.setIdShort("idShortExample");

        Reference submodelSemanticReference = new DefaultReference();
        submodelSemanticReference.setType(ReferenceTypes.EXTERNAL_REFERENCE);
        Key key = new DefaultKey();
        key.setType(KeyTypes.SUBMODEL);
        key.setValue(semanticId);
        submodelSemanticReference.setKeys(List.of(key));

        submodelSemanticReference.setKeys(List.of(key));
        submodelDescriptor.setSemanticId(submodelSemanticReference);

        LangStringTextType description1 = new DefaultLangStringTextType();
        description1.setLanguage("de");
        description1.setText("hello text");
        LangStringTextType description2 = new DefaultLangStringTextType();
        description2.setLanguage("en");
        description2.setText("hello s");

        LangStringNameType displayName = new DefaultLangStringNameType();
        displayName.setLanguage("en");
        displayName.setText("this is submodel display name");

        ProtocolInformation protocolInformation = new DefaultProtocolInformation();
        protocolInformation.setEndpointProtocol("endpointProtocolExample");
        protocolInformation.setHref(endpointUrl);
        protocolInformation.setEndpointProtocolVersion(List.of("e"));
        protocolInformation.setSubprotocol("subprotocolExample");
        protocolInformation.setSubprotocolBody("subprotocolBodyExample");
        protocolInformation.setSubprotocolBodyEncoding("subprotocolBodyExample");

        DefaultSecurityAttributeObject securityAttribute = new DefaultSecurityAttributeObject();
        securityAttribute.setType(SecurityTypeEnum.NONE);
        securityAttribute.setKey("Security Attribute key");
        securityAttribute.setValue("Security Attribute value");
        protocolInformation.setSecurityAttributes(List.of(securityAttribute));

        Endpoint endpoint = new DefaultEndpoint();
        endpoint.set_interface("interfaceNameExample");
        endpoint.setProtocolInformation(protocolInformation);

        Reference submodelSupplemSemanticIdReference = new DefaultReference();
        submodelSupplemSemanticIdReference.setType(ReferenceTypes.EXTERNAL_REFERENCE);
        Key submodelSupplemSemanticIdkey = new DefaultKey();
        submodelSupplemSemanticIdkey.setType(KeyTypes.SUBMODEL);
        submodelSupplemSemanticIdkey.setValue("supplementalsemanticIdExample value");
        submodelSupplemSemanticIdReference.setKeys(List.of(submodelSupplemSemanticIdkey));

        submodelDescriptor.setSupplementalSemanticId(List.of(submodelSupplemSemanticIdReference));
        submodelDescriptor.setDescription(List.of(description1, description2));
        submodelDescriptor.setDisplayName(List.of(displayName));
        submodelDescriptor.setEndpoints(List.of(endpoint));
        return submodelDescriptor;
    }


    public static DefaultSpecificAssetId createSpecificAssetId() {
        DefaultSpecificAssetId DefaultSpecificAssetId1 = new DefaultSpecificAssetId();
        DefaultSpecificAssetId1.setName("identifier1KeyExample");
        DefaultSpecificAssetId1.setValue("identifier1ValueExample");

        Reference reference = new DefaultReference();
        reference.setType(ReferenceTypes.EXTERNAL_REFERENCE);
        Key key = new DefaultKey();
        key.setType(KeyTypes.SUBMODEL);
        key.setValue("key");
        reference.setKeys(List.of(key));

        DefaultSpecificAssetId1.setSupplementalSemanticIds(List.of(reference));
        DefaultSpecificAssetId1.setExternalSubjectId(reference);
        return DefaultSpecificAssetId1;
    }


    public static DefaultSpecificAssetId createSpecificAssetId(String name, String value, List<String> externalSubjectIds) {
        DefaultSpecificAssetId DefaultSpecificAssetId1 = new DefaultSpecificAssetId();
        DefaultSpecificAssetId1.setName(name);
        DefaultSpecificAssetId1.setValue(value);

        if (externalSubjectIds != null && !externalSubjectIds.isEmpty()) {
            Reference reference = new DefaultReference();
            reference.setType(ReferenceTypes.EXTERNAL_REFERENCE);
            List<Key> keys = new ArrayList<>();
            externalSubjectIds.forEach(externalSubjectId -> {
                Key key = new DefaultKey();
                key.setType(KeyTypes.SUBMODEL);
                key.setValue(externalSubjectId);
                keys.add(key);
            });
            reference.setKeys(keys);
            DefaultSpecificAssetId1.setExternalSubjectId(reference);
        }

        Key assetIdKey = new DefaultKey();
        assetIdKey.setType(KeyTypes.BASIC_EVENT_ELEMENT);
        assetIdKey.setValue("assetIdKey value");

        Reference assetIdReference = new DefaultReference();
        assetIdReference.setType(ReferenceTypes.EXTERNAL_REFERENCE);
        assetIdReference.setKeys(List.of(assetIdKey));
        DefaultSpecificAssetId1.setSemanticId(assetIdReference);
        DefaultSpecificAssetId1.setSupplementalSemanticIds(List.of(assetIdReference));
        return DefaultSpecificAssetId1;
    }


    public static AssetLink createAssetLink() {
        AssetLink assetLink = new AssetLink();
        assetLink.setName("identifier1KeyExample");
        assetLink.setValue("identifier1ValueExample");
        return assetLink;
    }


    public static AssetLink createAssetLink(String name, String value) {
        AssetLink assetLink = new AssetLink();
        assetLink.setName(name);
        assetLink.setValue(value);
        return assetLink;
    }


    public static String getEncodedValue(String shellId) {
        return Base64.getUrlEncoder().encodeToString(shellId.getBytes());
    }


    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.writeValue(os, obj);
        return os.toByteArray();
    }
}
