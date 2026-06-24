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
 * Handles the assetKind field which is stored as SMALLINT in the DB
 * but referenced as a string ("Instance", "Type", "NotApplicable") in queries.
 * This is injected into the parameter resolution when the field is $aasdesc#assetKind.
 */
public class AssetKindParameterHandler {

    /**
     * Converts a query-side assetKind string value to the DB integer.
     * Call this before binding the parameter.
     *
     * @param fieldIdentifier The desired field identifier.
     * @param value The value.
     * @return The converted database integer.
     */
    public static Object convertParameterValue(String fieldIdentifier, Object value) {
        if ("$aasdesc#assetKind".equals(fieldIdentifier) && value instanceof String strVal) {
            return AssetKindMapping.toDbValue(strVal);
        }
        return value;
    }
}
