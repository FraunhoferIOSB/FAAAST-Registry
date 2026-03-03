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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringNameType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest-mult.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ShellMultipleTestIT {

    @LocalServerPort
    protected int port;

    private final HttpClient client;

    public ShellMultipleTestIT() {
        client = HttpClient.newHttpClient();
    }


    @Test
    void testCreateAas() throws SerializationException, InterruptedException, ExecutionException {
        AssetAdministrationShellDescriptor aas1 = getAasMultiple1();
        AssetAdministrationShellDescriptor aas2 = getAasMultiple2();
        String url = "http://localhost:" + port + "/api/v3.0/shell-descriptors";

        var futureResponse1 = doCreateAas(url, aas1);
        var futureResponse2 = doCreateAas(url, aas2);

        HttpResponse<String> response1 = futureResponse1.get();
        Assertions.assertEquals(HttpStatus.CREATED.value(), response1.statusCode());

        HttpResponse<String> response2 = futureResponse2.get();
        Assertions.assertEquals(HttpStatus.CREATED.value(), response2.statusCode());
    }


    private CompletableFuture<HttpResponse<String>> doCreateAas(String url, AssetAdministrationShellDescriptor aas) throws SerializationException {

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(new JsonSerializer().write(aas)))
                .build();

        return client.sendAsync(request1, HttpResponse.BodyHandlers.ofString());
    }


    private static AssetAdministrationShellDescriptor getAasMultiple1() {
        return new DefaultAssetAdministrationShellDescriptor.Builder()
                .idShort("MultipleTest1")
                .id("http://iosb.fraunhofer.de/IntegrationTest/Mult1")
                .displayName(new DefaultLangStringNameType.Builder().text("Multiple Test 1 Name").language("de-DE").build())
                .globalAssetId("http://iosb.fraunhofer.de/GlobalAssetId/Mult1")
                .assetType("AssetTypeMult1")
                .build();
    }


    private static AssetAdministrationShellDescriptor getAasMultiple2() {
        return new DefaultAssetAdministrationShellDescriptor.Builder()
                .idShort("MultipleTest2")
                .id("http://iosb.fraunhofer.de/IntegrationTest/Mult2")
                .displayName(new DefaultLangStringNameType.Builder().text("Multiple Test 2 Name").language("de-DE").build())
                .globalAssetId("http://iosb.fraunhofer.de/GlobalAssetId/Mult2")
                .assetType("AssetTypeMult2")
                .build();
    }

}
