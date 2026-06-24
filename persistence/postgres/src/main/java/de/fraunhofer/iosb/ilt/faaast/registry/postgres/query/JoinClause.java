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

/**
 * Represents a single JOIN clause in the SQL mapping.
 */
public class JoinClause {

    /**
     * Join Type.
     */
    public enum JoinType {
        INNER,
        LEFT,
        RIGHT
    }

    private final JoinType joinType;
    private final String table;
    private final String alias;
    private final String onCondition;
    private final String additionalCondition; // e.g., array index filter

    public JoinClause(JoinType joinType, String table, String alias,
            String onCondition, String additionalCondition) {
        this.joinType = joinType;
        this.table = table;
        this.alias = alias;
        this.onCondition = onCondition;
        this.additionalCondition = additionalCondition;
    }


    public JoinClause(JoinType joinType, String table, String alias, String onCondition) {
        this(joinType, table, alias, onCondition, null);
    }


    public JoinType getJoinType() {
        return joinType;
    }


    public String getTable() {
        return table;
    }


    public String getAlias() {
        return alias;
    }


    public String getOnCondition() {
        return onCondition;
    }


    public String getAdditionalCondition() {
        return additionalCondition;
    }


    /**
     * Gets the SQL statement.
     * 
     * @return The SQL statement.
     */
    public String toSql() {
        String joinKeyword = switch (joinType) {
            case INNER -> "JOIN";
            case LEFT -> "LEFT JOIN";
            case RIGHT -> "RIGHT JOIN";
        };
        StringBuilder sb = new StringBuilder();
        sb.append(joinKeyword).append(" ").append(table).append(" ").append(alias);
        sb.append(" ON ").append(onCondition);
        if (additionalCondition != null && !additionalCondition.isBlank()) {
            sb.append(" AND ").append(additionalCondition);
        }
        return sb.toString();
    }
}
