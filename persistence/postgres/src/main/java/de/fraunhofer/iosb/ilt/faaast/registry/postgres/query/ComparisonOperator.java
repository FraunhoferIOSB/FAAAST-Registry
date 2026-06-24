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
 * Maps AAS Query Language comparison operators to SQL operators.
 */
public enum ComparisonOperator {
    EQ("$eq", "="),
    NE("$ne", "!="),
    GT("$gt", ">"),
    GE("$ge", ">="),
    LT("$lt", "<"),
    LE("$le", "<="),
    CONTAINS("$contains", "LIKE"), // wraps value with %...%
    STARTS_WITH("$starts-with", "LIKE"), // wraps value with ...%
    ENDS_WITH("$ends-with", "LIKE"), // wraps value with %...
    REGEX("$regex", "~"); // PostgreSQL regex operator

    private final String queryLangToken;
    private final String sqlOperator;

    ComparisonOperator(String queryLangToken, String sqlOperator) {
        this.queryLangToken = queryLangToken;
        this.sqlOperator = sqlOperator;
    }


    public String getQueryLangToken() {
        return queryLangToken;
    }


    public String getSqlOperator() {
        return sqlOperator;
    }


    /**
     * Converts the the given token to the corresponding comparison operator.
     *
     * @param token The desired token.
     * @return The corresponding comparison operator.
     */
    public static ComparisonOperator fromToken(String token) {
        for (ComparisonOperator op: values()) {
            if (op.queryLangToken.equals(token)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown operator: " + token);
    }


    /**
     * Transforms the literal value for LIKE-based operators.
     *
     * @param value the desired value.
     * @return The wrapped value.
     */
    public String wrapValueForLike(String value) {
        return switch (this) {
            case CONTAINS -> "%" + value + "%";
            case STARTS_WITH -> value + "%";
            case ENDS_WITH -> "%" + value;
            default -> value;
        };
    }
}
