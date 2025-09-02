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
package de.fraunhofer.iosb.ilt.faaast.registry.service.service;

import de.fraunhofer.iosb.ilt.faaast.registry.core.AasRepository;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.BadRequestException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.InternalServerErrorException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.MovedPermanentlyException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.registry.service.model.BulkOperationStatusStore;
import java.net.URI;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;


/**
 * Service class for transaction functionality.
 * Having this service as a separate class is needed for bulk operations with transactions.
 * statusStore saves the current bulk operation status.
 * It is a Spring-managed bean to ensure test context and app context share the same store instance
 * to avoid visibility/race issues with static variables.
 */
@Service
public class TransactionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);
    private final BulkOperationStatusStore statusStore;

    @Autowired
    private AasRepository aasRepository;

    /**
     * Instantiates the Transaction Service.
     *
     * @param statusStore Utility class for storing and retrieving the status of asynchronous bulk operations.
     */
    public TransactionService(BulkOperationStatusStore statusStore) {
        this.statusStore = statusStore;
    }


    /**
     * This method implements the logic for POST on the /bulk/shell-descriptors endpoint.
     * Creates multiple new Asset Administration Shell Descriptors, i.e. registers multiple Asset Administration Shells.
     *
     * @param shells list of shell descriptors that shall be created.
     * @param handleId id of the operation handle for future reference.
     */
    public void createShells(List<AssetAdministrationShellDescriptor> shells, String handleId) {
        try {
            LOGGER.debug("createShells start");
            statusStore.setStatus(handleId, ExecutionState.RUNNING);
            for (AssetAdministrationShellDescriptor shell: shells) {
                aasRepository.create(shell);
                //statusStore.setStatus(handleId, ExecutionState.RUNNING);
            }
            statusStore.setStatus(handleId, ExecutionState.COMPLETED);
            LOGGER.debug("createShells finished");
        }
        catch (Exception ex) {
            statusStore.setStatus(handleId, ExecutionState.FAILED);
            LOGGER.info("createShells error", ex);
        }
    }


    /**
     * This method implements the logic for GET on the /bulk/status/{handleId} endpoint.
     * Returns the status of an asynchronously invoked bulk operation
     *
     * @param handleId id of the operation handle for future reference.
     * @return Bulk operation result object containing information that the 'executionState' is still 'Running'
     * @throws ResourceNotFoundException if there is no handle with that id.
     */
    public OperationResult getStatus(String handleId) throws ResourceNotFoundException {
        LOGGER.debug("getStatus: {}", handleId);
        ExecutionState status = statusStore.getStatus(handleId);

        if (status == null) {
            LOGGER.debug("getStatus: not found: {}", handleId);
            throw new ResourceNotFoundException("Unknown handleId: " + handleId);
        }
        else
            switch (status) {
                case RUNNING:
                    LOGGER.debug("getStatus: running: {}", handleId);
                    DefaultOperationResult operationResult = new DefaultOperationResult();
                    operationResult.setExecutionState(status);
                    return operationResult;
                case COMPLETED:
                case FAILED:
                    URI location = URI.create("../result/" + handleId);
                    HttpHeaders headers = new HttpHeaders();
                    LOGGER.debug("getStatus: status {}; location: {}", status, location);
                    headers.setLocation(location);
                    throw new MovedPermanentlyException("Operation completed. See result endpoint.", headers);
                default:
                    LOGGER.info("getStatus: Operation status {}", status);
                    break;
            }
        throw new InternalServerErrorException("operation status unknown");
    }


    /**
     * This method implements the logic for GET on the /bulk/status/{handleId} endpoint.
     * Returns the result object of an asynchronously invoked bulk operation
     *
     * @param handleId id of the operation handle for future reference.
     * @throws ResourceNotFoundException if there is no handle with that id.
     */
    public void getResult(String handleId) throws ResourceNotFoundException {
        ExecutionState status = statusStore.getStatus(handleId);
        if (status == null || status == ExecutionState.RUNNING) {
            throw new ResourceNotFoundException("Result not available or still running for handleId: " + handleId);
        }

        if (status == ExecutionState.COMPLETED) {
            return;
        }

        throw new BadRequestException("One or more items failed.");
    }
}
