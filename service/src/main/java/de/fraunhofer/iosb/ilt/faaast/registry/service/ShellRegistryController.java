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
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
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
 * REST controller for the registry.
 */
@RestController
@RequestMapping("/registry/shell-descriptors")
public class ShellRegistryController {

    @Autowired
    RegistryService service;

    /**
     * Retrieves a list of all registered Asset Administration Shells.
     *
     * @return The list of all registered Asset Administration Shells.
     * @throws Exception When an error occurs.
     */
    @GetMapping()
    public List<AssetAdministrationShellDescriptor> getAASs() throws Exception {
        try {
            return service.getAASs();
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }


    /**
     * Retrieves the Asset Administration Shell with the given ID.
     *
     * @param aasIdentifier The ID of the desired Asset Administration Shell.
     * @return The desired Asset Administration Shell.
     * @throws Exception When an error occurs.
     */
    @GetMapping(value = "/{aasIdentifier}")
    public AssetAdministrationShellDescriptor getAAS(@PathVariable("aasIdentifier") String aasIdentifier) throws Exception {
        try {
            return service.getAAS(aasIdentifier);
        }
        catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }


    /**
     * Create the given Asset Administration Shell.
     *
     * @param resource The desired Asset Administration Shell.
     * @return The created Asset Administration Shell.
     * @throws Exception When an error occurs.
     */
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public AssetAdministrationShellDescriptor create(@RequestBody AssetAdministrationShellDescriptor resource) throws Exception {
        try {
            return service.createAAS(resource);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }


    /**
     * Deletes the Asset Administration Shell with the given ID.
     *
     * @param aasIdentifier The ID of the desired Asset Administration Shell.
     * @return Success message if the AAS was successfully deleted, error message otherwise.
     */
    @DeleteMapping(value = "/{aasIdentifier}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> delete(@PathVariable("aasIdentifier") String aasIdentifier) {
        try {
            service.deleteAAS(aasIdentifier);
            return new ResponseEntity<>("Successfully deleted AAS", HttpStatus.OK);
        }
        catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }


    /**
     * Updates the given Asset Administration Shell.
     *
     * @param aasIdentifier The ID of the desired Asset Administration Shell.
     * @param aas The desired Asset Administration Shell.
     * @return The updated Asset Administration Shell.
     * @throws Exception When an error occurs.
     */
    @PutMapping(value = "/{aasIdentifier}")
    @ResponseStatus(HttpStatus.OK)
    public AssetAdministrationShellDescriptor update(@PathVariable("aasIdentifier") String aasIdentifier,
                                                     @RequestBody AssetAdministrationShellDescriptor aas)
            throws Exception {
        try {
            return service.updateAAS(aasIdentifier, aas);
        }
        catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }


    /**
     * Retrieves a list of all Submodels of the given Asset Administration Shell.
     *
     * @param aasIdentifier The ID of the desired Asset Administration Shell.
     * @return The list of Submodels.
     * @throws Exception Exception When an error occurs.
     */
    @GetMapping(value = "/{aasIdentifier}/submodel-descriptors")
    public List<SubmodelDescriptor> getSubmodelsOfAAS(@PathVariable("aasIdentifier") String aasIdentifier) throws Exception {
        try {
            return service.getSubmodels(aasIdentifier);
        }
        catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }


    /**
     * Retrieves the Submodel with given AAS ID and Submodel ID.
     *
     * @param aasIdentifier The ID of the desired Asset Administration Shell.
     * @param submodelIdentifier The ID of the desired Submodel.
     * @return The desired Submodel.
     * @throws Exception When an error occurs.
     */
    @GetMapping(value = "/{aasIdentifier}/submodel-descriptors/{submodelIdentifier}")
    public SubmodelDescriptor getSubmodelOfAAS(@PathVariable("aasIdentifier") String aasIdentifier,
                                               @PathVariable("submodelIdentifier") String submodelIdentifier)
            throws Exception {
        try {
            return service.getSubmodel(aasIdentifier, submodelIdentifier);
        }
        catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }


    /**
     * Create a new submodel.
     *
     * @param aasIdentifier The ID of the desired AAS.
     * @param submodel The submodel to add.
     * @return The descriptor of the created submodel.
     * @throws Exception When an error occurs.
     */
    @PostMapping(value = "/{aasIdentifier}/submodel-descriptors")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmodelDescriptor create(@PathVariable("aasIdentifier") String aasIdentifier,
                                     @RequestBody SubmodelDescriptor submodel)
            throws Exception {
        try {
            return service.createSubmodel(aasIdentifier, submodel);
        }
        catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }


    /**
     * Updates the given Submodel.
     *
     * @param aasIdentifier The ID of the desired AAS.
     * @param submodelIdentifier The ID of the desired Submodel.
     * @param submodel The desired Submodel.
     * @return The updated Submodel.
     * @throws Exception When an error occurs.
     */
    @PutMapping(value = "/{aasIdentifier}/submodel-descriptors/{submodelIdentifier}")
    @ResponseStatus(HttpStatus.OK)
    public SubmodelDescriptor updateSubmodelOfAAS(@PathVariable("aasIdentifier") String aasIdentifier,
                                                  @PathVariable("submodelIdentifier") String submodelIdentifier,
                                                  @RequestBody SubmodelDescriptor submodel)
            throws Exception {
        try {
            return service.updateSubmodel(aasIdentifier, submodelIdentifier, submodel);
        }
        catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }


    /**
     * Deletes the Submodel with the given ID.
     *
     * @param aasIdentifier The ID of the desired AAS.
     * @param submodelIdentifier The ID of the desired Submodel.
     * @return Success message if the AAS was successfully deleted, error message otherwise.
     */
    @DeleteMapping(value = "/{aasIdentifier}/submodel-descriptors/{submodelIdentifier}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> deleteSubmodelOfAAS(@PathVariable("aasIdentifier") String aasIdentifier,
                                                      @PathVariable("submodelIdentifier") String submodelIdentifier) {
        try {
            service.deleteSubmodel(aasIdentifier, submodelIdentifier);
            return new ResponseEntity<>("Successfully deleted Submodel", HttpStatus.OK);
        }
        catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
