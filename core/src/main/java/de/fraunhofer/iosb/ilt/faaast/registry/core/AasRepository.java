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
package de.fraunhofer.iosb.ilt.faaast.registry.core;

import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.Query;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;


/**
 * AAS Registry main repository.
 */
public interface AasRepository {
    static final int DEFAULT_LIMIT = 500;

    /**
     * Retrieves a list of all registered Asset Administration Shells.
     *
     * @param paging The desired Paging info.
     * @return The list of all registered Asset Administration Shells.
     */
    public Page<AssetAdministrationShellDescriptor> getAASs(PagingInfo paging);


    /**
     * Retrieves a list of all registered Asset Administration Shells which meet the given conditions.
     *
     * @param assetType The desired Asset Type.
     * @param assetKind The desired Asset Kind.
     * @param paging The desired Paging info.
     * @return The list of the registered Asset Administration Shells for the desired page.
     */
    public Page<AssetAdministrationShellDescriptor> getAASs(String assetType, AssetKind assetKind, PagingInfo paging);


    /**
     * Retrieves the Asset Administration Shell with the given ID.
     *
     * @param aasId The ID of the desired Asset Administration Shell.
     * @return The desired Asset Administration Shell.
     * @throws ResourceNotFoundException if the requested resource does not exist
     */
    public AssetAdministrationShellDescriptor getAAS(String aasId) throws ResourceNotFoundException;


    /**
     * Retrieves the Asset Administration Shells Identifiers with the given SpecificAssetIDs. *All* of the SpecificAssetIds
     * must match.
     *
     * @param specificAssetIds The SpecificAssetIDs of the desired Asset Administration Shells. If a specificAssetId is a
     *            globalAssetId according to AASd-116, it is treated
     *            as the globalAssetId of the desired Asset Administration Shells.
     * @return The desired Asset Administration Shells identifiers.
     */
    public Page<String> getAASIdentifiers(List<SpecificAssetId> specificAssetIds, PagingInfo pagingInfo);


    /**
     * Create the given Asset Administration Shell.
     *
     * @param descriptor The desired Asset Administration Shell.
     * @return The deleted Asset Administration Shell.
     * @throws ResourceAlreadyExistsException if the resource already exists
     */
    public AssetAdministrationShellDescriptor create(AssetAdministrationShellDescriptor descriptor) throws ResourceAlreadyExistsException;


    /**
     * Deletes the Asset Administration Shell with the given ID.
     *
     * @param aasId The ID of the desired Asset Administration Shell.
     * @throws ResourceNotFoundException if the requested resource does not exist
     */
    public void deleteAAS(String aasId) throws ResourceNotFoundException;


    /**
     * Updates the given Asset Administration Shell.
     *
     * @param aasId The ID of the desired Asset Administration Shell.
     * @param descriptor The desired Asset Administration Shell.
     * @return The updated Asset Administration Shell.
     * @throws ResourceNotFoundException if the requested resource does not exist
     */
    public AssetAdministrationShellDescriptor update(String aasId, AssetAdministrationShellDescriptor descriptor) throws ResourceNotFoundException;


    /**
     * Retrieves a list of all Submodels of the given Asset Administration Shell.
     *
     * @param aasId The ID of the desired Asset Administration Shell.
     * @param paging The desired Paging info.
     * @return The list of Submodels for the desired page.
     * @throws ResourceNotFoundException if the requested resource does not exist
     */
    public Page<SubmodelDescriptor> getSubmodels(String aasId, PagingInfo paging) throws ResourceNotFoundException;


    /**
     * Retrieves a list of all registered Submodels.
     *
     * @param paging The desired Paging info.
     * @return The list of Submodels for the desired page.
     */
    public Page<SubmodelDescriptor> getSubmodels(PagingInfo paging);


    /**
     * Retrieves the Submodel with given AAS ID and Submodel ID.
     *
     * @param aasId The ID of the desired Asset Administration Shell.
     * @param submodelId The ID of the desired Submodel.
     * @return The desired Submodel.
     * @throws ResourceNotFoundException if the requested resource does not exist
     */
    public SubmodelDescriptor getSubmodel(String aasId, String submodelId) throws ResourceNotFoundException;


    /**
     * Retrieves the Submodel with given Submodel ID.
     *
     * @param submodelId The ID of the desired Submodel.
     * @return The desired Submodel.
     * @throws ResourceNotFoundException if the requested resource does not exist
     */
    public SubmodelDescriptor getSubmodel(String submodelId) throws ResourceNotFoundException;


    /**
     * Adds a Submodel to the given AAS.
     *
     * @param aasId The ID of the desired AAS.
     * @param descriptor The submodel to add.
     * @return The descriptor of the created submodel.
     * @throws ResourceNotFoundException if the aas does not exist
     * @throws ResourceAlreadyExistsException if the submodel already exists
     */
    public SubmodelDescriptor addSubmodel(String aasId, SubmodelDescriptor descriptor) throws ResourceNotFoundException, ResourceAlreadyExistsException;


    /**
     * Adds a Submodel to the given AAS.
     *
     * @param descriptor The submodel to add.
     * @return The descriptor of the created submodel.
     * @throws ResourceAlreadyExistsException if the submodel already exists
     */
    public SubmodelDescriptor addSubmodel(SubmodelDescriptor descriptor) throws ResourceAlreadyExistsException;


    /**
     * Deletes the Submodel with the given AAS ID and Submodel ID.
     *
     * @param aasId The ID of the desired AAS.
     * @param submodelId The ID of the desired Submodel.
     * @throws ResourceNotFoundException if the requested resource does not exist
     */
    public void deleteSubmodel(String aasId, String submodelId) throws ResourceNotFoundException;


    /**
     * Deletes the Submodel with the given ID.
     *
     * @param submodelId The ID of the desired Submodel.
     * @throws ResourceNotFoundException if the requested resource does not exist
     */
    public void deleteSubmodel(String submodelId) throws ResourceNotFoundException;


    /**
     * Starts a transaction.
     */
    public void startTransaction();


    /**
     * Commits a Transaction.
     */
    public void commitTransaction();


    /**
     * Rollback a Transaction.
     */
    public void rollbackTransaction();


    /**
     * Returns a value indicating whether a transaction is active.
     *
     * @return True when a transaction is active, false otherwise.
     */
    public boolean getTransactionActive();


    /**
     * Clears the repos and deletes all descriptors.
     * Only used for tests!
     */
    public void clear();


    /**
     * Execute queries for specific AASs.
     *
     * @param query The desired query.
     * @param paging The requested Paging info.
     * @return The list of matching Asset Administration Shells.
     */
    public Page<AssetAdministrationShellDescriptor> queryAASs(Query query, PagingInfo paging);


    /**
     * Execute queries for specific Submodels.
     *
     * @param query The desired query.
     * @param paging The requested Paging info.
     * @return The list of matching Submodels.
     */
    public Page<SubmodelDescriptor> querySubmodels(Query query, PagingInfo paging);
}
