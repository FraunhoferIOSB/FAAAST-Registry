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
package de.fraunhofer.iosb.ilt.faaast.registry.service.controller;

import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.*;
import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.Constants;
import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.OperationHelper;
import de.fraunhofer.iosb.ilt.faaast.registry.service.service.RegistryService;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * REST controller for the bulk operations.
 */
@RestController
@RequestMapping(value = Constants.BULK_REQUEST_PATH)
public class BulkOperationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkOperationController.class);

    //@Autowired
    private final RegistryService service;

    @Autowired
    public BulkOperationController(RegistryService service) {
        this.service = service;
    }


    /**
     * Bulk operation for creating multiple submodel descriptors.
     *
     * @param submodels The desired submodel.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     */
    @PostMapping(value = "/submodel-descriptors")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void bulkCreateSubmodels(@RequestBody List<SubmodelDescriptor> submodels)
            throws BadRequestException, UnauthorizedException, ForbiddenException, InternalServerErrorException {
        try {
            service.bulkCreateSubmodels(submodels);
        }
        catch (ConstraintViolatedException e) {
            throw new BadRequestException(e.getMessage());
        }
    }


    /**
     * Bulk operation for updating multiple submodel descriptors.
     *
     * @param submodels The desired Submodels.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     */
    @PutMapping(value = "/submodel-descriptors")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void bulkUpdateSubmodels(@RequestBody List<SubmodelDescriptor> submodels)
            throws BadRequestException, UnauthorizedException, ForbiddenException, InternalServerErrorException {
        service.bulkUpdateSubmodels(submodels);
    }


    /**
     * Bulk operation for deleting multiple submodel descriptors with the given IDs.
     *
     * @param submodelIdentifiers The ID of the desired Submodels.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ResourceNotFoundException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     */
    @DeleteMapping(value = "/submodel-descriptors/")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void bulkDeleteSubmodels(@RequestBody List<String> submodelIdentifiers)
            throws BadRequestException, UnauthorizedException, ResourceNotFoundException, InternalServerErrorException {
        service.bulkDeleteSubmodels(submodelIdentifiers);
    }


    /**
     * Bulk operation for creating multiple aas descriptors.
     *
     * @param shells The desired asset administration shell descriptors.
     * @return The ResponseEntity object.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     * @throws ResourceAlreadyExistsException When an AAS aleady exists.
     * @throws InterruptedException an error occurs.
     * @throws ExecutionException an error occurs.
     */
    @PostMapping(value = "/shell-descriptors")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<Void> bulkCreateShells(@RequestBody List<AssetAdministrationShellDescriptor> shells)
            throws BadRequestException, UnauthorizedException, ForbiddenException, InternalServerErrorException, ResourceAlreadyExistsException, InterruptedException,
            ExecutionException {
        String handleId = OperationHelper.generateOperationHandleId();
        service.bulkCreateShells(shells, handleId);

        LOGGER.debug("bulkCreateShells: Handle: {}", handleId);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("../status/" + handleId));

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .headers(headers)
                .build();
    }


    /**
     * Bulk operation for updating multiple aas descriptors.
     *
     * @param shells The desired Submodels.
     * @return The ResponseEntity object.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     * @throws InterruptedException The execution was interrupted.
     */
    @PutMapping(value = "/shell-descriptors")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<Void> bulkUpdateShells(@RequestBody List<AssetAdministrationShellDescriptor> shells)
            throws BadRequestException, UnauthorizedException, ForbiddenException, InternalServerErrorException, InterruptedException {
        String handleId = OperationHelper.generateOperationHandleId();
        service.bulkUpdateShells(shells, handleId);

        LOGGER.debug("bulkUpdateShells: Handle: {}", handleId);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("../status/" + handleId));

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .headers(headers)
                .build();
    }


    /**
     * Bulk operation for deleting multiple aas descriptors with the given IDs.
     *
     * @param shellIdentifiers The ID of the desired aas.
     * @return The ResponseEntity object.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ResourceNotFoundException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     * @throws InterruptedException The execution was interrupted.
     */
    @DeleteMapping(value = "/shell-descriptors")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<Void> bulkDeleteShells(@RequestBody List<String> shellIdentifiers)
            throws BadRequestException, UnauthorizedException, ResourceNotFoundException, InternalServerErrorException, InterruptedException {
        String handleId = OperationHelper.generateOperationHandleId();
        service.bulkDeleteShells(shellIdentifiers, handleId);

        LOGGER.debug("bulkDeleteShells: Handle: {}", handleId);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("../status/" + handleId));

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .headers(headers)
                .build();
    }


    /**
     * Returns the status of an asynchronously invoked bulk operation.
     *
     * @param handleId the id for retrieving the bulk operation status.
     * @return The operation result.
     * @throws MovedPermanentlyException an error occurs.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     * @throws ResourceNotFoundException an error occurs.
     */
    @GetMapping(value = "/status/{handleId}")
    @ResponseStatus(HttpStatus.OK)
    public OperationResult getBulkOperationStatus(@PathVariable("handleId") String handleId)
            throws MovedPermanentlyException, UnauthorizedException, ForbiddenException, ResourceNotFoundException, InternalServerErrorException {
        return service.getBulkOperationStatus(handleId);
    }


    /**
     * Returns the result of an asynchronously invoked bulk operation.
     *
     * @param handleId the id for retrieving the bulk operation result object.
     * @throws BadRequestException an error occurs.
     * @throws MovedPermanentlyException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     * @throws ResourceNotFoundException an error occurs.
     */
    @GetMapping(value = "/result/{handleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void getBulkOperationResult(@PathVariable("handleId") String handleId)
            throws MovedPermanentlyException, UnauthorizedException, ForbiddenException, ResourceNotFoundException, InternalServerErrorException {
        service.getBulkOperationResult(handleId);
    }

}
