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

import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.BadRequestException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ConstraintViolatedException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.CommonConstraintHelper;
import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import java.net.URI;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


/**
 * REST controller for the Asset Administration Shell registry.
 */
@RestController
@RequestMapping(value = Constants.SHELL_REQUEST_PATH)
public class ShellRegistryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShellRegistryController.class);

    @Autowired
    RegistryService service;

    /**
     * Retrieves a list of all registered Asset Administration Shells.
     *
     * @param assetType The desired Asset Type.
     * @param assetKind The desired Asset Kind.
     * @param limit The limit value.
     * @param cursor The cursor value.
     * @return The list of all registered Asset Administration Shells.
     */
    @GetMapping()
    public Page<AssetAdministrationShellDescriptor> getAASs(@RequestParam(name = "assetType", required = false) String assetType,
                                                            @RequestParam(name = "assetKind", required = false) AssetKind assetKind,
                                                            @RequestParam(name = "limit", required = false) Long limit,
                                                            @RequestParam(name = "cursor", required = false) String cursor) {
        PagingInfo.Builder pageBuilder = PagingInfo.builder().cursor(cursor);
        if (limit != null) {
            if (limit == 0) {
                throw new BadRequestException("Limit must be greater than 0");
            }
            pageBuilder.limit(limit);
        }
        return service.getAASs(assetType, assetKind, pageBuilder.build());
    }


    /**
     * Retrieves the Asset Administration Shell with the given ID.
     *
     * @param aasIdentifier The ID of the desired Asset Administration Shell.
     * @return The desired Asset Administration Shell.
     * @throws ResourceNotFoundException When the AAS was not found.
     */
    @GetMapping(value = "/{aasIdentifier}")
    public AssetAdministrationShellDescriptor getAAS(@PathVariable("aasIdentifier") String aasIdentifier) throws ResourceNotFoundException {
        return service.getAAS(aasIdentifier);
    }


    /**
     * Create the given Asset Administration Shell.
     *
     * @param resource The desired Asset Administration Shell.
     * @return The created Asset Administration Shell.
     * @throws ResourceAlreadyExistsException When the AAS already exists.
     */
    @PostMapping()
    public ResponseEntity<AssetAdministrationShellDescriptor> create(@RequestBody AssetAdministrationShellDescriptor resource) throws ResourceAlreadyExistsException {
        try {
            AssetAdministrationShellDescriptor aas = service.createAAS(resource);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path(String.format("/%s", EncodingHelper.base64UrlEncode(aas.getId())))
                    .build().toUri();
            return ResponseEntity.created(location).body(aas);
        }
        catch (ConstraintViolatedException e) {
            logConstraintViolated("create AAS", e.getMessage(), resource);
            throw new BadRequestException(e.getMessage());
        }
    }


    /**
     * Deletes the Asset Administration Shell with the given ID.
     *
     * @param aasIdentifier The ID of the desired Asset Administration Shell.
     * @throws ResourceNotFoundException When the AAS was not found.
     */
    @DeleteMapping(value = "/{aasIdentifier}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("aasIdentifier") String aasIdentifier) throws ResourceNotFoundException {
        service.deleteAAS(aasIdentifier);
    }


    /**
     * Updates the given Asset Administration Shell.
     *
     * @param aasIdentifier The ID of the desired Asset Administration Shell.
     * @param aas The desired Asset Administration Shell.
     * @return The updated Asset Administration Shell.
     * @throws ResourceNotFoundException When the AAS was not found.
     */
    @PutMapping(value = "/{aasIdentifier}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public AssetAdministrationShellDescriptor update(@PathVariable("aasIdentifier") String aasIdentifier,
                                                     @RequestBody AssetAdministrationShellDescriptor aas)
            throws ResourceNotFoundException {
        return service.updateAAS(aasIdentifier, aas);
    }


    /**
     * Retrieves a list of all Submodels of the given Asset Administration Shell.
     *
     * @param aasIdentifier The ID of the desired Asset Administration Shell.
     * @param limit The limit value.
     * @param cursor The cursor value.
     * @return The list of Submodels.
     * @throws ResourceNotFoundException When the AAS was not found.
     */
    @GetMapping(value = "/{aasIdentifier}/submodel-descriptors")
    public Page<SubmodelDescriptor> getSubmodelsOfAAS(@PathVariable("aasIdentifier") String aasIdentifier,
                                                      @RequestParam(name = "limit", required = false) Long limit,
                                                      @RequestParam(name = "cursor", required = false) String cursor)
            throws ResourceNotFoundException {
        PagingInfo.Builder pageBuilder = PagingInfo.builder().cursor(cursor);
        if (limit != null) {
            if (limit == 0) {
                throw new BadRequestException("Limit must be greater than 0");
            }
            pageBuilder.limit(limit);
        }
        return service.getSubmodels(aasIdentifier, pageBuilder.build());
    }


    /**
     * Retrieves the Submodel with given AAS ID and Submodel ID.
     *
     * @param aasIdentifier The ID of the desired Asset Administration Shell.
     * @param submodelIdentifier The ID of the desired Submodel.
     * @return The desired Submodel.
     * @throws ResourceNotFoundException When the AAS or Submodel was not found.
     */
    @GetMapping(value = "/{aasIdentifier}/submodel-descriptors/{submodelIdentifier}")
    public SubmodelDescriptor getSubmodelOfAAS(@PathVariable("aasIdentifier") String aasIdentifier,
                                               @PathVariable("submodelIdentifier") String submodelIdentifier)
            throws ResourceNotFoundException {
        return service.getSubmodel(aasIdentifier, submodelIdentifier);
    }


    /**
     * Create a new submodel.
     *
     * @param aasIdentifier The ID of the desired AAS.
     * @param submodel The submodel to add.
     * @return The descriptor of the created submodel.
     * @throws ResourceNotFoundException When the AAS was not found.
     * @throws ResourceAlreadyExistsException When the Submodel already exists.
     */
    @PostMapping(value = "/{aasIdentifier}/submodel-descriptors")
    public ResponseEntity<SubmodelDescriptor> create(@PathVariable("aasIdentifier") String aasIdentifier,
                                                     @RequestBody SubmodelDescriptor submodel)
            throws ResourceNotFoundException, ResourceAlreadyExistsException {
        try {
            SubmodelDescriptor descriptor = service.createSubmodel(aasIdentifier, submodel);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path(String.format("/%s", EncodingHelper.base64UrlEncode(descriptor.getId())))
                    .build().toUri();
            return ResponseEntity.created(location).body(descriptor);
        }
        catch (ConstraintViolatedException e) {
            logConstraintViolated("create Submodel", e.getMessage(), aasIdentifier, submodel);
            throw new BadRequestException(e.getMessage());
        }
    }


    /**
     * Updates the given Submodel.
     *
     * @param aasIdentifier The ID of the desired AAS.
     * @param submodelIdentifier The ID of the desired Submodel.
     * @param submodel The desired Submodel.
     * @return The updated Submodel.
     * @throws ResourceNotFoundException When the AAS was not found.
     * @throws ResourceAlreadyExistsException When the Submodel already exists.
     */
    @PutMapping(value = "/{aasIdentifier}/submodel-descriptors/{submodelIdentifier}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public SubmodelDescriptor updateSubmodelOfAAS(@PathVariable("aasIdentifier") String aasIdentifier,
                                                  @PathVariable("submodelIdentifier") String submodelIdentifier,
                                                  @RequestBody SubmodelDescriptor submodel)
            throws ResourceNotFoundException, ResourceAlreadyExistsException {
        return service.updateSubmodel(aasIdentifier, submodelIdentifier, submodel);
    }


    /**
     * Deletes the Submodel with the given ID.
     *
     * @param aasIdentifier The ID of the desired AAS.
     * @param submodelIdentifier The ID of the desired Submodel.
     * @throws ResourceNotFoundException When the Submodel was not found.
     */
    @DeleteMapping(value = "/{aasIdentifier}/submodel-descriptors/{submodelIdentifier}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubmodelOfAAS(@PathVariable("aasIdentifier") String aasIdentifier,
                                    @PathVariable("submodelIdentifier") String submodelIdentifier)
            throws ResourceNotFoundException {
        service.deleteSubmodel(aasIdentifier, submodelIdentifier);
    }


    private void logConstraintViolated(String method, String message, AssetAdministrationShellDescriptor aas) {
        LOGGER.atInfo().log(CommonConstraintHelper.getLogText(method, message, aas));
    }


    private void logConstraintViolated(String method, String message, String aasId, SubmodelDescriptor submodel) {
        LOGGER.atInfo().log(CommonConstraintHelper.getLogText(method, message, aasId, submodel));
    }
}
