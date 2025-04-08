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
package de.fraunhofer.iosb.ilt.faaast.registry.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState;


/**
 * Utility class for storing and retrieving the status of asynchronous bulk operations.
 */
public class BulkOperationStatusStore {

    private static final Map<String, ExecutionState> statusMap = new ConcurrentHashMap<>();

    /**
     * Sets the status of a bulk operation.
     *
     * @param handleId unique identifier for the bulk operation
     * @param status the current status of the operation
     */
    public static void setStatus(String handleId, ExecutionState status) {
        statusMap.put(handleId, status);
    }


    /**
     * Retrieves the status of a bulk operation.
     *
     * @param handleId unique identifier for the bulk operation
     * @return the current status, or {@code OperationStatus.PENDING} if not found
     */
    public static ExecutionState getStatus(String handleId) {
        return statusMap.getOrDefault(handleId, ExecutionState.INITIATED);
    }
}
