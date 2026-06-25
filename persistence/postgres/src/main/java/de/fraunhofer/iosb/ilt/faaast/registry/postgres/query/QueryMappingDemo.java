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
package de.fraunhofer.iosb.ilt.faaast.registry.postgres.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.Query;


/**
 * An exapmple application class.
 */
public class QueryMappingDemo {

    /**
     * An example main method.
     *
     * @param args Command line arguments.
     * @throws Exception If an error occurs.
     */
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Example JSON query: find AAS descriptors with a specific globalAssetId
        String json = """
                {
                  "Query": {
                    "$condition": {
                      "$eq": [
                        { "$field": "$aasdesc#assetKind" },
                        { "$strVal": "Instance" }
                      ]
                    }
                  }
                }
                """;

        // Deserialize using jsonschema2pojo-generated classes
        // (Root class name depends on your jsonschema2pojo config)
        var root = mapper.readTree(json);
        Query query = mapper.treeToValue(root.get("Query"), Query.class);

        // Translate to SQL
        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        SqlTranslationResult result = translator.translate(query, "$aasdesc");

        System.out.println(result.toSql());
        System.out.println("Parameters: " + result.getParameters());
    }
}
