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

import static de.fraunhofer.iosb.ilt.faaast.registry.service.helper.Constants.SHELL_REQUEST_PATH;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEndpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProtocolInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelDescriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.EntityExchangeResult;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureRestTestClient
class ShellRegistryControllerIT extends AbstractShellRegistryControllerIT {

    @Test
    void testGetAASs() {
        assertGetAASs("");
    }


    @Test
    void testGetAASsWithSlash() {
        assertGetAASs("/");
    }


    public ShellRegistryControllerIT() {
        super(SHELL_REQUEST_PATH);
    }


    @Test
    void testCreateAas() {
        AssetAdministrationShellDescriptor expected = getAas();
        createAas(expected);
        checkGetAas(expected);

        EntityExchangeResult<Page<AssetAdministrationShellDescriptor>> response2 = restClient.get().uri(createURLWithPort("")).exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Page<AssetAdministrationShellDescriptor>>() {}).returnResult();

        Assertions.assertNotNull(response2.getResponseBody());
        var list = response2.getResponseBody().getContent();
        Assertions.assertNotNull(list);
        Assertions.assertTrue(list.size() >= 0);

        Optional<AssetAdministrationShellDescriptor> actual = list.stream().filter(x -> expected.getId().equals(x.getId())).findFirst();
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(expected, actual.get());

