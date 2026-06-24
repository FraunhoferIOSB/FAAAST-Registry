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

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a resolved SQL mapping for a single FieldIdentifier.
 */
public class SqlMapping {

    private final String baseTable;
    private final String baseTableAlias;
    private final String column;
    private final List<JoinClause> joins;
    private final Integer arrayIndex; // null = wildcard (all), otherwise specific index

    public SqlMapping(String baseTable, String baseTableAlias, String column,
            List<JoinClause> joins, Integer arrayIndex) {
        this.baseTable = baseTable;
        this.baseTableAlias = baseTableAlias;
        this.column = column;
        this.joins = joins != null ? joins : new ArrayList<>();
        this.arrayIndex = arrayIndex;
    }


    public String getBaseTable() {
        return baseTable;
    }


    public String getBaseTableAlias() {
        return baseTableAlias;
    }


    public String getColumn() {
        return column;
    }


    public List<JoinClause> getJoins() {
        return joins;
    }


    public Integer getArrayIndex() {
        return arrayIndex;
    }


    /**
     * Returns fully qualified column reference, e.g., "ad.id_short".
     * 
     * @return column reference.
     */
    public String getQualifiedColumn() {
        String alias = joins.isEmpty() ? baseTableAlias : joins.get(joins.size() - 1).getAlias();
        return alias + "." + column;
    }


    /**
     * Generates the FROM + JOIN clause portion of a SQL query.
     *
     * @return The SQL query.
     */
    public String toFromJoinSql() {
        StringBuilder sb = new StringBuilder();
        sb.append(baseTable).append(" ").append(baseTableAlias);
        for (JoinClause join: joins) {
            sb.append("\n    ").append(join.toSql());
        }
        return sb.toString();
    }


    @Override
    public String toString() {
        return "SqlMapping{table=" + baseTable + ", column=" + getQualifiedColumn() +
                ", joins=" + joins.size() + "}";
    }
}
