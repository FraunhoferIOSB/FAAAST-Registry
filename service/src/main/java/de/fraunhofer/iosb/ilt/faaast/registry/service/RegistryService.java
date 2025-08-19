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
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.BadRequestException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.ConstraintHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
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
        //List<AssetAdministrationShellDescriptor> list = aasRepository.getAASs(assetTypeDecoded, assetKind);
        //return preparePagedResult(list, paging);
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


    private void checkSubmodelIdentifiers(SubmodelDescriptor submodel) throws BadRequestException {
        Ensure.requireNonNull(submodel, SUBMODEL_NOT_NULL_TXT);
        if ((submodel.getId() == null) || (submodel.getId().length() == 0)) {
            throw new BadRequestException("no Submodel identification provided");
        }
    }


    private void checkShellIdentifiers(AssetAdministrationShellDescriptor aas) throws BadRequestException {
        Ensure.requireNonNull(aas, AAS_NOT_NULL_TXT);
        if ((aas.getId() == null) || (aas.getId().length() == 0)) {
            throw new BadRequestException("no AAS Identification provided");
        }
    }

    //    private static <T> Page<T> preparePagedResult(List<T> input, PagingInfo paging) {
    //        Stream<T> result = input.stream();
    //        if (Objects.nonNull(paging.getCursor())) {
    //            long skip = readCursor(paging.getCursor());
    //            if (skip < 0 || skip >= input.size()) {
    //                throw new BadRequestException(String.format("invalid cursor (cursor: %s)", paging.getCursor()));
    //            }
    //            result = result.skip(skip);
    //        }
    //        if (paging.hasLimit()) {
    //            if (paging.getLimit() < 1) {
    //                throw new BadRequestException(String.format("invalid limit - must be >= 1 (actual: %s)", paging.getLimit()));
    //            }
    //            result = result.limit(paging.getLimit() + 1);
    //        }
    //        List<T> temp = result.toList();
    //        return Page.<T> builder()
    //                .result(temp.stream()
    //                        .limit(paging.hasLimit() ? paging.getLimit() : temp.size())
    //                        .toList())
    //                .metadata(PagingMetadata.builder()
    //                        .cursor(nextCursor(paging, temp.size()))
    //                        .build())
    //                .build();
    //    }
    //
    //
    //    private static long readCursor(String cursor) {
    //        return Long.parseLong(cursor);
    //    }
    //
    //
    //    private static String writeCursor(long index) {
    //        return Long.toString(index);
    //    }
    //
    //
    //    private static String nextCursor(PagingInfo paging, int resultCount) {
    //        return nextCursor(paging, paging.hasLimit() && resultCount > paging.getLimit());
    //    }
    //
    //
    //    private static String nextCursor(PagingInfo paging, boolean hasMoreData) {
    //        if (!hasMoreData) {
    //            return null;
    //        }
    //        if (!paging.hasLimit()) {
    //            throw new IllegalStateException("unable to generate next cursor for paging - there should not be more data available if previous request did not have a limit set");
    //        }
    //        if (Objects.isNull(paging.getCursor())) {
    //            return writeCursor(paging.getLimit());
    //        }
    //        return writeCursor(readCursor(paging.getCursor()) + paging.getLimit());
    //    }
}
