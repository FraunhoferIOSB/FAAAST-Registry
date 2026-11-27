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

import de.fraunhofer.iosb.ilt.faaast.service.model.ServiceDescription;
import de.fraunhofer.iosb.ilt.faaast.service.model.ServiceSpecificationProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@AutoConfigureRestTestClient
class DescriptionControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    RestTestClient restClient;

    @Value("${server.servlet.context-path}")
    private String apiPrefix;

    @Test
    void testDescription() {
        assertDescriptions(restClient, "");
    }


    @Test
    void testDescriptionWithSlash() {
        assertDescriptions(restClient, "/");
    }


    private void assertDescriptions(RestTestClient restClient, String urlPostfix) {
        ServiceDescription expected = ServiceDescription.builder()
                .profile(ServiceSpecificationProfile.AAS_REGISTRY_FULL)
                .profile(ServiceSpecificationProfile.SUBMODEL_REGISTRY_FULL)
                .profile(ServiceSpecificationProfile.DISCOVERY_FULL)
                .profile(ServiceSpecificationProfile.AAS_REGISTRY_BULK)
                .profile(ServiceSpecificationProfile.SUBMODEL_REGISTRY_BULK)
                .build();

        EntityExchangeResult<ServiceDescription> response = restClient.get().uri(createURLWithPort(urlPostfix)).exchange()
                .expectStatus().isOk()
                .expectBody(ServiceDescription.class)
                .returnResult();
        Assertions.assertNotNull(response);
        Assertions.assertEquals(expected, response.getResponseBody());
    }


    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + apiPrefix + "/description" + uri;
    }
}
