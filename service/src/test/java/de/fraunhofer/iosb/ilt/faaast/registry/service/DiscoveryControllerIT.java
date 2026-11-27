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

import static de.fraunhofer.iosb.ilt.faaast.registry.service.helper.Constants.DISCOVERY_PATH;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.FaaastConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.ExchangeResult;


// @ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureRestTestClient
class DiscoveryControllerIT extends AbstractShellRegistryControllerIT {

    public DiscoveryControllerIT() {
        super(DISCOVERY_PATH);
    }


    @Test
    void getAllAssetLinksByIdWithUnknownAasIdentifier() {
        String identifierB64 = EncodingHelper.base64UrlEncode("my-aas-identifier");
        String urlWithPort = createURLWithPort(String.format("/shells/%s", identifierB64));

        restClient.get().uri(urlWithPort).exchange()
                .expectStatus().isNotFound();
        //ResponseEntity<Object> response = restTemplate.exchange(urlWithPort, HttpMethod.GET, null, Object.class);

        //Assertions.assertNotNull(response);
        //Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        //Assertions.assertNotNull(response.getResponseBody());
    }


    @Test
    void getAllAssetLinksByIdWithKnownAasIdentifier() {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        String identifierB64 = EncodingHelper.base64UrlEncode(descriptor.getId());

        String urlWithPort = createURLWithPort(String.format("/shells/%s", identifierB64));

        EntityExchangeResult<List<SpecificAssetId>> response = getAllAssetLinksById(urlWithPort);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());

        var expected = descriptor.getSpecificAssetIds();

        if (descriptor.getGlobalAssetId() != null) {
            expected.add(new DefaultSpecificAssetId.Builder()
                    .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                    .value(descriptor.getGlobalAssetId())
                    .build());
        }

