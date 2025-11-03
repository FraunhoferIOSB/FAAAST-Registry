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

import de.fraunhofer.iosb.ilt.faaast.registry.core.AasRepository;
import de.fraunhofer.iosb.ilt.faaast.registry.service.model.BulkCreateShellData;
import de.fraunhofer.iosb.ilt.faaast.registry.service.model.BulkCreateSubmodelData;
import de.fraunhofer.iosb.ilt.faaast.registry.service.model.BulkDeleteShellData;
import de.fraunhofer.iosb.ilt.faaast.registry.service.model.BulkDeleteSubmodelData;
import de.fraunhofer.iosb.ilt.faaast.registry.service.model.BulkUpdateShellData;
import de.fraunhofer.iosb.ilt.faaast.registry.service.model.BulkUpdateSubmodelData;
import de.fraunhofer.iosb.ilt.faaast.registry.service.service.TransactionService;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Thread to handle transactions.
 */
public class TransactionThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionThread.class);

    private final AasRepository aasRepository;
    private final TransactionService transactionService;
    private final LinkedBlockingQueue<Object> queue;
    private boolean ende = false;

    public TransactionThread(AasRepository aasRepository, TransactionService transactionService) {
        this.aasRepository = aasRepository;
        this.transactionService = transactionService;
        queue = new LinkedBlockingQueue<>();
    }


    @Override
    public void run() {
        while (!ende) {
            try {
                Object obj = queue.take();
                while (aasRepository.getTransactionActive()) {
                    LOGGER.debug("run: wait for transaction to finish");
                    Thread.sleep(50);
                }
                
                if (obj instanceof BulkCreateShellData createData) {
                    doCreateShells(createData.getShells(), createData.getHandleId());
                }
                else if (obj instanceof BulkUpdateShellData updateData) {
                    doUpdateShells(updateData.getShells(), updateData.getHandleId());
                }
                else if (obj instanceof BulkDeleteShellData deleteData) {
                    doDeleteShells(deleteData.getIdentifiers(), deleteData.getHandleId());
                }
                else if (obj instanceof BulkCreateSubmodelData createSubmodelData) {
                    doCreateSubmodels(createSubmodelData.getSubmodels(), createSubmodelData.getHandleId());
                }
                else if (obj instanceof BulkUpdateSubmodelData updateSubmodelData) {
                    doUpdateSubmodels(updateSubmodelData.getSubmodels(), updateSubmodelData.getHandleId());
                }
                else if (obj instanceof BulkDeleteSubmodelData deleteSubmodelData) {
                    doDeleteSubmodels(deleteSubmodelData.getIdentifiers(), deleteSubmodelData.getHandleId());
                }
                
                // Wait for half a second so it doesn't print too fast
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.warn("TransactionThread interrupted");
                Thread.currentThread().interrupt();
            }
        }
        LOGGER.trace("TransactionThread finished");
    }


    /**
     * Stops the Thread.
     */
    public void stopThread() {
        ende = true;
        this.interrupt();
    }


    /**
     * Adds a task to create a list of shell descriptors.
     *
     * @param shells The desired shell descriptors.
     * @param handleId The handle.
     */
    public void createShells(List<AssetAdministrationShellDescriptor> shells, String handleId) {
        queue.add(new BulkCreateShellData(shells, handleId));
    }


    /**
     * Adds a task to update a list of shell descriptors.
     *
     * @param shells The desired shell descriptors.
     * @param handleId The handle.
     */
    public void updateShells(List<AssetAdministrationShellDescriptor> shells, String handleId) {
        queue.add(new BulkUpdateShellData(shells, handleId));
    }


    /**
     * Adds a task to delete a list of shell descriptors.
     *
     * @param shellIdentifiers The desired list of shell identifiers.
     * @param handleId The handle.
     */
    public void deleteShells(List<String> shellIdentifiers, String handleId) {
        queue.add(new BulkDeleteShellData(shellIdentifiers, handleId));
    }


    /**
     * Adds a task to create a list of Submodel Descriptors.
     *
     * @param submodels The desired submodel descriptors.
     * @param handleId The handle.
     */
    public void createSubmodels(List<SubmodelDescriptor> submodels, String handleId) {
        queue.add(new BulkCreateSubmodelData(submodels, handleId));
    }


    /**
     * Adds a task to update a list of Submodel Descriptors.
     *
     * @param submodels The desired submodel descriptors.
     * @param handleId The handle.
     */
    public void updateSubmodels(List<SubmodelDescriptor> submodels, String handleId) {
        queue.add(new BulkUpdateSubmodelData(submodels, handleId));
    }


    /**
     * Adds a task to delete a list of Submodel Descriptors.
     *
     * @param submodelIdentifiers The desired submodel identifiers.
     * @param handleId The handle.
     */
    public void deleteSubmodels(List<String> submodelIdentifiers, String handleId) {
        queue.add(new BulkDeleteSubmodelData(submodelIdentifiers, handleId));
    }


    private void doCreateShells(List<AssetAdministrationShellDescriptor> shells, String handleId) {
        try {
            // don't call rollbackTransaction when startTransaction fails
            LOGGER.info("createShells start");
            aasRepository.startTransaction();
            doCreateShellsIntern(handleId, shells);
        }
        catch (Exception ex) {
            transactionService.updateState(handleId, ExecutionState.FAILED, ex.getMessage());
            LOGGER.info("createShells error starting transaction: {}", ex.getMessage(), ex);
        }
    }


    private void doCreateShellsIntern(String handleId, List<AssetAdministrationShellDescriptor> shells) {
        try {
            LOGGER.info("createShells execute");
            transactionService.updateState(handleId, ExecutionState.RUNNING);
            for (AssetAdministrationShellDescriptor shell: shells) {
                aasRepository.create(shell);
            }
            Thread.sleep(5000);
            aasRepository.commitTransaction();
            transactionService.updateState(handleId, ExecutionState.COMPLETED);
            LOGGER.info("createShells finished");
        }
        catch (InterruptedException ex) {
            transactionService.updateState(handleId, ExecutionState.FAILED, ex.getMessage());
            aasRepository.rollbackTransaction();
            LOGGER.info("createShells interrupted");
            Thread.currentThread().interrupt();
        }
        catch (Exception ex) {
            transactionService.updateState(handleId, ExecutionState.FAILED, ex.getMessage());
            aasRepository.rollbackTransaction();
            LOGGER.info("createShells error");
        }
    }


    private void doUpdateShells(List<AssetAdministrationShellDescriptor> shells, String handleId) {

        try {
            // don't call rollbackTransaction when startTransaction fails
            aasRepository.startTransaction();
            doUpdateShellsIntern(handleId, shells);
        }
        catch (Exception ex) {
            transactionService.updateState(handleId, ExecutionState.FAILED, ex.getMessage());
            LOGGER.info("updateShells error starting transaction: {}", ex.getMessage(), ex);
        }
    }


    private void doUpdateShellsIntern(String handleId, List<AssetAdministrationShellDescriptor> shells) {
        try {
            LOGGER.debug("updateShells start");
            transactionService.updateState(handleId, ExecutionState.RUNNING);
            for (AssetAdministrationShellDescriptor shell: shells) {
                Ensure.requireNonNull(shell);
                aasRepository.update(shell.getId(), shell);
            }
            aasRepository.commitTransaction();
            transactionService.updateState(handleId, ExecutionState.COMPLETED);
            LOGGER.debug("updateShells finished");
        }
        catch (Exception ex) {
            aasRepository.rollbackTransaction();
            transactionService.updateState(handleId, ExecutionState.FAILED, ex.getMessage());
            LOGGER.info("updateShells error", ex);
        }
    }


    private void doDeleteShells(List<String> shellIdentifiers, String handleId) {

        try {
            // don't call rollbackTransaction when startTransaction fails
            aasRepository.startTransaction();
            doDeleteShellsIntern(handleId, shellIdentifiers);
        }
        catch (Exception ex) {
            transactionService.updateState(handleId, ExecutionState.FAILED, ex.getMessage());
            LOGGER.info("deleteShells error starting transaction: {}", ex.getMessage(), ex);
        }
    }


    private void doDeleteShellsIntern(String handleId, List<String> shellIdentifiers) {
        try {
            LOGGER.debug("deleteShells start");
            transactionService.updateState(handleId, ExecutionState.RUNNING);
            for (String shell: shellIdentifiers) {
                Ensure.requireNonNull(shell);
                aasRepository.deleteAAS(shell);
            }
            aasRepository.commitTransaction();
            transactionService.updateState(handleId, ExecutionState.COMPLETED);
            LOGGER.debug("deleteShells finished");
        }
        catch (Exception ex) {
            aasRepository.rollbackTransaction();
            transactionService.updateState(handleId, ExecutionState.FAILED, ex.getMessage());
            LOGGER.info("deleteShells error", ex);
        }
    }


    private void doCreateSubmodels(List<SubmodelDescriptor> submodels, String handleId) {
        try {
            // don't call rollbackTransaction when startTransaction fails
            LOGGER.info("doCreateSubmodels start");
            aasRepository.startTransaction();
            doCreateSubmodelsIntern(handleId, submodels);
        }
        catch (Exception ex) {
            transactionService.updateState(handleId, ExecutionState.FAILED, ex.getMessage());
            LOGGER.info("doCreateSubmodels error starting transaction: {}", ex.getMessage(), ex);
        }
    }


    private void doCreateSubmodelsIntern(String handleId, List<SubmodelDescriptor> submodels) {
        try {
            LOGGER.info("doCreateSubmodels execute");
            transactionService.updateState(handleId, ExecutionState.RUNNING);
            for (SubmodelDescriptor submodel: submodels) {
                aasRepository.addSubmodel(submodel);
            }
            Thread.sleep(5000);
            aasRepository.commitTransaction();
            transactionService.updateState(handleId, ExecutionState.COMPLETED);
            LOGGER.info("doCreateSubmodels finished");
        }
        catch (InterruptedException ex) {
            transactionService.updateState(handleId, ExecutionState.FAILED, ex.getMessage());
            aasRepository.rollbackTransaction();
            LOGGER.info("doCreateSubmodels interrupted");
            Thread.currentThread().interrupt();
        }
        catch (Exception ex) {
            transactionService.updateState(handleId, ExecutionState.FAILED, ex.getMessage());
            aasRepository.rollbackTransaction();
            LOGGER.info("doCreateSubmodels error");
        }
    }


    private void doUpdateSubmodels(List<SubmodelDescriptor> submodels, String handleId) {
        try {
            // don't call rollbackTransaction when startTransaction fails
            LOGGER.info("doUpdateSubmodels start");
            aasRepository.startTransaction();
            doUpdateSubmodelsIntern(handleId, submodels);
        }
        catch (Exception ex) {
            transactionService.updateState(handleId, ExecutionState.FAILED, ex.getMessage());
            LOGGER.info("doUpdateSubmodels error starting transaction: {}", ex.getMessage(), ex);
        }
    }


    private void doUpdateSubmodelsIntern(String handleId, List<SubmodelDescriptor> submodels) {
        try {
            LOGGER.info("doUpdateSubmodels execute");
            transactionService.updateState(handleId, ExecutionState.RUNNING);
            for (SubmodelDescriptor submodel: submodels) {
                aasRepository.deleteSubmodel(submodel.getId());
                aasRepository.addSubmodel(submodel);
            }
            Thread.sleep(5000);
            aasRepository.commitTransaction();
            transactionService.updateState(handleId, ExecutionState.COMPLETED);
            LOGGER.info("doUpdateSubmodels finished");
        }
        catch (InterruptedException ex) {
            transactionService.updateState(handleId, ExecutionState.FAILED, ex.getMessage());
            aasRepository.rollbackTransaction();
            LOGGER.info("doUpdateSubmodels interrupted");
            Thread.currentThread().interrupt();
        }
        catch (Exception ex) {
            transactionService.updateState(handleId, ExecutionState.FAILED, ex.getMessage());
            aasRepository.rollbackTransaction();
            LOGGER.info("doUpdateSubmodels error");
        }
    }


    private void doDeleteSubmodels(List<String> submodelIdentifiers, String handleId) {
        try {
            // don't call rollbackTransaction when startTransaction fails
            LOGGER.info("doDeleteSubmodels start");
            aasRepository.startTransaction();
            doDeleteSubmodelsIntern(handleId, submodelIdentifiers);
        }
        catch (Exception ex) {
            transactionService.updateState(handleId, ExecutionState.FAILED, ex.getMessage());
            LOGGER.info("doDeleteSubmodels error starting transaction: {}", ex.getMessage(), ex);
        }
    }


    private void doDeleteSubmodelsIntern(String handleId, List<String> submodelIdentifiers) {
        try {
            LOGGER.info("doDeleteSubmodels execute");
            transactionService.updateState(handleId, ExecutionState.RUNNING);
            for (String submodel: submodelIdentifiers) {
                aasRepository.deleteSubmodel(submodel);
            }
            Thread.sleep(5000);
            aasRepository.commitTransaction();
            transactionService.updateState(handleId, ExecutionState.COMPLETED);
            LOGGER.info("doDeleteSubmodels finished");
        }
        catch (InterruptedException ex) {
            transactionService.updateState(handleId, ExecutionState.FAILED, ex.getMessage());
            aasRepository.rollbackTransaction();
            LOGGER.info("doDeleteSubmodels interrupted");
            Thread.currentThread().interrupt();
        }
        catch (Exception ex) {
            transactionService.updateState(handleId, ExecutionState.FAILED, ex.getMessage());
            aasRepository.rollbackTransaction();
            LOGGER.info("doDeleteSubmodels error");
        }
    }
}
