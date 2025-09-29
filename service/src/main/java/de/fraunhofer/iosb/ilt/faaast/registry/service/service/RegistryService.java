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
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.*;
import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.ConstraintHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * The service for the registry.
 */
@Service
public class RegistryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryService.class);
    public static final String AAS_NOT_NULL_TXT = "aas must be non-null";
    public static final String SUBMODEL_NOT_NULL_TXT = "submodel must be non-null";

    private final AasRepository aasRepository;
    private final TransactionService transactionService;

    @Autowired
    public RegistryService(AasRepository aasRepository, TransactionService transactionService) {
        this.aasRepository = aasRepository;
        this.transactionService = transactionService;
    }


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
        return aasRepository.getAASs(assetTypeDecoded, assetKind, paging);
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
        aasRepository.startTransaction();
        try {
            AssetAdministrationShellDescriptor retval = aasRepository.create(aas);
            aasRepository.commitTransaction();
            return retval;
        }
        catch (Exception ex) {
            aasRepository.rollbackTransaction();
            throw ex;
        }
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
        Page<SubmodelDescriptor> retval;
        if (aasId == null) {
            retval = aasRepository.getSubmodels(paging);
        }
        else {
            String aasIdDecoded = EncodingHelper.base64UrlDecode(aasId);
            retval = aasRepository.getSubmodels(aasIdDecoded, paging);
        }
        return retval;
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
     * @return The transaction handle.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     * @throws InterruptedException The operation was interrupted.
     */
    public String bulkCreateSubmodels(List<SubmodelDescriptor> submodels)
            throws BadRequestException, UnauthorizedException, ForbiddenException, InternalServerErrorException, InterruptedException {
        ConstraintHelper.validateSubmodels(submodels);
        return transactionService.createSubmodels(submodels);
    }


    /**
     * Updates the given Submodels.
     *
     * @param submodels The desired Submodels.
     * @return The transaction handle.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     */
    public String bulkUpdateSubmodels(List<SubmodelDescriptor> submodels) throws BadRequestException, UnauthorizedException, ForbiddenException, InternalServerErrorException {
        // todo: Change this to loop over all submodels. Use transactions
        return "";
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
     * @return The transaction handle.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     * @throws ResourceAlreadyExistsException When an AAS already exists.
     * @throws InterruptedException The operation was interrupted.
     */
    //@Async
    //@Transactional
    public String bulkCreateShells(List<AssetAdministrationShellDescriptor> shells)
            throws BadRequestException, UnauthorizedException, ForbiddenException, InternalServerErrorException, ResourceAlreadyExistsException, InterruptedException {

        ConstraintHelper.validate(shells);
        return transactionService.createShells(shells);
    }


    /**
     * Bulk operation for updating multiple aas descriptors.
     *
     * @param shells The desired aas.
     * @return The transaction handle.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     * @throws InterruptedException The execution was interrupted.
     */
    //@Async
    public String bulkUpdateShells(List<AssetAdministrationShellDescriptor> shells)
            throws BadRequestException, UnauthorizedException, ForbiddenException, InternalServerErrorException, InterruptedException {
        ConstraintHelper.validate(shells);
        return transactionService.updateShells(shells);
    }


    /**
     * Bulk operation for deleting multiple aas descriptors with the given IDs.
     *
     * @param shellIdentifiers The ID of the desired aas.
     * @return The transaction handle.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ResourceNotFoundException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     * @throws java.lang.InterruptedException
     */
    //@Async
    public String bulkDeleteShells(List<String> shellIdentifiers)
            throws BadRequestException, UnauthorizedException, ResourceNotFoundException, InternalServerErrorException, InterruptedException {
        return transactionService.deleteShells(shellIdentifiers);
    }


    /**
     * Returns the status of an asynchronously invoked bulk operation.
     *
     * @param handleId the id for retrieving the bulk operation result object.
     * @return The operation result.
     * @throws BadRequestException an error occurs.
     * @throws MovedPermanentlyException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     * @throws ResourceNotFoundException an error occurs.
     */
    public OperationResult getBulkOperationStatus(String handleId)
            throws MovedPermanentlyException, UnauthorizedException, ForbiddenException, ResourceNotFoundException, InternalServerErrorException {
        return transactionService.getStatus(handleId);
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
        transactionService.getResult(handleId);
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

}
