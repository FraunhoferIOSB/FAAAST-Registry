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
package de.fraunhofer.iosb.ilt.faaast.registry.core;

import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import java.util.List;


/**
 * AAS Registry main repository.
 */
public interface AASRepository {

    /**
     * Retrieves a list of all registered Asset Administration Shells.
     *
     * @return The list of all registered Asset Administration Shells.
     * @throws Exception When an error occurs.
     */
    public List<AssetAdministrationShellDescriptor> getAASs() throws Exception;


    /**
     * Adds a Submodel to the given AAS.
     *
     * @param aasId The ID of the desired AAS.
     * @param submodel The submodel to add.
     * @return The descriptor of the created submodel.
     * @throws Exception When an error occurs.
     */
    public SubmodelDescriptor addSubmodel(String aasId, SubmodelDescriptor submodel) throws Exception;


    /**
     * Adds a Submodel to the given AAS.
     *
     * @param submodel The submodel to add.
     * @return The descriptor of the created submodel.
     * @throws Exception When an error occurs.
     */
    public SubmodelDescriptor addSubmodel(SubmodelDescriptor submodel) throws Exception;
}