        Assertions.assertEquals(expected, response.getResponseBody());
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithNoInput() {
        Page<String> expected = Page.of(List.of());

        EntityExchangeResult<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(createURLWithPort("/shells"));

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertEquals(expected.getContent(), response.getResponseBody().getContent());
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithUnknownGlobalAssetId() throws SerializationException {
        Page<String> expected = Page.of();

        var mySpecificAssetIds = List.of(new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(UUID.randomUUID().toString()).build());

        String serializedSpecificAssetIds = new JsonSerializer().write(mySpecificAssetIds);
        String encodedSpecificAssetIds = EncodingHelper.base64UrlEncode(serializedSpecificAssetIds);

        String urlWithPort = createURLWithPort(String.format("/shells?assetIds=%s", encodedSpecificAssetIds));

        EntityExchangeResult<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertEquals(expected.getContent(), response.getResponseBody().getContent());
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithNoArguments() {
        Page<String> expected = Page.of();

        String urlWithPort = createURLWithPort("/shells");

        EntityExchangeResult<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertEquals(expected.getContent(), response.getResponseBody().getContent());
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithMalformedLimit() {
        String urlWithPort = createURLWithPort("/shells?limit=-42");

        EntityExchangeResult<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithMalformedCursor() {
        String urlWithPort = createURLWithPort("/shells?cursor=NonSensicalCursorArgument");

        EntityExchangeResult<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithMalformedSpecificAssetIds() {
        String urlWithPort = createURLWithPort("/shells?assetIds=NonSensicalArgument");

        EntityExchangeResult<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithOneKnownSpecificAssetId() throws SerializationException {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<SpecificAssetId> toFilterFor = List.of(descriptor.getSpecificAssetIds().stream().findAny().orElseThrow());

        String toFilterForEncoded = EncodingHelper.base64UrlEncode(new JsonSerializer().write(toFilterFor));

        String urlWithPort = createURLWithPort(String.format("/shells?assetIds=%s", toFilterForEncoded));

        EntityExchangeResult<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertNotNull(response.getResponseBody().getContent());
        Assertions.assertEquals(1, response.getResponseBody().getContent().size());
        Assertions.assertEquals(descriptor.getId(), response.getResponseBody().getContent().get(0));
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithOneKnownOneUnknownSpecificAssetId() throws SerializationException {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<SpecificAssetId> toFilterFor = List.of(
                descriptor.getSpecificAssetIds().stream().findAny().orElseThrow(),
                new DefaultSpecificAssetId.Builder().name("Unmatchable Specific Asset Id").value(UUID.randomUUID().toString()).build());

        String toFilterForEncoded = EncodingHelper.base64UrlEncode(new JsonSerializer().write(toFilterFor));

        String urlWithPort = createURLWithPort(String.format("/shells?assetIds=%s", toFilterForEncoded));

        EntityExchangeResult<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertNotNull(response.getResponseBody().getContent());
        Assertions.assertEquals(0, response.getResponseBody().getContent().size());
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithMatchingGlobalAssetIdAndOneUnknownSpecificAssetId() throws SerializationException {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<SpecificAssetId> toFilterFor = List.of(
                new DefaultSpecificAssetId.Builder().name(FaaastConstants.KEY_GLOBAL_ASSET_ID).value(descriptor.getGlobalAssetId()).build(),
                new DefaultSpecificAssetId.Builder().name("Unmatchable Specific Asset Id").value(UUID.randomUUID().toString()).build());

        String toFilterForEncoded = EncodingHelper.base64UrlEncode(new JsonSerializer().write(toFilterFor));

        String urlWithPort = createURLWithPort(String.format("/shells?assetIds=%s", toFilterForEncoded));

        EntityExchangeResult<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertNotNull(response.getResponseBody().getContent());
        Assertions.assertEquals(0, response.getResponseBody().getContent().size());
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithMatchingGlobalAssetIdAndOneKnownSpecificAssetId() throws SerializationException {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<SpecificAssetId> toFilterFor = List.of(descriptor.getSpecificAssetIds().stream().findAny().orElseThrow(),
                new DefaultSpecificAssetId.Builder().name(FaaastConstants.KEY_GLOBAL_ASSET_ID).value(descriptor.getGlobalAssetId()).build());

        String toFilterForEncoded = EncodingHelper.base64UrlEncode(new JsonSerializer().write(toFilterFor));

        String urlWithPort = createURLWithPort(String.format("/shells?assetIds=%s", toFilterForEncoded));

        EntityExchangeResult<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertNotNull(response.getResponseBody().getContent());
        Assertions.assertEquals(1, response.getResponseBody().getContent().size());
        Assertions.assertEquals(descriptor.getId(), response.getResponseBody().getContent().get(0));
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithMatchingGlobalAssetIdAndTwoKnownSpecificAssetId() throws SerializationException {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<SpecificAssetId> toFilterFor = descriptor.getSpecificAssetIds();
        toFilterFor.add(new DefaultSpecificAssetId.Builder().name(FaaastConstants.KEY_GLOBAL_ASSET_ID).value(descriptor.getGlobalAssetId()).build());

        String toFilterForEncoded = EncodingHelper.base64UrlEncode(new JsonSerializer().write(toFilterFor));

        String urlWithPort = createURLWithPort(String.format("/shells?assetIds=%s", toFilterForEncoded));

        EntityExchangeResult<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertNotNull(response.getResponseBody().getContent());
        Assertions.assertEquals(1, response.getResponseBody().getContent().size());
        Assertions.assertEquals(descriptor.getId(), response.getResponseBody().getContent().get(0));
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithTwoKnownSpecificAssetId() throws SerializationException {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<SpecificAssetId> toFilterFor = descriptor.getSpecificAssetIds();

        String toFilterForEncoded = EncodingHelper.base64UrlEncode(new JsonSerializer().write(toFilterFor));

        String urlWithPort = createURLWithPort(String.format("/shells?assetIds=%s", toFilterForEncoded));

        EntityExchangeResult<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertNotNull(response.getResponseBody().getContent());
        Assertions.assertEquals(1, response.getResponseBody().getContent().size());
        Assertions.assertEquals(descriptor.getId(), response.getResponseBody().getContent().get(0));
    }


    @Test
    void postAllAssetLinksByIdWithNewGlobalAssetId() {
        AssetAdministrationShellDescriptor shellDescriptor = getAas();
        createAas(shellDescriptor);

        String urlWithPort = createURLWithPort(String.format("/shells/%s", EncodingHelper.base64UrlEncode(shellDescriptor.getId())));

        String newGlobalAssetId = UUID.randomUUID().toString();
        List<SpecificAssetId> updatedSpecificAssetIds = List.of(new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(newGlobalAssetId)
                .build());

        EntityExchangeResult<List<SpecificAssetId>> response = postAllAssetLinksById(urlWithPort, updatedSpecificAssetIds);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());

        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertEquals(newGlobalAssetId, response.getResponseBody().stream()
                .filter(id -> FaaastConstants.KEY_GLOBAL_ASSET_ID.equals(id.getName()))
                .findFirst()
                .orElseThrow()
                .getValue());
        assertSameSpecificAssetIds(shellDescriptor, response.getResponseBody());
    }


    @Test
    void postAllAssetLinksByIdWithUnknownAssetId() {
        AssetAdministrationShellDescriptor shellDescriptor = getAas();
        createAas(shellDescriptor);

        String urlWithPort = createURLWithPort(String.format("/shells/%s", EncodingHelper.base64UrlEncode("unknown")));

        String newGlobalAssetId = UUID.randomUUID().toString();
        List<SpecificAssetId> updatedSpecificAssetIds = List.of(new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(newGlobalAssetId)
                .build());

        restClient.post().uri(urlWithPort).body(updatedSpecificAssetIds).exchange()
                .expectStatus().isNotFound();

        //ResponseEntity<Object> response = restTemplate.exchange(urlWithPort, HttpMethod.POST, new HttpEntity<>(updatedSpecificAssetIds), Object.class);

        //Assertions.assertNotNull(response);
        //Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        //Assertions.assertNotNull(response.getBody());
    }


    @Test
    void postAllAssetLinksByIdWithNewSpecificAssetId() {
        AssetAdministrationShellDescriptor shellDescriptor = getAas();
        createAas(shellDescriptor);

        String urlWithPort = createURLWithPort(String.format("/shells/%s", EncodingHelper.base64UrlEncode(shellDescriptor.getId())));

        SpecificAssetId newSpecificAssetId = new DefaultSpecificAssetId.Builder()
                .name(UUID.randomUUID().toString())
                .value(UUID.randomUUID().toString())
                .build();

        List<SpecificAssetId> updatedSpecificAssetIds = List.of(newSpecificAssetId);

        EntityExchangeResult<List<SpecificAssetId>> response = postAllAssetLinksById(urlWithPort, updatedSpecificAssetIds);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());

        Assertions.assertNotNull(response.getResponseBody());

        Assertions.assertEquals(1,
                response.getResponseBody().stream()
                        .map(SpecificAssetId::getName)
                        .filter(name -> newSpecificAssetId.getName().equals(name))
                        .count());

        Assertions.assertTrue(response.getResponseBody().contains(newSpecificAssetId));

        assertSameGlobalAssetId(shellDescriptor, response.getResponseBody());

        Assertions.assertEquals(shellDescriptor.getSpecificAssetIds(), response.getResponseBody().stream()
                .filter(id -> !FaaastConstants.KEY_GLOBAL_ASSET_ID.equals(id.getName()))
                .filter(id -> !newSpecificAssetId.getName().equals(id.getName()))
                .toList());
    }


    @Test
    void postAllAssetLinksByIdWithUpdatedSpecificAssetId() {
        AssetAdministrationShellDescriptor shellDescriptor = getAas();
        createAas(shellDescriptor);

        String urlWithPort = createURLWithPort(String.format("/shells/%s", EncodingHelper.base64UrlEncode(shellDescriptor.getId())));

        SpecificAssetId newSpecificAssetId = new DefaultSpecificAssetId.Builder()
                .name(shellDescriptor.getSpecificAssetIds().stream().findAny().orElseThrow().getName())
                .value(UUID.randomUUID().toString())
                .build();

        List<SpecificAssetId> updatedSpecificAssetIds = List.of(newSpecificAssetId);

        EntityExchangeResult<List<SpecificAssetId>> response = postAllAssetLinksById(urlWithPort, updatedSpecificAssetIds);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());

        Assertions.assertNotNull(response.getResponseBody());

        Assertions.assertEquals(1,
                response.getResponseBody().stream()
                        .map(SpecificAssetId::getName)
                        .filter(name -> newSpecificAssetId.getName().equals(name))
                        .count());

        Assertions.assertTrue(response.getResponseBody().contains(newSpecificAssetId));

        Assertions.assertEquals(newSpecificAssetId.getValue(),
                response.getResponseBody().stream()
                        .filter(id -> newSpecificAssetId.getName().equals(id.getName()))
                        .findFirst()
                        .orElseThrow()
                        .getValue());

        assertSameGlobalAssetId(shellDescriptor, response.getResponseBody());
    }


    @Test
    void deleteAllAssetLinksByIdWithKnownAssetId() {
        AssetAdministrationShellDescriptor shellDescriptor = getAas();
        createAas(shellDescriptor);

        String urlWithPort = createURLWithPort(String.format("/shells/%s", EncodingHelper.base64UrlEncode(shellDescriptor.getId())));

        // precondition: specific asset ids are not empty
        EntityExchangeResult<List<SpecificAssetId>> preconditionResponse = getAllAssetLinksById(urlWithPort);

        Assertions.assertNotNull(preconditionResponse.getResponseBody());
        Assertions.assertFalse(preconditionResponse.getResponseBody().isEmpty());

        ExchangeResult response = deleteAllAssetLinksById(urlWithPort);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatus());

        // postcondition: no specific asset ids
        EntityExchangeResult<List<SpecificAssetId>> postconditionResponse = getAllAssetLinksById(urlWithPort);

        Assertions.assertNotNull(postconditionResponse.getResponseBody());
        Assertions.assertTrue(postconditionResponse.getResponseBody().isEmpty());
    }


    @Test
    void deleteAllAssetLinksByIdWithUnknownAssetId() {
        AssetAdministrationShellDescriptor shellDescriptor = getAas();
        createAas(shellDescriptor);

        String urlWithPort = createURLWithPort(String.format("/shells/%s", EncodingHelper.base64UrlEncode("unknown")));
        ExchangeResult response = deleteAllAssetLinksById(urlWithPort);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
    }


    @Test
    void searchAllAssetAdministrationShellIdsBySpecificAssetIdsWithNoInput() {
        Page<String> expected = Page.of(List.of());

        EntityExchangeResult<Page<String>> response = searchAllAssetAdministrationShellIdsBySpecificAssetIds(createURLWithPort("/shellsByAssetLink"), new ArrayList<>());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertEquals(expected.getContent(), response.getResponseBody().getContent());
    }


    @Test
    void searchAllAssetAdministrationShellIdsBySpecificAssetIdsWithUnknownGlobalAssetId() {
        Page<String> expected = Page.of();

        var mySpecificAssetIds = List.of(new AssetLinkHelper(FaaastConstants.KEY_GLOBAL_ASSET_ID, UUID.randomUUID().toString()));

        String urlWithPort = createURLWithPort("/shellsByAssetLink");

        EntityExchangeResult<Page<String>> response = searchAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort, mySpecificAssetIds);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertEquals(expected.getContent(), response.getResponseBody().getContent());
    }


    @Test
    void searchAllAssetAdministrationShellIdsBySpecificAssetIdsWithoutBody() {
        Page<String> expected = Page.of();

        String urlWithPort = createURLWithPort("/shellsByAssetLink");

        //EntityExchangeResult<Page<String>> response = searchAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort, null);
        EntityExchangeResult<Page<String>> response = restClient.post().uri(urlWithPort).exchange().expectBody(new ParameterizedTypeReference<Page<String>>() {}).returnResult();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertEquals(expected.getContent(), response.getResponseBody().getContent());
    }


    @Test
    void searchAllAssetAdministrationShellIdsBySpecificAssetIdsWithMalformedLimit() {
        String urlWithPort = createURLWithPort("/shellsByAssetLink?limit=-42");

        EntityExchangeResult<Page<String>> response = searchAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort, new ArrayList<>());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
    }


    @Test
    void searchAllAssetAdministrationShellIdsBySpecificAssetIdsWithMalformedCursor() {
        String urlWithPort = createURLWithPort("/shellsByAssetLink?cursor=NonSensicalCursorArgument");

        EntityExchangeResult<Page<String>> response = searchAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort, new ArrayList<>());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    }


    @Test
    void searchAllAssetAdministrationShellIdsBySpecificAssetIdsWithOneKnownSpecificAssetId() {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<AssetLinkHelper> toFilterFor = List.of(AssetLinkHelper.of(descriptor.getSpecificAssetIds().stream().findAny().orElseThrow()));

        String urlWithPort = createURLWithPort("/shellsByAssetLink");

        EntityExchangeResult<Page<String>> response = searchAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort, toFilterFor);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertNotNull(response.getResponseBody().getContent());
        Assertions.assertEquals(1, response.getResponseBody().getContent().size());
        Assertions.assertEquals(descriptor.getId(), response.getResponseBody().getContent().get(0));
    }


    @Test
    void searchAllAssetAdministrationShellIdsBySpecificAssetIdsWithOneKnownOneUnknownSpecificAssetId() {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<AssetLinkHelper> toFilterFor = List.of(
                AssetLinkHelper.of(descriptor.getSpecificAssetIds().stream().findAny().orElseThrow()),
                new AssetLinkHelper("Unmatchable Specific Asset Id", UUID.randomUUID().toString()));

        String urlWithPort = createURLWithPort("/shellsByAssetLink");

        EntityExchangeResult<Page<String>> response = searchAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort, toFilterFor);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertNotNull(response.getResponseBody().getContent());
        Assertions.assertEquals(0, response.getResponseBody().getContent().size());
    }


    @Test
    void searchAllAssetAdministrationShellIdsBySpecificAssetIdsWithMatchingGlobalAssetIdAndOneUnknownSpecificAssetId() {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<AssetLinkHelper> toFilterFor = List.of(
                new AssetLinkHelper(FaaastConstants.KEY_GLOBAL_ASSET_ID, descriptor.getGlobalAssetId()),
                new AssetLinkHelper("Unmatchable Specific Asset Id", UUID.randomUUID().toString()));

        String urlWithPort = createURLWithPort("/shellsByAssetLink");

        EntityExchangeResult<Page<String>> response = searchAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort, toFilterFor);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertNotNull(response.getResponseBody().getContent());
        Assertions.assertEquals(0, response.getResponseBody().getContent().size());
    }


    @Test
    void searchAllAssetAdministrationShellIdsBySpecificAssetIdsWithMatchingGlobalAssetIdAndOneKnownSpecificAssetId() {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<AssetLinkHelper> toFilterFor = List.of(AssetLinkHelper.of(descriptor.getSpecificAssetIds().stream().findAny().orElseThrow()),
                new AssetLinkHelper(FaaastConstants.KEY_GLOBAL_ASSET_ID, descriptor.getGlobalAssetId()));

        String urlWithPort = createURLWithPort("/shellsByAssetLink");

        EntityExchangeResult<Page<String>> response = searchAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort, toFilterFor);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertNotNull(response.getResponseBody().getContent());
        Assertions.assertEquals(1, response.getResponseBody().getContent().size());
        Assertions.assertEquals(descriptor.getId(), response.getResponseBody().getContent().get(0));
    }


