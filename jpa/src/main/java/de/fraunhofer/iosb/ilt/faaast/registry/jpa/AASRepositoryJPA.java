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

import de.fraunhofer.iosb.ilt.faaast.registry.core.AASRepository;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JPAAssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.registry.jpa.model.JPASubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;


/**
 * Relational database implementation of the Repository.
 */
@Repository
@Transactional
public class AASRepositoryJPA implements AASRepository {

    @PersistenceContext(name = "AASRepositoryJPA")
    private final EntityManager entityManager;

    public AASRepositoryJPA(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    @Override
    public List<AssetAdministrationShellDescriptor> getAASs() throws Exception {
        Query query = entityManager.createQuery("SELECT x FROM JPAAssetAdministrationShellDescriptor x");
        return query.getResultList();
    }


    @Override
    public AssetAdministrationShellDescriptor getAAS(String id) throws Exception {
        AssetAdministrationShellDescriptor aas = fetchAAS(id);
        if (aas == null) {
            throw new ResourceNotFoundException("AAS '" + id + "' not found");
        }
        return aas;
    }


    @Override
    public AssetAdministrationShellDescriptor create(AssetAdministrationShellDescriptor entity) throws Exception {
        AssetAdministrationShellDescriptor aas = fetchAAS(entity.getIdentification().getIdentifier());
        if (aas == null) {
            JPAAssetAdministrationShellDescriptor jpaEntity = new JPAAssetAdministrationShellDescriptor.Builder().from(entity).build();
            //List<SubmodelDescriptor> submodels = new ArrayList<>();
            //entity.getSubmodels().forEach((s) -> {
            //    submodels.add(new JPASubmodelDescriptor.Builder().from(s).build());
            //});
            //jpaEntity.setSubmodels(submodels);
            entityManager.persist(jpaEntity);
            aas = jpaEntity;
        }
        else {
            throw new IllegalArgumentException("An AAS with this ID already exists");
        }
        return aas;
    }


    @Override
    public void deleteAAS(String entityId) throws Exception {
        AssetAdministrationShellDescriptor aas = fetchAAS(entityId);
        if (aas == null) {
            throw new ResourceNotFoundException("AAS '" + entityId + "' not found");
        }
        entityManager.remove(aas);
    }


    @Override
    public AssetAdministrationShellDescriptor update(String id, AssetAdministrationShellDescriptor entity) throws Exception {
        AssetAdministrationShellDescriptor a = fetchAAS(entity.getIdentification().getIdentifier());
        if (a == null) {
            throw new ResourceNotFoundException("AAS '" + entity.getIdentification().getIdentifier() + "' not found");
        }
        entityManager.remove(a);
        return create(entity);
    }


    @Override
    public List<SubmodelDescriptor> getSubmodels(String aasId) throws Exception {
        AssetAdministrationShellDescriptor a = fetchAAS(aasId);
        if (a == null) {
            throw new ResourceNotFoundException("AAS '" + aasId + "' not found");
        }
        return new ArrayList<>(a.getSubmodels());
    }


    @Override
    public List<SubmodelDescriptor> getSubmodels() throws Exception {
        Query query = entityManager.createQuery("SELECT x FROM JPASubmodelDescriptor x");
        return query.getResultList();
    }


    @Override
    public SubmodelDescriptor getSubmodel(String aasId, String submodelId) throws Exception {
        AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
        if (aas == null) {
            throw new ResourceNotFoundException("AAS '" + aasId + "' not found");
        }

        List<SubmodelDescriptor> submodels = aas.getSubmodels();
        Optional<SubmodelDescriptor> submodel = submodels.stream()
                .filter(x -> ((x.getIdentification() != null) && (x.getIdentification().getIdentifier() != null) && x.getIdentification().getIdentifier().equals(submodelId)))
                .findAny();
        if (submodel.isEmpty()) {
            throw new ResourceNotFoundException("Submodel '" + submodelId + "' not found");
        }
        return submodel.get();
    }


    @Override
    public SubmodelDescriptor getSubmodel(String submodelId) throws Exception {
        SubmodelDescriptor submodel = fetchSubmodel(submodelId);
        if (submodel == null) {
            throw new ResourceNotFoundException("Submodel '" + submodelId + "' not found");
        }
        return submodel;
    }


    @Override
    public SubmodelDescriptor addSubmodel(String aasId, SubmodelDescriptor submodel) throws Exception {
        AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
        if (aas == null) {
            throw new ResourceNotFoundException("AAS '" + aasId + "' not found");
        }
        JPASubmodelDescriptor jpaSubmodel = new JPASubmodelDescriptor.Builder().from(submodel).build();

        try {
            getSubmodel(aasId, jpaSubmodel.getId());
            throw new IllegalArgumentException("A submodel with the ID '" + submodel.getIdentification().getIdentifier()
                    + "' already exists in AAS with ID '" + aasId + "'");
        }
        catch (ResourceNotFoundException ignored) {}

        entityManager.remove(aas);
        aas.getSubmodels().add(jpaSubmodel);
        entityManager.persist(aas);
        return jpaSubmodel;
    }


    @Override
    public SubmodelDescriptor addSubmodel(SubmodelDescriptor submodel) throws Exception {
        if (!containsSubmodel(submodel)) {
            JPASubmodelDescriptor jpaSubmodel = new JPASubmodelDescriptor.Builder().from(submodel).build();
            entityManager.persist(jpaSubmodel);
            return jpaSubmodel;
        }
        else {
            throw new IllegalArgumentException("A submodel with the ID '" + submodel.getIdentification().getIdentifier()
                    + "' already exists");
        }
    }


    @Override
    public void deleteSubmodel(String aasId, String submodelId) throws Exception {
        AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
        if (aas == null) {
            throw new ResourceNotFoundException("AAS '" + aasId + "' not found");
        }
        Optional<SubmodelDescriptor> submodel = aas.getSubmodels().stream()
                .filter(s -> s.getIdentification().getIdentifier().equalsIgnoreCase(submodelId))
                .findAny();
        if (submodel.isEmpty()) {
            throw new ResourceNotFoundException("Submodel '" + submodelId + "' not found");
        }
        entityManager.remove(aas);
        aas.getSubmodels().removeIf(x -> (x.getIdentification().getIdentifier().equals(submodelId)));
        entityManager.persist(aas);
    }


    @Override
    public void deleteSubmodel(String submodelId) throws Exception {
        if (containsSubmodel(submodelId)) {
            entityManager.remove(fetchSubmodel(submodelId));
        }
        else {
            throw new ResourceNotFoundException("Submodel '" + submodelId + "' not found");
        }
    }


    private AssetAdministrationShellDescriptor fetchAAS(String id) {
        return entityManager.find(JPAAssetAdministrationShellDescriptor.class, id);
    }


    private JPASubmodelDescriptor fetchSubmodel(String id) {
        try {
            return entityManager.createQuery("SELECT x FROM JPASubmodelDescriptor x WHERE id='" + id + "'", JPASubmodelDescriptor.class).getSingleResult();
        }
        catch (Exception e) {
            return null;
        }
    }


    private boolean containsSubmodel(String submodelId) {
        return fetchSubmodel(submodelId) != null;
    }


    private boolean containsSubmodel(SubmodelDescriptor submodel) {
        return containsSubmodel(submodel.getIdentification().getIdentifier());
    }

}
