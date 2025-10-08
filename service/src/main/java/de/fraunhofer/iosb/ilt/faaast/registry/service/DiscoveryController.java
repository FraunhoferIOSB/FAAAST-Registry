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
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.FaaastConstants;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


/**
 * REST controller for the Submodel registry.
 */
@RestController
@RequestMapping(value = Constants.DISCOVERY_PATH)
public class DiscoveryController {

    @Autowired
    RegistryService service;

    /**
     * Returns a list of Asset Administration Shell ids linked to specific Asset identifiers.
     *
     * @param assetIds A list of AssetLinks that all shall match. An AssetLink might be either derived from a
     *            SpecificAssetId ("name": "'specificAssetId.name'", "value": "'specificAssetId.value'")
     *            or a globalAssetId ("name": "globalAssetId", "value": "'globalAssetId-value'").
     * @param limit The maximum number of elements in the response array. minimum: 1
     * @param cursor The cursor value.
     * @return Requested Asset Administration Shell ids.
     */
    @GetMapping("")
    public Page<String> getAllAssetAdministrationShellIdsBySpecificAssetIds(
                                                                            // Default Value is a b64URL-encoded '[]'
                                                                            @RequestParam(name = "assetIds", required = false, defaultValue = "W10") List<SpecificAssetId> assetIds,
                                                                            @RequestParam(name = "limit", required = false) Long limit,
                                                                            @RequestParam(name = "cursor", required = false) String cursor) {

        PagingInfo pagingInfo = PagingInfo.builder()
                .cursor(cursor)
                .limit(limit == null ? PagingInfo.DEFAULT_LIMIT : limit)
                .build();

        return service.getAASIdsBySpecificAssetId(assetIds, pagingInfo);
    }


    /**
     * Returns a list of specific asset identifiers based on an Asset Administration Shell ID to edit discoverable content.
     * The global asset ID is returned as specific asset ID with "name" equal to "globalAssetId" (see Constraint AASd-116).
     *
     * @param aasIdentifier The Asset Administration Shell’s unique id (UTF8-BASE64-URL-encoded).
     * @return Requested specific Asset identifiers.
     */
    @GetMapping("/{aasIdentifier}")
    public List<SpecificAssetId> getAllAssetLinksById(
                                                      @PathVariable(name = "aasIdentifier") String aasIdentifier)
            throws ResourceNotFoundException {
        AssetAdministrationShellDescriptor selectedDescriptor = service.getAAS(aasIdentifier);

        List<SpecificAssetId> result = new ArrayList<>(selectedDescriptor.getSpecificAssetIds());
        if (Objects.nonNull(selectedDescriptor.getGlobalAssetId())) {
            result.add(new DefaultSpecificAssetId.Builder()
                    .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                    .value(selectedDescriptor.getGlobalAssetId())
                    .build());
        }

        return result;
    }


    /**
     * Returns a list of specific asset identifiers based on an Asset Administration Shell ID to edit discoverable content.
     * The global asset ID is returned as specific asset ID with "name" equal to "globalAssetId" (see Constraint AASd-116).
     *
     * @param aasIdentifier The Asset Administration Shell’s unique id (UTF8-BASE64-URL-encoded).
     * @return Requested specific Asset identifiers.
     */
    @PostMapping("/{aasIdentifier}")
    public ResponseEntity<List<SpecificAssetId>> postAllAssetLinksById(
                                                                       @PathVariable(name = "aasIdentifier") String aasIdentifier,
                                                                       @RequestBody List<SpecificAssetId> specificAssetIds)
            throws ResourceNotFoundException {
        AssetAdministrationShellDescriptor selectedDescriptor = service.getAAS(aasIdentifier);

        List<SpecificAssetId> globalKeys = specificAssetIds.stream()
                .filter(x -> FaaastConstants.KEY_GLOBAL_ASSET_ID.equals(x.getName()))
                .toList();

        if (!globalKeys.isEmpty()) {
            if (globalKeys.size() == 1 && globalKeys.get(0) != null) {
                selectedDescriptor.setGlobalAssetId(globalKeys.get(0).getValue());
            }
            else {
                throw new BadRequestException(String.format("%s must be unique in the specificAssetIds but was found %s times.",
                        FaaastConstants.KEY_GLOBAL_ASSET_ID,
                        globalKeys.size()));
            }
        }

        specificAssetIds.removeAll(globalKeys);

        List<SpecificAssetId> updatedSpecificAssetIds = selectedDescriptor.getSpecificAssetIds();

        specificAssetIds.forEach(specificAssetId -> {
            var existing = updatedSpecificAssetIds.stream()
                    .filter(id -> Objects.equals(specificAssetId.getName(), id.getName()))
                    .toList();
            if (existing.size() == 1) {
                updatedSpecificAssetIds.remove(existing.get(0));
            }
            else if (existing.size() > 1) {
                throw new BadRequestException(String.format("Name %s must be unique in the specificAssetIds but was found %s times.",
                        specificAssetId.getName(), existing.size()));
            }
            updatedSpecificAssetIds.add(specificAssetId);
        });

        service.updateAAS(aasIdentifier, selectedDescriptor);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path(String.format("/%s", EncodingHelper.base64UrlEncode(selectedDescriptor.getId())))
                .build().toUri();

        List<SpecificAssetId> result = selectedDescriptor.getSpecificAssetIds();
        if (selectedDescriptor.getGlobalAssetId() != null) {
            SpecificAssetId globalAssetIdAsSpecificAssetId = new DefaultSpecificAssetId.Builder()
                    .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                    .value(selectedDescriptor.getGlobalAssetId())
                    .build();

            result.add(globalAssetIdAsSpecificAssetId);
        }

        return ResponseEntity.created(location).body(result);
    }


    /**
     * Returns a list of specific asset identifiers based on an Asset Administration Shell ID to edit discoverable content.
     * The global asset ID is returned as specific asset ID with "name" equal to "globalAssetId" (see Constraint AASd-116).
     *
     * @param aasIdentifier The Asset Administration Shell’s unique id (UTF8-BASE64-URL-encoded).
     * @return Requested specific Asset identifiers.
     */
    @DeleteMapping("/{aasIdentifier}")
    public ResponseEntity<Void> deleteAllAssetLinksById(
                                                        @PathVariable(name = "aasIdentifier") String aasIdentifier)
            throws ResourceNotFoundException {
        AssetAdministrationShellDescriptor selectedDescriptor = service.getAAS(aasIdentifier);
        selectedDescriptor.getSpecificAssetIds().clear();
        selectedDescriptor.setGlobalAssetId(null);

        service.updateAAS(aasIdentifier, selectedDescriptor);

        return ResponseEntity.noContent().build();
    }
}
