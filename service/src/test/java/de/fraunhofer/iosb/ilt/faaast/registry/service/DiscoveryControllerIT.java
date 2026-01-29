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
import java.util.List;
import java.util.UUID;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestRestTemplate
class DiscoveryControllerIT extends AbstractShellRegistryControllerIT {

    public DiscoveryControllerIT() {
        super(DISCOVERY_PATH);
    }


    @Test
    void getAllAssetLinksByIdWithUnknownAasIdentifier() {
        String identifierB64 = EncodingHelper.base64UrlEncode("my-aas-identifier");
        String urlWithPort = createURLWithPort(String.format("/%s", identifierB64));

        ResponseEntity<Object> response = restTemplate.exchange(urlWithPort, HttpMethod.GET, null, Object.class);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
    }


    @Test
    void getAllAssetLinksByIdWithKnownAasIdentifier() {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        String identifierB64 = EncodingHelper.base64UrlEncode(descriptor.getId());

        String urlWithPort = createURLWithPort(String.format("/%s", identifierB64));

        ResponseEntity<List<SpecificAssetId>> response = getAllAssetLinksById(urlWithPort);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());

        var expected = descriptor.getSpecificAssetIds();

        if (descriptor.getGlobalAssetId() != null) {
            expected.add(new DefaultSpecificAssetId.Builder()
                    .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                    .value(descriptor.getGlobalAssetId())
                    .build());
        }

