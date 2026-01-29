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

import de.fraunhofer.iosb.ilt.faaast.registry.core.AasRepository;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestRestTemplate
class BulkOperationControllerIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkOperationControllerIT.class);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AasRepository aasRepository;

    @BeforeEach
    void init() {
        aasRepository.clear();
    }


    @Test
    void testAsyncAasCommit() {
        List<AssetAdministrationShellDescriptor> commitAASList = List.of(
                generateAas("001"),
                generateAas("002"),
                generateAas("003"));

        HttpEntity<List<AssetAdministrationShellDescriptor>> entity = new HttpEntity<>(commitAASList);
        ResponseEntity<Void> createResponse = restTemplate.exchange(createURLWithPort("/shell-descriptors"), HttpMethod.POST, entity, Void.class);
        Assertions.assertNotNull(createResponse);
        Assertions.assertEquals(HttpStatus.ACCEPTED, createResponse.getStatusCode());
        URI location = createResponse.getHeaders().getLocation();
        Assertions.assertNotNull(location);
        String fullCreate = location.toString().replace("..", createURLWithPort(""));
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> {
                    ResponseEntity<String> statusResponse = restTemplate.getForEntity(
                            fullCreate,
                            String.class);
                    return statusResponse.getStatusCode() == HttpStatusCode.valueOf(204);
                });

        Assertions.assertEquals(commitAASList, aasRepository.getAASs(PagingInfo.ALL).getContent());

        for (var aas: commitAASList) {
            aas.setIdShort(aas.getIdShort() + "_new");
        }

        HttpEntity<List<AssetAdministrationShellDescriptor>> updateEntity = new HttpEntity<>(commitAASList);
        ResponseEntity<Void> updateResponse = restTemplate.exchange(createURLWithPort("/shell-descriptors"), HttpMethod.PUT, updateEntity, Void.class);
        Assertions.assertNotNull(updateResponse);
        Assertions.assertEquals(HttpStatus.ACCEPTED, updateResponse.getStatusCode());
        location = updateResponse.getHeaders().getLocation();
        Assertions.assertNotNull(location);
        String fullUpdate = location.toString().replace("..", createURLWithPort(""));

        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> {
                    ResponseEntity<String> statusResponse = restTemplate.getForEntity(
                            fullUpdate,
                            String.class);
                    return statusResponse.getStatusCode() == HttpStatusCode.valueOf(204);
                });

        Assertions.assertEquals(commitAASList, aasRepository.getAASs(PagingInfo.ALL).getContent());

        List<String> shellIds = new ArrayList<>();
        for (var aas: commitAASList) {
            shellIds.add(aas.getId());
        }
        HttpEntity<List<String>> deleteEntity = new HttpEntity<>(shellIds);
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(createURLWithPort("/shell-descriptors"), HttpMethod.DELETE, deleteEntity, Void.class);
        Assertions.assertNotNull(deleteResponse);
        Assertions.assertEquals(HttpStatus.ACCEPTED, deleteResponse.getStatusCode());
        location = deleteResponse.getHeaders().getLocation();
        Assertions.assertNotNull(location);
        String fullDelete = location.toString().replace("..", createURLWithPort(""));
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> {
                    ResponseEntity<String> statusResponse = restTemplate.getForEntity(
                            fullDelete,
                            String.class);
                    LOGGER.info("status: {}", statusResponse.getStatusCode());
                    return statusResponse.getStatusCode() == HttpStatusCode.valueOf(204);
                });
        Assertions.assertEquals(new ArrayList<AssetAdministrationShellDescriptor>(), aasRepository.getAASs(PagingInfo.ALL).getContent());
    }


    @Test
    void testAsyncCreateRollback() {
        List<AssetAdministrationShellDescriptor> rollbackAASList = List.of(
                generateAas("004"),
                generateAas("005"),
                generateAas("004"));

        HttpEntity<List<AssetAdministrationShellDescriptor>> entity = new HttpEntity<>(rollbackAASList);
        ResponseEntity<Void> createResponse = restTemplate.exchange(createURLWithPort("/shell-descriptors"), HttpMethod.POST, entity, Void.class);
        Assertions.assertNotNull(createResponse);
        Assertions.assertEquals(HttpStatus.ACCEPTED, createResponse.getStatusCode());
        URI location = createResponse.getHeaders().getLocation();
        Assertions.assertNotNull(location);
        String fullCreate = location.toString().replace("..", createURLWithPort(""));

        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> {
                    ResponseEntity<String> statusResponse = restTemplate.getForEntity(
                            fullCreate,
                            String.class);
                    return statusResponse.getStatusCode() == HttpStatusCode.valueOf(400);
                });

        Assertions.assertEquals(new ArrayList<AssetAdministrationShellDescriptor>(), aasRepository.getAASs(PagingInfo.ALL).getContent());
    }


    @Test
    void testAsyncSubmodelCommit() {
        List<SubmodelDescriptor> commitSubmodelList = List.of(
                generateSubmodel("001"),
                generateSubmodel("002"),
                generateSubmodel("003"));

        HttpEntity<List<SubmodelDescriptor>> entity = new HttpEntity<>(commitSubmodelList);
        ResponseEntity<Void> createResponse = restTemplate.exchange(createURLWithPort("/submodel-descriptors"), HttpMethod.POST, entity, Void.class);
        Assertions.assertNotNull(createResponse);
        Assertions.assertEquals(HttpStatus.ACCEPTED, createResponse.getStatusCode());
        URI location = createResponse.getHeaders().getLocation();
        Assertions.assertNotNull(location);
        String fullCreate = location.toString().replace("..", createURLWithPort(""));
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> {
                    ResponseEntity<String> statusResponse = restTemplate.getForEntity(
                            fullCreate,
                            String.class);
                    return statusResponse.getStatusCode() == HttpStatusCode.valueOf(204);
                });

        Assertions.assertTrue(listEquals(commitSubmodelList, aasRepository.getSubmodels(PagingInfo.ALL).getContent()));

        for (var aas: commitSubmodelList) {
            aas.setIdShort(aas.getIdShort() + "_new");
        }

        HttpEntity<List<SubmodelDescriptor>> updateEntity = new HttpEntity<>(commitSubmodelList);
        ResponseEntity<Void> updateResponse = restTemplate.exchange(createURLWithPort("/submodel-descriptors"), HttpMethod.PUT, updateEntity, Void.class);
        Assertions.assertNotNull(updateResponse);
        Assertions.assertEquals(HttpStatus.ACCEPTED, updateResponse.getStatusCode());
        location = updateResponse.getHeaders().getLocation();
        Assertions.assertNotNull(location);
        String fullUpdate = location.toString().replace("..", createURLWithPort(""));

        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> {
                    ResponseEntity<String> statusResponse = restTemplate.getForEntity(
                            fullUpdate,
                            String.class);
                    return statusResponse.getStatusCode() == HttpStatusCode.valueOf(204);
                });

        Assertions.assertTrue(listEquals(commitSubmodelList, aasRepository.getSubmodels(PagingInfo.ALL).getContent()));

        List<String> submodelIds = new ArrayList<>();
        for (var aas: commitSubmodelList) {
            submodelIds.add(aas.getId());
        }
        HttpEntity<List<String>> deleteEntity = new HttpEntity<>(submodelIds);
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(createURLWithPort("/submodel-descriptors"), HttpMethod.DELETE, deleteEntity, Void.class);
        Assertions.assertNotNull(deleteResponse);
        Assertions.assertEquals(HttpStatus.ACCEPTED, deleteResponse.getStatusCode());
        location = deleteResponse.getHeaders().getLocation();
        Assertions.assertNotNull(location);
        String fullDelete = location.toString().replace("..", createURLWithPort(""));
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> {
                    ResponseEntity<String> statusResponse = restTemplate.getForEntity(
                            fullDelete,
                            String.class);
                    LOGGER.info("status: {}", statusResponse.getStatusCode());
                    return statusResponse.getStatusCode() == HttpStatusCode.valueOf(204);
                });
        Assertions.assertEquals(new ArrayList<SubmodelDescriptor>(), aasRepository.getSubmodels(PagingInfo.ALL).getContent());
    }


    @Test
    void testStatusUnknownHandle() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                createURLWithPort("/status/unknown-id"),
                String.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    @Test
    void testResultUnknownHandle() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                createURLWithPort("/result/unknown-id"),
                String.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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


    private SubmodelDescriptor generateSubmodel(String id) {
        return new DefaultSubmodelDescriptor.Builder()
                .id("http://iosb.fraunhofer.de/IntegrationTest/Submodel" + id)
                .idShort("SubmodelIT" + id)
                .administration(new DefaultAdministrativeInformation.Builder()
                        .version("2")
                        .revision("5")
                        .build())
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value(String.format("http://iosb.fraunhofer.de/IntegrationTest/SubmodelIT%s/SemanticId", id))
                                .build())
                        .build())
                .endpoints(new DefaultEndpoint.Builder()
                        ._interface("http")
                        .protocolInformation(new DefaultProtocolInformation.Builder()
                                .endpointProtocol("http")
                                .href("http://iosb.fraunhofer.de/Endpoints/SubmodelIT" + id)
                                .endpointProtocolVersion(List.of("2.0"))
                                .build())
                        .build())
                .build();
    }


    private boolean listEquals(List<SubmodelDescriptor> first, List<SubmodelDescriptor> second) {
        return first.size() == second.size() && first.containsAll(second) && second.containsAll(first);
    }

}
