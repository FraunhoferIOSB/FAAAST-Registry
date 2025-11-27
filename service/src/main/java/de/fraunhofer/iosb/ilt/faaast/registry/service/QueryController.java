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

import de.fraunhofer.iosb.ilt.faaast.registry.core.AasRepository;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.BadRequestException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.query.json.Query;
import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST controller for the Query API.
 */
@RestController
@RequestMapping(value = Constants.QUERY_PATH)
public class QueryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryController.class);

    private final RegistryService service;

    @Autowired
    public QueryController(RegistryService service) {
        this.service = service;
    }


    /**
     * Queries for specific AASs.
     *
     * @param limit The limit value.
     * @param cursor The cursor value.
     * @param query The desired query.
     * @return The list of matching Asset Administration Shells.
     */
    @PostMapping(value = "/shell-descriptors")
    @ResponseStatus(HttpStatus.OK)
    public Page<AssetAdministrationShellDescriptor> queryAASs(@RequestParam(name = "limit", required = false) Long limit,
                                                              @RequestParam(name = "cursor", required = false) String cursor, @RequestBody Query query) {
        PagingInfo.Builder pageBuilder = PagingInfo.builder().cursor(cursor);
        if (limit != null) {
            if (limit == 0) {
                throw new BadRequestException("Limit must be greater than 0");
            }
            pageBuilder.limit(limit);
        }
        else {
            LOGGER.trace("queryAASs: no limit set - use default limit {}", AasRepository.DEFAULT_LIMIT);
            pageBuilder.limit(AasRepository.DEFAULT_LIMIT);
        }
        return service.queryAASs(query, pageBuilder.build());
    }


    /**
     * Queries for specific Submodels.
     *
     * @param limit The limit value.
     * @param cursor The cursor value.
     * @param query The desired query.
     * @return The list of matching Submodels.
     */
    @PostMapping(value = "/submodel-descriptors")
    @ResponseStatus(HttpStatus.OK)
    public Page<SubmodelDescriptor> querySubmodels(@RequestParam(name = "limit", required = false) Long limit,
                                                   @RequestParam(name = "cursor", required = false) String cursor, @RequestBody Query query) {
        PagingInfo.Builder pageBuilder = PagingInfo.builder().cursor(cursor);
        if (limit != null) {
            if (limit == 0) {
                throw new BadRequestException("Limit must be greater than 0");
            }
            pageBuilder.limit(limit);
        }
        else {
            LOGGER.trace("querySubmodels: no limit set - use default limit {}", AasRepository.DEFAULT_LIMIT);
            pageBuilder.limit(AasRepository.DEFAULT_LIMIT);
        }
        return service.querySubmodels(query, pageBuilder.build());
    }
}
