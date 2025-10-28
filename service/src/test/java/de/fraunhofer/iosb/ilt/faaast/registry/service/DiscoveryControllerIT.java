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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class DiscoveryControllerIT extends AbstractShellRegistryControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    public DiscoveryControllerIT() {
        super(DISCOVERY_PATH);
    }


    @Test
    public void getAllAssetLinksByIdWithUnknownAasIdentifier() {
        String identifierB64 = EncodingHelper.base64UrlEncode("my-aas-identifier");
        String urlWithPort = createURLWithPort(String.format("/%s", identifierB64));

        ResponseEntity<Object> response = restTemplate.exchange(urlWithPort, HttpMethod.GET, null, Object.class);

        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
    }


    @Test
    public void getAllAssetLinksByIdWithKnownAasIdentifier() {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        String identifierB64 = EncodingHelper.base64UrlEncode(descriptor.getId());

        String urlWithPort = createURLWithPort(String.format("/%s", identifierB64));

        ResponseEntity<List<SpecificAssetId>> response = getAllAssetLinksById(urlWithPort);

        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());

        var expected = descriptor.getSpecificAssetIds();

        if (descriptor.getGlobalAssetId() != null) {
            expected.add(new DefaultSpecificAssetId.Builder()
                    .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                    .value(descriptor.getGlobalAssetId())
                    .build());
        }

        Assert.assertEquals(expected, response.getBody());
    }


    @Test
    public void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithNoInput() {
        Page<String> expected = Page.of(List.of());

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(createURLWithPort(""));

        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        Assert.assertEquals(expected.getContent(), response.getBody().getContent());
    }


    @Test
    public void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithUnknownGlobalAssetId() throws SerializationException {
        Page<String> expected = Page.of();

        var mySpecificAssetIds = List.of(new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(UUID.randomUUID().toString()).build());

        String serializedSpecificAssetIds = new JsonSerializer().write(mySpecificAssetIds);
        String encodedSpecificAssetIds = EncodingHelper.base64UrlEncode(serializedSpecificAssetIds);

        String urlWithPort = createURLWithPort(String.format("?assetIds=%s", encodedSpecificAssetIds));

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);

        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        Assert.assertEquals(expected.getContent(), response.getBody().getContent());
    }


    @Test
    public void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithNoArguments() {
        Page<String> expected = Page.of();

        String urlWithPort = createURLWithPort("");

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);

        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        Assert.assertEquals(expected.getContent(), response.getBody().getContent());
    }


    @Test
    public void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithMalformedLimit() {
        String urlWithPort = createURLWithPort("?limit=-42");

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);

        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    public void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithMalformedCursor() {
        String urlWithPort = createURLWithPort("?cursor=NonSensicalCursorArgument");

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);

        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    public void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithMalformedSpecificAssetIds() {
        String urlWithPort = createURLWithPort("?assetIds=NonSensicalArgument");

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    public void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithOneKnownSpecificAssetId() throws SerializationException {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<SpecificAssetId> toFilterFor = List.of(descriptor.getSpecificAssetIds().stream().findAny().orElseThrow());

        String toFilterForEncoded = EncodingHelper.base64UrlEncode(new JsonSerializer().write(toFilterFor));

        String urlWithPort = createURLWithPort(String.format("?assetIds=%s", toFilterForEncoded));

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        Assert.assertNotNull(response.getBody().getContent());
        Assert.assertEquals(1, response.getBody().getContent().size());
        Assert.assertEquals(descriptor.getId(), response.getBody().getContent().get(0));
    }


    @Test
    public void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithOneKnownOneUnknownSpecificAssetId() throws SerializationException {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<SpecificAssetId> toFilterFor = List.of(
                descriptor.getSpecificAssetIds().stream().findAny().orElseThrow(),
                new DefaultSpecificAssetId.Builder().name("Unmatchable Specific Asset Id").value(UUID.randomUUID().toString()).build());

        String toFilterForEncoded = EncodingHelper.base64UrlEncode(new JsonSerializer().write(toFilterFor));

        String urlWithPort = createURLWithPort(String.format("?assetIds=%s", toFilterForEncoded));

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        Assert.assertNotNull(response.getBody().getContent());
        Assert.assertEquals(0, response.getBody().getContent().size());
    }


    @Test
    public void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithMatchingGlobalAssetIdAndOneUnknownSpecificAssetId() throws SerializationException {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<SpecificAssetId> toFilterFor = List.of(
                new DefaultSpecificAssetId.Builder().name(FaaastConstants.KEY_GLOBAL_ASSET_ID).value(descriptor.getGlobalAssetId()).build(),
                new DefaultSpecificAssetId.Builder().name("Unmatchable Specific Asset Id").value(UUID.randomUUID().toString()).build());

        String toFilterForEncoded = EncodingHelper.base64UrlEncode(new JsonSerializer().write(toFilterFor));

        String urlWithPort = createURLWithPort(String.format("?assetIds=%s", toFilterForEncoded));

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        Assert.assertNotNull(response.getBody().getContent());
        Assert.assertEquals(0, response.getBody().getContent().size());
    }


    @Test
    public void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithMatchingGlobalAssetIdAndOneKnownSpecificAssetId() throws SerializationException {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<SpecificAssetId> toFilterFor = List.of(
                descriptor.getSpecificAssetIds().stream().findAny().orElseThrow(),
                new DefaultSpecificAssetId.Builder().name(FaaastConstants.KEY_GLOBAL_ASSET_ID).value(descriptor.getGlobalAssetId()).build());

        String toFilterForEncoded = EncodingHelper.base64UrlEncode(new JsonSerializer().write(toFilterFor));

        String urlWithPort = createURLWithPort(String.format("?assetIds=%s", toFilterForEncoded));

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        Assert.assertNotNull(response.getBody().getContent());
        Assert.assertEquals(1, response.getBody().getContent().size());
        Assert.assertEquals(descriptor.getId(), response.getBody().getContent().get(0));
    }


    @Test
    public void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithMatchingGlobalAssetIdAndTwoKnownSpecificAssetId() throws SerializationException {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<SpecificAssetId> toFilterFor = descriptor.getSpecificAssetIds();
        toFilterFor.add(new DefaultSpecificAssetId.Builder().name(FaaastConstants.KEY_GLOBAL_ASSET_ID).value(descriptor.getGlobalAssetId()).build());

        String toFilterForEncoded = EncodingHelper.base64UrlEncode(new JsonSerializer().write(toFilterFor));

        String urlWithPort = createURLWithPort(String.format("?assetIds=%s", toFilterForEncoded));

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        Assert.assertNotNull(response.getBody().getContent());
        Assert.assertEquals(1, response.getBody().getContent().size());
        Assert.assertEquals(descriptor.getId(), response.getBody().getContent().get(0));
    }


    @Test
    public void getAllAssetAdministrationShellIdsBySpecificAssetIdsWithTwoKnownSpecificAssetId() throws SerializationException {
        AssetAdministrationShellDescriptor descriptor = getAas();
        createAas(descriptor);

        List<SpecificAssetId> toFilterFor = descriptor.getSpecificAssetIds();

        String toFilterForEncoded = EncodingHelper.base64UrlEncode(new JsonSerializer().write(toFilterFor));

        String urlWithPort = createURLWithPort(String.format("?assetIds=%s", toFilterForEncoded));

        ResponseEntity<Page<String>> response = getAllAssetAdministrationShellIdsBySpecificAssetIds(urlWithPort);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        Assert.assertNotNull(response.getBody().getContent());
        Assert.assertEquals(1, response.getBody().getContent().size());
        Assert.assertEquals(descriptor.getId(), response.getBody().getContent().get(0));
    }


    @Test
    public void postAllAssetLinksByIdWithNewGlobalAssetId() {
        AssetAdministrationShellDescriptor shellDescriptor = getAas();
        createAas(shellDescriptor);

        String urlWithPort = createURLWithPort(String.format("/%s", EncodingHelper.base64UrlEncode(shellDescriptor.getId())));

        String newGlobalAssetId = UUID.randomUUID().toString();
        List<SpecificAssetId> updatedSpecificAssetIds = List.of(new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(newGlobalAssetId)
                .build());

        ResponseEntity<List<SpecificAssetId>> response = postAllAssetLinksById(urlWithPort, updatedSpecificAssetIds);

        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Assert.assertNotNull(response.getBody());
        Assert.assertEquals(newGlobalAssetId, response.getBody().stream()
                .filter(id -> FaaastConstants.KEY_GLOBAL_ASSET_ID.equals(id.getName()))
                .findFirst()
                .orElseThrow()
                .getValue());
        assertSameSpecificAssetIds(shellDescriptor, response.getBody());
    }


    @Test
    public void postAllAssetLinksByIdWithUnknownAssetId() {
        AssetAdministrationShellDescriptor shellDescriptor = getAas();
        createAas(shellDescriptor);

        String urlWithPort = createURLWithPort(String.format("/%s", EncodingHelper.base64UrlEncode("unknown")));

        String newGlobalAssetId = UUID.randomUUID().toString();
        List<SpecificAssetId> updatedSpecificAssetIds = List.of(new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(newGlobalAssetId)
                .build());

        ResponseEntity<Object> response = restTemplate.exchange(urlWithPort, HttpMethod.POST, new HttpEntity<>(updatedSpecificAssetIds),
                Object.class);

        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
    }


    @Test
    public void postAllAssetLinksByIdWithNewSpecificAssetId() {
        AssetAdministrationShellDescriptor shellDescriptor = getAas();
        createAas(shellDescriptor);

        String urlWithPort = createURLWithPort(String.format("/%s", EncodingHelper.base64UrlEncode(shellDescriptor.getId())));

        SpecificAssetId newSpecificAssetId = new DefaultSpecificAssetId.Builder()
                .name(UUID.randomUUID().toString())
                .value(UUID.randomUUID().toString())
                .build();

        List<SpecificAssetId> updatedSpecificAssetIds = List.of(newSpecificAssetId);

        ResponseEntity<List<SpecificAssetId>> response = postAllAssetLinksById(urlWithPort, updatedSpecificAssetIds);

        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Assert.assertNotNull(response.getBody());

        Assert.assertEquals(1,
                response.getBody().stream()
                        .map(SpecificAssetId::getName)
                        .filter(name -> newSpecificAssetId.getName().equals(name))
                        .count());

        Assert.assertTrue(response.getBody().contains(newSpecificAssetId));

        assertSameGlobalAssetId(shellDescriptor, response.getBody());

        Assert.assertEquals(shellDescriptor.getSpecificAssetIds(), response.getBody().stream()
                .filter(id -> !FaaastConstants.KEY_GLOBAL_ASSET_ID.equals(id.getName()))
                .filter(id -> !newSpecificAssetId.getName().equals(id.getName()))
                .toList());
    }


    @Test
    public void postAllAssetLinksByIdWithUpdatedSpecificAssetId() {
        AssetAdministrationShellDescriptor shellDescriptor = getAas();
        createAas(shellDescriptor);

        String urlWithPort = createURLWithPort(String.format("/%s", EncodingHelper.base64UrlEncode(shellDescriptor.getId())));

        SpecificAssetId newSpecificAssetId = new DefaultSpecificAssetId.Builder()
                .name(shellDescriptor.getSpecificAssetIds().stream().findAny().orElseThrow().getName())
                .value(UUID.randomUUID().toString())
                .build();

        List<SpecificAssetId> updatedSpecificAssetIds = List.of(newSpecificAssetId);

        ResponseEntity<List<SpecificAssetId>> response = postAllAssetLinksById(urlWithPort, updatedSpecificAssetIds);

        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Assert.assertNotNull(response.getBody());

        Assert.assertEquals(1,
                response.getBody().stream()
                        .map(SpecificAssetId::getName)
                        .filter(name -> newSpecificAssetId.getName().equals(name))
                        .count());

        Assert.assertTrue(response.getBody().contains(newSpecificAssetId));

        Assert.assertEquals(newSpecificAssetId.getValue(),
                response.getBody().stream()
                        .filter(id -> newSpecificAssetId.getName().equals(id.getName()))
                        .findFirst()
                        .orElseThrow()
                        .getValue());

        assertSameGlobalAssetId(shellDescriptor, response.getBody());
    }


    @Test
    public void deleteAllAssetLinksByIdWithKnownAssetId() {
        AssetAdministrationShellDescriptor shellDescriptor = getAas();
        createAas(shellDescriptor);

        String urlWithPort = createURLWithPort(String.format("/%s", EncodingHelper.base64UrlEncode(shellDescriptor.getId())));

        // precondition: specific asset ids are not empty
        ResponseEntity<List<SpecificAssetId>> preconditionResponse = getAllAssetLinksById(urlWithPort);

        Assert.assertNotNull(preconditionResponse.getBody());
        Assert.assertFalse(preconditionResponse.getBody().isEmpty());

        ResponseEntity<Void> response = deleteAllAssetLinksById(urlWithPort);

        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // postcondition: no specific asset ids
        ResponseEntity<List<SpecificAssetId>> postconditionResponse = getAllAssetLinksById(urlWithPort);

        Assert.assertNotNull(postconditionResponse.getBody());
        Assert.assertTrue(postconditionResponse.getBody().isEmpty());
    }


    @Test
    public void deleteAllAssetLinksByIdWithUnknownAssetId() {
        AssetAdministrationShellDescriptor shellDescriptor = getAas();
        createAas(shellDescriptor);

        String urlWithPort = createURLWithPort(String.format("/%s", EncodingHelper.base64UrlEncode("unknown")));
        ResponseEntity<Void> response = deleteAllAssetLinksById(urlWithPort);

        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    private void assertSameGlobalAssetId(AssetAdministrationShellDescriptor expected, List<SpecificAssetId> actual) {
        Assert.assertEquals(expected.getGlobalAssetId(), actual.stream()
                .filter(id -> FaaastConstants.KEY_GLOBAL_ASSET_ID.equals(id.getName()))
                .findFirst()
                .orElseThrow()
                .getValue());
    }


    private void assertSameSpecificAssetIds(AssetAdministrationShellDescriptor expected, List<SpecificAssetId> actual) {
        Assert.assertEquals(expected.getSpecificAssetIds(), actual.stream()
                .filter(id -> !FaaastConstants.KEY_GLOBAL_ASSET_ID.equals(id.getName())).toList());
    }


    private ResponseEntity<Page<String>> getAllAssetAdministrationShellIdsBySpecificAssetIds(String urlWithPort) {
        return restTemplate.exchange(urlWithPort, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
    }


    private ResponseEntity<List<SpecificAssetId>> getAllAssetLinksById(String urlWithPort) {
        return restTemplate.exchange(urlWithPort, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
    }


    private ResponseEntity<List<SpecificAssetId>> postAllAssetLinksById(String urlWithPort, List<SpecificAssetId> specificAssetIds) {
        return restTemplate.exchange(urlWithPort, HttpMethod.POST, new HttpEntity<>(specificAssetIds),
                new ParameterizedTypeReference<>() {});
    }


    private ResponseEntity<Void> deleteAllAssetLinksById(String urlWithPort) {
        return restTemplate.exchange(urlWithPort, HttpMethod.DELETE, null,
                Void.class);
    }
}
