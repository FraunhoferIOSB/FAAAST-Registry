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

import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


/**
 * REST controller for the Submodel registry.
 */
@RestController
@RequestMapping(value = "/registry/submodel-descriptors")
public class SubmodelRegistryController {

    @Autowired
    RegistryService service;

    /**
     * Retrieves a list of all registered Submodels.
     *
     * @return The list of Submodels.
     * @throws Exception When an error occurs.
     */
    @GetMapping()
    public List<SubmodelDescriptor> getSubmodels() throws Exception {
        try {
            return service.getSubmodels();
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }


    /**
     * Retrieves the Submodel with given Submodel ID.
     *
     * @param submodelIdentifier The ID of the desired Submodel.
     * @return The desired Submodel.
     * @throws Exception When an error occurs.
     */
    @GetMapping(value = "/{submodelIdentifier}")
    public SubmodelDescriptor getSubmodel(@PathVariable("submodelIdentifier") String submodelIdentifier) throws Exception {
        try {
            return service.getSubmodel(submodelIdentifier);
        }
        catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }


    /**
     * Creates a new submodel.
     *
     * @param submodel The desired submodel.
     * @return The created submodel.
     * @throws Exception When an error occurs.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubmodelDescriptor createSubmodel(@RequestBody SubmodelDescriptor submodel) throws Exception {
        try {
            return service.createSubmodel(submodel);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }


    /**
     * Updates the given Submodel.
     *
     * @param submodelIdentifier The ID of the desired Submodel.
     * @param submodel The desired Submodel.
     * @return The updated Submodel.
     * @throws Exception When an error occurs.
     */
    @PutMapping(value = "/{submodelIdentifier}")
    @ResponseStatus(HttpStatus.OK)
    public SubmodelDescriptor update(@PathVariable("submodelIdentifier") String submodelIdentifier, @RequestBody SubmodelDescriptor submodel) throws Exception {
        try {
            return service.updateSubmodel(submodelIdentifier, submodel);
        }
        catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
            //} catch (DataIntegrityViolationException e) {
            //    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Constraint Violation: Submodel with ID '" + submodelIdentifier +"' belongs to an AAS. Please update the AAS", e);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }


    /**
     * Deletes the Submodel with the given ID.
     *
     * @param submodelIdentifier The ID of the desired Submodel.
     * @return Success message if the Submodel was successfully deleted, error message otherwise.
     * @throws Exception When an error occurs.
     */
    @DeleteMapping(value = "/{submodelIdentifier}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> delete(@PathVariable("submodelIdentifier") String submodelIdentifier) throws Exception {
        try {
            service.deleteSubmodel(submodelIdentifier);
            return new ResponseEntity<>("Successfully deleted Submodel", HttpStatus.OK);
        }
        catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
            //} catch (DataIntegrityViolationException e) {
            //    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Constraint Violation: Submodel with ID '" + submodelIdentifier +"' belongs to an AAS. Please delete AAS", e);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
