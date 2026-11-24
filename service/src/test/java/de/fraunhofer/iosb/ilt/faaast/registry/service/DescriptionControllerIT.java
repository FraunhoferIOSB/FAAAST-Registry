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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.web.servlet.client.assertj.RestTestClientResponse;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@AutoConfigureRestTestClient
class DescriptionControllerIT {

    @LocalServerPort
    private int port;

    //@Autowired
    //private TestRestTemplate restTemplate;

    @Value("${server.servlet.context-path}")
    private String apiPrefix;

    @Test
    void testDescription(@Autowired RestTestClient restClient) {
        assertDescriptions(restClient, "");
    }


    @Test
    void testDescriptionWithSlash(@Autowired RestTestClient restClient) {
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

        RestTestClientResponse response = RestTestClientResponse.from(restClient.get().uri(urlPostfix).exchange());
        response.assertThat().hasStatusOk().bodyJson().convertTo(ServiceDescription.class).isEqualTo(expected);

        //ResponseEntity<ServiceDescription> response = restTemplate.exchange(createURLWithPort(urlPostfix), HttpMethod.GET, null, ServiceDescription.class);
        //Assertions.assertNotNull(response);
        //Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        //Assertions.assertNotNull(response.getBody());
        //Assertions.assertEquals(expected, response.getBody());
    }


    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + apiPrefix + "/description" + uri;
    }
}
