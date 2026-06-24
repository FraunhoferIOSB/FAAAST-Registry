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
 * Maps the string representation of AssetKind to the SMALLINT stored in the DB.
 * The DB constraint is: asset_kind >= 0 AND asset_kind <= 2
 */
public enum AssetKindMapping {
    INSTANCE(0, "Instance"),
    NOT_APPLICABLE(1, "NotApplicable"),
    TYPE(2, "Type");

    private final int dbValue;
    private final String queryValue;

    AssetKindMapping(int dbValue, String queryValue) {
        this.dbValue = dbValue;
        this.queryValue = queryValue;
    }


    public int getDbValue() {
        return dbValue;
    }


    public String getQueryValue() {
        return queryValue;
    }


    /**
     * Converts the given query value to a database value.
     *
     * @param queryValue The desired query value.
     * @return The converted database value.
     */
    public static int toDbValue(String queryValue) {
        for (AssetKindMapping m: values()) {
            if (m.queryValue.equalsIgnoreCase(queryValue)) {
                return m.dbValue;
            }
        }
        throw new IllegalArgumentException("Unknown assetKind: " + queryValue);
    }


    /**
     * Converts the given database value to a query value.
     *
     * @param dbValue The desired database value.
     * @return The converted query value.
     */
    public static String toQueryValue(int dbValue) {
        for (AssetKindMapping m: values()) {
            if (m.dbValue == dbValue) {
                return m.queryValue;
            }
        }
        throw new IllegalArgumentException("Unknown assetKind DB value: " + dbValue);
    }
}
