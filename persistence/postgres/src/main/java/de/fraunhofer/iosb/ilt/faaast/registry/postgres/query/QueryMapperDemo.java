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

import java.util.List;


/**
 * Demonstrates the usage of the AAS Query Language to SQL mapper.
 */
public class QueryMapperDemo {

    /**
     * An example main method.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        QueryLanguageToSqlMapper mapper = new QueryLanguageToSqlMapper();

        System.out.println("=== Example 1: $aasdesc#idShort $eq \"MyAAS\" ===");
        SqlQueryResult result1 = mapper.mapComparison("$aasdesc#idShort", "$eq", "MyAAS");
        System.out.println(result1.toSql());
        System.out.println("Parameters: " + result1.getParameters());
        System.out.println();

        System.out.println("=== Example 2: $aasdesc#assetKind $eq \"Instance\" ===");
        SqlQueryResult result2 = mapper.mapComparison("$aasdesc#assetKind", "$eq", "Instance");
        System.out.println(result2.toSql());
        System.out.println();

        System.out.println("=== Example 3: $aasdesc#globalAssetId $eq \"urn:example:asset:1\" ===");
        SqlQueryResult result3 = mapper.mapComparison("$aasdesc#globalAssetId", "$eq", "urn:example:asset:1");
        System.out.println(result3.toSql());
        System.out.println();

        System.out.println("=== Example 4: $aasdesc#specificAssetIds[].name $eq \"serialNumber\" ===");
        SqlQueryResult result4 = mapper.mapComparison("$aasdesc#specificAssetIds[].name", "$eq", "serialNumber");
        System.out.println(result4.toSql());
        System.out.println();

        System.out.println("=== Example 5: $aasdesc#endpoints[0].interface $eq \"AAS-3.0\" ===");
        SqlQueryResult result5 = mapper.mapComparison("$aasdesc#endpoints[0].interface", "$eq", "AAS-3.0");
        System.out.println(result5.toSql());
        System.out.println();

        System.out.println("=== Example 6: $smdesc#semanticId.keys[0].value $eq \"0173-1#01-AHD205#001\" ===");
        SqlQueryResult result6 = mapper.mapComparison("$smdesc#semanticId.keys[0].value", "$eq", "0173-1#01-AHD205#001");
        System.out.println(result6.toSql());
        System.out.println();

        System.out.println("=== Example 7: $smdesc#idShort $contains \"Nameplate\" ===");
        SqlQueryResult result7 = mapper.mapComparison("$smdesc#idShort", "$contains", "Nameplate");
        System.out.println(result7.toSql());
        System.out.println();

        System.out.println("=== Example 8: $smdesc#endpoints[].protocolinformation.href $starts-with \"https://\" ===");
        SqlQueryResult result8 = mapper.mapComparison("$smdesc#endpoints[].protocolinformation.href", "$starts-with", "https://");
        System.out.println(result8.toSql());
        System.out.println();

        System.out.println("=== Example 9: $and expression ===");
        SqlQueryResult result9 = mapper.mapAndExpression(List.of(
                new QueryLanguageToSqlMapper.ComparisonInput("$aasdesc#assetKind", "$eq", "Instance"),
                new QueryLanguageToSqlMapper.ComparisonInput("$aasdesc#specificAssetIds[].name", "$eq", "serialNumber")));
        System.out.println(result9.toSql());
        System.out.println("Parameters: " + result9.getParameters());
        System.out.println();

        System.out.println("=== Example 10: $select id with $smdesc ===");
        SqlQueryResult result10 = mapper.mapComparison("$smdesc#id", "$eq", "urn:example:sm:1");
        System.out.println(result10.toSql(true)); // SELECT only id
        System.out.println();
    }
}
