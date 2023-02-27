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
package de.fraunhofer.iosb.ilt.faaast.registry.memory;

import de.fraunhofer.iosb.ilt.faaast.registry.core.AASRepository;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


/**
 * In-memory implementation of the Repository.
 */
public class AASRepositoryMemory implements AASRepository {

    private final Map<String, AssetAdministrationShellDescriptor> shellDescriptors;
    private final Map<String, SubmodelDescriptor> submodelDescriptors;

    public AASRepositoryMemory() {
        shellDescriptors = new HashMap<>();
        submodelDescriptors = new HashMap<>();
    }


    @Override
    public List<AssetAdministrationShellDescriptor> getAASs() {
        return new ArrayList<>(shellDescriptors.values());
    }


    @Override
    public AssetAdministrationShellDescriptor getAAS(String id) {
        AssetAdministrationShellDescriptor aas = fetchAAS(id);
        if (aas == null) {
            throw new ResourceNotFoundException("AAS '" + id + "' not found");
        }
        return aas;
    }


    @Override
    public AssetAdministrationShellDescriptor create(AssetAdministrationShellDescriptor entity) {
        AssetAdministrationShellDescriptor aas = fetchAAS(entity.getIdentification().getIdentifier());
        if (aas == null) {
            shellDescriptors.put(entity.getIdentification().getIdentifier(), entity);
            entity.getSubmodels().forEach(s -> {
                //setIdentifier(s);
                submodelDescriptors.putIfAbsent(s.getIdentification().getIdentifier(), s);
            });
        }
        else {
            throw new IllegalArgumentException("An AAS with the ID '" + entity.getIdentification().getIdentifier() + "' already exists");
        }
        return entity;
    }


    @Override
    public void deleteAAS(String aasId) {
        AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
        if (aas == null) {
            throw new ResourceNotFoundException("AAS '" + aasId + "' not found");
        }
        shellDescriptors.remove(aasId);
        aas.getSubmodels().forEach(s -> submodelDescriptors.remove(s.getIdentification().getIdentifier()));
    }


    @Override
    public AssetAdministrationShellDescriptor update(String id, AssetAdministrationShellDescriptor entity) {
        AssetAdministrationShellDescriptor oldAAS = getAAS(id);
        if (oldAAS != null) {
            shellDescriptors.remove(id);
        }
        shellDescriptors.put(entity.getIdentification().getIdentifier(), entity);
        return entity;
    }


    @Override
    public List<SubmodelDescriptor> getSubmodels(String aasId) {
        AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
        if (aas == null) {
            throw new ResourceNotFoundException("AAS '" + aasId + "' not found");
        }
        return aas.getSubmodels();
    }


    @Override
    public List<SubmodelDescriptor> getSubmodels() throws Exception {
        return new ArrayList<>(submodelDescriptors.values());
    }


    @Override
    public SubmodelDescriptor getSubmodel(String aasId, String submodelId) {
        AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
        if (aas == null) {
            throw new ResourceNotFoundException("AAS '" + aasId + "' not found");
        }

        List<SubmodelDescriptor> submodels = aas.getSubmodels();
        Optional<SubmodelDescriptor> submodel = submodels.stream()
                .filter(x -> ((x.getIdentification() != null) && (x.getIdentification().getIdentifier() != null) && x.getIdentification().getIdentifier().equals(submodelId)))
                .findAny();
        if (submodel.isEmpty()) {
            throw new ResourceNotFoundException("Submodel '" + submodelId + "' not found in AAS '" + aasId + "'");
        }

        return submodel.get();
    }


    @Override
    public SubmodelDescriptor getSubmodel(String submodelId) throws Exception {
        if (submodelDescriptors.containsKey(submodelId)) {
            return submodelDescriptors.get(submodelId);
        }
        else {
            throw new ResourceNotFoundException("Submodel '" + submodelId + "' not found");
        }
    }


    @Override
    public SubmodelDescriptor addSubmodel(String aasId, SubmodelDescriptor submodel) throws Exception {
        AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
        if (aas == null) {
            throw new ResourceNotFoundException("AAS '" + aasId + "' not found");
        }
        //setIdentifier(submodel);
        try {
            getSubmodel(aasId, submodel.getIdentification().getIdentifier());
            throw new IllegalArgumentException("A submodel with the ID '" + submodel.getIdentification().getIdentifier()
                    + "' already exists in AAS with ID '" + aasId + "'");
        }
        catch (ResourceNotFoundException ignored) {}
        aas.getSubmodels().add(submodel);
        submodelDescriptors.putIfAbsent(submodel.getIdentification().getIdentifier(), submodel);
        return submodel;
    }


    @Override
    public SubmodelDescriptor addSubmodel(SubmodelDescriptor submodel) throws Exception {
        if (submodelDescriptors.containsKey(submodel.getIdentification().getIdentifier())) {
            throw new IllegalArgumentException("A submodel with the ID '" + submodel.getIdentification().getIdentifier()
                    + "' already exists");
        }
        else {
            //setIdentifier(submodel);
            submodelDescriptors.put(submodel.getIdentification().getIdentifier(), submodel);
        }
        return submodel;
    }


    @Override
    public void deleteSubmodel(String aasId, String submodelId) {
        AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
        if (aas == null) {
            throw new ResourceNotFoundException("AAS '" + aasId + "' not found");
        }

        boolean found = aas.getSubmodels().removeIf(x -> Objects.equals(x.getIdentification().getIdentifier(), submodelId));
        if (!found) {
            throw new ResourceNotFoundException("Submodel '" + submodelId + "' not found");
        }
        submodelDescriptors.remove(submodelId);
    }


    @Override
    public void deleteSubmodel(String submodelId) throws Exception {
        if (!submodelDescriptors.containsKey(submodelId)) {
            throw new ResourceNotFoundException("Submodel '" + submodelId + "' not found");
        }
        else {
            submodelDescriptors.remove(submodelId);
        }
    }


    private AssetAdministrationShellDescriptor fetchAAS(String id) {
        return shellDescriptors.getOrDefault(id, null);
    }
}
