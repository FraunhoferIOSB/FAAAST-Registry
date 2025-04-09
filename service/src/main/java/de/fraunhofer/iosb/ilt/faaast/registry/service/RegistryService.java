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

import de.fraunhofer.iosb.ilt.faaast.registry.core.AasRepository;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.*;
import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.ConstraintHelper;
import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.OperationHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;


/**
 * The service for the registry.
 */
@Service
public class RegistryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryService.class);
    public static final String AAS_NOT_NULL_TXT = "aas must be non-null";
    public static final String SUBMODEL_NOT_NULL_TXT = "submodel must be non-null";
    private final PlatformTransactionManager transactionManager;
    private final BulkOperationStatusStore statusStore; // Spring-managed bean to ensure test context and app context share the same store instance to avoid visibility/race issues with static variables

    public RegistryService(PlatformTransactionManager transactionManager, BulkOperationStatusStore statusStore) {
        this.transactionManager = transactionManager;
        this.statusStore = statusStore;
    }

    @Autowired
    private AasRepository aasRepository;

    /**
     * Retrieves a list of all registered Asset Administration Shells.
     *
     * @param assetType The desired Asset Type.
     * @param assetKind The desired Asset Kind.
     * @param paging The paging information.
     * @return The list of all registered Asset Administration Shells.
     */
    public Page<AssetAdministrationShellDescriptor> getAASs(String assetType, AssetKind assetKind, PagingInfo paging) {
        // Asset type is Base64URL encoded
        String assetTypeDecoded = EncodingHelper.base64UrlDecode(assetType);
        if ((assetTypeDecoded != null) && (assetTypeDecoded.length() > ConstraintHelper.MAX_IDENTIFIER_LENGTH)) {
            throw new BadRequestException("AssetType too long");
        }
        if (assetType != null) {
            LOGGER.atDebug().log("getAASs: AssetType {}", assetType.replaceAll("[\n\r]", "_"));
        }
        if (assetKind != null) {
            LOGGER.debug("getAASs: AssetKind {}", assetKind);
        }
        List<AssetAdministrationShellDescriptor> list = aasRepository.getAASs(assetTypeDecoded, assetKind);
        return preparePagedResult(list, paging);
    }


    /**
     * Retrieves the Asset Administration Shell with the given ID.
     *
     * @param id The ID of the desired Asset Administration Shell.
     * @return The desired Asset Administration Shell.
     * @throws ResourceNotFoundException When the AAS was not found.
     */
    public AssetAdministrationShellDescriptor getAAS(String id) throws ResourceNotFoundException {
        return aasRepository.getAAS(EncodingHelper.base64UrlDecode(id));
    }


    /**
     * Create the given Asset Administration Shell.
     *
     * @param aas The desired Asset Administration Shell.
     * @return The created Asset Administration Shell.
     * @throws ResourceAlreadyExistsException When the AAS already exists.
     */
    public AssetAdministrationShellDescriptor createAAS(AssetAdministrationShellDescriptor aas) throws ResourceAlreadyExistsException {
        ConstraintHelper.validate(aas);
        LOGGER.debug("createAAS: {}", aas.getId());
        if (aas.getSubmodelDescriptors() != null) {
            aas.getSubmodelDescriptors().stream().forEach(this::checkSubmodelIdentifiers);
        }
        return aasRepository.create(aas);
    }


    /**
     * Deletes the Asset Administration Shell with the given ID.
     *
     * @param id The ID of the desired Asset Administration Shell.
     * @throws ResourceNotFoundException When the AAS was not found.
     */
    public void deleteAAS(String id) throws ResourceNotFoundException {
        String idDecoded = EncodingHelper.base64UrlDecode(id);
        LOGGER.debug("deleteAAS: AAS {}", idDecoded);
        aasRepository.deleteAAS(idDecoded);
    }


    /**
     * Updates the given Asset Administration Shell.
     *
     * @param id The ID of the desired Asset Administration Shell.
     * @param aas The desired Asset Administration Shell.
     * @return The updated Asset Administration Shell.
     * @throws ResourceNotFoundException When the AAS was not found.
     */
    public AssetAdministrationShellDescriptor updateAAS(String id, AssetAdministrationShellDescriptor aas) throws ResourceNotFoundException {
        Ensure.requireNonNull(aas, AAS_NOT_NULL_TXT);
        String idDecoded = EncodingHelper.base64UrlDecode(id);
        LOGGER.debug("updateAAS: {}", idDecoded);
        checkShellIdentifiers(aas);
        aas.getSubmodelDescriptors().stream().forEach(this::checkSubmodelIdentifiers);
        return aasRepository.update(idDecoded, aas);
    }


    /**
     * Retrieves a list of all registered Submodels.
     *
     * @param paging The paging information.
     * @return The list of Submodels.
     * @throws ResourceNotFoundException When the AAS was not found.
     */
    public Page<SubmodelDescriptor> getSubmodels(PagingInfo paging) throws ResourceNotFoundException {
        return getSubmodels(null, paging);
    }


    /**
     * Retrieves a list of all Submodels of the given Asset Administration Shell.
     *
     * @param aasId The ID of the desired Asset Administration Shell.
     * @param paging The paging information.
     * @return The list of Submodels.
     * @throws ResourceNotFoundException When the AAS was not found.
     */
    public Page<SubmodelDescriptor> getSubmodels(String aasId, PagingInfo paging) throws ResourceNotFoundException {
        List<SubmodelDescriptor> list;
        if (aasId == null) {
            list = aasRepository.getSubmodels();
        }
        else {
            String aasIdDecoded = EncodingHelper.base64UrlDecode(aasId);
            list = aasRepository.getSubmodels(aasIdDecoded);
        }
        return preparePagedResult(list, paging);
    }


    /**
     * Retrieves the Submodel with given Submodel ID.
     *
     * @param submodelId The ID of the desired Submodel.
     * @return The desired Submodel.
     * @throws ResourceNotFoundException When the Submodel was not found.
     */
    public SubmodelDescriptor getSubmodel(String submodelId) throws ResourceNotFoundException {
        return getSubmodel(null, submodelId);
    }


    /**
     * Retrieves the Submodel with given AAS ID and Submodel ID.
     *
     * @param aasId The ID of the desired Asset Administration Shell.
     * @param submodelId The ID of the desired Submodel.
     * @return The desired Submodel.
     * @throws ResourceNotFoundException When the AAS or Submodel was not found.
     */
    public SubmodelDescriptor getSubmodel(String aasId, String submodelId) throws ResourceNotFoundException {
        String submodelIdDecoded = EncodingHelper.base64UrlDecode(submodelId);
        if (aasId == null) {
            return aasRepository.getSubmodel(submodelIdDecoded);
        }
        else {
            String aasIdDecoded = EncodingHelper.base64UrlDecode(aasId);
            return aasRepository.getSubmodel(aasIdDecoded, submodelIdDecoded);
        }
    }


    /**
     * Creates a new submodel.
     *
     * @param submodel The desired submodel.
     * @return The created submodel.
     * @throws ResourceNotFoundException When the AAS was not found.
     * @throws ResourceAlreadyExistsException When the Submodel already exists.
     */
    public SubmodelDescriptor createSubmodel(SubmodelDescriptor submodel) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        return createSubmodel(null, submodel);
    }


    /**
     * Create a new Submodel in the given AAS.
     *
     * @param aasId The ID of the desired AAS.
     * @param submodel The submodel to add.
     * @return The descriptor of the created submodel.
     * @throws ResourceNotFoundException When the AAS was not found.
     * @throws ResourceAlreadyExistsException When the Submodel already exists.
     */
    public SubmodelDescriptor createSubmodel(String aasId, SubmodelDescriptor submodel) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        ConstraintHelper.validate(submodel);
        if (aasId == null) {
            LOGGER.debug("createSubmodel: Submodel {}", submodel.getId());
            return aasRepository.addSubmodel(submodel);
        }
        else {
            String aasIdDecoded = EncodingHelper.base64UrlDecode(aasId);
            LOGGER.debug("createSubmodel: AAS '{}'; Submodel {}", aasIdDecoded, submodel.getId());
            return aasRepository.addSubmodel(aasIdDecoded, submodel);
        }
    }


    /**
     * Deletes the Submodel with the given ID.
     *
     * @param submodelId The ID of the desired Submodel.
     * @throws ResourceNotFoundException When the Submodel was not found.
     */
    public void deleteSubmodel(String submodelId) throws ResourceNotFoundException {
        deleteSubmodel(null, submodelId);
    }


    /**
     * Deletes the Submodel with the given AAS ID and Submodel ID.
     *
     * @param aasId The ID of the desired AAS.
     * @param submodelId The ID of the desired Submodel.
     * @throws ResourceNotFoundException When the Submodel was not found.
     */
    public void deleteSubmodel(String aasId, String submodelId) throws ResourceNotFoundException {
        String submodelIdDecoded = EncodingHelper.base64UrlDecode(submodelId);
        if (aasId == null) {
            LOGGER.debug("deleteSubmodel: Submodel {}", submodelIdDecoded);
            aasRepository.deleteSubmodel(submodelIdDecoded);
        }
        else {
            String aasIdDecoded = EncodingHelper.base64UrlDecode(aasId);
            LOGGER.debug("deleteSubmodel: AAS '{}'; Submodel {}", aasIdDecoded, submodelIdDecoded);
            aasRepository.deleteSubmodel(aasIdDecoded, submodelIdDecoded);
        }
    }


    /**
     * Updates the given Submodel.
     *
     * @param submodelId The ID of the desired Submodel.
     * @param submodel The desired Submodel.
     * @return The updated Submodel.
     * @throws ResourceNotFoundException When the Submodel was not found.
     * @throws ResourceAlreadyExistsException When the Submodel already exists.
     */
    public SubmodelDescriptor updateSubmodel(String submodelId, SubmodelDescriptor submodel) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        Ensure.requireNonNull(submodel, SUBMODEL_NOT_NULL_TXT);
        String submodelIdDecoded = EncodingHelper.base64UrlDecode(submodelId);
        checkSubmodelIdentifiers(submodel);
        LOGGER.debug("updateSubmodel: Submodel {}", submodelIdDecoded);
        aasRepository.deleteSubmodel(submodelIdDecoded);
        return aasRepository.addSubmodel(submodel);
    }


    /**
     * Updates the given Submodel.
     *
     * @param aasId The ID of the desired AAS.
     * @param submodelId The ID of the desired Submodel.
     * @param submodel The desired Submodel.
     * @return The updated Submodel.
     * @throws ResourceNotFoundException When the AAS was not found.
     * @throws ResourceAlreadyExistsException When the Submodel already exists.
     */
    public SubmodelDescriptor updateSubmodel(String aasId, String submodelId, SubmodelDescriptor submodel) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        Ensure.requireNonNull(submodel, SUBMODEL_NOT_NULL_TXT);
        String aasIdDecoded = EncodingHelper.base64UrlDecode(aasId);
        String submodelIdDecoded = EncodingHelper.base64UrlDecode(submodelId);
        checkSubmodelIdentifiers(submodel);
        LOGGER.debug("updateSubmodel: AAS '{}'; Submodel {}", aasIdDecoded, submodelIdDecoded);
        aasRepository.deleteSubmodel(aasIdDecoded, submodelIdDecoded);
        return aasRepository.addSubmodel(aasIdDecoded, submodel);
    }


    /**
     * Bulk operation for creating multiple submodel descriptors.
     *
     * @param submodels The desired submodel.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     */
    public void bulkCreateSubmodels(List<SubmodelDescriptor> submodels) throws BadRequestException, UnauthorizedException, ForbiddenException, InternalServerErrorException {
        // todo: Change this to loop over all submodels. Use transactions
    }


    /**
     * Updates the given Submodels.
     *
     * @param submodels The desired Submodels.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     */
    public void bulkUpdateSubmodels(List<SubmodelDescriptor> submodels) throws BadRequestException, UnauthorizedException, ForbiddenException, InternalServerErrorException {
        // todo: Change this to loop over all submodels. Use transactions
    }


    /**
     * Bulk operation for deleting multiple submodel descriptors with the given IDs.
     *
     * @param submodelIdentifiers The ID of the desired Submodels.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ResourceNotFoundException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     */
    public void bulkDeleteSubmodels(List<String> submodelIdentifiers) throws BadRequestException, UnauthorizedException, ResourceNotFoundException, InternalServerErrorException {
        // todo: Change this to loop over all submodels. Use transactions
    }


    /**
     * Bulk operation for creating multiple aas descriptors.
     *
     * @param shells The desired asset administration shell descriptors.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     * @return handleId
     */
    public String bulkCreateShells(List<AssetAdministrationShellDescriptor> shells)
            throws BadRequestException, UnauthorizedException, ForbiddenException, InternalServerErrorException {

        ConstraintHelper.validate(shells);
        String handleId = OperationHelper.generateOperationHandleId();

        statusStore.setStatus(handleId, ExecutionState.INITIATED);

        CompletableFuture.runAsync(() -> {
            TransactionStatus txStatus = null;
            try {
                statusStore.setStatus(handleId, ExecutionState.RUNNING);
                txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

                for (AssetAdministrationShellDescriptor shell: shells) {
                    aasRepository.create(shell);
                }

                transactionManager.commit(txStatus);
                statusStore.setStatus(handleId, ExecutionState.COMPLETED);
            }
            catch (Exception e) {
                if (txStatus != null) {
                    transactionManager.rollback(txStatus);
                }
                statusStore.setStatus(handleId, ExecutionState.FAILED);
            }
        });

        return handleId;
    }


    /**
     * Bulk operation for updating multiple aas descriptors.
     *
     * @param shells The desired aas.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     */
    public void bulkUpdateShells(List<AssetAdministrationShellDescriptor> shells)
            throws BadRequestException, UnauthorizedException, ForbiddenException, InternalServerErrorException {
        // todo: Change this to loop over all shells. Use transactions
    }


    /**
     * Bulk operation for deleting multiple aas descriptors with the given IDs.
     *
     * @param shellIdentifiers The ID of the desired aas.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ResourceNotFoundException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     */
    public void bulkDeleteShells(List<String> shellIdentifiers) throws BadRequestException, UnauthorizedException, ResourceNotFoundException, InternalServerErrorException {
        // todo: Change this to loop over all shells. Use transactions
    }


    /**
     * Returns the status of an asynchronously invoked bulk operation.
     *
     * @param handleId the id for retrieving the bulk operation result object.
     * @throws BadRequestException an error occurs.
     * @throws MovedPermanentlyException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     * @throws ResourceNotFoundException an error occurs.
     */
    public OperationResult getBulkOperationStatus(String handleId)
            throws MovedPermanentlyException, UnauthorizedException, ForbiddenException, ResourceNotFoundException, InternalServerErrorException {
        ExecutionState status = statusStore.getStatus(handleId);

        if (status == null) {
            throw new ResourceNotFoundException("Unknown handleId: " + handleId);
        }

        if (status == ExecutionState.RUNNING) {
            DefaultOperationResult operationResult = new DefaultOperationResult();
            operationResult.setExecutionState(status);
            return operationResult;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/bulk/result/" + handleId));
        throw new MovedPermanentlyException("Operation completed. See result endpoint.", headers);
    }


    /**
     * Returns the status of an asynchronously invoked bulk operation.
     *
     * @param handleId the id for retrieving the bulk operation result object.
     * @throws BadRequestException an error occurs.
     * @throws MovedPermanentlyException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     * @throws ResourceNotFoundException an error occurs.
     */
    public void getBulkOperationResult(String handleId)
            throws MovedPermanentlyException, UnauthorizedException, ForbiddenException, ResourceNotFoundException, InternalServerErrorException {
        ExecutionState status = statusStore.getStatus(handleId);
        if (status == null || status == ExecutionState.RUNNING) {
            throw new ResourceNotFoundException("Result not available or still running for handleId: " + handleId);
        }

        if (status == ExecutionState.COMPLETED) {
            return;
        }

        throw new BadRequestException("One or more items failed.");
    }


    private void checkSubmodelIdentifiers(SubmodelDescriptor submodel) throws BadRequestException {
        Ensure.requireNonNull(submodel, SUBMODEL_NOT_NULL_TXT);
        if ((submodel.getId() == null) || (submodel.getId().isEmpty())) {
            throw new BadRequestException("no Submodel identification provided");
        }
    }


    private void checkShellIdentifiers(AssetAdministrationShellDescriptor aas) throws BadRequestException {
        Ensure.requireNonNull(aas, AAS_NOT_NULL_TXT);
        if ((aas.getId() == null) || (aas.getId().isEmpty())) {
            throw new BadRequestException("no AAS Identification provided");
        }
    }


    private static <T> Page<T> preparePagedResult(List<T> input, PagingInfo paging) {
        Stream<T> result = input.stream();
        if (Objects.nonNull(paging.getCursor())) {
            long skip = readCursor(paging.getCursor());
            if (skip < 0 || skip >= input.size()) {
                throw new BadRequestException(String.format("invalid cursor (cursor: %s)", paging.getCursor()));
            }
            result = result.skip(skip);
        }
        if (paging.hasLimit()) {
            if (paging.getLimit() < 1) {
                throw new BadRequestException(String.format("invalid limit - must be >= 1 (actual: %s)", paging.getLimit()));
            }
            result = result.limit(paging.getLimit() + 1);
        }
        List<T> temp = result.toList();
        return Page.<T> builder()
                .result(temp.stream()
                        .limit(paging.hasLimit() ? paging.getLimit() : temp.size())
                        .toList())
                .metadata(PagingMetadata.builder()
                        .cursor(nextCursor(paging, temp.size()))
                        .build())
                .build();
    }


    private static long readCursor(String cursor) {
        return Long.parseLong(cursor);
    }


    private static String writeCursor(long index) {
        return Long.toString(index);
    }


    private static String nextCursor(PagingInfo paging, int resultCount) {
        return nextCursor(paging, paging.hasLimit() && resultCount > paging.getLimit());
    }


    private static String nextCursor(PagingInfo paging, boolean hasMoreData) {
        if (!hasMoreData) {
            return null;
        }
        if (!paging.hasLimit()) {
            throw new IllegalStateException("unable to generate next cursor for paging - there should not be more data available if previous request did not have a limit set");
        }
        if (Objects.isNull(paging.getCursor())) {
            return writeCursor(paging.getLimit());
        }
        return writeCursor(readCursor(paging.getCursor()) + paging.getLimit());
    }
}
