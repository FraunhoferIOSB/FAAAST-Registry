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
package de.fraunhofer.iosb.ilt.faaast.registry.postgres;

import de.fraunhofer.iosb.ilt.faaast.registry.core.AbstractAasRepository;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.model.AssetLink;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.Query;
import de.fraunhofer.iosb.ilt.faaast.registry.postgres.model.AssetAdministrationShellDescriptorEntity;
import de.fraunhofer.iosb.ilt.faaast.registry.postgres.model.SubmodelDescriptorEntity;
import de.fraunhofer.iosb.ilt.faaast.registry.postgres.model.SubmodelDescriptorEntityStandalone;
import de.fraunhofer.iosb.ilt.faaast.registry.postgres.util.EntityManagerHelper;
import de.fraunhofer.iosb.ilt.faaast.registry.postgres.util.ModelTransformationHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.FaaastConstants;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;


/**
 * Relational database implementation of the Repository for PostgreQL.
 */
@Repository
public class AasRepositoryPostgres extends AbstractAasRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AasRepositoryPostgres.class);

    //@Autowired
    //private AssetAdministrationShellDescriptorRepository myRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final Map<Integer, TransactionStatus> transactions = new ConcurrentHashMap<>();
    private final AtomicInteger transactionCounter = new AtomicInteger(0);
    private final PlatformTransactionManager txManager;

    @Autowired
    public AasRepositoryPostgres(PlatformTransactionManager txManager) {
        this.txManager = txManager;
        //this.aasRepository = aasRepository;
    }


    @Override
    public Page<AssetAdministrationShellDescriptor> getAASs(String assetType, AssetKind assetKind, PagingInfo paging) {
        try {
            LOGGER.debug("getAASs");
            return EntityManagerHelper.getPagedAas(entityManager, assetType, assetKind, readLimit(paging), readCursor(paging));
        }
        catch (DeserializationException ex) {
            throw new IllegalArgumentException(ex);
        }
    }


    @Override
    public AssetAdministrationShellDescriptor getAAS(String aasId) throws ResourceNotFoundException {
        try {
            Ensure.requireNonNull(aasId, "id must be non-null");
            AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
            Ensure.requireNonNull(aas, buildAASNotFoundException(aasId));
            return aas;
        }
        catch (DeserializationException ex) {
            throw new IllegalArgumentException(ex);
        }
    }


    @Override
    public Page<String> getAASIdentifiersByAssetLink(List<AssetLink> assetLinks, PagingInfo pagingInfo) {
        try {
            Ensure.requireNonNull(assetLinks, "specificAssetIds must be non-null");
            List<AssetAdministrationShellDescriptor> prefilteredDescriptors = filterDescriptorsByGlobalAssetId(assetLinks);

            // We already filtered for global asset id -> No need to add it to specific asset ids again
            return filterAssetAdministrationShellDescriptorsByAssetLink(prefilteredDescriptors, assetLinks, pagingInfo);
        }
        catch (DeserializationException ex) {
            throw new IllegalArgumentException(ex);
        }
    }


    @Override
    public AssetAdministrationShellDescriptor create(AssetAdministrationShellDescriptor descriptor) throws ResourceAlreadyExistsException {
        AssetAdministrationShellDescriptor retval;
        if (!transactions.isEmpty()) {
            retval = doCreate(descriptor);
        }
        else {
            // use internal transaction
            int nr = startTransaction();
            try {
                retval = doCreate(descriptor);
                commitTransaction(nr);
            }
            catch (Exception ex) {
                rollbackTransaction(nr);
                throw ex;
            }
        }
        return retval;
    }


    @Override
    public void deleteAAS(String aasId) throws ResourceNotFoundException {
        if (!transactions.isEmpty()) {
            doDeleteAAS(aasId);
        }
        else {
            // use internal transaction
            int nr = startTransaction();
            try {
                doDeleteAAS(aasId);
                commitTransaction(nr);
            }
            catch (Exception ex) {
                rollbackTransaction(nr);
                throw ex;
            }
        }
    }


    @Override
    public AssetAdministrationShellDescriptor update(String aasId, AssetAdministrationShellDescriptor descriptor) throws ResourceNotFoundException {
        AssetAdministrationShellDescriptor retval;
        if (!transactions.isEmpty()) {
            retval = doUpdate(aasId, descriptor);
        }
        else {
            // use internal transaction
            int nr = startTransaction();
            try {
                retval = doUpdate(aasId, descriptor);
                commitTransaction(nr);
            }
            catch (Exception ex) {
                rollbackTransaction(nr);
                throw ex;
            }
        }
        return retval;
    }


    @Override
    public Page<SubmodelDescriptor> getSubmodels(String aasId, PagingInfo paging) throws ResourceNotFoundException {
        try {
            ensureAasId(aasId);
            AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
            Ensure.requireNonNull(aas, buildAASNotFoundException(aasId));
            List<SubmodelDescriptor> list = aas.getSubmodelDescriptors();
            return getPage(list, readCursor(paging), list.size());
        }
        catch (DeserializationException ex) {
            throw new IllegalArgumentException(ex);
        }
    }


    @Override
    public Page<SubmodelDescriptor> getSubmodels(PagingInfo paging) {
        try {
            return EntityManagerHelper.getPagedSubmodelStandalone(entityManager, readLimit(paging), readCursor(paging));
        }
        catch (DeserializationException ex) {
            throw new IllegalArgumentException(ex);
        }
    }


    @Override
    public SubmodelDescriptor getSubmodel(String aasId, String submodelId) throws ResourceNotFoundException {
        try {
            ensureAasId(aasId);
            ensureSubmodelId(submodelId);
            SubmodelDescriptor retval = fetchSubmodel(aasId, submodelId);
            Ensure.requireNonNull(retval, buildSubmodelNotFoundInAASException(aasId, submodelId));
            return retval;
        }
        catch (DeserializationException ex) {
            throw new IllegalArgumentException(ex);
        }
    }


    @Override
    public SubmodelDescriptor getSubmodel(String submodelId) throws ResourceNotFoundException {
        try {
            ensureSubmodelId(submodelId);
            SubmodelDescriptor submodel = fetchSubmodelStandalone(submodelId);
            Ensure.requireNonNull(submodel, buildSubmodelNotFoundException(submodelId));
            return submodel;
        }
        catch (DeserializationException ex) {
            throw new IllegalArgumentException(ex);
        }
    }


    @Override
    public SubmodelDescriptor addSubmodel(String aasId, SubmodelDescriptor descriptor) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        SubmodelDescriptor retval = null;
        if (!transactions.isEmpty()) {
            retval = doAddSubmodel(aasId, descriptor);
        }
        else {
            // use internal transaction
            int nr = startTransaction();
            try {
                retval = doAddSubmodel(aasId, descriptor);
                commitTransaction(nr);
            }
            catch (Exception ex) {
                rollbackTransaction(nr);
                throw ex;
            }
        }
        return retval;
    }


    @Override
    public SubmodelDescriptor addSubmodel(SubmodelDescriptor descriptor) throws ResourceAlreadyExistsException {
        SubmodelDescriptor retval = null;
        if (!transactions.isEmpty()) {
            retval = doAddSubmodel(descriptor);
        }
        else {
            // use internal transaction
            int nr = startTransaction();
            try {
                retval = doAddSubmodel(descriptor);
                commitTransaction(nr);
            }
            catch (Exception ex) {
                rollbackTransaction(nr);
                throw ex;
            }
        }
        return retval;
    }


    @Override
    public void deleteSubmodel(String aasId, String submodelId) throws ResourceNotFoundException {
        if (!transactions.isEmpty()) {
            doDeleteSubmodel(aasId, submodelId);
        }
        else {
            // use internal transaction
            int nr = startTransaction();
            try {
                doDeleteSubmodel(aasId, submodelId);
                commitTransaction(nr);
            }
            catch (Exception ex) {
                rollbackTransaction(nr);
                throw ex;
            }
        }
    }


    @Override
    public void deleteSubmodel(String submodelId) throws ResourceNotFoundException {
        if (!transactions.isEmpty()) {
            doDeleteSubmodel(submodelId);
        }
        else {
            // use internal transaction
            int nr = startTransaction();
            try {
                doDeleteSubmodel(submodelId);
                commitTransaction(nr);
            }
            catch (Exception ex) {
                rollbackTransaction(nr);
                throw ex;
            }

        }
    }


    @Override
    public int startTransaction() {
        int retval = 0;
        if (txManager != null) {
            retval = transactionCounter.incrementAndGet();
            LOGGER.debug("startTransaction {}", retval);
            transactions.put(retval, txManager.getTransaction(null));
        }
        return retval;
    }


    @Override
    public void commitTransaction(int nr) {
        LOGGER.debug("commitTransaction {}", nr);
        if (txManager != null) {
            TransactionStatus transaction = transactions.get(nr);
            if ((transaction == null) || (transaction.isCompleted())) {
                LOGGER.info("transaction already completed");
            }
            else {
                txManager.commit(transaction);
            }
            transactions.remove(nr);
        }
    }


    @Override
    public void rollbackTransaction(int nr) {
        LOGGER.debug("rollbackTransaction {}", nr);
        if (txManager != null) {
            TransactionStatus transaction = transactions.get(nr);
            if ((transaction == null) || (transaction.isCompleted())) {
                LOGGER.info("transaction already completed");
            }
            else {
                txManager.rollback(transaction);
            }
            transactions.remove(nr);
        }
    }


    @Override
    public boolean getTransactionActive() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


    @Override
    public Page<AssetAdministrationShellDescriptor> queryAASs(Query query, PagingInfo paging) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


    @Override
    public Page<SubmodelDescriptor> querySubmodels(Query query, PagingInfo paging) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


    private AssetAdministrationShellDescriptor fetchAAS(String aasId) throws DeserializationException {
        AssetAdministrationShellDescriptorEntity entity = fetchAASEntity(aasId);
        return ModelTransformationHelper.convertAAS(entity);
    }


    private AssetAdministrationShellDescriptorEntity fetchAASEntity(String aasId) {
        try {
            return entityManager.find(AssetAdministrationShellDescriptorEntity.class, aasId);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }


    private SubmodelDescriptor fetchSubmodel(String aasId, String submodelId) throws DeserializationException {
        return ModelTransformationHelper.convertSubmodel(fetchSubmodelEntity(aasId, submodelId));
    }


    private SubmodelDescriptorEntity fetchSubmodelEntity(String aasId, String submodelId) throws DeserializationException {
        //CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        //CriteriaQuery<SubmodelDescriptorEntity> queryCriteria = builder.createQuery(SubmodelDescriptorEntity.class);
        //Root<SubmodelDescriptorEntity> root = queryCriteria.from(SubmodelDescriptorEntity.class);
        //queryCriteria.where(builder.equal(root.get("aasid"), assetType))
        var query = entityManager.createNativeQuery("select * from submodel_descriptors where aas_id = ? and id = ?", SubmodelDescriptorEntity.class);
        query.setParameter(1, aasId);
        query.setParameter(2, submodelId);
        SubmodelDescriptorEntity result = (SubmodelDescriptorEntity) query.getSingleResultOrNull();
        LOGGER.debug("fetchSubmodel: result: {}", result);
        return result;
    }


    private SubmodelDescriptor fetchSubmodelStandalone(String submodelId) throws DeserializationException {
        return ModelTransformationHelper.convertSubmodel(fetchSubmodelEntityStandalone(submodelId));
    }


    private SubmodelDescriptorEntityStandalone fetchSubmodelEntityStandalone(String submodelId) throws DeserializationException {
        var query = entityManager.createNativeQuery("select * from submodel_descriptors_standalone where id = ?", SubmodelDescriptorEntityStandalone.class);
        query.setParameter(1, submodelId);
        SubmodelDescriptorEntityStandalone result = (SubmodelDescriptorEntityStandalone) query.getSingleResultOrNull();
        LOGGER.debug("fetchSubmodel: result: {}", result);
        return result;
    }


    private AssetAdministrationShellDescriptor doCreate(AssetAdministrationShellDescriptor descriptor) throws ResourceAlreadyExistsException {
        try {
            ensureDescriptorId(descriptor);
            LOGGER.atDebug().log("create AAS {}", descriptor.getId());
            AssetAdministrationShellDescriptorEntity entity = fetchAASEntity(descriptor.getId());
            Ensure.require(Objects.isNull(entity), buildAASAlreadyExistsException(descriptor.getId()));
            entity = ModelTransformationHelper.convertAAS(descriptor);
            entityManager.persist(entity);
            return ModelTransformationHelper.convertAAS(entity);
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }


    private List<AssetAdministrationShellDescriptor> filterDescriptorsByGlobalAssetId(List<AssetLink> assetLinks) throws DeserializationException {
        List<AssetLink> globalAssetIds = assetLinks.stream()
                .filter(x -> FaaastConstants.KEY_GLOBAL_ASSET_ID.equalsIgnoreCase(x.getName()))
                .toList();

        String globalAssetIdString = null;

        if (globalAssetIds.size() > 1) {
            // An AAS descriptor can only have one globalAssetId.
            return new ArrayList<>();
        }
        else if (!globalAssetIds.isEmpty()) {
            AssetLink globalAssetId = globalAssetIds.get(0);
            // Disentangle specificAssetId from globalAssetId
            assetLinks.remove(globalAssetId);
            globalAssetIdString = globalAssetIds.get(0).getValue();
        }

        Map<String, String> specificAssetIdNameValueMap = new HashMap<>();
        assetLinks.forEach(id -> specificAssetIdNameValueMap.put(id.getName(), id.getValue()));

        // Pre-filter to get subset of descriptors matching most commonly defined fields in a specific asset id (name,value) and global asset id
        return EntityManagerHelper.getAas(entityManager, assetLinks, globalAssetIdString);
        //return new ArrayList<>();
    }


    private void doDeleteAAS(String aasId) throws ResourceNotFoundException {
        ensureAasId(aasId);
        AssetAdministrationShellDescriptorEntity aas = fetchAASEntity(aasId);
        Ensure.requireNonNull(aas, buildAASNotFoundException(aasId));
        entityManager.remove(aas);
    }


    private AssetAdministrationShellDescriptor doUpdate(String aasId, AssetAdministrationShellDescriptor descriptor) throws ResourceNotFoundException {
        try {
            ensureAasId(aasId);
            ensureDescriptorId(descriptor);
            AssetAdministrationShellDescriptorEntity aas = fetchAASEntity(descriptor.getId());
            Ensure.requireNonNull(aas, buildAASNotFoundException(aasId));
            AssetAdministrationShellDescriptorEntity newAas = ModelTransformationHelper.convertAAS(descriptor);
            if (aasId.equals(descriptor.getId())) {
                newAas = entityManager.merge(newAas);
            }
            else {
                entityManager.remove(aas);
                entityManager.persist(newAas);
            }
            return ModelTransformationHelper.convertAAS(newAas);
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }


    private SubmodelDescriptor doAddSubmodel(String aasId, SubmodelDescriptor descriptor) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        try {
            ensureAasId(aasId);
            ensureDescriptorId(descriptor);
            AssetAdministrationShellDescriptorEntity aas = fetchAASEntity(aasId);
            Ensure.requireNonNull(aas, buildAASNotFoundException(aasId));
            if (fetchSubmodel(aasId, descriptor.getId()) != null) {
                throw buildSubmodelAlreadyExistsException(descriptor.getId());
            }
            SubmodelDescriptorEntity submodel = ModelTransformationHelper.convertSubmodel(descriptor);
            aas.getSubmodelDescriptors().add(submodel);
            entityManager.merge(aas);
            return ModelTransformationHelper.convertSubmodel(submodel);
        }
        catch (SerializationException | DeserializationException ex) {
            throw new IllegalArgumentException(ex);
        }
    }


    private SubmodelDescriptor doAddSubmodel(SubmodelDescriptor descriptor) throws ResourceAlreadyExistsException {
        try {
            ensureDescriptorId(descriptor);
            //SubmodelDescriptor submodel = fetchSubmodelStandalone(descriptor.getId());
            Ensure.require(Objects.isNull(fetchSubmodelStandalone(descriptor.getId())), buildSubmodelAlreadyExistsException(descriptor.getId()));
            SubmodelDescriptorEntityStandalone submodel = ModelTransformationHelper.convertSubmodelStandalone(descriptor);
            entityManager.persist(submodel);
            return ModelTransformationHelper.convertSubmodel(submodel);
        }
        catch (SerializationException | DeserializationException ex) {
            throw new IllegalArgumentException(ex);
        }
    }


    private void doDeleteSubmodel(String aasId, String submodelId) throws ResourceNotFoundException {
        try {
            ensureAasId(aasId);
            ensureSubmodelId(submodelId);
            //AssetAdministrationShellDescriptorEntity aas = fetchAASEntity(aasId);
            //Ensure.requireNonNull(aas, buildAASNotFoundException(aasId));
            //Optional<SubmodelDescriptor> submodel = aas.getSubmodelDescriptors().stream()
            //        .filter(x -> Objects.equals(x.getId(), submodelId)
            //                || (Objects.nonNull(x.getId())
            //                        && x.getId().equalsIgnoreCase(submodelId)))
            //        .findAny();
            SubmodelDescriptorEntity submodel = fetchSubmodelEntity(aasId, submodelId);
            Ensure.requireNonNull(submodel, buildSubmodelNotFoundInAASException(aasId, submodelId));
            entityManager.remove(submodel);
            //aas.getSubmodelDescriptors().removeIf(x -> x.getId().equals(submodelId));
            //entityManager.persist(aas);
        }
        catch (DeserializationException ex) {
            throw new IllegalArgumentException(ex);
        }
    }


    private void doDeleteSubmodel(String submodelId) throws ResourceNotFoundException {
        try {
            ensureSubmodelId(submodelId);
            SubmodelDescriptorEntityStandalone submodel = fetchSubmodelEntityStandalone(submodelId);
            Ensure.requireNonNull(submodel, buildSubmodelNotFoundException(submodelId));
            entityManager.remove(submodel);
        }
        catch (DeserializationException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
