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
 * Represents the resolved SQL expression for a single $field identifier
 * from the query language mapped to the PostgreSQL schema.
 */
public class FieldMapping {

    /**
     * Enumerator for access type.
     */
    public enum AccessType {
        /**
         * Direct column access (e.g., aas_descriptors.id).
         */
        DIRECT,
        /**
         * JSONB path access with ->> for text result.
         */
        JSONB_TEXT,
        /**
         * JSONB path access with -> for object result (intermediate).
         */
        JSONB_OBJECT,
        /**
         * Requires CROSS JOIN LATERAL jsonb_array_elements for [] notation.
         */
        JSONB_ARRAY_ANY,
        /**
         * Requires JOIN to another table (submodel_descriptors).
         */
        JOIN_REQUIRED
    }

    private final String baseTable;
    private final String baseTableAlias;
    private final String sqlExpression;
    private final String joinClause;
    private final String lateralClause;
    private final AccessType accessType;
    private final boolean requiresCast;
    private final String castType;

    private FieldMapping(Builder builder) {
        this.baseTable = builder.baseTable;
        this.baseTableAlias = builder.baseTableAlias;
        this.sqlExpression = builder.sqlExpression;
        this.joinClause = builder.joinClause;
        this.lateralClause = builder.lateralClause;
        this.accessType = builder.accessType;
        this.requiresCast = builder.requiresCast;
        this.castType = builder.castType;
    }


    public String getBaseTable() {
        return baseTable;
    }


    public String getBaseTableAlias() {
        return baseTableAlias;
    }


    public String getSqlExpression() {
        return sqlExpression;
    }


    public String getJoinClause() {
        return joinClause;
    }


    public String getLateralClause() {
        return lateralClause;
    }


    public AccessType getAccessType() {
        return accessType;
    }


    public boolean isRequiresCast() {
        return requiresCast;
    }


    public String getCastType() {
        return castType;
    }


    /**
     * Gets a value indicating whether a join is available.
     *
     * @return True if a join is avilable, false otherwise.
     */
    public boolean hasJoin() {
        return joinClause != null && !joinClause.isBlank();
    }


    /**
     * Get a value indicating whether a lateral is available.
     *
     * @return True if it has a lateral, false otherwise.
     */
    public boolean hasLateral() {
        return lateralClause != null && !lateralClause.isBlank();
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String baseTable;
        private String baseTableAlias;
        private String sqlExpression;
        private String joinClause;
        private String lateralClause;
        private AccessType accessType = AccessType.DIRECT;
        private boolean requiresCast = false;
        private String castType;

        public Builder baseTable(String t) {
            this.baseTable = t;
            return this;
        }


        public Builder baseTableAlias(String a) {
            this.baseTableAlias = a;
            return this;
        }


        public Builder sqlExpression(String e) {
            this.sqlExpression = e;
            return this;
        }


        public Builder joinClause(String j) {
            this.joinClause = j;
            return this;
        }


        public Builder lateralClause(String l) {
            this.lateralClause = l;
            return this;
        }


        public Builder accessType(AccessType a) {
            this.accessType = a;
            return this;
        }


        public Builder requiresCast(boolean c) {
            this.requiresCast = c;
            return this;
        }


        public Builder castType(String ct) {
            this.castType = ct;
            return this;
        }


        public FieldMapping build() {
            return new FieldMapping(this);
        }
    }
}
