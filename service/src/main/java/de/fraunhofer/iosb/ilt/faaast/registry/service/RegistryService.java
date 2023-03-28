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
import java.util.Base64;
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
        String idDecoded = new String(Base64.getUrlDecoder().decode(id));
        AssetAdministrationShellDescriptor aas = aasRepository.getAAS(idDecoded);
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
        String idDecoded = new String(Base64.getUrlDecoder().decode(id));
        aasRepository.deleteAAS(idDecoded);
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
        String idDecoded = new String(Base64.getUrlDecoder().decode(id));
        checkShellIdentifiers(entity);
        entity.getSubmodels().stream().map((SubmodelDescriptor submodel) -> {
            checkSubmodelIdentifiers(submodel);
            return submodel;
        });
        return aasRepository.update(idDecoded, entity);
    }


    /**
     * Retrieves a list of all registered Submodels.
     *
     * @return The list of Submodels.
     * @throws Exception When an error occurs.
     */
    public List<SubmodelDescriptor> getSubmodels() throws Exception {
        return getSubmodels(null);
    }


    /**
     * Retrieves a list of all Submodels of the given Asset Administration Shell.
     *
     * @param aasId The ID of the desired Asset Administration Shell.
     * @return The list of Submodels.
     * @throws Exception When an error occurs.
     */
    public List<SubmodelDescriptor> getSubmodels(String aasId) throws Exception {
        if (aasId == null) {
            return aasRepository.getSubmodels();
        }
        else {
            String aasIdDecoded = new String(Base64.getUrlDecoder().decode(aasId));
            return aasRepository.getSubmodels(aasIdDecoded);
        }
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
        String submodelIdDecoded = new String(Base64.getUrlDecoder().decode(submodelId));
        if (aasId == null) {
            return aasRepository.getSubmodel(submodelIdDecoded);
        }
        else {
            String aasIdDecoded = new String(Base64.getUrlDecoder().decode(aasId));
            return aasRepository.getSubmodel(aasIdDecoded, submodelIdDecoded);
        }
    }


    /**
     * Creates a new submodel.
     *
     * @param submodel The desired submodel.
     * @return The created submodel.
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
            String aasIdDecoded = new String(Base64.getUrlDecoder().decode(aasId));
            return aasRepository.addSubmodel(aasIdDecoded, submodel);
        }
    }


    /**
     * Deletes the Submodel with the given ID.
     *
     * @param submodelId The ID of the desired Submodel.
     * @throws Exception When an error occurs.
     */
    public void deleteSubmodel(String submodelId) throws Exception {
        deleteSubmodel(null, submodelId);
    }


    /**
     * Deletes the Submodel with the given AAS ID and Submodel ID.
     *
     * @param aasId The ID of the desired AAS.
     * @param submodelId The ID of the desired Submodel.
     * @throws Exception When an error occurs.
     */
    public void deleteSubmodel(String aasId, String submodelId) throws Exception {
        String submodelIdDecoded = new String(Base64.getUrlDecoder().decode(submodelId));
        if (aasId == null) {
            aasRepository.deleteSubmodel(submodelIdDecoded);
        }
        else {
            String aasIdDecoded = new String(Base64.getUrlDecoder().decode(aasId));
            aasRepository.deleteSubmodel(aasIdDecoded, submodelIdDecoded);
        }
    }


    /**
     * Updates the given Submodel.
     *
     * @param submodelId The ID of the desired Submodel.
     * @param submodel The desired Submodel.
     * @return The updated Submodel.
     * @throws Exception When an error occurs.
     */
    public SubmodelDescriptor updateSubmodel(String submodelId, SubmodelDescriptor submodel) throws Exception {
        String submodelIdDecoded = new String(Base64.getUrlDecoder().decode(submodelId));
        checkSubmodelIdentifiers(submodel);
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
     * @throws Exception When an error occurs.
     */
    public SubmodelDescriptor updateSubmodel(String aasId, String submodelId, SubmodelDescriptor submodel) throws Exception {
        String aasIdDecoded = new String(Base64.getUrlDecoder().decode(aasId));
        String submodelIdDecoded = new String(Base64.getUrlDecoder().decode(submodelId));
        checkSubmodelIdentifiers(submodel);
        aasRepository.deleteSubmodel(aasIdDecoded, submodelIdDecoded);
        return aasRepository.addSubmodel(aasIdDecoded, submodel);
    }


    private void checkSubmodelIdentifiers(SubmodelDescriptor submodel) throws BadRequestException {
        if ((submodel.getIdentification() == null) || (submodel.getIdentification().getIdentifier() == null) || (submodel.getIdentification().getIdentifier().length() == 0)) {
            throw new BadRequestException("no Submodel identification provided");
        }
    }


    private void checkShellIdentifiers(AssetAdministrationShellDescriptor aas) throws BadRequestException {
        if ((aas.getIdentification() == null) || (aas.getIdentification().getIdentifier() == null) || (aas.getIdentification().getIdentifier().length() == 0)) {
            throw new BadRequestException("no AAS Identification provided");
        }
    }
}
