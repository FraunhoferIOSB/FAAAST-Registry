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
package de.fraunhofer.iosb.ilt.faaast.registry.service;

import java.util.Arrays;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SecurityTypeEnum;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultDataSpecificationIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEmbeddedDataSpecification;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEndpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultExtension;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringDefinitionTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringPreferredNameTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringShortNameTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProtocolInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSecurityAttributeObject;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultValueList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultValueReferencePair;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public abstract class AbstractShellRegistryControllerIT {

    private final String baseResourceName;

    public AbstractShellRegistryControllerIT(String baseResourceName) {
        this.baseResourceName = baseResourceName;
    }

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    protected void createAas(AssetAdministrationShellDescriptor aas) {
        HttpEntity<AssetAdministrationShellDescriptor> entity = new HttpEntity<>(aas);
        ResponseEntity<AssetAdministrationShellDescriptor> responsePost = restTemplate.exchange("http://localhost:" + port + "/api/v3" +
                ".0/shell-descriptors", HttpMethod.POST, entity,
                AssetAdministrationShellDescriptor.class);
        Assert.assertNotNull(responsePost);
        Assert.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode());
        Assert.assertEquals(aas, responsePost.getBody());
    }


    protected String createURLWithPort(String uri) {
        return "http://localhost:" + port + "/api/v3.0" + baseResourceName + uri;
    }


    protected static AssetAdministrationShellDescriptor getAas() {
        return new DefaultAssetAdministrationShellDescriptor.Builder()
                .idShort("IntegrationTest99")
                .id("http://iosb.fraunhofer.de/IntegrationTest/AAS99")
                .displayName(new DefaultLangStringNameType.Builder().text("Integration Test 99 Name").language("de-DE").build())
                .description(new DefaultLangStringTextType.Builder()
                        .language("en-US")
                        .text("AAS 99 Integration Test")
                        .build())
                .description(new DefaultLangStringTextType.Builder()
                        .language("de-DE")
                        .text("AAS 99 Integrationstest")
                        .build())
                .globalAssetId("http://iosb.fraunhofer.de/GlobalAssetId/IntegrationTest99")
                .assetType("AssetType99")
                .assetKind(AssetKind.INSTANCE)
                .administration(new DefaultAdministrativeInformation.Builder()
                        .creator(new DefaultReference.Builder()
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value("http://anydomain.com/users/User99-1")
                                        .build())
                                .build())
                        .version("12")
                        .revision("25")
                        .embeddedDataSpecifications(new DefaultEmbeddedDataSpecification.Builder()
                                .dataSpecification(new DefaultReference.Builder()
                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                        .keys(new DefaultKey.Builder()
                                                .type(KeyTypes.GLOBAL_REFERENCE)
                                                .value("http://iosb.fraunhofer.de/IntegrationTest/AAS99/DataSpecificationIEC61360")
                                                .build())
                                        .build())
                                .dataSpecificationContent(new DefaultDataSpecificationIec61360.Builder()
                                        .preferredName(Arrays.asList(
                                                new DefaultLangStringPreferredNameTypeIec61360.Builder().text("AAS 99 Spezifikation").language("de").build(),
                                                new DefaultLangStringPreferredNameTypeIec61360.Builder().text("AAS 99 Specification").language("en" +
                                                        "-us").build()))
                                        .dataType(DataTypeIec61360.REAL_MEASURE)
                                        .definition(new DefaultLangStringDefinitionTypeIec61360.Builder().text("Dies ist eine Data Specification " +
                                                "fuer Integration Test")
                                                .language("de").build())
                                        .definition(
                                                new DefaultLangStringDefinitionTypeIec61360.Builder().text("This is a DataSpecification for " +
                                                        "integration testing purposes")
                                                        .language("en-us").build())
                                        .shortName(new DefaultLangStringShortNameTypeIec61360.Builder().text("Test Spezifikation").language("de").build())
                                        .shortName(new DefaultLangStringShortNameTypeIec61360.Builder().text("Test Spec").language("en-us").build())
                                        .unit("SpaceUnit")
                                        .unitId(new DefaultReference.Builder()
                                                .keys(new DefaultKey.Builder()
                                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                                        .value("http://iosb.fraunhofer.de/IntegrationTest/Units/TestUnit")
                                                        .build())
                                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                                .build())
                                        .sourceOfDefinition("http://iosb.fraunhofer.de/IntegrationTest/AAS99/DataSpec/ExampleDef")
                                        .symbol("SU")
                                        .valueFormat("string")
                                        .value("TEST")
                                        .valueList(new DefaultValueList.Builder()
                                                .valueReferencePairs(new DefaultValueReferencePair.Builder()
                                                        .value("http://iosb.fraunhofer.de/IntegrationTest/ValueId/ExampleValueId")
                                                        .valueId(new DefaultReference.Builder()
                                                                .keys(new DefaultKey.Builder()
                                                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                                                        .value("http://iosb.fraunhofer.de/IntegrationTest/ExampleValueId")
                                                                        .build())
                                                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                                                .build())
                                                        .build())
                                                .valueReferencePairs(new DefaultValueReferencePair.Builder()
                                                        .value("http://iosb.fraunhofer.de/IntegrationTest/ValueId/ExampleValueId2")
                                                        .valueId(new DefaultReference.Builder()
                                                                .keys(new DefaultKey.Builder()
                                                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                                                        .value("http://iosb.fraunhofer.de/IntegrationTest/ValueId/ExampleValueId2")
                                                                        .build())
                                                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .endpoints(new DefaultEndpoint.Builder()
                        ._interface("http")
                        .protocolInformation(new DefaultProtocolInformation.Builder()
                                .endpointProtocol("http")
                                .href("http://iosb.fraunhofer.de/IntegrationTest/Endpoints/AAS99")
                                .endpointProtocolVersion(List.of("2.1"))
                                .subprotocol("https")
                                .subprotocolBody("any body")
                                .subprotocolBodyEncoding("UTF-8")
                                .securityAttributes(new DefaultSecurityAttributeObject.Builder()
                                        .type(SecurityTypeEnum.NONE)
                                        .key("")
                                        .value("")
                                        .build())
                                .build())
                        .build())
                .extensions(new DefaultExtension.Builder()
                        .name("AAS99 Extension Name")
                        .value("AAS99 Extension Value")
                        .semanticId(new DefaultReference.Builder()
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value("http://iosb.fraunhofer.de/IntegrationTest/Extension99/SemanticId1")
                                        .build())
                                .build())
                        .refersTo(new DefaultReference.Builder()
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value("http://iosb.fraunhofer.de/IntegrationTest/Extension99/RefersTo1")
                                        .build())
                                .build())
                        .valueType(DataTypeDefXsd.STRING)
                        .supplementalSemanticIds(new DefaultReference.Builder()
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value("http://iosb.fraunhofer.de/IntegrationTest/Extension99/SupplementalSemanticId1")
                                        .build())
                                .build())
                        .build())
                .submodelDescriptors(new DefaultSubmodelDescriptor.Builder()
                        .id("http://iosb.fraunhofer.de/IntegrationTest/Submodel99-1")
                        .idShort("Submodel-99-1")
                        .administration(new DefaultAdministrativeInformation.Builder()
                                .version("1")
                                .revision("12")
                                .build())
                        .semanticId(new DefaultReference.Builder()
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value("http://iosb.fraunhofer.de/IntegrationTest/Submodel99-1/SemanticId")
                                        .build())
                                .build())
                        .endpoints(new DefaultEndpoint.Builder()
                                ._interface("http")
                                .protocolInformation(new DefaultProtocolInformation.Builder()
                                        .endpointProtocol("http")
                                        .href("http://iosb.fraunhofer.de/Endpoints/Submodel99-1")
                                        .endpointProtocolVersion(List.of("2.0"))
                                        .build())
                                .build())
                        .build())
                .specificAssetIds(
                        new DefaultSpecificAssetId.Builder()
                                .name("DefaultSpecificAssetId-1 Name")
                                .value("DefaultSpecificAssetId-1 Value")
                                .supplementalSemanticIds(new DefaultReference.Builder()
                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                        .keys(new DefaultKey.Builder()
                                                .type(KeyTypes.GLOBAL_REFERENCE)
                                                .value("http://iosb.fraunhofer.de/IntegrationTest/Extension99/SupplementalSemanticId1")
                                                .build())
                                        .build())
                                .semanticId(new DefaultReference.Builder()
                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                        .keys(new DefaultKey.Builder()
                                                .type(KeyTypes.GLOBAL_REFERENCE)
                                                .value("http://iosb.fraunhofer.de/IntegrationTest/DefaultSpecificAssetId-1/SemanticId")
                                                .build())
                                        .build())
                                .externalSubjectId(new DefaultReference.Builder()
                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                        .keys(new DefaultKey.Builder()
                                                .type(KeyTypes.GLOBAL_REFERENCE)
                                                .value("http://iosb.fraunhofer.de/IntegrationTest/DefaultSpecificAssetId-1/ExternalSubjectId")
                                                .build())
                                        .build())
                                .build())
                .specificAssetIds(
                        new DefaultSpecificAssetId.Builder()
                                .name("DefaultSpecificAssetId-2 Name")
                                .value("DefaultSpecificAssetId-2 Value")
                                .supplementalSemanticIds(new DefaultReference.Builder()
                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                        .keys(new DefaultKey.Builder()
                                                .type(KeyTypes.GLOBAL_REFERENCE)
                                                .value("http://iosb.fraunhofer.de/IntegrationTest/Extension99/SupplementalSemanticId2")
                                                .build())
                                        .build())
                                .semanticId(new DefaultReference.Builder()
                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                        .keys(new DefaultKey.Builder()
                                                .type(KeyTypes.GLOBAL_REFERENCE)
                                                .value("http://iosb.fraunhofer.de/IntegrationTest/DefaultSpecificAssetId-2/SemanticId")
                                                .build())
                                        .build())
                                .externalSubjectId(new DefaultReference.Builder()
                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                        .keys(new DefaultKey.Builder()
                                                .type(KeyTypes.GLOBAL_REFERENCE)
                                                .value("http://iosb.fraunhofer.de/IntegrationTest/DefaultSpecificAssetId-2/ExternalSubjectId")
                                                .build())
                                        .build())
                                .build())
                .build();
    }

}
