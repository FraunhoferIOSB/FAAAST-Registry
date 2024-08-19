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
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultAssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringNameType;
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

    //private AssetAdministrationShellDescriptor aas1 = new DefaultAssetAdministrationShellDescriptor.Builder().build();

    @Test
    public void testGetAASsEmpty() {
        //Page<AssetAdministrationShellDescriptor> rv = restTemplate.getForObject(createURLWithPort(""), Page.class);
        ResponseEntity<Page<AssetAdministrationShellDescriptor>> response = restTemplate.exchange(
                createURLWithPort(""), HttpMethod.GET, null, new ParameterizedTypeReference<Page<AssetAdministrationShellDescriptor>>() {});
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        var list = response.getBody().getContent();
        Assert.assertNotNull(list);
        Assert.assertEquals(0, list.size());
    }


    @Test
    public void testPostAas() {
        AssetAdministrationShellDescriptor expected = getAas();
        //AssetAdministrationShellDescriptor response = restTemplate.postForObject(createURLWithPort(""), expected, AssetAdministrationShellDescriptor.class);
        HttpEntity<AssetAdministrationShellDescriptor> entity = new HttpEntity<>(expected);
        ResponseEntity<AssetAdministrationShellDescriptor> responsePost = restTemplate.exchange(createURLWithPort(""), HttpMethod.POST, entity,
                AssetAdministrationShellDescriptor.class);
        Assert.assertNotNull(responsePost);
        Assert.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode());
        Assert.assertEquals(expected, responsePost.getBody());
        ResponseEntity<Page<AssetAdministrationShellDescriptor>> response2 = restTemplate.exchange(
                createURLWithPort(""), HttpMethod.GET, null, new ParameterizedTypeReference<Page<AssetAdministrationShellDescriptor>>() {});

        Assert.assertNotNull(response2);
        Assert.assertEquals(HttpStatus.OK, response2.getStatusCode());
        Assert.assertNotNull(response2.getBody());
        var list = response2.getBody().getContent();
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());

        AssetAdministrationShellDescriptor actual = list.get(0);
        Assert.assertEquals(expected, actual);
    }


    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + "/api/v3.0/shell-descriptors" + uri;
    }


    private static AssetAdministrationShellDescriptor getAas() {
        return new DefaultAssetAdministrationShellDescriptor.Builder()
                .idShort("IntegrationTest99")
                .id("http://iosb.fraunhofer.de/IntegrationTest/AAS99")
                .displayName(new DefaultLangStringNameType.Builder().text("Integration Test 99 Name").language("de-DE").build())
                .globalAssetId("http://iosb.fraunhofer.de/GlobalAssetId/IntegrationTest99")
                .assetType("AssetType99")
                .build();
    }
}
