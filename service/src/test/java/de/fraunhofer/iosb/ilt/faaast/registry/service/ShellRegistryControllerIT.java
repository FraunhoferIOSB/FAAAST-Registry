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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultAssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultEndpoint;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultProtocolInformation;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultSubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
public class ShellRegistryControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testGetAASs() {
        ResponseEntity<Page<AssetAdministrationShellDescriptor>> response = restTemplate.exchange(
                createURLWithPort(""), HttpMethod.GET, null, new ParameterizedTypeReference<Page<AssetAdministrationShellDescriptor>>() {});
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        var list = response.getBody().getContent();
        Assert.assertNotNull(list);
    }


    @Test
    public void testCreateAas() {
        AssetAdministrationShellDescriptor expected = getAas();
        createAas(expected);
        checkGetAas(expected);

        ResponseEntity<Page<AssetAdministrationShellDescriptor>> response2 = restTemplate.exchange(
                createURLWithPort(""), HttpMethod.GET, null, new ParameterizedTypeReference<Page<AssetAdministrationShellDescriptor>>() {});

        Assert.assertNotNull(response2);
        Assert.assertEquals(HttpStatus.OK, response2.getStatusCode());
        Assert.assertNotNull(response2.getBody());
        var list = response2.getBody().getContent();
        Assert.assertNotNull(list);
        Assert.assertTrue(list.size() >= 0);

        Optional<AssetAdministrationShellDescriptor> actual = list.stream().filter(x -> expected.getId().equals(x.getId())).findFirst();
        Assert.assertTrue(actual.isPresent());
        Assert.assertEquals(expected, actual.get());
    }


    @Test
    public void testUpdateDeleteAas() {
        // create AAS
        AssetAdministrationShellDescriptor original = getAasUpdate();
        createAas(original);

        // update AAS
        AssetAdministrationShellDescriptor expected = getAasUpdate();
        expected.setIdShort("IntegrationTest100A");
        expected.getDisplayNames().add(new DefaultLangStringNameType.Builder().text("Integration Test 100 Name Updated").language("en-US").build());

        HttpEntity<AssetAdministrationShellDescriptor> entity = new HttpEntity<>(expected);
        ResponseEntity responsePut = restTemplate.exchange(createURLWithPort("/" + EncodingHelper.base64UrlEncode(expected.getId())), HttpMethod.PUT, entity, Void.class);
        Assert.assertNotNull(responsePut);
        Assert.assertEquals(HttpStatus.NO_CONTENT, responsePut.getStatusCode());

        checkGetAas(expected);

        // delete AAS
        ResponseEntity responseDelete = restTemplate.exchange(createURLWithPort("/" + EncodingHelper.base64UrlEncode(expected.getId())), HttpMethod.DELETE, entity, Void.class);
        Assert.assertNotNull(responseDelete);
        Assert.assertEquals(HttpStatus.NO_CONTENT, responsePut.getStatusCode());

        checkGetAasNotExist(expected.getId());
    }


    @Test
    public void testInvalidLimit() {
        ResponseEntity response = restTemplate.exchange(createURLWithPort("?limit=0"), HttpMethod.GET, null, Void.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    public void testPageCursorWithAssetType() {
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

        ResponseEntity<Page<AssetAdministrationShellDescriptor>> response = restTemplate.exchange(
                createURLWithPort("?limit=1&assetType=" + EncodingHelper.base64UrlEncode(assetType)), HttpMethod.GET, null,
                new ParameterizedTypeReference<Page<AssetAdministrationShellDescriptor>>() {});
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        var list = response.getBody().getContent();
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());
        var metadata = response.getBody().getMetadata();
        Assert.assertNotNull(metadata);
        Assert.assertNotNull(metadata.getCursor());
        Assert.assertFalse(metadata.getCursor().isEmpty());

        AssetAdministrationShellDescriptor actual = list.get(0);
        Assert.assertTrue(expectedMap.containsKey(actual.getId()));
        Assert.assertEquals(expectedMap.get(actual.getId()), actual);
        expectedMap.remove(actual.getId());

        ResponseEntity<Page<AssetAdministrationShellDescriptor>> response2 = restTemplate.exchange(
                createURLWithPort("?limit=1&assetType=" + EncodingHelper.base64UrlEncode(assetType) + "&cursor=" + metadata.getCursor()), HttpMethod.GET, null,
                new ParameterizedTypeReference<Page<AssetAdministrationShellDescriptor>>() {});
        Assert.assertNotNull(response2);
        Assert.assertEquals(HttpStatus.OK, response2.getStatusCode());
        Assert.assertNotNull(response2.getBody());
        list = response2.getBody().getContent();
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());

        actual = list.get(0);
        Assert.assertTrue(expectedMap.containsKey(actual.getId()));
        Assert.assertEquals(expectedMap.get(actual.getId()), actual);
        expectedMap.remove(actual.getId());
        Assert.assertTrue(expectedMap.isEmpty());
    }


    @Test
    public void testAddSubmodel() {
        // create AAS
        AssetAdministrationShellDescriptor original = getAas101();
        createAas(original);

        SubmodelDescriptor newSubmodel = getSubmodel2A();
        checkGetSubmodelNotExist(original.getId(), newSubmodel.getId());

        // update AAS
        AssetAdministrationShellDescriptor expected = getAas101();
        expected.getSubmodels().add(newSubmodel);

        HttpEntity<AssetAdministrationShellDescriptor> entity = new HttpEntity<>(expected);
        ResponseEntity responsePut = restTemplate.exchange(createURLWithPort("/" + EncodingHelper.base64UrlEncode(expected.getId())), HttpMethod.PUT, entity, Void.class);
        Assert.assertNotNull(responsePut);
        Assert.assertEquals(HttpStatus.NO_CONTENT, responsePut.getStatusCode());

        checkGetAas(expected);
        checkGetSubmodel(expected.getId(), newSubmodel);
    }


    private void checkGetAas(AssetAdministrationShellDescriptor expected) {
        ResponseEntity<AssetAdministrationShellDescriptor> response = restTemplate.exchange(
                createURLWithPort("/" + EncodingHelper.base64UrlEncode(expected.getId())), HttpMethod.GET, null, AssetAdministrationShellDescriptor.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        Assert.assertEquals(expected, response.getBody());
    }


    private void checkGetAasNotExist(String id) {
        ResponseEntity<AssetAdministrationShellDescriptor> response = restTemplate.exchange(
                createURLWithPort("/" + EncodingHelper.base64UrlEncode(id)), HttpMethod.GET, null, AssetAdministrationShellDescriptor.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    private void checkGetSubmodel(String aasId, SubmodelDescriptor submodel) {
        ResponseEntity<SubmodelDescriptor> response = restTemplate.exchange(
                createURLWithPort("/" + EncodingHelper.base64UrlEncode(aasId) + "/submodel-descriptors/" + EncodingHelper.base64UrlEncode(submodel.getId())), HttpMethod.GET, null,
                SubmodelDescriptor.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        Assert.assertEquals(submodel, response.getBody());
    }


    private void checkGetSubmodelNotExist(String aasId, String submodelId) {
        ResponseEntity<SubmodelDescriptor> response = restTemplate.exchange(
                createURLWithPort("/" + EncodingHelper.base64UrlEncode(aasId) + "/submodel-descriptors/" + EncodingHelper.base64UrlEncode(submodelId)), HttpMethod.GET, null,
                SubmodelDescriptor.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + "/api/v3.0/shell-descriptors" + uri;
    }


    private void createAas(AssetAdministrationShellDescriptor aas) {
        HttpEntity<AssetAdministrationShellDescriptor> entity = new HttpEntity<>(aas);
        ResponseEntity<AssetAdministrationShellDescriptor> responsePost = restTemplate.exchange(createURLWithPort(""), HttpMethod.POST, entity,
                AssetAdministrationShellDescriptor.class);
        Assert.assertNotNull(responsePost);
        Assert.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode());
        Assert.assertEquals(aas, responsePost.getBody());
    }


    private static AssetAdministrationShellDescriptor getAas() {
        return new DefaultAssetAdministrationShellDescriptor.Builder()
                .idShort("IntegrationTest99")
                .id("http://iosb.fraunhofer.de/IntegrationTest/AAS99")
                .displayName(new DefaultLangStringNameType.Builder().text("Integration Test 99 Name").language("de-DE").build())
                .globalAssetId("http://iosb.fraunhofer.de/GlobalAssetId/IntegrationTest99")
                .assetType("AssetType99")
                .submodel(new DefaultSubmodelDescriptor.Builder()
                        .id("http://iosb.fraunhofer.de/IntegrationTest/Submodel99-1")
                        .idShort("Submodel-99-1")
                        .administration(new DefaultAdministrativeInformation.Builder()
                                .version("1")
                                .revision("12")
                                .build())
                        .endpoint(new DefaultEndpoint.Builder()
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
                .submodel(new DefaultSubmodelDescriptor.Builder()
                        .id("http://iosb.fraunhofer.de/IntegrationTest/Submodel101-1")
                        .idShort("Submodel-101-1")
                        .administration(new DefaultAdministrativeInformation.Builder()
                                .version("2")
                                .revision("15")
                                .build())
                        .endpoint(new DefaultEndpoint.Builder()
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
                .endpoint(new DefaultEndpoint.Builder()
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
}
