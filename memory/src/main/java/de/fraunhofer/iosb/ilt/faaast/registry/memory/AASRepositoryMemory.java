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
package de.fraunhofer.iosb.ilt.faaast.registry.memory;

import de.fraunhofer.iosb.ilt.faaast.registry.core.AASRepository;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * In-memory implementation of the Repository.
 */
public class AASRepositoryMemory implements AASRepository {

    private final Map<String, AssetAdministrationShellDescriptor> shellDescriptors;

    public AASRepositoryMemory() {
        shellDescriptors = new HashMap<>();
    }


    @Override
    public SubmodelDescriptor addSubmodel(String aasId, SubmodelDescriptor submodel) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public SubmodelDescriptor addSubmodel(SubmodelDescriptor submodel) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public List<AssetAdministrationShellDescriptor> getAASs() {
        return new ArrayList<>(shellDescriptors.values());
    }
}