        Assertions.assertEquals(expected, response.getBody());
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithNoInput() {
        Page<String> expected = Page.of(List.of());

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(createURLWithPort(""));

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(expected.getContent(), response.getBody().getContent());
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithUnknownGlobalAssetId() throws SerializationException {
        Page<String> expected = Page.of();

        var mySpecificAssetIds = List.of(new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(UUID.randomUUID().toString()).build());

        String serializedSpecificAssetIds = new JsonSerializer().write(mySpecificAssetIds);
        String encodedSpecificAssetIds = EncodingHelper.base64UrlEncode(serializedSpecificAssetIds);

        String urlWithPort = createURLWithPort(String.format("?assetIds=%s", encodedSpecificAssetIds));

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(expected.getContent(), response.getBody().getContent());
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithNoArguments() {
        Page<String> expected = Page.of();

        String urlWithPort = createURLWithPort("");

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(expected.getContent(), response.getBody().getContent());
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithMalformedLimit() {
        String urlWithPort = createURLWithPort("?limit=-42");

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithMalformedCursor() {
        String urlWithPort = createURLWithPort("?cursor=NonSensicalCursorArgument");

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithMalformedSpecificAssetIds() {
        String urlWithPort = createURLWithPort("?assetIds=NonSensicalArgument");

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithOneKnownSpecificAssetId() throws SerializationException {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<SpecificAssetId> toFilterFor = List.of(descriptor.getSpecificAssetIds().stream().findAny().orElseThrow());

        String toFilterForEncoded = EncodingHelper.base64UrlEncode(new JsonSerializer().write(toFilterFor));

        String urlWithPort = createURLWithPort(String.format("?assetIds=%s", toFilterForEncoded));

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertNotNull(response.getBody().getContent());
        Assertions.assertEquals(1, response.getBody().getContent().size());
        Assertions.assertEquals(descriptor.getId(), response.getBody().getContent().get(0));
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithOneKnownOneUnknownSpecificAssetId() throws SerializationException {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<SpecificAssetId> toFilterFor = List.of(
                descriptor.getSpecificAssetIds().stream().findAny().orElseThrow(),
                new DefaultSpecificAssetId.Builder().name("Unmatchable Specific Asset Id").value(UUID.randomUUID().toString()).build());

        String toFilterForEncoded = EncodingHelper.base64UrlEncode(new JsonSerializer().write(toFilterFor));

        String urlWithPort = createURLWithPort(String.format("?assetIds=%s", toFilterForEncoded));

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertNotNull(response.getBody().getContent());
        Assertions.assertEquals(0, response.getBody().getContent().size());
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithMatchingGlobalAssetIdAndOneUnknownSpecificAssetId() throws SerializationException {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<SpecificAssetId> toFilterFor = List.of(
                new DefaultSpecificAssetId.Builder().name(FaaastConstants.KEY_GLOBAL_ASSET_ID).value(descriptor.getGlobalAssetId()).build(),
                new DefaultSpecificAssetId.Builder().name("Unmatchable Specific Asset Id").value(UUID.randomUUID().toString()).build());

        String toFilterForEncoded = EncodingHelper.base64UrlEncode(new JsonSerializer().write(toFilterFor));

        String urlWithPort = createURLWithPort(String.format("?assetIds=%s", toFilterForEncoded));

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertNotNull(response.getBody().getContent());
        Assertions.assertEquals(0, response.getBody().getContent().size());
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithMatchingGlobalAssetIdAndOneKnownSpecificAssetId() throws SerializationException {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<SpecificAssetId> toFilterFor = List.of(descriptor.getSpecificAssetIds().stream().findAny().orElseThrow(),
                new DefaultSpecificAssetId.Builder().name(FaaastConstants.KEY_GLOBAL_ASSET_ID).value(descriptor.getGlobalAssetId()).build());

        String toFilterForEncoded = EncodingHelper.base64UrlEncode(new JsonSerializer().write(toFilterFor));

        String urlWithPort = createURLWithPort(String.format("?assetIds=%s", toFilterForEncoded));

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertNotNull(response.getBody().getContent());
        Assertions.assertEquals(1, response.getBody().getContent().size());
        Assertions.assertEquals(descriptor.getId(), response.getBody().getContent().get(0));
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithMatchingGlobalAssetIdAndTwoKnownSpecificAssetId() throws SerializationException {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<SpecificAssetId> toFilterFor = descriptor.getSpecificAssetIds();
        toFilterFor.add(new DefaultSpecificAssetId.Builder().name(FaaastConstants.KEY_GLOBAL_ASSET_ID).value(descriptor.getGlobalAssetId()).build());

        String toFilterForEncoded = EncodingHelper.base64UrlEncode(new JsonSerializer().write(toFilterFor));

        String urlWithPort = createURLWithPort(String.format("?assetIds=%s", toFilterForEncoded));

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertNotNull(response.getBody().getContent());
        Assertions.assertEquals(1, response.getBody().getContent().size());
        Assertions.assertEquals(descriptor.getId(), response.getBody().getContent().get(0));
    }


    @Test
    void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithTwoKnownSpecificAssetId() throws SerializationException {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<SpecificAssetId> toFilterFor = descriptor.getSpecificAssetIds();

        String toFilterForEncoded = EncodingHelper.base64UrlEncode(new JsonSerializer().write(toFilterFor));

        String urlWithPort = createURLWithPort(String.format("?assetIds=%s", toFilterForEncoded));

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertNotNull(response.getBody().getContent());
        Assertions.assertEquals(1, response.getBody().getContent().size());
        Assertions.assertEquals(descriptor.getId(), response.getBody().getContent().get(0));
    }


    @Test
    void postAllAssetLinksByIdWithNewGlobalAssetId() {
        AssetAdministrationShellDescriptor shellDescriptor = getAas();
        createAas(shellDescriptor);

        String urlWithPort = createURLWithPort(String.format("/%s", EncodingHelper.base64UrlEncode(shellDescriptor.getId())));

        String newGlobalAssetId = UUID.randomUUID().toString();
        List<SpecificAssetId> updatedSpecificAssetIds = List.of(new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(newGlobalAssetId)
                .build());

        ResponseEntity<List<SpecificAssetId>> response = postAllAssetLinksById(urlWithPort, updatedSpecificAssetIds);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(newGlobalAssetId, response.getBody().stream()
                .filter(id -> FaaastConstants.KEY_GLOBAL_ASSET_ID.equals(id.getName()))
                .findFirst()
                .orElseThrow()
                .getValue());
        assertSameSpecificAssetIds(shellDescriptor, response.getBody());
    }


    @Test
    void postAllAssetLinksByIdWithUnknownAssetId() {
        AssetAdministrationShellDescriptor shellDescriptor = getAas();
        createAas(shellDescriptor);

        String urlWithPort = createURLWithPort(String.format("/%s", EncodingHelper.base64UrlEncode("unknown")));

        String newGlobalAssetId = UUID.randomUUID().toString();
        List<SpecificAssetId> updatedSpecificAssetIds = List.of(new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(newGlobalAssetId)
                .build());

        ResponseEntity<Object> response = restTemplate.exchange(urlWithPort, HttpMethod.POST, new HttpEntity<>(updatedSpecificAssetIds), Object.class);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
    }


    @Test
    void postAllAssetLinksByIdWithNewSpecificAssetId() {
        AssetAdministrationShellDescriptor shellDescriptor = getAas();
        createAas(shellDescriptor);

        String urlWithPort = createURLWithPort(String.format("/%s", EncodingHelper.base64UrlEncode(shellDescriptor.getId())));

        SpecificAssetId newSpecificAssetId = new DefaultSpecificAssetId.Builder()
                .name(UUID.randomUUID().toString())
                .value(UUID.randomUUID().toString())
                .build();

        List<SpecificAssetId> updatedSpecificAssetIds = List.of(newSpecificAssetId);

        ResponseEntity<List<SpecificAssetId>> response = postAllAssetLinksById(urlWithPort, updatedSpecificAssetIds);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Assertions.assertNotNull(response.getBody());

        Assertions.assertEquals(1,
                response.getBody().stream()
                        .map(SpecificAssetId::getName)
                        .filter(name -> newSpecificAssetId.getName().equals(name))
                        .count());

        Assertions.assertTrue(response.getBody().contains(newSpecificAssetId));

        assertSameGlobalAssetId(shellDescriptor, response.getBody());

        Assertions.assertEquals(shellDescriptor.getSpecificAssetIds(), response.getBody().stream()
                .filter(id -> !FaaastConstants.KEY_GLOBAL_ASSET_ID.equals(id.getName()))
                .filter(id -> !newSpecificAssetId.getName().equals(id.getName()))
                .toList());
    }


    @Test
    void postAllAssetLinksByIdWithUpdatedSpecificAssetId() {
        AssetAdministrationShellDescriptor shellDescriptor = getAas();
        createAas(shellDescriptor);

        String urlWithPort = createURLWithPort(String.format("/%s", EncodingHelper.base64UrlEncode(shellDescriptor.getId())));

        SpecificAssetId newSpecificAssetId = new DefaultSpecificAssetId.Builder()
                .name(shellDescriptor.getSpecificAssetIds().stream().findAny().orElseThrow().getName())
                .value(UUID.randomUUID().toString())
                .build();

        List<SpecificAssetId> updatedSpecificAssetIds = List.of(newSpecificAssetId);

        ResponseEntity<List<SpecificAssetId>> response = postAllAssetLinksById(urlWithPort, updatedSpecificAssetIds);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Assertions.assertNotNull(response.getBody());

        Assertions.assertEquals(1,
                response.getBody().stream()
                        .map(SpecificAssetId::getName)
                        .filter(name -> newSpecificAssetId.getName().equals(name))
                        .count());

        Assertions.assertTrue(response.getBody().contains(newSpecificAssetId));

        Assertions.assertEquals(newSpecificAssetId.getValue(),
                response.getBody().stream()
                        .filter(id -> newSpecificAssetId.getName().equals(id.getName()))
                        .findFirst()
                        .orElseThrow()
                        .getValue());

        assertSameGlobalAssetId(shellDescriptor, response.getBody());
    }


    @Test
    void deleteAllAssetLinksByIdWithKnownAssetId() {
        AssetAdministrationShellDescriptor shellDescriptor = getAas();
        createAas(shellDescriptor);

        String urlWithPort = createURLWithPort(String.format("/%s", EncodingHelper.base64UrlEncode(shellDescriptor.getId())));

        // precondition: specific asset ids are not empty
        ResponseEntity<List<SpecificAssetId>> preconditionResponse = getAllAssetLinksById(urlWithPort);

        Assertions.assertNotNull(preconditionResponse.getBody());
        Assertions.assertFalse(preconditionResponse.getBody().isEmpty());

        ResponseEntity<Void> response = deleteAllAssetLinksById(urlWithPort);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // postcondition: no specific asset ids
        ResponseEntity<List<SpecificAssetId>> postconditionResponse = getAllAssetLinksById(urlWithPort);

        Assertions.assertNotNull(postconditionResponse.getBody());
        Assertions.assertTrue(postconditionResponse.getBody().isEmpty());
    }


    @Test
    void deleteAllAssetLinksByIdWithUnknownAssetId() {
        AssetAdministrationShellDescriptor shellDescriptor = getAas();
        createAas(shellDescriptor);

        String urlWithPort = createURLWithPort(String.format("/%s", EncodingHelper.base64UrlEncode("unknown")));
        ResponseEntity<Void> response = deleteAllAssetLinksById(urlWithPort);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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


    private ResponseEntity<Page<String>> getAllAssetAdministrationShellIdsBySpecificAssetIds(String urlWithPort) {
        return restTemplate.exchange(urlWithPort, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
    }


    private ResponseEntity<List<SpecificAssetId>> getAllAssetLinksById(String urlWithPort) {
        return restTemplate.exchange(urlWithPort, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
    }


    private ResponseEntity<List<SpecificAssetId>> postAllAssetLinksById(String urlWithPort, List<SpecificAssetId> specificAssetIds) {
        return restTemplate.exchange(urlWithPort, HttpMethod.POST, new HttpEntity<>(specificAssetIds), new ParameterizedTypeReference<>() {});
    }


    private ResponseEntity<Void> deleteAllAssetLinksById(String urlWithPort) {
        return restTemplate.exchange(urlWithPort, HttpMethod.DELETE, null, Void.class);
    }
}
