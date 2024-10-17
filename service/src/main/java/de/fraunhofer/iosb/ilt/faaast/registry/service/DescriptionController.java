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

import de.fraunhofer.iosb.ilt.faaast.service.model.ServiceDescription;
import de.fraunhofer.iosb.ilt.faaast.service.model.ServiceSpecificationProfile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST controller for the description.
 */
@RestController
@RequestMapping("/api/v3.0/description")
public class DescriptionController {

    /**
     * Returns a description object containing the capabilities and supported features of the server.
     *
     * @return The description object.
     */
    @GetMapping
    public ServiceDescription getDescription() {
        return ServiceDescription.builder()
                .profile(ServiceSpecificationProfile.AAS_REGISTRY_FULL)
                .profile(ServiceSpecificationProfile.SUBMODEL_REGISTRY_FULL)
                .build();
    }

}
