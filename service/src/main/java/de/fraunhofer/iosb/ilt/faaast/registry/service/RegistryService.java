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

import de.fraunhofer.iosb.ilt.faaast.registry.core.AASRepository;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.BadRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * The service for the registry.
 */
@Service
public class RegistryService {

    @Autowired
    private AASRepository aasRepository;

    /**
     * Retrieves a list of all registered Asset Administration Shells.
     *
     * @return The list of all registered Asset Administration Shells.
     */
    public List<AssetAdministrationShellDescriptor> getAASs() {
        try {
            return aasRepository.getAASs();
        }
        catch (Exception ex) {
            throw new BadRequestException();
        }
    }


    /**
     * Retrieves the Asset Administration Shell with the given ID.
     *
     * @param id The ID of the desired Asset Administration Shell.
     * @return The desired Asset Administration Shell.
     * @throws Exception When an error occurs.
     */
    public AssetAdministrationShellDescriptor getAAS(String id) throws Exception {
        AssetAdministrationShellDescriptor aas = aasRepository.getAAS(id);
        return aas;

    }


    /**
     * Create the given Asset Administration Shell.
     *
     * @param entity The desired Asset Administration Shell.
     * @return The created Asset Administration Shell.
     * @throws Exception When an error occurs.
     */
    public AssetAdministrationShellDescriptor createAAS(AssetAdministrationShellDescriptor entity) throws Exception {
        //String id = entity.getIdentification().getId();
        //entity.setId(id);
        checkShellIdentifiers(entity);
        if (entity.getSubmodels() != null) {
            entity.getSubmodels().stream().map((submodel) -> {
                checkSubmodelIdentifiers(submodel);
                return submodel;
            });
        }
        return aasRepository.create(entity);
    }


    /**
     * Deletes the Asset Administration Shell with the given ID.
     *
     * @param id The ID of the desired Asset Administration Shell.
     * @throws Exception
     */
    public void deleteAAS(String id) throws Exception {
        aasRepository.deleteAAS(id);
    }


    /**
     * Updates the given Asset Administration Shell.
     *
     * @param id The ID of the desired Asset Administration Shell.
     * @param entity The desired Asset Administration Shell.
     * @return The updated Asset Administration Shell.
     * @throws Exception When an error occurs.
     */
    public AssetAdministrationShellDescriptor updateAAS(String id, AssetAdministrationShellDescriptor entity) throws Exception {
        //String id = entity.getIdentification().getId();
        //String id = Base64.getUrlEncoder().encodeToString(entity.getIdentification().getId().getBytes());
        //entity.setId(id);
        checkShellIdentifiers(entity);
        entity.getSubmodels().stream().map((SubmodelDescriptor submodel) -> {
            checkSubmodelIdentifiers(submodel);
            return submodel;
        });
        return aasRepository.update(id, entity);
    }


    /**
     * Retrieves the Submodel with given Submodel ID.
     *
     * @param submodelId The ID of the desired Submodel.
     * @return The desired Submodel.
     * @throws Exception When an error occurs.
     */
    public SubmodelDescriptor getSubmodel(String submodelId) throws Exception {
        return getSubmodel(null, submodelId);
    }


    /**
     * Retrieves the Submodel with given AAS ID and Submodel ID.
     *
     * @param aasId The ID of the desired Asset Administration Shell.
     * @param submodelId The ID of the desired Submodel.
     * @return The desired Submodel.
     * @throws Exception When an error occurs.
     */
    public SubmodelDescriptor getSubmodel(String aasId, String submodelId) throws Exception {
        if (aasId == null) {
            return aasRepository.getSubmodel(submodelId);
        }
        else {
            return aasRepository.getSubmodel(aasId, submodelId);
        }
    }


    /**
     * Creates a new submodel.
     *
     * @param submodel The desired submodel.
     * @return The descriptor of the created submodel.
     * @throws Exception When an error occurs.
     */
    public SubmodelDescriptor createSubmodel(SubmodelDescriptor submodel) throws Exception {
        return createSubmodel(null, submodel);
    }


    /**
     * Create a new Submodel in the given AAS.
     *
     * @param aasId The ID of the desired AAS.
     * @param submodel The submodel to add.
     * @return The descriptor of the created submodel.
     * @throws Exception When an error occurs.
     */
    public SubmodelDescriptor createSubmodel(String aasId, SubmodelDescriptor submodel) throws Exception {
        checkSubmodelIdentifiers(submodel);
        if (aasId == null) {
            return aasRepository.addSubmodel(submodel);
        }
        else {
            return aasRepository.addSubmodel(aasId, submodel);
        }
    }


    private void checkSubmodelIdentifiers(SubmodelDescriptor submodel) throws BadRequestException {
        if ((submodel.getIdentification() == null) || (submodel.getIdentification().getId() == null) || (submodel.getIdentification().getId().length() == 0)) {
            throw new BadRequestException("no Submodel identification provided");
        }
    }


    private void checkShellIdentifiers(AssetAdministrationShellDescriptor aas) throws BadRequestException {
        if ((aas.getIdentification() == null) || (aas.getIdentification().getId() == null) || (aas.getIdentification().getId().length() == 0)) {
            throw new BadRequestException("no AAS Identification provided");
        }
    }
}
