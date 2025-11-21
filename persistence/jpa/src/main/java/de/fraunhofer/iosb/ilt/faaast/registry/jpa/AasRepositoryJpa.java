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
package de.fraunhofer.iosb.ilt.faaast.registry.jpa;

import de.fraunhofer.iosb.ilt.faaast.registry.core.AbstractAasRepository;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaAssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaSubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JpaSubmodelDescriptorStandalone;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.util.EntityManagerHelper;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.util.ModelTransformationHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.FaaastConstants;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;


/**
 * Relational database implementation of the Repository.
 */
@Repository
public class AasRepositoryJpa extends AbstractAasRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AasRepositoryJpa.class);

    @PersistenceContext
    private EntityManager entityManager;

    private PlatformTransactionManager txManager;
    private TransactionStatus transactionStatus;

    @Autowired
    public AasRepositoryJpa(PlatformTransactionManager txManager) {
        this.txManager = txManager;
    }


    /**
     * Constructor with EntityManager as parameter.
     * Used only for unit test.
     *
     * @param entityManager The desired EntityManager.
     */
    public AasRepositoryJpa(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    @Override
    public Page<AssetAdministrationShellDescriptor> getAASs(String assetType, AssetKind assetKind, PagingInfo paging) {
        return EntityManagerHelper.getPagedAas(entityManager, assetType, assetKind, readLimit(paging), readCursor(paging));
    }


    @Override
    public AssetAdministrationShellDescriptor getAAS(String aasId) throws ResourceNotFoundException {
        Ensure.requireNonNull(aasId, "id must be non-null");
        AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
        Ensure.requireNonNull(aas, buildAASNotFoundException(aasId));
        return aas;
    }


    @Override
    public Page<String> getAASIdentifiers(List<SpecificAssetId> specificAssetIds, PagingInfo pagingInfo) {
        Ensure.requireNonNull(specificAssetIds, "specificAssetIds must be non-null");

        List<SpecificAssetId> globalAssetIds = specificAssetIds.stream()
                .filter(specificAssetId -> FaaastConstants.KEY_GLOBAL_ASSET_ID.equalsIgnoreCase(specificAssetId.getName()))
                .toList();

        String globalAssetIdString = null;

        if (globalAssetIds.size() > 1) {
            // An AAS descriptor can only have one globalAssetId.
            return Page.of();
        }
        else if (!globalAssetIds.isEmpty()) {
            SpecificAssetId globalAssetId = globalAssetIds.get(0);
            // Disentangle specificAssetId from globalAssetId
            specificAssetIds.remove(globalAssetId);
            globalAssetIdString = globalAssetIds.get(0).getValue();
        }

        Map<String, String> specificAssetIdNameValueMap = new HashMap<>();
        specificAssetIds.forEach(id -> specificAssetIdNameValueMap.put(id.getName(), id.getValue()));

        // Pre-filter to get subset of descriptors matching most commonly defined fields in a specific asset id (name,value) and global asset id
        List<AssetAdministrationShellDescriptor> prefilteredDescriptors = EntityManagerHelper.getAas(entityManager, specificAssetIdNameValueMap, globalAssetIdString);

        // We already filtered for global asset id -> No need to add it to specific asset ids again
        return filterAssetAdministrationShellDescriptors(prefilteredDescriptors, specificAssetIds, pagingInfo);
    }


    @Override
    public AssetAdministrationShellDescriptor create(AssetAdministrationShellDescriptor descriptor) throws ResourceAlreadyExistsException {
        AssetAdministrationShellDescriptor retval;
        if (transactionStatus != null) {
            retval = doCreate(descriptor);
        }
        else {
            // use internal transaction
            startTransaction();
            try {
                retval = doCreate(descriptor);
                commitTransaction();
            }
            catch (Exception ex) {
                rollbackTransaction();
                throw ex;
            }
        }
        return retval;
    }


    @Override
    public void deleteAAS(String aasId) throws ResourceNotFoundException {
        if (transactionStatus != null) {
            doDeleteAAS(aasId);
        }
        else {
            // use internal transaction
            startTransaction();
            try {
                doDeleteAAS(aasId);
                commitTransaction();
            }
            catch (Exception ex) {
                rollbackTransaction();
                throw ex;
            }
        }
    }


    @Override
    public AssetAdministrationShellDescriptor update(String aasId, AssetAdministrationShellDescriptor descriptor) throws ResourceNotFoundException {
        AssetAdministrationShellDescriptor retval;
        if (transactionStatus != null) {
            retval = doUpdate(aasId, descriptor);
        }
        else {
            // use internal transaction
            startTransaction();
            try {
                retval = doUpdate(aasId, descriptor);
                commitTransaction();
            }
            catch (Exception ex) {
                rollbackTransaction();
                throw ex;
            }
        }
        return retval;
    }


    @Override
    public Page<SubmodelDescriptor> getSubmodels(String aasId, PagingInfo paging) throws ResourceNotFoundException {
        ensureAasId(aasId);
        AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
        Ensure.requireNonNull(aas, buildAASNotFoundException(aasId));
        List<SubmodelDescriptor> list = aas.getSubmodelDescriptors();
        return getPage(list, readCursor(paging), list.size());
    }


    @Override
    public Page<SubmodelDescriptor> getSubmodels(PagingInfo paging) {
        return EntityManagerHelper.getAllPaged(entityManager, JpaSubmodelDescriptorStandalone.class, SubmodelDescriptor.class, readLimit(paging), readCursor(paging));
    }


    @Override
    public SubmodelDescriptor getSubmodel(String aasId, String submodelId) throws ResourceNotFoundException {
        ensureAasId(aasId);
        ensureSubmodelId(submodelId);
        AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
        Ensure.requireNonNull(aas, buildAASNotFoundException(aasId));

        List<SubmodelDescriptor> submodels = aas.getSubmodelDescriptors();
        Optional<SubmodelDescriptor> submodel = submodels.stream()
                .filter(x -> Objects.nonNull(x.getId())
                        && Objects.equals(x.getId(), submodelId))
                .findAny();
        Ensure.require(submodel.isPresent(), buildSubmodelNotFoundInAASException(aasId, submodelId));
        return submodel.get();
    }


    @Override
    public SubmodelDescriptor getSubmodel(String submodelId) throws ResourceNotFoundException {
        ensureSubmodelId(submodelId);
        SubmodelDescriptor submodel = fetchSubmodelStandalone(submodelId);
        Ensure.requireNonNull(submodel, buildSubmodelNotFoundException(submodelId));
        return submodel;
    }


    @Override
    public SubmodelDescriptor addSubmodel(String aasId, SubmodelDescriptor descriptor) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        SubmodelDescriptor retval = null;
        if (transactionStatus != null) {
            retval = doAddSubmodel(aasId, descriptor);
        }
        else {
            // use internal transaction
            startTransaction();
            try {
                retval = doAddSubmodel(aasId, descriptor);
                commitTransaction();
            }
            catch (Exception ex) {
                rollbackTransaction();
                throw ex;
            }
        }
        return retval;
    }


    @Override
    public SubmodelDescriptor addSubmodel(SubmodelDescriptor descriptor) throws ResourceAlreadyExistsException {
        SubmodelDescriptor retval = null;
        if (transactionStatus != null) {
            retval = doAddSubmodel(descriptor);
        }
        else {
            // use internal transaction
            startTransaction();
            try {
                retval = doAddSubmodel(descriptor);
                commitTransaction();
            }
            catch (Exception ex) {
                rollbackTransaction();
                throw ex;
            }
        }
        return retval;
    }


    @Override
    public void deleteSubmodel(String aasId, String submodelId) throws ResourceNotFoundException {
        if (transactionStatus != null) {
            doDeleteSubmodel(aasId, submodelId);
        }
        else {
            // use internal transaction
            startTransaction();
            try {
                doDeleteSubmodel(aasId, submodelId);
                commitTransaction();
            }
            catch (Exception ex) {
                rollbackTransaction();
                throw ex;
            }
        }
    }


    @Override
    public void deleteSubmodel(String submodelId) throws ResourceNotFoundException {
        if (transactionStatus != null) {
            doDeleteSubmodel(submodelId);
        }
        else {
            // use internal transaction
            startTransaction();
            try {
                doDeleteSubmodel(submodelId);
                commitTransaction();
            }
            catch (Exception ex) {
                rollbackTransaction();
                throw ex;
            }

        }
    }


    @Override
    public void startTransaction() {
        LOGGER.debug("startTransaction");
        if (txManager != null) {
            transactionStatus = txManager.getTransaction(null);
        }
    }


    @Override
    public void commitTransaction() {
        LOGGER.debug("commitTransaction");
        if (txManager != null) {
            txManager.commit(transactionStatus);
            transactionStatus = null;
        }
    }


    @Override
    public void rollbackTransaction() {
        LOGGER.debug("rollbackTransaction");
        if (txManager != null) {
            txManager.rollback(transactionStatus);
            transactionStatus = null;
        }
    }


    @Override
    public void clear() {
        throw new UnsupportedOperationException("clear not implemented");
    }


    private JpaAssetAdministrationShellDescriptor fetchAAS(String aasId) {
        try {
            return entityManager.find(JpaAssetAdministrationShellDescriptor.class, aasId);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }


    private JpaSubmodelDescriptorStandalone fetchSubmodelStandalone(String submodelId) {
        return entityManager.find(JpaSubmodelDescriptorStandalone.class, submodelId);
    }


    @Override
    public boolean getTransactionActive() {
        return transactionStatus != null;
    }


    private void doDeleteAAS(String aasId) throws ResourceNotFoundException {
        ensureAasId(aasId);
        AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
        Ensure.requireNonNull(aas, buildAASNotFoundException(aasId));
        entityManager.remove(aas);
    }


    private AssetAdministrationShellDescriptor doCreate(AssetAdministrationShellDescriptor descriptor) throws ResourceAlreadyExistsException {
        ensureDescriptorId(descriptor);
        AssetAdministrationShellDescriptor aas = fetchAAS(descriptor.getId());
        Ensure.require(Objects.isNull(aas), buildAASAlreadyExistsException(descriptor.getId()));
        JpaAssetAdministrationShellDescriptor result = ModelTransformationHelper.convertAAS(descriptor);
        entityManager.persist(result);
        return result;
    }


    private AssetAdministrationShellDescriptor doUpdate(String aasId, AssetAdministrationShellDescriptor descriptor) throws ResourceNotFoundException {
        ensureAasId(aasId);
        ensureDescriptorId(descriptor);
        JpaAssetAdministrationShellDescriptor aas = fetchAAS(descriptor.getId());
        Ensure.requireNonNull(aas, buildAASNotFoundException(aasId));
        return entityManager.merge(new JpaAssetAdministrationShellDescriptor.Builder()
                .id(aas.getId())
                .from(descriptor)
                .build());
    }


    private SubmodelDescriptor doAddSubmodel(String aasId, SubmodelDescriptor descriptor) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        ensureAasId(aasId);
        ensureDescriptorId(descriptor);
        AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
        Ensure.requireNonNull(aas, buildAASNotFoundException(aasId));
        if (getSubmodelInternal(aas.getSubmodelDescriptors(), descriptor.getId()).isPresent()) {
            throw buildSubmodelAlreadyExistsException(descriptor.getId());
        }
        JpaSubmodelDescriptor submodel = ModelTransformationHelper.convertSubmodel(descriptor);
        aas.getSubmodelDescriptors().add(submodel);
        entityManager.merge(aas);
        return submodel;
    }


    private SubmodelDescriptor doAddSubmodel(SubmodelDescriptor descriptor) throws ResourceAlreadyExistsException {
        ensureDescriptorId(descriptor);
        SubmodelDescriptor submodel = fetchSubmodelStandalone(descriptor.getId());
        Ensure.require(Objects.isNull(submodel), buildSubmodelAlreadyExistsException(descriptor.getId()));
        submodel = ModelTransformationHelper.convertSubmodelStandalone(descriptor);
        entityManager.persist(submodel);
        return submodel;
    }


    private void doDeleteSubmodel(String aasId, String submodelId) throws ResourceNotFoundException {
        ensureAasId(aasId);
        ensureSubmodelId(submodelId);
        AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
        Ensure.requireNonNull(aas, buildAASNotFoundException(aasId));
        Optional<SubmodelDescriptor> submodel = aas.getSubmodelDescriptors().stream()
                .filter(x -> Objects.equals(x.getId(), submodelId)
                        || (Objects.nonNull(x.getId())
                                && x.getId().equalsIgnoreCase(submodelId)))
                .findAny();
        Ensure.require(submodel.isPresent(), buildSubmodelNotFoundInAASException(aasId, submodelId));
        entityManager.remove(aas);
        aas.getSubmodelDescriptors().removeIf(x -> x.getId().equals(submodelId));
        entityManager.persist(aas);
    }


    private void doDeleteSubmodel(String submodelId) throws ResourceNotFoundException {
        ensureSubmodelId(submodelId);
        SubmodelDescriptor submodel = fetchSubmodelStandalone(submodelId);
        Ensure.requireNonNull(submodel, buildSubmodelNotFoundException(submodelId));
        entityManager.remove(submodel);
    }
}