        // check that an error is reposrted, when an AAS shall be created, that already exists
        checkCreateAasError(expected, HttpStatus.CONFLICT);
    }


    @Test
    void testCreateInvalidAas() {
        AssetAdministrationShellDescriptor expected = getAasInvalid();
        // Workaround for an erroneous Codacy warning
        Assertions.assertNotNull(expected);
        checkCreateAasError(expected, HttpStatus.BAD_REQUEST);
    }


    @Test
    void testUpdateDeleteAas() {
        // create AAS
        AssetAdministrationShellDescriptor original = getAasUpdate();
        createAas(original);

        // update AAS
        AssetAdministrationShellDescriptor expected = getAasUpdate();
        expected.setIdShort("IntegrationTest100A");
        expected.getDisplayName().add(new DefaultLangStringNameType.Builder().text("Integration Test 100 Name Updated").language("en-US").build());

        restClient.put().uri(createURLWithPort("/" + EncodingHelper.base64UrlEncode(expected.getId()))).body(expected).exchange()
                .expectStatus().isNoContent();

        checkGetAas(expected);

        // delete AAS
        restClient.delete().uri(createURLWithPort("/" + EncodingHelper.base64UrlEncode(expected.getId()))).exchange()
                .expectStatus().isNoContent();

        checkGetAasNotExist(expected.getId());
    }


    @Test
    void testInvalidLimit() {
        restClient.get().uri(createURLWithPort("?limit=0")).exchange()
                .expectStatus().isBadRequest();
    }


    @Test
    void testPageCursorWithAssetType() {
        Map<String, AssetAdministrationShellDescriptor> expectedMap = new HashMap<>();
        String assetType = "PageCursorTest";
        AssetAdministrationShellDescriptor aas1 = getAas();
        aas1.setId("http://iosb.fraunhofer.de/IntegrationTest/PageCursor/AAS1");
        aas1.setAssetType(assetType);
        createAas(aas1);
        expectedMap.put(aas1.getId(), aas1);

        AssetAdministrationShellDescriptor aas2 = getAas();
        aas2.setId("http://iosb.fraunhofer.de/IntegrationTest/PageCursor/AAS2");
        aas2.setAssetType(assetType);
        createAas(aas2);
        expectedMap.put(aas2.getId(), aas2);

        EntityExchangeResult<Page<AssetAdministrationShellDescriptor>> response = restClient.get()
                .uri(createURLWithPort("?limit=1&assetType=" + EncodingHelper.base64UrlEncode(assetType))).exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Page<AssetAdministrationShellDescriptor>>() {}).returnResult();
        Assertions.assertNotNull(response.getResponseBody());
        var list = response.getResponseBody().getContent();
        Assertions.assertNotNull(list);
        Assertions.assertEquals(1, list.size());
        var metadata = response.getResponseBody().getMetadata();
        Assertions.assertNotNull(metadata);
        Assertions.assertNotNull(metadata.getCursor());
        Assertions.assertFalse(metadata.getCursor().isEmpty());

        AssetAdministrationShellDescriptor actual = list.get(0);
        Assertions.assertTrue(expectedMap.containsKey(actual.getId()));
        Assertions.assertEquals(expectedMap.get(actual.getId()), actual);
        expectedMap.remove(actual.getId());

        EntityExchangeResult<Page<AssetAdministrationShellDescriptor>> response2 = restClient.get()
                .uri(createURLWithPort("?limit=1&assetType=" + EncodingHelper.base64UrlEncode(assetType) + "&cursor=" + metadata.getCursor())).exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Page<AssetAdministrationShellDescriptor>>() {}).returnResult();
        Assertions.assertNotNull(response2.getResponseBody());
        list = response2.getResponseBody().getContent();
        Assertions.assertNotNull(list);
        Assertions.assertEquals(1, list.size());

        actual = list.get(0);
        Assertions.assertTrue(expectedMap.containsKey(actual.getId()));
        Assertions.assertEquals(expectedMap.get(actual.getId()), actual);
        expectedMap.remove(actual.getId());
        Assertions.assertTrue(expectedMap.isEmpty());
    }


    @Test
    void testAddUpdateDeleteSubmodel() {
        // create AAS
        AssetAdministrationShellDescriptor aas = getAas101();
        createAas(aas);

        SubmodelDescriptor newSubmodel = getSubmodel2A();
        checkGetSubmodelError(aas.getId(), newSubmodel.getId(), HttpStatus.NOT_FOUND);

        // add Submodel
        EntityExchangeResult<SubmodelDescriptor> responsePost = restClient.post()
                .uri(createURLWithPort("/" + EncodingHelper.base64UrlEncode(aas.getId()) + "/submodel-descriptors")).body(newSubmodel).exchange()
                .expectStatus().isCreated()
                .expectBody(SubmodelDescriptor.class).returnResult();
        Assertions.assertEquals(newSubmodel, responsePost.getResponseBody());

        checkGetSubmodel(aas.getId(), newSubmodel);

        // update Submodel
        newSubmodel.setIdShort("Submodel-101-2 updated");
        newSubmodel.getDescription().add(new DefaultLangStringTextType.Builder().language("en-US").text("Submodel 101-2 new Description").build());
        restClient.put()
                .uri(createURLWithPort("/" + EncodingHelper.base64UrlEncode(aas.getId()) + "/submodel-descriptors/" + EncodingHelper.base64UrlEncode(newSubmodel.getId())))
                .body(newSubmodel).exchange()
                .expectStatus().isNoContent()
                .expectBody(SubmodelDescriptor.class);
        checkGetSubmodel(aas.getId(), newSubmodel);

        // delete Submodel
        restClient.delete()
                .uri(createURLWithPort("/" + EncodingHelper.base64UrlEncode(aas.getId()) + "/submodel-descriptors/" + EncodingHelper.base64UrlEncode(newSubmodel.getId())))
                .exchange()
                .expectStatus().isNoContent();
        checkGetSubmodelError(aas.getId(), newSubmodel.getId(), HttpStatus.NOT_FOUND);
    }


    @Test
    void testAddInvalidSubmodel() {
        AssetAdministrationShellDescriptor aas = getAas();
        aas.setId("http://iosb.fraunhofer.de/IntegrationTest/Invalid/AAS1");
        createAas(aas);

        restClient.post().uri(createURLWithPort("/" + EncodingHelper.base64UrlEncode(aas.getId()) + "/submodel-descriptors"))
                .body(getSubmodelInvalid()).exchange()
                .expectStatus().isBadRequest();
    }


    private void checkGetAas(AssetAdministrationShellDescriptor expected) {
        EntityExchangeResult<AssetAdministrationShellDescriptor> response = restClient.get().uri(createURLWithPort("/" + EncodingHelper.base64UrlEncode(expected.getId())))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AssetAdministrationShellDescriptor.class).returnResult();
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertEquals(expected, response.getResponseBody());
    }


    private void checkGetAasNotExist(String id) {
        restClient.get().uri(createURLWithPort("/" + EncodingHelper.base64UrlEncode(id))).exchange()
                .expectStatus().isNotFound();
    }


    private void checkGetSubmodel(String aasId, SubmodelDescriptor submodel) {
        EntityExchangeResult<SubmodelDescriptor> response = restClient.get()
                .uri(createURLWithPort("/" + EncodingHelper.base64UrlEncode(aasId) + "/submodel-descriptors/" + EncodingHelper.base64UrlEncode(submodel.getId()))).exchange()
                .expectStatus().isOk()
                .expectBody(SubmodelDescriptor.class).returnResult();
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertEquals(submodel, response.getResponseBody());
    }


    private void checkGetSubmodelError(String aasId, String submodelId, HttpStatusCode statusCode) {
        restClient.get()
                .uri(createURLWithPort("/" + EncodingHelper.base64UrlEncode(aasId) + "/submodel-descriptors/" + EncodingHelper.base64UrlEncode(submodelId))).exchange()
                .expectStatus().isEqualTo(statusCode);
    }


    private void checkCreateAasError(AssetAdministrationShellDescriptor aas, HttpStatusCode statusCode) {
        restClient.post().uri(createURLWithPort("")).body(aas).exchange()
                .expectStatus().isEqualTo(statusCode);
    }


    private static AssetAdministrationShellDescriptor getAasUpdate() {
        return new DefaultAssetAdministrationShellDescriptor.Builder()
                .idShort("IntegrationTest100")
                .id("http://iosb.fraunhofer.de/IntegrationTest/AAS100")
                .displayName(new DefaultLangStringNameType.Builder().text("Integration Test 100 Name aktualisiert").language("de-DE").build())
                .globalAssetId("http://iosb.fraunhofer.de/GlobalAssetId/IntegrationTest100")
                .assetType("AssetType100")
                .build();
    }


    private static AssetAdministrationShellDescriptor getAas101() {
        return new DefaultAssetAdministrationShellDescriptor.Builder()
                .idShort("IntegrationTest99")
                .id("http://iosb.fraunhofer.de/IntegrationTest/AAS101")
                .displayName(new DefaultLangStringNameType.Builder().text("Integration Test 101 Name").language("de-DE").build())
                .globalAssetId("http://iosb.fraunhofer.de/GlobalAssetId/IntegrationTest101")
                .assetType("AssetType101")
                .submodelDescriptors(new DefaultSubmodelDescriptor.Builder()
                        .id("http://iosb.fraunhofer.de/IntegrationTest/Submodel101-1")
                        .idShort("Submodel-101-1")
                        .administration(new DefaultAdministrativeInformation.Builder()
                                .version("2")
                                .revision("15")
                                .build())
                        .endpoints(new DefaultEndpoint.Builder()
                                ._interface("http")
                                .protocolInformation(new DefaultProtocolInformation.Builder()
                                        .endpointProtocol("http")
                                        .href("http://iosb.fraunhofer.de/Endpoints/Submodel101-1")
                                        .endpointProtocolVersion(List.of("2.0"))
                                        .build())
                                .build())
                        .displayName(new DefaultLangStringNameType.Builder()
                                .language("de-DE")
                                .text("Submodel 101-1")
                                .build())
                        .build())
                .build();
    }


    private static SubmodelDescriptor getSubmodel2A() {
        return new DefaultSubmodelDescriptor.Builder()
                .id("http://iosb.fraunhofer.de/IntegrationTest/Submodel101-2")
                .idShort("Submodel-101-2")
                .administration(new DefaultAdministrativeInformation.Builder()
                        .version("2")
                        .revision("18")
                        .templateId("Template101-2")
                        .build())
                .endpoints(new DefaultEndpoint.Builder()
                        ._interface("http")
                        .protocolInformation(new DefaultProtocolInformation.Builder()
                                .endpointProtocol("http")
                                .href("http://iosb.fraunhofer.de/Endpoints/Submodel101-2")
                                .endpointProtocolVersion(List.of("2.1"))
                                .build())
                        .build())
                .displayName(new DefaultLangStringNameType.Builder()
                        .language("de-DE")
                        .text("Submodel 101-2")
                        .build())
                .description(new DefaultLangStringTextType.Builder()
                        .language("de-DE")
                        .text("Submodel 101-2 Beschreibung")
                        .build())
                .build();
    }


    private static AssetAdministrationShellDescriptor getAasInvalid() {
        return new DefaultAssetAdministrationShellDescriptor.Builder()
                .idShort("AasInvalid")
                .displayName(new DefaultLangStringNameType.Builder().text("AAS Invalid Name").language("en-US").build())
                .globalAssetId("http://iosb.fraunhofer.de/GlobalAssetId/AasInvalid")
                .assetType("AssetTypeInvalid")
                .build();
    }


    private static SubmodelDescriptor getSubmodelInvalid() {
        return new DefaultSubmodelDescriptor.Builder()
                .idShort("Submodel-Invalid")
                .administration(new DefaultAdministrativeInformation.Builder()
                        .version("1")
                        .revision("A")
                        .build())
                .build();
    }


    private void assertGetAASs(String urlPostFix) {
        EntityExchangeResult<Page<AssetAdministrationShellDescriptor>> response = restClient.get().uri(createURLWithPort(urlPostFix)).exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Page<AssetAdministrationShellDescriptor>>() {}).returnResult();
        Assertions.assertNotNull(response.getResponseBody());
        var list = response.getResponseBody().getContent();
        Assertions.assertNotNull(list);
    }

}