    @Test
    void searchAllAssetAdministrationShellIdsBySpecificAssetIdsWithMatchingGlobalAssetIdAndTwoKnownSpecificAssetId() {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<AssetLinkHelper> toFilterFor = new ArrayList<>();
        for (var sai: descriptor.getSpecificAssetIds()) {
            toFilterFor.add(AssetLinkHelper.of(sai));
        }
        toFilterFor.add(new AssetLinkHelper(FaaastConstants.KEY_GLOBAL_ASSET_ID, descriptor.getGlobalAssetId()));

        String urlWithPort = createURLWithPort("/shellsByAssetLink");

        EntityExchangeResult<Page<String>> response = searchAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort, toFilterFor);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertNotNull(response.getResponseBody().getContent());
        Assertions.assertEquals(1, response.getResponseBody().getContent().size());
        Assertions.assertEquals(descriptor.getId(), response.getResponseBody().getContent().get(0));
    }


    @Test
    void searchAllAssetAdministrationShellIdsBySpecificAssetIdsWithTwoKnownSpecificAssetId() {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<AssetLinkHelper> toFilterFor = new ArrayList<>();
        for (var sai: descriptor.getSpecificAssetIds()) {
            toFilterFor.add(AssetLinkHelper.of(sai));
        }

        String urlWithPort = createURLWithPort("/shellsByAssetLink");

        EntityExchangeResult<Page<String>> response = searchAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort, toFilterFor);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertNotNull(response.getResponseBody().getContent());
        Assertions.assertEquals(1, response.getResponseBody().getContent().size());
        Assertions.assertEquals(descriptor.getId(), response.getResponseBody().getContent().get(0));
    }


    private void assertSameGlobalAssetId(AssetAdministrationShellDescriptor expected, List<SpecificAssetId> actual) {
        Assertions.assertEquals(expected.getGlobalAssetId(), actual.stream()
                .filter(id -> FaaastConstants.KEY_GLOBAL_ASSET_ID.equals(id.getName()))
                .findFirst()
                .orElseThrow()
                .getValue());
    }


    private void assertSameSpecificAssetIds(AssetAdministrationShellDescriptor expected, List<SpecificAssetId> actual) {
        Assertions.assertEquals(expected.getSpecificAssetIds(), actual.stream().filter(id -> !FaaastConstants.KEY_GLOBAL_ASSET_ID.equals(id.getName())).toList());
    }


    private EntityExchangeResult<Page<String>> getAllAssetAdministrationShellIdsBySpecificAssetIds(String urlWithPort) {
        //RestTestClientResponse response = RestTestClientResponse.from(restClient.get().uri(urlWithPort).exchange());
        //return restClient.get().uri(urlWithPort).exchange().returnResult(new ParameterizedTypeReference<Page<String>>() {}).getResponseBody();
        return restClient.get().uri(urlWithPort).exchange().expectBody(new ParameterizedTypeReference<Page<String>>() {}).returnResult();
        //response.getExchangeResult().getResponseBodyContent()
        //return restTemplate.exchange(urlWithPort, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
    }


    private EntityExchangeResult<List<SpecificAssetId>> getAllAssetLinksById(String urlWithPort) {
        return restClient.get().uri(urlWithPort).exchange().expectBody(new ParameterizedTypeReference<List<SpecificAssetId>>() {}).returnResult();
        //return restTemplate.exchange(urlWithPort, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
    }


    private EntityExchangeResult<List<SpecificAssetId>> postAllAssetLinksById(String urlWithPort, List<SpecificAssetId> specificAssetIds) {
        return restClient.post().uri(urlWithPort).body(specificAssetIds).exchange().expectBody(new ParameterizedTypeReference<List<SpecificAssetId>>() {}).returnResult();
        //return restTemplate.exchange(urlWithPort, HttpMethod.POST, new HttpEntity<>(specificAssetIds), new ParameterizedTypeReference<>() {});
    }


    private ExchangeResult deleteAllAssetLinksById(String urlWithPort) {
        return restClient.delete().uri(urlWithPort).exchange().returnResult();
        //return restTemplate.exchange(urlWithPort, HttpMethod.DELETE, null, Void.class);
    }


    private EntityExchangeResult<Page<String>> searchAllAssetAdministrationShellIdsBySpecificAssetIds(String urlWithPort, List<AssetLinkHelper> assetLinks) {
        return restClient.post().uri(urlWithPort).body(assetLinks).exchange().expectBody(new ParameterizedTypeReference<Page<String>>() {}).returnResult();
        //return restTemplate.exchange(urlWithPort, HttpMethod.POST, new HttpEntity<>(assetLinks), new ParameterizedTypeReference<>() {});
    }

}
