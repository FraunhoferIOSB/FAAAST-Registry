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
package de.fraunhofer.iosb.ilt.faaast.registry.service.helper;

import java.util.UUID;


/**
 * Utility class for handling operation-related helper methods.
 */
public class OperationHelper {

    private OperationHelper() {}


    /**
     * Generates a unique identifier for a bulk operation handle.
     *
     * @return a unique operation handle ID as a {@link String}
     */
    public static String generateOperationHandleId() {
        return "OperationHandle-" + UUID.randomUUID();
    }
}
