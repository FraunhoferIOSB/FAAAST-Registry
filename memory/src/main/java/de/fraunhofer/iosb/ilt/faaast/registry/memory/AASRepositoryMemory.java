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

import de.fraunhofer.iosb.ilt.faaast.registry.core.AbstractAasRepository;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


/**
 * In-memory implementation of the Repository.
 */
public class AasRepositoryMemory extends AbstractAasRepository {

    private final Map<String, AssetAdministrationShellDescriptor> shellDescriptors;
    private final Map<String, SubmodelDescriptor> submodelDescriptors;

    public AasRepositoryMemory() {
        shellDescriptors = new HashMap<>();
        submodelDescriptors = new HashMap<>();
    }


    /**
     * Clear the repository.
     */
    public void clear() {
        shellDescriptors.clear();
        submodelDescriptors.clear();
    }


    @Override
    public List<AssetAdministrationShellDescriptor> getAASs() {
        return new ArrayList<>(shellDescriptors.values());
    }


    @Override
    public AssetAdministrationShellDescriptor getAAS(String id) throws ResourceNotFoundException {
        Ensure.requireNonNull(id, "id must be non-null");
        AssetAdministrationShellDescriptor aas = fetchAAS(id);
        Ensure.requireNonNull(aas, buildAASNotFoundException(id));
        return aas;
    }


    @Override
    public AssetAdministrationShellDescriptor create(AssetAdministrationShellDescriptor descriptor) throws ResourceAlreadyExistsException {
        ensureDescriptorId(descriptor);
        AssetAdministrationShellDescriptor aas = fetchAAS(descriptor.getIdentification().getIdentifier());
        Ensure.require(Objects.isNull(aas), buildAASAlreadyExistsException(descriptor.getIdentification().getIdentifier()));
        shellDescriptors.put(descriptor.getIdentification().getIdentifier(), descriptor);
        descriptor.getSubmodels().forEach(s -> {
            submodelDescriptors.putIfAbsent(s.getIdentification().getIdentifier(), s);
        });
        return descriptor;
    }


    @Override
    public void deleteAAS(String aasId) throws ResourceNotFoundException {
        ensureAasId(aasId);
        AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
        Ensure.requireNonNull(aas, buildAASNotFoundException(aasId));
        shellDescriptors.remove(aasId);
        aas.getSubmodels().forEach(s -> submodelDescriptors.remove(s.getIdentification().getIdentifier()));
    }


    @Override
    public AssetAdministrationShellDescriptor update(String aasId, AssetAdministrationShellDescriptor descriptor) throws ResourceNotFoundException {
        ensureAasId(aasId);
        ensureDescriptorId(descriptor);
        AssetAdministrationShellDescriptor oldAAS = getAAS(aasId);
        if (Objects.nonNull(oldAAS)) {
            shellDescriptors.remove(aasId);
        }
        shellDescriptors.put(descriptor.getIdentification().getIdentifier(), descriptor);
        return descriptor;
    }


    @Override
    public List<SubmodelDescriptor> getSubmodels(String aasId) throws ResourceNotFoundException {
        ensureAasId(aasId);
        AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
        Ensure.requireNonNull(aas, buildAASNotFoundException(aasId));
        return aas.getSubmodels();
    }


    @Override
    public List<SubmodelDescriptor> getSubmodels() {
        return new ArrayList<>(submodelDescriptors.values());
    }


    @Override
    public SubmodelDescriptor getSubmodel(String aasId, String submodelId) throws ResourceNotFoundException {
        ensureAasId(aasId);
        ensureSubmodelId(submodelId);
        AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
        Ensure.requireNonNull(aas, buildAASNotFoundException(aasId));
        List<SubmodelDescriptor> submodels = aas.getSubmodels();
        Optional<SubmodelDescriptor> submodel = getSubmodelIntern(submodels, submodelId);
        Ensure.require(submodel.isPresent(), buildSubmodelNotFoundInAASException(aasId, submodelId));
        return submodel.get();
    }


    @Override
    public SubmodelDescriptor getSubmodel(String submodelId) throws ResourceNotFoundException {
        ensureSubmodelId(submodelId);
        Ensure.require(submodelDescriptors.containsKey(submodelId), buildSubmodelNotFoundException(submodelId));
        return submodelDescriptors.get(submodelId);
    }


    @Override
    public SubmodelDescriptor addSubmodel(String aasId, SubmodelDescriptor descriptor) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        ensureAasId(aasId);
        ensureDescriptorId(descriptor);
        AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
        Ensure.requireNonNull(aas, buildAASNotFoundException(aasId));
        if (getSubmodelIntern(aas.getSubmodels(), descriptor.getIdentification().getIdentifier()).isPresent()) {
            throw buildSubmodelAlreadyExistsException(descriptor.getIdentification().getIdentifier());
        }
        aas.getSubmodels().add(descriptor);
        submodelDescriptors.putIfAbsent(descriptor.getIdentification().getIdentifier(), descriptor);
        return descriptor;
    }


    @Override
    public SubmodelDescriptor addSubmodel(SubmodelDescriptor descriptor) throws ResourceAlreadyExistsException {
        ensureDescriptorId(descriptor);
        Ensure.require(
                !submodelDescriptors.containsKey(descriptor.getIdentification().getIdentifier()),
                buildSubmodelAlreadyExistsException(descriptor.getIdentification().getIdentifier()));
        submodelDescriptors.put(descriptor.getIdentification().getIdentifier(), descriptor);
        return descriptor;
    }


    @Override
    public void deleteSubmodel(String aasId, String submodelId) throws ResourceNotFoundException {
        ensureAasId(aasId);
        ensureSubmodelId(submodelId);
        AssetAdministrationShellDescriptor aas = fetchAAS(aasId);
        Ensure.requireNonNull(aas, buildAASNotFoundException(aasId));
        boolean found = aas.getSubmodels().removeIf(x -> Objects.equals(x.getIdentification().getIdentifier(), submodelId));
        Ensure.require(found, buildSubmodelNotFoundException(submodelId));
        submodelDescriptors.remove(submodelId);
    }


    @Override
    public void deleteSubmodel(String submodelId) throws ResourceNotFoundException {
        ensureSubmodelId(submodelId);
        Ensure.require(submodelDescriptors.containsKey(submodelId), buildSubmodelNotFoundException(submodelId));
        submodelDescriptors.remove(submodelId);
    }


    private static Optional<SubmodelDescriptor> getSubmodelIntern(List<SubmodelDescriptor> submodels, String submodelId) {
        return submodels.stream()
                .filter(x -> ((x.getIdentification() != null) && (x.getIdentification().getIdentifier() != null) && x.getIdentification().getIdentifier().equals(submodelId)))
                .findAny();
    }


    private AssetAdministrationShellDescriptor fetchAAS(String aasId) {
        ensureAasId(aasId);
        return shellDescriptors.getOrDefault(aasId, null);
    }
}
