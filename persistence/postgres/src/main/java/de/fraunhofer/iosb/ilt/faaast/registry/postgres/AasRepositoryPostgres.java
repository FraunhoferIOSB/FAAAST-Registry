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
import de.fraunhofer.iosb.ilt.faaast.registry.postgres.util.EntityManagerHelper;
import de.fraunhofer.iosb.ilt.faaast.registry.postgres.util.ModelTransformationHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


    @Override
    public AssetAdministrationShellDescriptor update(String aasId, AssetAdministrationShellDescriptor descriptor) throws ResourceNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


    @Override
    public Page<SubmodelDescriptor> getSubmodels(String aasId, PagingInfo paging) throws ResourceNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


    @Override
    public Page<SubmodelDescriptor> getSubmodels(PagingInfo paging) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


    @Override
    public SubmodelDescriptor getSubmodel(String aasId, String submodelId) throws ResourceNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


    @Override
    public SubmodelDescriptor getSubmodel(String submodelId) throws ResourceNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


    @Override
    public SubmodelDescriptor addSubmodel(String aasId, SubmodelDescriptor descriptor) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


    @Override
    public SubmodelDescriptor addSubmodel(SubmodelDescriptor descriptor) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


    @Override
    public void deleteSubmodel(String aasId, String submodelId) throws ResourceNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


    @Override
    public void deleteSubmodel(String submodelId) throws ResourceNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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
        catch (SerializationException | DeserializationException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
