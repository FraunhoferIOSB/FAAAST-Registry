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
import de.fraunhofer.iosb.ilt.faaast.registry.service.model.BulkDeleteShellData;
import de.fraunhofer.iosb.ilt.faaast.registry.service.model.BulkUpdateShellData;
import de.fraunhofer.iosb.ilt.faaast.registry.service.service.TransactionService;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState;
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
        //long startTime = System.currentTimeMillis();
        //int i = 0;
        while (!ende) {
            try {
                // TODO: queue handling
                Object obj = queue.take();
                while (aasRepository.getTransactionActive()) {
                    LOGGER.debug("createShells: wait for transaction to finish");
                    Thread.sleep(50);
                }
                
                if (obj instanceof BulkCreateShellData createData) {
                    doCreateShells(createData.getShells(), createData.getHandleId());
                }
                else if (obj instanceof BulkUpdateShellData updateData) {
                    doUpdateShells(updateData.getShells(), updateData.getHandleId());
                }
                else if (obj instanceof BulkDeleteShellData deleteData) {
                    doDeleteShells(deleteData.getShellIdentifiers(), deleteData.getHandleId());
                }
                
                //Wait for one sec so it doesn't print too fast
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.warn("TransactionThread interrupted");
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


    private void doCreateShells(List<AssetAdministrationShellDescriptor> shells, String handleId) throws InterruptedException {
        //statusStore.setStatus(handleId, ExecutionState.INITIATED);

        //while (aasRepository.getTransactionActive()) {
        //    LOGGER.debug("createShells: wait for transaction to finish");
        //    synchronized (MONITOR) {
        //        MONITOR.wait();
        //    }
        //    //Thread.sleep(100);
        //}

        try {
            // don't call rollbackTransaction when startTransaction fails
            LOGGER.info("createShells start");
            aasRepository.startTransaction();
            try {
                LOGGER.info("createShells execute");
                transactionService.updateState(handleId, ExecutionState.RUNNING);
                for (AssetAdministrationShellDescriptor shell: shells) {
                    aasRepository.create(shell);
                    //statusStore.setStatus(handleId, ExecutionState.RUNNING);
                }
                Thread.sleep(5000);
                aasRepository.commitTransaction();
                transactionService.updateState(handleId, ExecutionState.COMPLETED);
                LOGGER.info("createShells finished");
            }
            catch (Exception ex) {
                transactionService.updateState(handleId, ExecutionState.FAILED);
                aasRepository.rollbackTransaction();
                LOGGER.info("createShells error");
            }
        }
        catch (Exception ex) {
            transactionService.updateState(handleId, ExecutionState.FAILED);
            LOGGER.info("createShells error starting transaction: {}", ex.getMessage(), ex);
            //throw ex;
        }
        //finally {
        //    synchronized (MONITOR) {
        //        LOGGER.debug("createShells: notify next thread");
        //        MONITOR.notify();
        //    }
        //}
    }


    private void doUpdateShells(List<AssetAdministrationShellDescriptor> shells, String handleId) throws InterruptedException {
        //statusStore.setStatus(handleId, ExecutionState.INITIATED);

        //while (aasRepository.getTransactionActive()) {
        //    LOGGER.debug("updateShells: wait for transaction to finish");
        //    synchronized (MONITOR) {
        //        MONITOR.wait();
        //    }
        //}

        try {
            // don't call rollbackTransaction when startTransaction fails
            aasRepository.startTransaction();
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
                transactionService.updateState(handleId, ExecutionState.FAILED);
                LOGGER.info("updateShells error", ex);
            }
        }
        catch (Exception ex) {
            transactionService.updateState(handleId, ExecutionState.FAILED);
            LOGGER.info("updateShells error starting transaction: {}", ex.getMessage(), ex);
            //throw ex;
        }
        //finally {
        //    synchronized (MONITOR) {
        //        LOGGER.debug("updateShells: notify next thread");
        //        MONITOR.notify();
        //    }
        //}
    }


    private void doDeleteShells(List<String> shellIdentifiers, String handleId) throws InterruptedException {
        //statusStore.setStatus(handleId, ExecutionState.INITIATED);

        //while (aasRepository.getTransactionActive()) {
        //    LOGGER.debug("deleteShells: wait for transaction to finish");
        //    synchronized (MONITOR) {
        //        MONITOR.wait();
        //    }
        //}

        try {
            // don't call rollbackTransaction when startTransaction fails
            aasRepository.startTransaction();
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
                transactionService.updateState(handleId, ExecutionState.FAILED);
                LOGGER.info("deleteShells error", ex);
            }
        }
        catch (Exception ex) {
            transactionService.updateState(handleId, ExecutionState.FAILED);
            LOGGER.info("deleteShells error starting transaction: {}", ex.getMessage(), ex);
        }
        //finally {
        //    synchronized (MONITOR) {
        //        LOGGER.debug("deleteShells: notify next thread");
        //        MONITOR.notify();
        //    }
        //}
    }
}
