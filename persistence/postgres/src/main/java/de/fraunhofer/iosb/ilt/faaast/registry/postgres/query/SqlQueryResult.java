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
 * Encapsulates the result of mapping a query language expression to SQL.
 */
public class SqlQueryResult {

    private final SqlMapping mapping;
    private final String whereClause;
    private final List<Object> parameters;

    public SqlQueryResult(SqlMapping mapping, String whereClause, List<Object> parameters) {
        this.mapping = mapping;
        this.whereClause = whereClause;
        this.parameters = parameters;
    }


    public SqlMapping getMapping() {
        return mapping;
    }


    public String getWhereClause() {
        return whereClause;
    }


    public List<Object> getParameters() {
        return parameters;
    }


    /**
     * Generates a complete SQL SELECT statement.
     *
     * @param selectId if true, SELECT only the id column; otherwise SELECT *
     * @return the full SQL query string
     */
    public String toSql(boolean selectId) {
        StringBuilder sb = new StringBuilder();

        String selectColumn = selectId
                ? mapping.getBaseTableAlias() + ".id"
                : mapping.getBaseTableAlias() + ".*";

        sb.append("SELECT DISTINCT ").append(selectColumn).append("\n");
        sb.append("FROM ").append(mapping.toFromJoinSql()).append("\n");
        sb.append("WHERE ").append(whereClause);

        return sb.toString();
    }


    /**
     * Generates a SELECT * SQL statement.
     *
     * @return The SQL string.
     */
    public String toSql() {
        return toSql(false);
    }


    @Override
    public String toString() {
        return toSql() + "\n-- Parameters: " + parameters;
    }
}
