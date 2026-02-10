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

import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.BadRequestException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.FaaastConstants;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;


/**
 * Abstract base class for implementing {@link AasRepository} interface providing helper methods for validation.
 */
public abstract class AbstractAasRepository implements AasRepository {

    protected AbstractAasRepository() {}


    @Override
    public Page<AssetAdministrationShellDescriptor> getAASs(PagingInfo paging) {
        return getAASs(null, null, paging);
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


    /**
     * Helper method to filter a shell descriptor list with the desired specificAssetIds and globalAssetId.
     *
     * @param descriptors The list of shell descriptors to filter.
     * @param specificAssetIds The specificAssetIds of the desired shells.
     * @param pagingInfo The pagingInfo
     * @return Page of AAS Descriptors, not null.
     */
    protected Page<String> filterAssetAdministrationShellDescriptors(Collection<AssetAdministrationShellDescriptor> descriptors,
                                                                     Collection<SpecificAssetId> specificAssetIds,
                                                                     PagingInfo pagingInfo) {
        int limit = readLimit(pagingInfo);
        int cursor = readCursor(pagingInfo);

        List<AssetAdministrationShellDescriptor> filteredDescriptors = new ArrayList<>(descriptors);

        List<SpecificAssetId> realSpecificAssetIds = new ArrayList<>(specificAssetIds);

        filterByGlobalAssetId(filteredDescriptors, specificAssetIds, realSpecificAssetIds);

        List<String> filteredDescriptorIds = new ArrayList<>();

        for (AssetAdministrationShellDescriptor descriptor: filteredDescriptors) {
            if (contains(realSpecificAssetIds, descriptor.getSpecificAssetIds())) {
                filteredDescriptorIds.add(descriptor.getId());
            }
        }

        return getPage(filteredDescriptorIds, cursor, filteredDescriptorIds.size());
    }


    /**
     * Helper method to filter a shell descriptor list with the desired AssetLinks.
     *
     * @param descriptors The list of shell descriptors to filter.
     * @param assetLinks The assetLinks of the desired shells.
     * @param pagingInfo The pagingInfo
     * @return Page of AAS Descriptors, not null.
     */
    protected Page<String> filterAssetAdministrationShellDescriptorsByAssetLink(Collection<AssetAdministrationShellDescriptor> descriptors,
                                                                                Collection<SpecificAssetId> assetLinks,
                                                                                PagingInfo pagingInfo) {
        int limit = readLimit(pagingInfo);
        int cursor = readCursor(pagingInfo);

        List<AssetAdministrationShellDescriptor> filteredDescriptors = new ArrayList<>(descriptors);

        List<SpecificAssetId> realSpecificAssetIds = new ArrayList<>(assetLinks);

        filterByGlobalAssetId(filteredDescriptors, assetLinks, realSpecificAssetIds);

        List<String> filteredDescriptorIds = new ArrayList<>();

        for (AssetAdministrationShellDescriptor descriptor: filteredDescriptors) {
            if (containsAssetLink(realSpecificAssetIds, descriptor.getSpecificAssetIds())) {
                filteredDescriptorIds.add(descriptor.getId());
            }
        }

        return getPage(filteredDescriptorIds, cursor, filteredDescriptorIds.size());
    }


    /* Returns true if all elements of subset are contained in superset */
    private boolean contains(List<SpecificAssetId> subset, List<SpecificAssetId> superset) {
        // Remove all that are contained in the superset (i.e. keep all that are not in superset)
        for (SpecificAssetId subId: subset) {
            if (superset.stream().noneMatch(superId -> specificAssetIdEquality(superId, subId))) {
                return false;
            }
        }
        return true;
    }


    private boolean specificAssetIdEquality(SpecificAssetId a, SpecificAssetId b) {
        return Objects.equals(a.getName(), b.getName()) &&
                Objects.equals(a.getValue(), b.getValue()) &&
                ReferenceHelper.equals(a.getSemanticId(), b.getSemanticId()) &&
                ReferenceHelper.equals(a.getExternalSubjectId(), b.getExternalSubjectId()) &&
                a.getSupplementalSemanticIds().stream()
                        .allMatch(aSuppId -> b.getSupplementalSemanticIds().stream()
                                .anyMatch(bSuppId -> ReferenceHelper.equals(aSuppId, bSuppId)))
                &&
                b.getSupplementalSemanticIds().stream()
                        .allMatch(aSuppId -> a.getSupplementalSemanticIds().stream()
                                .anyMatch(bSuppId -> ReferenceHelper.equals(aSuppId, bSuppId)));
    }


    /* Returns true if all elements of AssetLink subset are contained in AssetLink superset */
    private boolean containsAssetLink(List<SpecificAssetId> subset, List<SpecificAssetId> superset) {
        // Remove all that are contained in the superset (i.e. keep all that are not in superset)
        for (SpecificAssetId subId: subset) {
            if (superset.stream().noneMatch(superId -> assetLinkEquality(superId, subId))) {
                return false;
            }
        }
        return true;
    }


    private boolean assetLinkEquality(SpecificAssetId a, SpecificAssetId b) {
        return Objects.equals(a.getName(), b.getName()) &&
                Objects.equals(a.getValue(), b.getValue());
    }


    /**
     * Helper method to read the limit as integer from the paging info.
     *
     * @param paging The desired paging info.
     * @return The limit as integer value.
     */
    protected static int readLimit(PagingInfo paging) {
        if (!paging.hasLimit()) {
            return AasRepository.DEFAULT_LIMIT;
        }
        int limit = (int) paging.getLimit();
        if ((limit <= 0) || (limit > AasRepository.DEFAULT_LIMIT)) {
            limit = AasRepository.DEFAULT_LIMIT;
        }
        return limit;
    }


    /**
     * Helper method to read the cursor as integer value from the paging info.
     *
     * @param paging The desired paging info.
     * @return The cursor as integer value.
     */
    protected static int readCursor(PagingInfo paging) {
        int cursor = 0;
        try {
            if (paging.getCursor() != null) {
                cursor = Integer.parseInt(paging.getCursor());
            }
        }
        catch (NumberFormatException ex) {
            throw new BadRequestException("Cursor must be an Integer");
        }
        return cursor;
    }


    /**
     * Constructs a page from the given list.
     *
     * @param <T> The class of the list.
     * @param list The desired list.
     * @param cursor The cursor.
     * @param totalSize The total size.
     * @return The desired page.
     */
    protected static <T> Page<T> getPage(List<T> list, int cursor, int totalSize) {
        String nextCursor = null;
        if (cursor + list.size() < totalSize) {
            nextCursor = Integer.toString(cursor + list.size());
        }
        return Page.<T> builder()
                .result(list)
                .metadata(PagingMetadata.builder()
                        .cursor(nextCursor)
                        .build())
                .build();
    }


    private void filterByGlobalAssetId(List<AssetAdministrationShellDescriptor> descriptors, Collection<SpecificAssetId> specificAssetIds,
                                       List<SpecificAssetId> realSpecificAssetIds) {
        List<SpecificAssetId> globalAssetIds = specificAssetIds.stream()
                .filter(specificAssetId -> FaaastConstants.KEY_GLOBAL_ASSET_ID.equalsIgnoreCase(specificAssetId.getName()))
                .toList();

        if (globalAssetIds.size() > 1) {
            // An AAS descriptor can only have one globalAssetId.
            descriptors.clear();
        }
        else if (!globalAssetIds.isEmpty()) {
            String globalAssetId = globalAssetIds.get(0).getValue();
            descriptors.removeIf(descriptor -> !Objects.equals(globalAssetId, descriptor.getGlobalAssetId()));
        }
        realSpecificAssetIds.removeAll(globalAssetIds);
    }
}
