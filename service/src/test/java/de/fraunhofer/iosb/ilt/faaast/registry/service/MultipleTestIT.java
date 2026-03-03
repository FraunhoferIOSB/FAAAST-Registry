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

import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEndpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProtocolInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelDescriptor;
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
class MultipleTestIT {

    @LocalServerPort
    protected int port;

    private final HttpClient client;
    private final JsonSerializer jsonSerializer;

    public MultipleTestIT() {
        client = HttpClient.newHttpClient();
        jsonSerializer = new JsonSerializer();
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

        aas1.setAssetKind(AssetKind.INSTANCE);
        aas2.setAssetKind(AssetKind.TYPE);

        var futureResponseUpd1 = doUpdateAas(url, aas1);
        var futureResponseUpd2 = doUpdateAas(url, aas2);

        HttpResponse<String> responseUpd1 = futureResponseUpd1.get();
        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), responseUpd1.statusCode());

        HttpResponse<String> responseUpd2 = futureResponseUpd2.get();
        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), responseUpd2.statusCode());

        var futureResponseDel1 = doDelete(url, aas1.getId());
        var futureResponseDel2 = doDelete(url, aas2.getId());

        HttpResponse<Void> responseDel1 = futureResponseDel1.get();
        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), responseDel1.statusCode());

        HttpResponse<Void> responseDel2 = futureResponseDel2.get();
        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), responseDel2.statusCode());

    }


    @Test
    void testCreateSubmodel() throws SerializationException, InterruptedException, ExecutionException {
        SubmodelDescriptor submodel1 = getSubmodelMultiple1();
        SubmodelDescriptor submodel2 = getSubmodelMultiple2();
        String url = "http://localhost:" + port + "/api/v3.0/submodel-descriptors";

        var futureResponse1 = doCreateSubmodel(url, submodel1);
        var futureResponse2 = doCreateSubmodel(url, submodel2);

        HttpResponse<String> response1 = futureResponse1.get();
        Assertions.assertEquals(HttpStatus.CREATED.value(), response1.statusCode());

        HttpResponse<String> response2 = futureResponse2.get();
        Assertions.assertEquals(HttpStatus.CREATED.value(), response2.statusCode());

        submodel1.setIdShort("MultipleSubmodel1A");
        submodel2.setIdShort("MultipleSubmodel2B");

        var futureResponseUpd1 = doUpdateSubmodel(url, submodel1);
        var futureResponseUpd2 = doUpdateSubmodel(url, submodel2);

        HttpResponse<String> responseUpd1 = futureResponseUpd1.get();
        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), responseUpd1.statusCode());

        HttpResponse<String> responseUpd2 = futureResponseUpd2.get();
        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), responseUpd2.statusCode());

        var futureResponseDel1 = doDelete(url, submodel1.getId());
        var futureResponseDel2 = doDelete(url, submodel2.getId());

        HttpResponse<Void> responseDel1 = futureResponseDel1.get();
        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), responseDel1.statusCode());

        HttpResponse<Void> responseDel2 = futureResponseDel2.get();
        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), responseDel2.statusCode());

    }


    private CompletableFuture<HttpResponse<String>> doCreateAas(String url, AssetAdministrationShellDescriptor aas) throws SerializationException {
        return doCreate(url, jsonSerializer.write(aas));
    }


    private CompletableFuture<HttpResponse<String>> doCreateSubmodel(String url, SubmodelDescriptor submodel) throws SerializationException {
        return doCreate(url, jsonSerializer.write(submodel));
    }


    private CompletableFuture<HttpResponse<String>> doCreate(String url, String body) throws SerializationException {

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return client.sendAsync(request1, HttpResponse.BodyHandlers.ofString());
    }


    private CompletableFuture<HttpResponse<String>> doUpdateAas(String url, AssetAdministrationShellDescriptor aas) throws SerializationException {
        return doUpdate(url, aas.getId(), jsonSerializer.write(aas));
    }


    private CompletableFuture<HttpResponse<String>> doUpdateSubmodel(String url, SubmodelDescriptor submodel) throws SerializationException {
        return doUpdate(url, submodel.getId(), jsonSerializer.write(submodel));
    }


    private CompletableFuture<HttpResponse<String>> doUpdate(String url, String id, String body) throws SerializationException {

        URI uri = URI.create(String.format("%s/%s", url, EncodingHelper.base64UrlEncode(id)));
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return client.sendAsync(request1, HttpResponse.BodyHandlers.ofString());
    }


    private CompletableFuture<HttpResponse<Void>> doDelete(String baseUrl, String id) throws SerializationException {

        URI uri = URI.create(String.format("%s/%s", baseUrl, EncodingHelper.base64UrlEncode(id)));
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();

        return client.sendAsync(request1, HttpResponse.BodyHandlers.discarding());
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


    private SubmodelDescriptor getSubmodelMultiple1() {
        return new DefaultSubmodelDescriptor.Builder()
                .id("http://iosb.fraunhofer.de/IntegrationTest/SubmodelMult1")
                .idShort("MultipleSubmodel1")
                .displayName(new DefaultLangStringNameType.Builder().language("de-DE").text("Multiple Submodel 1 Name").build())
                .administration(new DefaultAdministrativeInformation.Builder()
                        .version("1")
                        .revision("12")
                        .build())
                .endpoints(new DefaultEndpoint.Builder()
                        ._interface("http")
                        .protocolInformation(new DefaultProtocolInformation.Builder()
                                .endpointProtocol("http")
                                .href("http://iosb.fraunhofer.de/Endpoints/SubmodelMult1")
                                .endpointProtocolVersion(List.of("2.2"))
                                .build())
                        .build())
                .build();
    }


    private SubmodelDescriptor getSubmodelMultiple2() {
        return new DefaultSubmodelDescriptor.Builder()
                .id("http://iosb.fraunhofer.de/IntegrationTest/SubmodelMult2")
                .idShort("MultipleSubmodel2")
                .displayName(new DefaultLangStringNameType.Builder().language("de-DE").text("Multiple Submodel 2 Name").build())
                .administration(new DefaultAdministrativeInformation.Builder()
                        .version("2")
                        .revision("26")
                        .build())
                .endpoints(new DefaultEndpoint.Builder()
                        ._interface("http")
                        .protocolInformation(new DefaultProtocolInformation.Builder()
                                .endpointProtocol("http")
                                .href("http://iosb.fraunhofer.de/Endpoints/SubmodelMult2")
                                .endpointProtocolVersion(List.of("2.2"))
                                .build())
                        .build())
                .build();
    }

}
