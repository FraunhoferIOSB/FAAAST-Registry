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

import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.*;
import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.CommonConstraintHelper;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


/**
 * REST controller for the bulk operations.
 */
@RestController
@RequestMapping(value = "/api/v3.0/bulk")
public class BulkOperationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkOperationController.class);

    @Autowired
    RegistryService service;

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
     * @throws ResourceNotFoundException When the Submodel was not found.
     * @throws ResourceAlreadyExistsException When an error occurs.
     */
    @PutMapping(value = "/shell-descriptors")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void bulkUpdateSubmodels(@RequestBody List<SubmodelDescriptor> submodels)
            throws ResourceNotFoundException, ResourceAlreadyExistsException {
        service.bulkUpdateSubmodels(submodels);
    }


    /**
     * Bulk operation for deleting multiple submodel descriptors with the given IDs.
     *
     * @param submodelIdentifier The ID of the desired Submodel.
     * @throws ResourceNotFoundException When the Submodel was not found.
     */
    @DeleteMapping(value = "/shell-descriptors")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void bulkDeleteSubmodels(@PathVariable("submodelIdentifier") String submodelIdentifier) throws ResourceNotFoundException {
        service.deleteSubmodel(submodelIdentifier);
    }


    /**
     * Bulk operation for creating multiple aas descriptors.
     *
     * @param shells The desired asset administration shell descriptors.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     */
    @PostMapping(value = "/shell-descriptors")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void bulkCreateShells(@RequestBody List<AssetAdministrationShellDescriptor> shells)
            throws BadRequestException, UnauthorizedException, ForbiddenException, InternalServerErrorException {
        try {
            service.bulkCreateShells(shells);
        }
        catch (ConstraintViolatedException e) {
            throw new BadRequestException(e.getMessage());
        }
    }


    /**
     * Bulk operation for updating multiple submodel descriptors.
     *
     * @param shells The desired Submodels.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     */
    @PutMapping(value = "/shell-descriptors")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void bulkUpdateShells(@RequestBody List<AssetAdministrationShellDescriptor> shells)
            throws BadRequestException, UnauthorizedException, ForbiddenException, InternalServerErrorException {
        service.bulkUpdateShells(shells);
    }


    /**
     * Bulk operation for deleting multiple submodel descriptors with the given IDs.
     *
     * @param shellIdentifiers The ID of the desired Submodel.
     * @throws BadRequestException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     */
    @DeleteMapping(value = "/shell-descriptors")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void bulkDeleteShells(@PathVariable("aasIdentifier") List<String> shellIdentifiers)
            throws BadRequestException, UnauthorizedException, ForbiddenException, InternalServerErrorException {
        service.bulkDeleteShells(shellIdentifiers);
    }


    /**
     * Returns the status of an asynchronously invoked bulk operation.
     *
     * @param handleId the id for retrieving the bulk operation result object.
     * @throws BadRequestException an error occurs.
     * @throws MovedPermanentlyException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     * @throws ResourceNotFoundException an error occurs.
     */
    @PutMapping(value = "/status/{handleId}")
    @ResponseStatus(HttpStatus.OK)
    public OperationResult getBulkOperationStatus(@RequestParam String handleId)
            throws MovedPermanentlyException, UnauthorizedException, ForbiddenException, ResourceNotFoundException, InternalServerErrorException {
        return service.getBulkOperationStatus(handleId);
    }


    /**
     * Returns the status of an asynchronously invoked bulk operation.
     *
     * @param handleId the id for retrieving the bulk operation result object.
     * @throws BadRequestException an error occurs.
     * @throws MovedPermanentlyException an error occurs.
     * @throws UnauthorizedException an error occurs.
     * @throws ForbiddenException an error occurs.
     * @throws InternalServerErrorException an error occurs.
     * @throws ResourceNotFoundException an error occurs.
     */
    @PutMapping(value = "/result/{handleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void getBulkOperationResult(@RequestParam String handleId)
            throws MovedPermanentlyException, UnauthorizedException, ForbiddenException, ResourceNotFoundException, InternalServerErrorException {
        service.getBulkOperationResult(handleId);
    }
}
