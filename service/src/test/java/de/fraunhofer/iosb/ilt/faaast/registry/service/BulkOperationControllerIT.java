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

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.fraunhofer.iosb.ilt.faaast.registry.core.AasRepository;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.registry.service.service.RegistryService;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
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
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultValueList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultValueReferencePair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BulkOperationControllerIT {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RegistryService registryService;

    @Autowired
    private AasRepository aasRepository;

    @Test
    public void testAsyncCommit() throws ResourceAlreadyExistsException{
        List<AssetAdministrationShellDescriptor> commitAASList = List.of(
                generateAas("001"),
                generateAas("002"),
                generateAas("003"));

        registryService.bulkCreateShells(commitAASList);

        await()
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertEquals(commitAASList, aasRepository.getAASs());
                });
    }


    @Test
    public void testAsyncRollback() throws ResourceAlreadyExistsException{
        List<AssetAdministrationShellDescriptor> rollbackAASList = List.of(
                generateAas("001"),
                generateAas("002"),
                generateAas("001"));

        registryService.bulkCreateShells(rollbackAASList);

        await()
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertEquals(new ArrayList<>(), aasRepository.getAASs());
                });
    }


    @Test
    public void testStatusUnknownHandle() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                createURLWithPort("/status/unknown-id"),
                String.class);

        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


   @Test
   public void testResultUnknownHandle() {
       ResponseEntity<String> response = restTemplate.getForEntity(
               createURLWithPort("/result/unknown-id"),
               String.class);

       Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
   }


    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + "/api/v3.0/bulk" + uri;
    }


    private static AssetAdministrationShellDescriptor generateAas(String id) {
        return new DefaultAssetAdministrationShellDescriptor.Builder()
                .idShort("IntegrationTest" + id)
                .id("http://iosb.fraunhofer.de/IntegrationTest/AAS" + id)
                .displayName(new DefaultLangStringNameType.Builder().text("Integration Test 99 Name").language("de-DE").build())
                .description(new DefaultLangStringTextType.Builder()
                        .language("en-US")
                        .text("AAS 99 Integration Test")
                        .build())
                .description(new DefaultLangStringTextType.Builder()
                        .language("de-DE")
                        .text("AAS 99 Integrationstest")
                        .build())
                .globalAssetId("http://iosb.fraunhofer.de/GlobalAssetId/IntegrationTest" + id)
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
                                                new DefaultLangStringPreferredNameTypeIec61360.Builder().text("AAS 99 Specification").language("en-us").build()))
                                        .dataType(DataTypeIec61360.REAL_MEASURE)
                                        .definition(new DefaultLangStringDefinitionTypeIec61360.Builder().text("Dies ist eine Data Specification fuer Integration Test")
                                                .language("de").build())
                                        .definition(
                                                new DefaultLangStringDefinitionTypeIec61360.Builder().text("This is a DataSpecification for integration testing purposes")
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
                .build();
    }


    public RestTemplate createRestTemplateWithNoRedirects() {
        CloseableHttpClient httpClient = HttpClients.custom()
                .disableRedirectHandling()
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(factory);
    }
}
