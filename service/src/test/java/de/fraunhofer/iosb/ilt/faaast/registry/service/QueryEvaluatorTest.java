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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.QueryEvaluator;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.Query;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEndpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProtocolInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelDescriptor;
import org.junit.Assert;
import org.junit.Test;


public class QueryEvaluatorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private QueryEvaluator evaluator = new QueryEvaluator();

    @Test
    public void testAasWithMatchingIdShort() throws JsonProcessingException {
        String json = """
                {
                  "$condition": {
                    "$eq": [
                      {
                        "$field": "$aasdesc#idShort"
                      },
                      {
                        "$strVal": "QueryTest1"
                      }
                    ]
                  }
                }
                """;
        assertAasTrue(json);
    }


    private void assertAasTrue(String json) throws JsonProcessingException {
        Query query = MAPPER.readValue(
                json, new TypeReference<>() {});
        AssetAdministrationShellDescriptor aas = getAas();
        Assert.assertTrue(evaluator.matches(query.get$condition(), aas));
    }


    @Test
    public void testAasEqWithNotMatchingIdShort() throws JsonProcessingException {
        String json = """
                {
                  "$condition": {
                    "$eq": [
                      {
                        "$field": "$aasdesc#idShort"
                      },
                      {
                        "$strVal": "WrongQuery"
                      }
                    ]
                  }
                }
                """;
        assertAasFalse(json);
    }


    private void assertAasFalse(String json) throws JsonProcessingException {
        Query query = MAPPER.readValue(
                json, new TypeReference<>() {});
        AssetAdministrationShellDescriptor aas = getAas();
        Assert.assertFalse(evaluator.matches(query.get$condition(), aas));
    }


    @Test
    public void testAasWithMatchingId() throws JsonProcessingException {
        String json = """
                {
                  "$condition": {
                    "$eq": [
                      {
                        "$field": "$aasdesc#id"
                      },
                      {
                        "$strVal": "http://iosb.fraunhofer.de/QueryTest/QueryTest1"
                      }
                    ]
                  }
                }
                """;
        assertAasTrue(json);
    }


    @Test
    public void testAasWithNotMatchingId() throws JsonProcessingException {
        String json = """
                {
                  "$condition": {
                    "$eq": [
                      {
                        "$field": "$aasdesc#id"
                      },
                      {
                        "$strVal": "http://iosb.fraunhofer.de/QueryTest/WrongValue"
                      }
                    ]
                  }
                }
                """;
        assertAasFalse(json);
    }


    @Test
    public void testSubmodelWithMatchingIdShort() throws JsonProcessingException {
        String json = """
                {
                  "$condition": {
                    "$eq": [
                      {
                        "$field": "$smdesc#idShort"
                      },
                      {
                        "$strVal": "QuerySubmodel1"
                      }
                    ]
                  }
                }
                """;
        Query query = MAPPER.readValue(
                json, new TypeReference<>() {});
        SubmodelDescriptor sm = getSubmodel();
        Assert.assertTrue(evaluator.matches(query.get$condition(), sm));
    }


    @Test
    public void testSubmodelWithNotMatchingIdShort() throws JsonProcessingException {
        String json = """
                {
                  "$condition": {
                    "$eq": [
                      {
                        "$field": "$smdesc#idShort"
                      },
                      {
                        "$strVal": "WrongValue"
                      }
                    ]
                  }
                }
                """;
        Query query = MAPPER.readValue(
                json, new TypeReference<>() {});
        SubmodelDescriptor sm = getSubmodel();
        Assert.assertFalse(evaluator.matches(query.get$condition(), sm));
    }


    @Test
    public void testSubmodelWithMatchingId() throws JsonProcessingException {
        String json = """
                {
                  "$condition": {
                    "$eq": [
                      {
                        "$field": "$smdesc#id"
                      },
                      {
                        "$strVal": "http://iosb.fraunhofer.de/QueryTest/Submodel1"
                      }
                    ]
                  }
                }
                """;
        Query query = MAPPER.readValue(
                json, new TypeReference<>() {});
        SubmodelDescriptor sm = getSubmodel();
        Assert.assertTrue(evaluator.matches(query.get$condition(), sm));
    }


    @Test
    public void testSubmodelWithNotMatchingId() throws JsonProcessingException {
        String json = """
                {
                  "$condition": {
                    "$eq": [
                      {
                        "$field": "$smdesc#id"
                      },
                      {
                        "$strVal": "http://iosb.fraunhofer.de/QueryTest/WrongValue"
                      }
                    ]
                  }
                }
                """;
        Query query = MAPPER.readValue(
                json, new TypeReference<>() {});
        SubmodelDescriptor sm = getSubmodel();
        Assert.assertFalse(evaluator.matches(query.get$condition(), sm));
    }


    @Test
    public void testOrMatchWithSpecificAssetIds() throws JsonProcessingException {
        String json = """
                {
                  "$condition": {
                    "$or": [
                      {
                        "$match": [
                          { "$eq": [
                              { "$field": "$aasdesc#specificAssetIds[].name" },
                              { "$strVal": "supplierId" }
                            ]
                          },
                          { "$eq": [
                              { "$field": "$aasdesc#specificAssetIds[].value" },
                              { "$strVal": "aas-1" }
                            ]
                          }
                        ]
                      },
                      {
                        "$match": [
                          {
                            "$eq": [
                              { "$field": "$aasdesc#specificAssetIds[].name" },
                              { "$strVal": "customerId" }
                            ]
                          },
                          {
                            "$eq": [
                              { "$field": "$aasdesc#specificAssetIds[].value" },
                              { "$strVal": "aas-2" }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                }
                """;
        assertAasTrue(json);
    }


    private static AssetAdministrationShellDescriptor getAas() {
        return new DefaultAssetAdministrationShellDescriptor.Builder()
                .idShort("QueryTest1")
                .id("http://iosb.fraunhofer.de/QueryTest/QueryTest1")
                .displayName(new DefaultLangStringNameType.Builder().text("Query Test 1 Name").language("de-DE").build())
                .globalAssetId("http://iosb.fraunhofer.de/GlobalAssetId/QueryTest1")
                .assetType("AssetType1")
                .assetKind(AssetKind.INSTANCE)
                .specificAssetIds(new DefaultSpecificAssetId.Builder()
                        .name("supplierId")
                        .value("aas-1")
                        .build())
                .build();
    }


    private static SubmodelDescriptor getSubmodel() {
        return new DefaultSubmodelDescriptor.Builder()
                .id("http://iosb.fraunhofer.de/QueryTest/Submodel1")
                .idShort("QuerySubmodel1")
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value("0173-1#02-BAF016#006")
                                .build())
                        .build())
                .endpoints(new DefaultEndpoint.Builder()
                        ._interface("http")
                        .protocolInformation(new DefaultProtocolInformation.Builder()
                                .endpointProtocol("http")
                                .href("http://iosb.fraunhofer.de/Endpoints/QuerySubmodel1")
                                .endpointProtocolVersion(List.of("2.1"))
                                .build())
                        .build())
                .build();
    }
}
