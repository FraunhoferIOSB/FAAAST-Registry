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
import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.FormulaEvaluator;
import de.fraunhofer.iosb.ilt.faaast.registry.service.query.json.LogicalExpression;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;


public class FormulaEvaluatorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void testFormula_ConditionsMet() throws JsonProcessingException {
        String json= """
                {
                                    "$and": [
                                        {
                                            "$or": [
                                                {
                                                    "$eq": [
                                                        {
                                                            "$attribute": {
                                                                "CLAIM": "organization"
                                                            }
                                                        },
                                                        {
                                                            "$strVal": "[MyCompany]"
                                                        }
                                                    ]
                                                },
                                                {
                                                    "$eq": [
                                                        {
                                                            "$attribute": {
                                                                "CLAIM": "organization"
                                                            }
                                                        },
                                                        {
                                                            "$strVal": "Company2"
                                                        }
                                                    ]
                                                }
                                            ]
                                        },
                                        {
                                            "$or": [
                                                {
                                                    "$eq": [
                                                        {
                                                            "$attribute": {
                                                                "CLAIM": "email"
                                                            }
                                                        },
                                                        {
                                                            "$strVal": "bob@example.com"
                                                        }
                                                    ]
                                                },
                                                {
                                                    "$eq": [
                                                        {
                                                            "$attribute": {
                                                                "CLAIM": "email"
                                                            }
                                                        },
                                                        {
                                                            "$strVal": "user2@company2.com"
                                                        }
                                                    ]
                                                }
                                            ]
                                        }
                                    ]
                                }
                """;
        LogicalExpression formula = MAPPER.readValue(
                json, new TypeReference<>() {});
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("CLAIM:organization", "Company2");
        ctx.put("CLAIM:email", "user2@company2.com");
        Assert.assertTrue(FormulaEvaluator.evaluate(formula, ctx));
    }
}
