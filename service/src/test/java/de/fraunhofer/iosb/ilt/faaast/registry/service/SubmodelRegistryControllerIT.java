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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@AutoConfigureRestTestClient
class SubmodelRegistryControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    RestTestClient restClient;

    @Value("${server.servlet.context-path}")
    private String apiPrefix;

    @Test
    void testGetSubmodels() {
        assertGetSubmodels("");
    }


    @Test
    void testGetSubmodelsWithSlash() {
        assertGetSubmodels("/");
    }


    @Test
    void testCreateSubmodel() {
        SubmodelDescriptor expected = getSubmodel();
        createSubmodel(expected);
        checkGetSubmodel(expected);

        EntityExchangeResult<Page<SubmodelDescriptor>> response = restClient.get().uri(createURLWithPort("")).exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Page<SubmodelDescriptor>>() {}).returnResult();
        Assertions.assertNotNull(response.getResponseBody());
        var list = response.getResponseBody().getContent();
        Assertions.assertNotNull(list);
        Assertions.assertTrue(list.size() >= 0);

        Optional<SubmodelDescriptor> actual = list.stream().filter(x -> expected.getId().equals(x.getId())).findFirst();
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(expected, actual.get());

        // check that an error is reposrted, when a Submodel shall be created, that already exists
        checkCreateSubmodelError(expected, HttpStatus.CONFLICT);
    }


    @Test
    void testCreateInvalidSubmodel() {
        SubmodelDescriptor expected = getSubmodelInvalid();
        // Workaround for an erroneous Codacy warning
        Assertions.assertNotNull(expected);
        checkCreateSubmodelError(expected, HttpStatus.BAD_REQUEST);
    }


    @Test
    void testUpdateDeleteSubmodel() {
        // create Submodel
        SubmodelDescriptor original = getSubmodelUpdate();
        createSubmodel(original);

        // update Submodel
        SubmodelDescriptor expected = getSubmodelUpdate();
        expected.setDescription(List.of(new DefaultLangStringTextType.Builder().language("en-US").text("Submodel 201 updated description").build()));
        expected.getDisplayName().get(0).setText("Submodel 201 Name");

        restClient.put().uri(createURLWithPort("/" + EncodingHelper.base64UrlEncode(expected.getId()))).body(expected).exchange()
                .expectStatus().isNoContent();

        checkGetSubmodel(expected);

        // delete Submodel
        restClient.delete().uri(createURLWithPort("/" + EncodingHelper.base64UrlEncode(expected.getId()))).exchange()
                .expectStatus().isNoContent();

        checkGetAasNotExist(expected.getId());
    }


    @Test
    void testInvalidLimit() {
        restClient.get().uri(createURLWithPort("?limit=0")).exchange()
                .expectStatus().isBadRequest();
    }


    private void checkGetSubmodel(SubmodelDescriptor expected) {
        EntityExchangeResult<SubmodelDescriptor> response = restClient.get().uri(createURLWithPort("/" + EncodingHelper.base64UrlEncode(expected.getId()))).exchange()
                .expectStatus().isOk()
                .expectBody(SubmodelDescriptor.class).returnResult();
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertEquals(expected, response.getResponseBody());
    }


    private void checkGetAasNotExist(String id) {
        restClient.get().uri(createURLWithPort("/" + EncodingHelper.base64UrlEncode(id))).exchange()
                .expectStatus().isNotFound();
    }


    private void createSubmodel(SubmodelDescriptor submodel) {
        EntityExchangeResult<SubmodelDescriptor> responsePost = restClient.post().uri(createURLWithPort("")).body(submodel).exchange()
                .expectStatus().isCreated()
                .expectBody(SubmodelDescriptor.class)
                .returnResult();
        Assertions.assertEquals(submodel, responsePost.getResponseBody());
    }


    private void checkCreateSubmodelError(SubmodelDescriptor submodel, HttpStatusCode statusCode) {
        restClient.post().uri(createURLWithPort("")).body(submodel).exchange()
                .expectStatus().isEqualTo(statusCode);
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
        return "http://localhost:" + port + apiPrefix + "/submodel-descriptors" + uri;
    }


    private void assertGetSubmodels(String urlPostfix) {
        EntityExchangeResult<Page<SubmodelDescriptor>> response = restClient.get().uri(createURLWithPort(urlPostfix)).exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Page<SubmodelDescriptor>>() {}).returnResult();
        Assertions.assertNotNull(response.getResponseBody());
        var list = response.getResponseBody().getContent();
        Assertions.assertNotNull(list);
    }
}
