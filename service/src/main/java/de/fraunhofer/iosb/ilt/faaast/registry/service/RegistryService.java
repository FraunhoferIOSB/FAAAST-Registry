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
        if (submodel.getIdShort() == null || submodel.getIdentification() == null) {
            throw new BadRequestException("no Submodel idShort provided");
        }
        else if (submodel.getIdShort().length() == 0 || submodel.getIdentification().getId().length() == 0) {
            throw new BadRequestException("no Submodel idShort provided");
        }
    }
}
