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

import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;


/**
 * Abstract base class for implementing {@link AasRepository} interface providing helper methods for validation.
 */
public abstract class AbstractAasRepository implements AasRepository {

    protected AbstractAasRepository() {}


    @Override
    public List<AssetAdministrationShellDescriptor> getAASs() {
        return getAASs(null, null);
    }


    /**
     * Creates a new {@link ResourceNotFoundException} for the AAS.
     *
     * @param aasId the ID of the AAS
     * @return the exception to throw
     */
    protected static ResourceNotFoundException buildAASNotFoundException(String aasId) {
        return new ResourceNotFoundException(String.format("AAS not found (id: %s)", aasId));
    }


    /**
     * Creates a new {@link ResourceNotFoundException} for the submodel.
     *
     * @param submodelId the ID of the submodel
     * @return the exception to throw
     */
    protected static ResourceNotFoundException buildSubmodelNotFoundException(String submodelId) {
        return new ResourceNotFoundException(String.format("Submodel not found (id: %s)", submodelId));
    }


    /**
     * Creates a new {@link ResourceAlreadyExistsException} for when the AAS already exists.
     *
     * @param aasId the ID of the AAS
     * @return the exception to throw
     */
    protected static ResourceAlreadyExistsException buildAASAlreadyExistsException(String aasId) {
        return new ResourceAlreadyExistsException(String.format("AAS already exists (id: %s)", aasId));
    }


    /**
     * Creates a new {@link ResourceAlreadyExistsException} for when the submodel already exists.
     *
     * @param submodelId the ID of the submodel
     * @return the exception to throw
     */
    protected static ResourceAlreadyExistsException buildSubmodelAlreadyExistsException(String submodelId) {
        return new ResourceAlreadyExistsException(String.format("Submodel already exists (id: %s)", submodelId));
    }


    /**
     * Creates a new {@link ResourceNotFoundException} for when an AAS does not contain a requested submodel.
     *
     * @param aasId the ID of the AAS
     * @param submodelId the ID of the submodel
     * @return the exception to throw
     */
    protected static ResourceNotFoundException buildSubmodelNotFoundInAASException(String aasId, String submodelId) {
        return new ResourceNotFoundException(String.format("Submodel not found in AAS (AAS: %s, submodel: %s)", aasId, submodelId));
    }


    /**
     * Helper method to ensure arguments are valid or correct exceptions are thrown.
     *
     * @param descriptor the descriptor to validate
     * @throws IllegalArgumentException if descriptor does not contain id information
     */
    protected static void ensureDescriptorId(AssetAdministrationShellDescriptor descriptor) {
        Ensure.requireNonNull(descriptor, "descriptor must be non-null");
        Ensure.requireNonNull(descriptor.getId(), "descriptor id must be non-null");
    }


    /**
     * Helper method to ensure arguments are valid or correct exceptions are thrown.
     *
     * @param descriptor the descriptor to validate
     * @throws IllegalArgumentException if descriptor does not contain id information
     */
    protected static void ensureDescriptorId(SubmodelDescriptor descriptor) {
        Ensure.requireNonNull(descriptor, "descriptor must be non-null");
        Ensure.requireNonNull(descriptor.getId(), "descriptor id must be non-null");
    }


    /**
     * Helper method to ensure an aasId is not null.
     *
     * @param aasId the aasId to validate
     * @throws IllegalArgumentException if aasId is null
     */
    protected static void ensureAasId(String aasId) {
        Ensure.requireNonNull(aasId, "aasId must be non-null");
    }


    /**
     * Helper method to ensure an submodelId is not null.
     *
     * @param submodelId the submodelId to validate
     * @throws IllegalArgumentException if submodelId is null
     */
    protected static void ensureSubmodelId(String submodelId) {
        Ensure.requireNonNull(submodelId, "submodelId must be non-null");
    }


    /**
     * Helper method to look for a submodel with the desired submodelId in a given list of submdels.
     *
     * @param submodels The list of submodels to search.
     * @param submodelId The ID of the desired submodel.
     * @return The desired submodel if it was found, an empty Optional if not.
     */
    protected static Optional<SubmodelDescriptor> getSubmodelInternal(List<SubmodelDescriptor> submodels, String submodelId) {
        return submodels.stream()
                .filter(x -> Objects.nonNull(x.getId())
                        && Objects.equals(x.getId(), submodelId))
                .findAny();
    }

}
