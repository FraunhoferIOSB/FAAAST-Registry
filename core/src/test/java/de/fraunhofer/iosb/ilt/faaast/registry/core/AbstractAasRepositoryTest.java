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

import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.model.AssetLink;
import de.fraunhofer.iosb.ilt.faaast.registry.core.util.AssetLinkHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.FaaastConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEndpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProtocolInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelDescriptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;


@Transactional
public abstract class AbstractAasRepositoryTest<T extends AasRepository> {

    protected T repository;

    @AfterEach
    public void clearDatastore() {}


    protected DefaultSubmodelDescriptor getSubmodel() {
        return new DefaultSubmodelDescriptor.Builder()
                .idShort("Submodel2")
                .id("TestSubmodel2")
                .description(new DefaultLangStringTextType.Builder().text("some submodel").language("en-US").build())
                .displayName(new DefaultLangStringNameType.Builder().text("Submodel 2 Name").language("de-DE").build())
                .semanticId(new DefaultReference.Builder()
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.SUBMODEL)
                                .value("http://example.org/smTest2")
                                .build())
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .build())
                .administration(new DefaultAdministrativeInformation.Builder()
                        .revision("1")
                        .version("1.1")
                        .build())
                .endpoints(
                        new DefaultEndpoint.Builder()
                                ._interface("http")
                                .protocolInformation(new DefaultProtocolInformation.Builder()
                                        .href("localhost:8080/factory1/submodel2")
                                        .endpointProtocol("http")
                                        .build())
                                .build())
                .build();
    }


    protected AssetAdministrationShellDescriptor getAASWithSubmodel() {
        return getAASWithSubmodel("TestAAS1", "TestSubmodel1");
    }


    protected AssetAdministrationShellDescriptor getAASWithSubmodel(String shellId, String submodelId) {
        AssetAdministrationShellDescriptor aas = new DefaultAssetAdministrationShellDescriptor.Builder()
                .idShort("Test1")
                .id(shellId)
                .description(new DefaultLangStringTextType.Builder().text("some aas").language("en-US").build())
                .displayName(new DefaultLangStringNameType.Builder().text("Test 1 AAS").language("en-US").build())
                .administration(new DefaultAdministrativeInformation.Builder()
                        .revision("1")
                        .version("1.1")
                        .build())
                .specificAssetIds(new ArrayList<>(Arrays.asList(new DefaultSpecificAssetId.Builder()
                        .name("TestKey")
                        .value("ValueTest")
                        .externalSubjectId(new DefaultReference.Builder()
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value("http://example.org/aasTest1")
                                        .build())
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .build())
                        .semanticId(new DefaultReference.Builder()
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value("http://example.org/aasTest1")
                                        .build())
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .build())
                        .build())))
                .globalAssetId("http://example.org/aasTest1")
                .endpoints(new DefaultEndpoint.Builder()
                        ._interface("http")
                        .protocolInformation(new DefaultProtocolInformation.Builder()
                                .href("localhost:8080/factory1")
                                .endpointProtocol("http")
                                .build())
                        .build())
                .build();
        List<SubmodelDescriptor> submodels = new ArrayList<>();
        submodels.add(new DefaultSubmodelDescriptor.Builder()
                .idShort("Submodel1")
                .id(submodelId)
                .description(new DefaultLangStringTextType.Builder().text("some submodel").language("en-US").build())
                .displayName(new DefaultLangStringNameType.Builder().text("Submodel 1 Name").language("en-US").build())
                .semanticId(new DefaultReference.Builder()
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.SUBMODEL)
                                .value("http://example.org/smTest1")
                                .build())
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .build())
                .administration(new DefaultAdministrativeInformation.Builder()
                        .revision("1")
                        .version("1.1")
                        .build())
                .endpoints(new DefaultEndpoint.Builder()
                        ._interface("http")
                        .protocolInformation(new DefaultProtocolInformation.Builder()
                                .href("localhost:8080/factory1/submodel")
                                .endpointProtocol("http")
                                .build())
                        .build())
                .build());
        aas.setSubmodelDescriptors(submodels);
        return aas;
    }


    @Test
    public void createAAS() throws Exception {
        repository.create(getAASWithSubmodel());
    }


    @Test
    public void listAllAAS() throws Exception {
        repository.create(getAASWithSubmodel());
        Page<AssetAdministrationShellDescriptor> aass = repository.getAASs(PagingInfo.ALL);
        Assertions.assertNotNull(aass);
        Assertions.assertEquals(1, aass.getContent().size());
    }


    @Test
    public void findAASById() throws Exception {
        repository.create(getAASWithSubmodel());
        AssetAdministrationShellDescriptor aas = repository.getAAS("TestAAS1");
        Assertions.assertNotNull(aas);
    }


    @Test
    public void updateAAS() throws Exception {
        repository.create(getAASWithSubmodel());
        String aasId = "TestAAS1";
        // We have to create a new AAS here, otherwise the test won't work
        AssetAdministrationShellDescriptor aas = getAASWithSubmodel();
        aas.setIdShort("NewIdShort");
        aas.getSubmodelDescriptors().get(0).setIdShort("NewSubmodelIdShort");
        repository.update(aasId, aas);
        aas = repository.getAAS(aas.getId());
        Assertions.assertEquals("NewIdShort", aas.getIdShort());
        Assertions.assertEquals("NewSubmodelIdShort", aas.getSubmodelDescriptors().get(0).getIdShort());
    }


    @Test
    public void deleteAAS() throws Exception {
        repository.create(getAASWithSubmodel());
        Page<AssetAdministrationShellDescriptor> aass = repository.getAASs(PagingInfo.ALL);
        Assertions.assertNotNull(aass);
        Assertions.assertEquals(1, aass.getContent().size());
        repository.deleteAAS("TestAAS1");
        aass = repository.getAASs(PagingInfo.ALL);
        Assertions.assertNotNull(aass);
        Assertions.assertEquals(0, aass.getContent().size());
    }


    @Test
    public void getAASIdentifiers() throws Exception {
        AssetAdministrationShellDescriptor aasWithSubmodel = getAASWithSubmodel();
        List<AssetLink> assetLinks = AssetLinkHelper.from(aasWithSubmodel.getSpecificAssetIds());
        String globalAssetId = aasWithSubmodel.getGlobalAssetId();

        repository.create(aasWithSubmodel);

        AssetLink globalAssetIdAssetLink = new AssetLink.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(globalAssetId)
                .build();

        assetLinks.add(globalAssetIdAssetLink);

        Page<String> aass = repository.getAASIdentifiersByAssetLink(assetLinks, PagingInfo.ALL);
        Assertions.assertNotNull(aass);
        Assertions.assertEquals(1, aass.getContent().size());
        repository.deleteAAS("TestAAS1");
        aass = repository.getAASIdentifiersByAssetLink(assetLinks, PagingInfo.ALL);
        Assertions.assertNotNull(aass);
        Assertions.assertEquals(0, aass.getContent().size());
    }


    @Test
    public void getAASIdentifiersReturnCorrectId() throws Exception {
        AssetAdministrationShellDescriptor aasWithSubmodel = getAASWithSubmodel();
        List<AssetLink> assetLinks = AssetLinkHelper.from(aasWithSubmodel.getSpecificAssetIds());
        String globalAssetId = aasWithSubmodel.getGlobalAssetId();

        // Create AASDescriptor that will not match.
        AssetAdministrationShellDescriptor anotherAasWithSubmodel = getAASWithSubmodel("my-aas-2", "my-sm-2");
        anotherAasWithSubmodel.getSpecificAssetIds().clear();

        repository.create(aasWithSubmodel);
        repository.create(anotherAasWithSubmodel);

        AssetLink globalAssetIdAssetLink = new AssetLink.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(globalAssetId)
                .build();

        assetLinks.add(globalAssetIdAssetLink);

        Page<String> aass = repository.getAASIdentifiersByAssetLink(assetLinks, PagingInfo.ALL);
        Assertions.assertNotNull(aass);
        Assertions.assertEquals(1, aass.getContent().size());
        repository.deleteAAS(aasWithSubmodel.getId());
        aass = repository.getAASIdentifiersByAssetLink(assetLinks, PagingInfo.ALL);
        Assertions.assertNotNull(aass);
        Assertions.assertEquals(0, aass.getContent().size());
    }


    @Test
    public void getAASIdentifiersMismatchingGlobalAssetId() throws Exception {
        AssetAdministrationShellDescriptor aasWithSubmodel = getAASWithSubmodel();
        List<AssetLink> assetLinks = AssetLinkHelper.from(aasWithSubmodel.getSpecificAssetIds());
        String globalAssetId = aasWithSubmodel.getGlobalAssetId();

        repository.create(aasWithSubmodel);

        AssetLink globalAssetIdAssetLink = new AssetLink.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(globalAssetId + "mismatch")
                .build();

        assetLinks.add(globalAssetIdAssetLink);

        Page<String> aass = repository.getAASIdentifiersByAssetLink(assetLinks, PagingInfo.ALL);
        Assertions.assertNotNull(aass);
        Assertions.assertEquals(0, aass.getContent().size());
        repository.deleteAAS(aasWithSubmodel.getId());
    }


    @Test
    public void getAASIdentifiersDuplicateSpecificAssetId() throws Exception {
        AssetAdministrationShellDescriptor aasWithSubmodel = getAASWithSubmodel();
        List<AssetLink> assetLinks = AssetLinkHelper.from(aasWithSubmodel.getSpecificAssetIds());
        String globalAssetId = aasWithSubmodel.getGlobalAssetId();

        repository.create(aasWithSubmodel);

        AssetLink globalAssetIdAssetLink = new AssetLink.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(globalAssetId)
                .build();

        assetLinks.add(globalAssetIdAssetLink);
        assetLinks.add(assetLinks.get(0));

        Page<String> aass = repository.getAASIdentifiersByAssetLink(assetLinks, PagingInfo.ALL);
        Assertions.assertNotNull(aass);
        Assertions.assertEquals(1, aass.getContent().size());
        repository.deleteAAS(aasWithSubmodel.getId());
        aass = repository.getAASIdentifiersByAssetLink(assetLinks, PagingInfo.ALL);
        Assertions.assertNotNull(aass);
        Assertions.assertEquals(0, aass.getContent().size());
    }


    @Test
    public void getAASIdentifiersMismatchingSpecificAssetId() throws Exception {
        AssetAdministrationShellDescriptor aasWithSubmodel = getAASWithSubmodel();
        List<AssetLink> assetLinks = new ArrayList<>();
        aasWithSubmodel.getSpecificAssetIds().stream().map(sid -> new AssetLink.Builder()
                .from(sid)
                //.name(sid.getName())
                //.value(sid.getValue())
                //.supplementalSemanticIds(sid.getSupplementalSemanticIds())
                //.semanticId(sid.getSemanticId())
                //.externalSubjectId(sid.getExternalSubjectId())
                .build())
                .forEach(assetLinks::add);

        String globalAssetId = aasWithSubmodel.getGlobalAssetId();

        repository.create(aasWithSubmodel);

        AssetLink globalAssetIdAssetLink = new AssetLink.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(globalAssetId)
                .build();

        assetLinks.get(0).setValue(assetLinks.get(0).getValue() + "mismatch");
        assetLinks.add(globalAssetIdAssetLink);

        Page<String> aass = repository.getAASIdentifiersByAssetLink(assetLinks, PagingInfo.ALL);
        Assertions.assertNotNull(aass);
        Assertions.assertEquals(0, aass.getContent().size());
        repository.deleteAAS(aasWithSubmodel.getId());
    }


    @Test
    public void getAASIdentifiersTooManySpecificAssetIds() throws Exception {
        AssetAdministrationShellDescriptor aasWithSubmodel = getAASWithSubmodel();
        List<AssetLink> assetLinks = AssetLinkHelper.from(aasWithSubmodel.getSpecificAssetIds());
        String globalAssetId = aasWithSubmodel.getGlobalAssetId();

        repository.create(aasWithSubmodel);

        AssetLink globalAssetIdAssetLink = new AssetLink.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(globalAssetId)
                .build();
        // Add random specific asset id that is not in the repository
        assetLinks.add(new AssetLink.Builder().name("my-sp-a-id").value("my-value").build());

        assetLinks.add(globalAssetIdAssetLink);

        Page<String> aass = repository.getAASIdentifiersByAssetLink(assetLinks, PagingInfo.ALL);
        Assertions.assertNotNull(aass);
        Assertions.assertEquals(0, aass.getContent().size());
        repository.deleteAAS(aasWithSubmodel.getId());
    }


    @Test
    public void getAASIdentifiersNoSpecificAssetId() throws Exception {
        AssetAdministrationShellDescriptor aasWithSubmodel = getAASWithSubmodel();
        List<AssetLink> assetLinks = AssetLinkHelper.from(aasWithSubmodel.getSpecificAssetIds());
        String globalAssetId = aasWithSubmodel.getGlobalAssetId();

        repository.create(aasWithSubmodel);

        AssetLink globalAssetIdAssetLink = new AssetLink.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(globalAssetId)
                .build();

        assetLinks.clear();
        assetLinks.add(globalAssetIdAssetLink);

        Page<String> aass = repository.getAASIdentifiersByAssetLink(assetLinks, PagingInfo.ALL);
        Assertions.assertNotNull(aass);
        Assertions.assertEquals(1, aass.getContent().size());
        repository.deleteAAS(aasWithSubmodel.getId());
        aass = repository.getAASIdentifiersByAssetLink(assetLinks, PagingInfo.ALL);
        Assertions.assertNotNull(aass);
        Assertions.assertEquals(0, aass.getContent().size());
    }


    @Test
    public void getAASIdentifiersNoIdsInQuery() throws Exception {
        AssetAdministrationShellDescriptor aasWithSubmodel = getAASWithSubmodel();

        repository.create(aasWithSubmodel);
        List<AssetLink> assetLinks = List.of();
        Page<String> aass = repository.getAASIdentifiersByAssetLink(assetLinks, PagingInfo.ALL);
        Assertions.assertNotNull(aass);
        Assertions.assertEquals(1, aass.getContent().size());
        repository.deleteAAS(aasWithSubmodel.getId());
        aass = repository.getAASIdentifiersByAssetLink(assetLinks, PagingInfo.ALL);
        Assertions.assertNotNull(aass);
        Assertions.assertEquals(0, aass.getContent().size());
    }


    @Test
    public void createSubmodel() throws Exception {
        repository.addSubmodel(getSubmodel());
    }


    @Test
    public void createSubmodelToAAS() throws Exception {
        SubmodelDescriptor submodel = getSubmodel();
        AssetAdministrationShellDescriptor aas = getAASWithSubmodel();
        repository.create(aas);
        repository.addSubmodel(aas.getId(), submodel);
        compareSubmodel(submodel, repository.getSubmodel(aas.getId(), submodel.getId()));
    }


    @Test
    public void listAllSubmodels() throws Exception {
        repository.create(getAASWithSubmodel());
        repository.addSubmodel(getSubmodel());
        Page<SubmodelDescriptor> submodels = repository.getSubmodels(PagingInfo.ALL);
        Assertions.assertNotNull(submodels);
        Assertions.assertEquals(1, submodels.getContent().size());
    }


    @Test
    public void findStandAloneSubmodelById() throws Exception {
        AssetAdministrationShellDescriptor aas = getAASWithSubmodel();
        SubmodelDescriptor submodel = getSubmodel();
        repository.create(getAASWithSubmodel());
        repository.addSubmodel(submodel);

        SubmodelDescriptor findSubmodel;
        // Ensure, submodel of the AAS is not registered
        Assertions.assertThrows(ResourceNotFoundException.class, () -> repository.getSubmodel(aas.getSubmodelDescriptors().get(0).getId()));
        findSubmodel = repository.getSubmodel(submodel.getId());
        Assertions.assertNotNull(findSubmodel);
    }


    @Test
    public void findAASSubmodelById() throws Exception {
        AssetAdministrationShellDescriptor aas = getAASWithSubmodel();
        SubmodelDescriptor submodel = getSubmodel();
        repository.create(getAASWithSubmodel());
        repository.addSubmodel(aas.getId(), submodel);

        SubmodelDescriptor findSubmodel = repository.getSubmodel(aas.getId(), submodel.getId());
        Assertions.assertNotNull(findSubmodel);
    }


    @Test
    public void deleteStandAloneSubmodel() throws Exception {
        SubmodelDescriptor submodel = getSubmodel();
        repository.addSubmodel(submodel);
        compareSubmodel(submodel, repository.getSubmodel(submodel.getId()));

        repository.deleteSubmodel(submodel.getId());
        Assertions.assertThrows(ResourceNotFoundException.class, () -> repository.getSubmodel(submodel.getId()));
    }


    @Test
    public void deleteAASSubmodel() throws Exception {
        SubmodelDescriptor submodel = getSubmodel();
        AssetAdministrationShellDescriptor aas = getAASWithSubmodel();
        repository.create(aas);
        repository.addSubmodel(aas.getId(), submodel);
        compareSubmodel(submodel, repository.getSubmodel(aas.getId(), submodel.getId()));

        repository.deleteSubmodel(aas.getId(), submodel.getId());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> repository.getSubmodel(aas.getId(), submodel.getId()));
    }


    protected void compareSubmodel(SubmodelDescriptor expected, SubmodelDescriptor actual) {
        Assertions.assertEquals(expected, actual);
    }
}
