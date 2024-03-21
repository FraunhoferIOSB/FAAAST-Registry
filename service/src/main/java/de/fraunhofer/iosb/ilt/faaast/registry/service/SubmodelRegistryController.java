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

import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import helper.RegistryHelper;
import java.net.URI;
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
 * REST controller for the Submodel registry.
 */
@RestController
@RequestMapping(value = "/api/v3.0/submodel-descriptors")
public class SubmodelRegistryController {

    @Autowired
    RegistryService service;

    /**
     * Retrieves a list of all registered Submodels.
     *
     * @param limit The limit value.
     * @param cursor The cursor value.
     * @return The list of Submodels.
     * @throws ResourceNotFoundException When the Submodel was not found.
     */
    @GetMapping()
    public Page<SubmodelDescriptor> getSubmodels(@RequestParam(name = "limit", required = false) Long limit, @RequestParam(name = "cursor", required = false) String cursor)
            throws ResourceNotFoundException {
        PagingInfo.Builder pageBuilder = PagingInfo.builder().cursor(cursor);
        if (limit != null) {
            pageBuilder.limit(limit);
        }
        return service.getSubmodels(pageBuilder.build());
    }


    /**
     * Retrieves the Submodel with given Submodel ID.
     *
     * @param submodelIdentifier The ID of the desired Submodel.
     * @return The desired Submodel.
     * @throws ResourceNotFoundException When the Submodel was not found.
     */
    @GetMapping(value = "/{submodelIdentifier}")
    public SubmodelDescriptor getSubmodel(@PathVariable("submodelIdentifier") String submodelIdentifier) throws ResourceNotFoundException {
        return service.getSubmodel(submodelIdentifier);
    }


    /**
     * Creates a new submodel.
     *
     * @param submodel The desired submodel.
     * @return The created submodel.
     * @throws ResourceNotFoundException When an error occurs.
     * @throws ResourceAlreadyExistsException When the Submodel already exists.
     */
    @PostMapping
    public ResponseEntity<SubmodelDescriptor> createSubmodel(@RequestBody SubmodelDescriptor submodel) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        SubmodelDescriptor descriptor = service.createSubmodel(submodel);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path(String.format("/%s", RegistryHelper.encode(descriptor.getId())))
                .build().toUri();
        return ResponseEntity.created(location).body(descriptor);
    }


    /**
     * Updates the given Submodel.
     *
     * @param submodelIdentifier The ID of the desired Submodel.
     * @param submodel The desired Submodel.
     * @return The updated Submodel.
     * @throws ResourceNotFoundException When the Submodel was not found.
     * @throws ResourceAlreadyExistsException When an error occurs.
     */
    @PutMapping(value = "/{submodelIdentifier}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public SubmodelDescriptor update(@PathVariable("submodelIdentifier") String submodelIdentifier, @RequestBody SubmodelDescriptor submodel)
            throws ResourceNotFoundException, ResourceAlreadyExistsException {
        return service.updateSubmodel(submodelIdentifier, submodel);
    }


    /**
     * Deletes the Submodel with the given ID.
     *
     * @param submodelIdentifier The ID of the desired Submodel.
     * @throws ResourceNotFoundException When the Submodel was not found.
     */
    @DeleteMapping(value = "/{submodelIdentifier}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("submodelIdentifier") String submodelIdentifier) throws ResourceNotFoundException {
        service.deleteSubmodel(submodelIdentifier);
    }
}
