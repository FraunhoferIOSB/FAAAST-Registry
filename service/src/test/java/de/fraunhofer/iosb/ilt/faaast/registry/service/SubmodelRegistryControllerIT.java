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
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import java.util.List;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEndpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProtocolInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelDescriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@AutoConfigureTestRestTemplate
public class SubmodelRegistryControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testGetSubmodels() {
        assertGetSubmodels("");
    }


    @Test
    public void testGetSubmodelsWithSlash() {
        assertGetSubmodels("/");
    }


    @Test
    public void testCreateSubmodel() {
        SubmodelDescriptor expected = getSubmodel();
        createSubmodel(expected);
        checkGetSubmodel(expected);

        ResponseEntity<Page<SubmodelDescriptor>> response = restTemplate.exchange(createURLWithPort(""), HttpMethod.GET, null,
                new ParameterizedTypeReference<Page<SubmodelDescriptor>>() {});
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        var list = response.getBody().getContent();
        Assertions.assertNotNull(list);
        Assertions.assertTrue(list.size() >= 0);

        Optional<SubmodelDescriptor> actual = list.stream().filter(x -> expected.getId().equals(x.getId())).findFirst();
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(expected, actual.get());

        // check that an error is reposrted, when a Submodel shall be created, that already exists
        checkCreateSubmodelError(expected, HttpStatus.CONFLICT);
    }


    @Test
    public void testCreateInvalidSubmodel() {
        SubmodelDescriptor expected = getSubmodelInvalid();
        checkCreateSubmodelError(expected, HttpStatus.BAD_REQUEST);
    }


    @Test
    public void testUpdateDeleteSubmodel() {
        // create Submodel
        SubmodelDescriptor original = getSubmodelUpdate();
        createSubmodel(original);

        // update Submodel
        SubmodelDescriptor expected = getSubmodelUpdate();
        expected.setDescription(List.of(new DefaultLangStringTextType.Builder().language("en-US").text("Submodel 201 updated description").build()));
        expected.getDisplayName().get(0).setText("Submodel 201 Name");

        HttpEntity<SubmodelDescriptor> entity = new HttpEntity<>(expected);
        ResponseEntity responsePut = restTemplate.exchange(createURLWithPort("/" + EncodingHelper.base64UrlEncode(expected.getId())), HttpMethod.PUT, entity, Void.class);
        Assertions.assertNotNull(responsePut);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, responsePut.getStatusCode());

        checkGetSubmodel(expected);

        // delete Submodel
        ResponseEntity responseDelete = restTemplate.exchange(createURLWithPort("/" + EncodingHelper.base64UrlEncode(expected.getId())), HttpMethod.DELETE, entity, Void.class);
        Assertions.assertNotNull(responseDelete);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, responsePut.getStatusCode());

        checkGetAasNotExist(expected.getId());
    }


    @Test
    public void testInvalidLimit() {
        ResponseEntity response = restTemplate.exchange(createURLWithPort("?limit=0"), HttpMethod.GET, null, Void.class);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    private void checkGetSubmodel(SubmodelDescriptor expected) {
        ResponseEntity<SubmodelDescriptor> response = restTemplate.exchange(createURLWithPort("/" + EncodingHelper.base64UrlEncode(expected.getId())), HttpMethod.GET, null,
                SubmodelDescriptor.class);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(expected, response.getBody());
    }


    private void checkGetAasNotExist(String id) {
        ResponseEntity<SubmodelDescriptor> response = restTemplate.exchange(
                createURLWithPort("/" + EncodingHelper.base64UrlEncode(id)), HttpMethod.GET, null, SubmodelDescriptor.class);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    private void createSubmodel(SubmodelDescriptor submodel) {
        HttpEntity<SubmodelDescriptor> entity = new HttpEntity<>(submodel);
        ResponseEntity<SubmodelDescriptor> responsePost = restTemplate.exchange(createURLWithPort(""), HttpMethod.POST, entity, SubmodelDescriptor.class);
        Assertions.assertNotNull(responsePost);
        Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode());
        Assertions.assertEquals(submodel, responsePost.getBody());
    }


    private void checkCreateSubmodelError(SubmodelDescriptor submodel, HttpStatusCode statusCode) {
        HttpEntity<SubmodelDescriptor> entity = new HttpEntity<>(submodel);
        ResponseEntity<SubmodelDescriptor> responsePost = restTemplate.exchange(createURLWithPort(""), HttpMethod.POST, entity, SubmodelDescriptor.class);
        Assertions.assertNotNull(responsePost);
        Assertions.assertEquals(statusCode, responsePost.getStatusCode());
    }


    private SubmodelDescriptor getSubmodel() {
        return new DefaultSubmodelDescriptor.Builder()
                .id("http://iosb.fraunhofer.de/IntegrationTest/Submodel200")
                .idShort("Submodel-200")
                .administration(new DefaultAdministrativeInformation.Builder()
                        .version("2")
                        .revision("27")
                        .build())
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value("http://iosb.fraunhofer.de/IntegrationTest/Submodel200/SemanticId")
                                .build())
                        .build())
                .endpoints(new DefaultEndpoint.Builder()
                        ._interface("http")
                        .protocolInformation(new DefaultProtocolInformation.Builder()
                                .endpointProtocol("http")
                                .href("http://iosb.fraunhofer.de/Endpoints/Submodel200")
                                .endpointProtocolVersion(List.of("2.1"))
                                .build())
                        .build())
                .build();
    }


    private SubmodelDescriptor getSubmodelUpdate() {
        return new DefaultSubmodelDescriptor.Builder()
                .id("http://iosb.fraunhofer.de/IntegrationTest/Submodel201")
                .idShort("Submodel-201")
                .displayName(new DefaultLangStringNameType.Builder().language("de-DE").text("Submodel 201 Name").build())
                .administration(new DefaultAdministrativeInformation.Builder()
                        .version("3")
                        .revision("31")
                        .build())
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value("http://iosb.fraunhofer.de/IntegrationTest/Submodel201/SemanticId")
                                .build())
                        .build())
                .endpoints(new DefaultEndpoint.Builder()
                        ._interface("http")
                        .protocolInformation(new DefaultProtocolInformation.Builder()
                                .endpointProtocol("http")
                                .href("http://iosb.fraunhofer.de/Endpoints/Submodel201")
                                .endpointProtocolVersion(List.of("2.2"))
                                .build())
                        .build())
                .build();
    }


    private SubmodelDescriptor getSubmodelInvalid() {
        return new DefaultSubmodelDescriptor.Builder()
                .idShort("Submodel-Invalid")
                .displayName(new DefaultLangStringNameType.Builder().language("de-DE").text("Submodel Invalid Name").build())
                .build();
    }


    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + "/api/v3.0/submodel-descriptors" + uri;
    }


    private void assertGetSubmodels(String urlPostfix) {
        ResponseEntity<Page<SubmodelDescriptor>> response = restTemplate.exchange(
                createURLWithPort(urlPostfix), HttpMethod.GET, null, new ParameterizedTypeReference<Page<SubmodelDescriptor>>() {});
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        var list = response.getBody().getContent();
        Assertions.assertNotNull(list);
    }
}
