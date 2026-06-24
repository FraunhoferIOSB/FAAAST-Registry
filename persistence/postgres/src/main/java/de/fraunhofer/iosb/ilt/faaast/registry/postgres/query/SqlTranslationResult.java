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
import java.util.Set;


/**
 * The assembled SQL query result ready for execution via JDBC PreparedStatement.
 */
public class SqlTranslationResult {

    private String baseTable;
    private String baseAlias;
    private boolean selectIdOnly;
    private String whereClause;
    private Set<String> joinClauses;
    private Set<String> lateralClauses;
    private List<Object> parameters;
    private Class entityClass;

    // --- Getters/Setters ---
    public String getBaseTable() {
        return baseTable;
    }


    public void setBaseTable(String baseTable) {
        this.baseTable = baseTable;
    }


    public String getBaseAlias() {
        return baseAlias;
    }


    public void setBaseAlias(String baseAlias) {
        this.baseAlias = baseAlias;
    }


    public boolean isSelectIdOnly() {
        return selectIdOnly;
    }


    public void setSelectIdOnly(boolean selectIdOnly) {
        this.selectIdOnly = selectIdOnly;
    }


    public String getWhereClause() {
        return whereClause;
    }


    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }


    public Set<String> getJoinClauses() {
        return joinClauses;
    }


    public void setJoinClauses(Set<String> joinClauses) {
        this.joinClauses = joinClauses;
    }


    public Set<String> getLateralClauses() {
        return lateralClauses;
    }


    public void setLateralClauses(Set<String> lateralClauses) {
        this.lateralClauses = lateralClauses;
    }


    public List<Object> getParameters() {
        return parameters;
    }


    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }


    public Class getEntityClass() {
        return entityClass;
    }


    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
    }


    /**
     * Builds the final SQL string.
     *
     * @return The SQL string.
     */
    public String toSql() {
        StringBuilder sql = new StringBuilder();

        // SELECT
        String selectCol = selectIdOnly ? baseAlias + ".id" : baseAlias + ".*";
        sql.append("SELECT DISTINCT ").append(selectCol).append("\n");

        // FROM
        sql.append("FROM ").append(baseTable).append(" ").append(baseAlias).append("\n");

        // JOINs
        if (joinClauses != null) {
            for (String join: joinClauses) {
                sql.append("  ").append(join).append("\n");
            }
        }

        // LATERAL joins
        if (lateralClauses != null) {
            for (String lateral: lateralClauses) {
                sql.append("  ").append(lateral).append("\n");
            }
        }

        // WHERE
        if (whereClause != null && !whereClause.isBlank()) {
            sql.append("WHERE ").append(whereClause).append("\n");
        }

        return sql.toString().trim();
    }


    @Override
    public String toString() {
        return toSql() + "\n-- Parameters: " + parameters;
    }
}
