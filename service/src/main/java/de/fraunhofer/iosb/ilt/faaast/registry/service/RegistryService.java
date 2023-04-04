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
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * The service for the registry.
 */
@Service
public class RegistryService {

    private static final String AAS_NOT_NULL_TXT = "aas must be non-null";
    private static final String SUBMODEL_NOT_NULL_TXT = "submodel must be non-null";

    @Autowired
    private AASRepository aasRepository;

    /**
     * Retrieves a list of all registered Asset Administration Shells.
     *
     * @return The list of all registered Asset Administration Shells.
     */
    public List<AssetAdministrationShellDescriptor> getAASs() {
        return aasRepository.getAASs();
    }


    /**
     * Retrieves the Asset Administration Shell with the given ID.
     *
     * @param id The ID of the desired Asset Administration Shell.
     * @return The desired Asset Administration Shell.
     * @throws ResourceNotFoundException When the AAS was not found.
     */
    public AssetAdministrationShellDescriptor getAAS(String id) throws ResourceNotFoundException {
        String idDecoded = decode(id);
        AssetAdministrationShellDescriptor aas = aasRepository.getAAS(idDecoded);
        return aas;
    }


    /**
     * Create the given Asset Administration Shell.
     *
     * @param aas The desired Asset Administration Shell.
     * @return The created Asset Administration Shell.
     * @throws ResourceAlreadyExistsException When the AAS already exists.
     */
    public AssetAdministrationShellDescriptor createAAS(AssetAdministrationShellDescriptor aas) throws ResourceAlreadyExistsException {
        Ensure.requireNonNull(aas, AAS_NOT_NULL_TXT);
        checkShellIdentifiers(aas);
        if (aas.getSubmodels() != null) {
            aas.getSubmodels().stream().map((submodel) -> {
                checkSubmodelIdentifiers(submodel);
                return submodel;
            });
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
        String idDecoded = decode(id);
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
        String idDecoded = decode(id);
        checkShellIdentifiers(aas);
        aas.getSubmodels().stream().map((SubmodelDescriptor submodel) -> {
            checkSubmodelIdentifiers(submodel);
            return submodel;
        });
        return aasRepository.update(idDecoded, aas);
    }


    /**
     * Retrieves a list of all registered Submodels.
     *
     * @return The list of Submodels.
     * @throws ResourceNotFoundException When the AAS was not found.
     */
    public List<SubmodelDescriptor> getSubmodels() throws ResourceNotFoundException {
        return getSubmodels(null);
    }


    /**
     * Retrieves a list of all Submodels of the given Asset Administration Shell.
     *
     * @param aasId The ID of the desired Asset Administration Shell.
     * @return The list of Submodels.
     * @throws ResourceNotFoundException When the AAS was not found.
     */
    public List<SubmodelDescriptor> getSubmodels(String aasId) throws ResourceNotFoundException {
        if (aasId == null) {
            return aasRepository.getSubmodels();
        }
        else {
            String aasIdDecoded = decode(aasId);
            return aasRepository.getSubmodels(aasIdDecoded);
        }
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
        String submodelIdDecoded = decode(submodelId);
        if (aasId == null) {
            return aasRepository.getSubmodel(submodelIdDecoded);
        }
        else {
            String aasIdDecoded = decode(aasId);
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
        Ensure.requireNonNull(submodel, SUBMODEL_NOT_NULL_TXT);
        checkSubmodelIdentifiers(submodel);
        if (aasId == null) {
            return aasRepository.addSubmodel(submodel);
        }
        else {
            String aasIdDecoded = decode(aasId);
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
        String submodelIdDecoded = decode(submodelId);
        if (aasId == null) {
            aasRepository.deleteSubmodel(submodelIdDecoded);
        }
        else {
            String aasIdDecoded = decode(aasId);
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
        String submodelIdDecoded = decode(submodelId);
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
     * @throws ResourceNotFoundException When the AAS was not found.
     * @throws ResourceAlreadyExistsException When the Submodel already exists.
     */
    public SubmodelDescriptor updateSubmodel(String aasId, String submodelId, SubmodelDescriptor submodel) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        Ensure.requireNonNull(submodel, SUBMODEL_NOT_NULL_TXT);
        String aasIdDecoded = decode(aasId);
        String submodelIdDecoded = decode(submodelId);
        checkSubmodelIdentifiers(submodel);
        aasRepository.deleteSubmodel(aasIdDecoded, submodelIdDecoded);
        return aasRepository.addSubmodel(aasIdDecoded, submodel);
    }


    private static String decode(String encoded) {
        return new String(Base64.getUrlDecoder().decode(encoded));
    }


    private void checkSubmodelIdentifiers(SubmodelDescriptor submodel) throws BadRequestException {
        Ensure.requireNonNull(submodel, SUBMODEL_NOT_NULL_TXT);
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
