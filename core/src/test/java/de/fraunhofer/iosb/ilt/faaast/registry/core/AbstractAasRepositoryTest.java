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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.FaaastConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
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
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;


@Transactional
public abstract class AbstractAasRepositoryTest<T extends AasRepository> {

    protected T repository;

    @After
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
        Assert.assertNotNull(aass);
        Assert.assertEquals(1, aass.getContent().size());
    }


    @Test
    public void findAASById() throws Exception {
        repository.create(getAASWithSubmodel());
        AssetAdministrationShellDescriptor aas = repository.getAAS("TestAAS1");
        Assert.assertNotNull(aas);
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
        Assert.assertEquals("NewIdShort", aas.getIdShort());
        Assert.assertEquals("NewSubmodelIdShort", aas.getSubmodelDescriptors().get(0).getIdShort());
    }


    @Test
    public void deleteAAS() throws Exception {
        repository.create(getAASWithSubmodel());
        Page<AssetAdministrationShellDescriptor> aass = repository.getAASs(PagingInfo.ALL);
        Assert.assertNotNull(aass);
        Assert.assertEquals(1, aass.getContent().size());
        repository.deleteAAS("TestAAS1");
        aass = repository.getAASs(PagingInfo.ALL);
        Assert.assertNotNull(aass);
        Assert.assertEquals(0, aass.getContent().size());
    }


    @Test
    public void getAASIdentifiers() throws Exception {
        AssetAdministrationShellDescriptor aasWithSubmodel = getAASWithSubmodel();
        List<SpecificAssetId> specificAssetIds = new ArrayList<>(aasWithSubmodel.getSpecificAssetIds());
        String globalAssetId = aasWithSubmodel.getGlobalAssetId();

        repository.create(aasWithSubmodel);

        SpecificAssetId globalAssetIdAsSpecificAssetId = new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(globalAssetId)
                .build();

        specificAssetIds.add(globalAssetIdAsSpecificAssetId);

        Page<String> aass = repository.getAASIdentifiers(specificAssetIds, PagingInfo.ALL);
        Assert.assertNotNull(aass);
        Assert.assertEquals(1, aass.getContent().size());
        repository.deleteAAS("TestAAS1");
        aass = repository.getAASIdentifiers(specificAssetIds, PagingInfo.ALL);
        Assert.assertNotNull(aass);
        Assert.assertEquals(0, aass.getContent().size());
    }


    @Test
    public void getAASIdentifiersReturnCorrectId() throws Exception {
        AssetAdministrationShellDescriptor aasWithSubmodel = getAASWithSubmodel();
        List<SpecificAssetId> specificAssetIds = new ArrayList<>(aasWithSubmodel.getSpecificAssetIds());
        String globalAssetId = aasWithSubmodel.getGlobalAssetId();

        // Create AASDescriptor that will not match.
        AssetAdministrationShellDescriptor anotherAasWithSubmodel = getAASWithSubmodel("my-aas-2", "my-sm-2");
        anotherAasWithSubmodel.getSpecificAssetIds().clear();

        repository.create(aasWithSubmodel);
        repository.create(anotherAasWithSubmodel);

        SpecificAssetId globalAssetIdAsSpecificAssetId = new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(globalAssetId)
                .build();

        specificAssetIds.add(globalAssetIdAsSpecificAssetId);

        Page<String> aass = repository.getAASIdentifiers(specificAssetIds, PagingInfo.ALL);
        Assert.assertNotNull(aass);
        Assert.assertEquals(1, aass.getContent().size());
        repository.deleteAAS(aasWithSubmodel.getId());
        aass = repository.getAASIdentifiers(specificAssetIds, PagingInfo.ALL);
        Assert.assertNotNull(aass);
        Assert.assertEquals(0, aass.getContent().size());
    }


    @Test
    public void getAASIdentifiersMismatchingGlobalAssetId() throws Exception {
        AssetAdministrationShellDescriptor aasWithSubmodel = getAASWithSubmodel();
        List<SpecificAssetId> specificAssetIds = new ArrayList<>(aasWithSubmodel.getSpecificAssetIds());
        String globalAssetId = aasWithSubmodel.getGlobalAssetId();

        repository.create(aasWithSubmodel);

        SpecificAssetId globalAssetIdAsSpecificAssetId = new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(globalAssetId + "mismatch")
                .build();

        specificAssetIds.add(globalAssetIdAsSpecificAssetId);

        Page<String> aass = repository.getAASIdentifiers(specificAssetIds, PagingInfo.ALL);
        Assert.assertNotNull(aass);
        Assert.assertEquals(0, aass.getContent().size());
        repository.deleteAAS(aasWithSubmodel.getId());
    }


    @Test
    public void getAASIdentifiersDuplicateSpecificAssetId() throws Exception {
        AssetAdministrationShellDescriptor aasWithSubmodel = getAASWithSubmodel();
        List<SpecificAssetId> specificAssetIds = new ArrayList<>(aasWithSubmodel.getSpecificAssetIds());
        String globalAssetId = aasWithSubmodel.getGlobalAssetId();

        repository.create(aasWithSubmodel);

        SpecificAssetId globalAssetIdAsSpecificAssetId = new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(globalAssetId)
                .build();

        specificAssetIds.add(globalAssetIdAsSpecificAssetId);
        specificAssetIds.add(specificAssetIds.get(0));

        Page<String> aass = repository.getAASIdentifiers(specificAssetIds, PagingInfo.ALL);
        Assert.assertNotNull(aass);
        Assert.assertEquals(1, aass.getContent().size());
        repository.deleteAAS(aasWithSubmodel.getId());
        aass = repository.getAASIdentifiers(specificAssetIds, PagingInfo.ALL);
        Assert.assertNotNull(aass);
        Assert.assertEquals(0, aass.getContent().size());
    }


    @Test
    public void getAASIdentifiersMismatchingSpecificAssetId() throws Exception {
        AssetAdministrationShellDescriptor aasWithSubmodel = getAASWithSubmodel();
        List<SpecificAssetId> specificAssetIds = new ArrayList<>();
        aasWithSubmodel.getSpecificAssetIds().stream().map(sid -> new DefaultSpecificAssetId.Builder()
                .name(sid.getName())
                .value(sid.getValue())
                .supplementalSemanticIds(sid.getSupplementalSemanticIds())
                .semanticId(sid.getSemanticId())
                .externalSubjectId(sid.getExternalSubjectId())
                .build())
                .forEach(specificAssetIds::add);

        String globalAssetId = aasWithSubmodel.getGlobalAssetId();

        repository.create(aasWithSubmodel);

        SpecificAssetId globalAssetIdAsSpecificAssetId = new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(globalAssetId)
                .build();

        specificAssetIds.get(0).setValue(specificAssetIds.get(0).getValue() + "mismatch");
        specificAssetIds.add(globalAssetIdAsSpecificAssetId);

        Page<String> aass = repository.getAASIdentifiers(specificAssetIds, PagingInfo.ALL);
        Assert.assertNotNull(aass);
        Assert.assertEquals(0, aass.getContent().size());
        repository.deleteAAS(aasWithSubmodel.getId());
    }


    @Test
    public void getAASIdentifiersMismatchingSemanticId() throws Exception {
        AssetAdministrationShellDescriptor aasWithSubmodel = getAASWithSubmodel();
        List<SpecificAssetId> specificAssetIds = new ArrayList<>();
        aasWithSubmodel.getSpecificAssetIds().stream().map(sid -> new DefaultSpecificAssetId.Builder()
                .name(sid.getName())
                .value(sid.getValue())
                .supplementalSemanticIds(sid.getSupplementalSemanticIds())
                .semanticId(sid.getSemanticId())
                .externalSubjectId(sid.getExternalSubjectId())
                .build())
                .forEach(specificAssetIds::add);

        String globalAssetId = aasWithSubmodel.getGlobalAssetId();

        repository.create(aasWithSubmodel);

        SpecificAssetId globalAssetIdAsSpecificAssetId = new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(globalAssetId)
                .build();

        specificAssetIds.get(0).setSemanticId(
                new DefaultReference.Builder()
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.ASSET_ADMINISTRATION_SHELL)
                                .value("id")
                                .build())
                        .build());

        specificAssetIds.add(globalAssetIdAsSpecificAssetId);

        Page<String> aass = repository.getAASIdentifiers(specificAssetIds, PagingInfo.ALL);
        Assert.assertNotNull(aass);
        Assert.assertEquals(0, aass.getContent().size());
        repository.deleteAAS(aasWithSubmodel.getId());
    }


    @Test
    public void getAASIdentifiersMismatchingSupplementalSemanticIds() throws Exception {
        AssetAdministrationShellDescriptor aasWithSubmodel = getAASWithSubmodel();
        List<SpecificAssetId> specificAssetIds = new ArrayList<>();
        aasWithSubmodel.getSpecificAssetIds().stream().map(sid -> new DefaultSpecificAssetId.Builder()
                .name(sid.getName())
                .value(sid.getValue())
                .supplementalSemanticIds(new ArrayList<>(sid.getSupplementalSemanticIds()))
                .semanticId(sid.getSemanticId())
                .externalSubjectId(sid.getExternalSubjectId())
                .build())
                .forEach(specificAssetIds::add);

        String globalAssetId = aasWithSubmodel.getGlobalAssetId();

        repository.create(aasWithSubmodel);

        SpecificAssetId globalAssetIdAsSpecificAssetId = new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(globalAssetId)
                .build();

        specificAssetIds.get(0).getSupplementalSemanticIds().add(
                new DefaultReference.Builder()
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.ASSET_ADMINISTRATION_SHELL)
                                .value("id")
                                .build())
                        .build());

        specificAssetIds.add(globalAssetIdAsSpecificAssetId);

        Page<String> aass = repository.getAASIdentifiers(specificAssetIds, PagingInfo.ALL);
        Assert.assertNotNull(aass);
        Assert.assertEquals(0, aass.getContent().size());
        repository.deleteAAS(aasWithSubmodel.getId());
    }


    @Test
    public void getAASIdentifiersMismatchingExternalSubjectIds() throws Exception {
        AssetAdministrationShellDescriptor aasWithSubmodel = getAASWithSubmodel();
        List<SpecificAssetId> specificAssetIds = new ArrayList<>();
        aasWithSubmodel.getSpecificAssetIds().stream().map(sid -> new DefaultSpecificAssetId.Builder()
                .name(sid.getName())
                .value(sid.getValue())
                .supplementalSemanticIds(new ArrayList<>(sid.getSupplementalSemanticIds()))
                .semanticId(sid.getSemanticId())
                .externalSubjectId(sid.getExternalSubjectId())
                .build())
                .forEach(specificAssetIds::add);

        String globalAssetId = aasWithSubmodel.getGlobalAssetId();

        repository.create(aasWithSubmodel);

        SpecificAssetId globalAssetIdAsSpecificAssetId = new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(globalAssetId)
                .build();

        specificAssetIds.get(0).setExternalSubjectId(
                new DefaultReference.Builder()
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.ASSET_ADMINISTRATION_SHELL)
                                .value("id")
                                .build())
                        .build());

        specificAssetIds.add(globalAssetIdAsSpecificAssetId);

        Page<String> aass = repository.getAASIdentifiers(specificAssetIds, PagingInfo.ALL);
        Assert.assertNotNull(aass);
        Assert.assertEquals(0, aass.getContent().size());
        repository.deleteAAS(aasWithSubmodel.getId());
    }


    @Test
    public void getAASIdentifiersTooManySpecificAssetIds() throws Exception {
        AssetAdministrationShellDescriptor aasWithSubmodel = getAASWithSubmodel();
        List<SpecificAssetId> specificAssetIds = new ArrayList<>(aasWithSubmodel.getSpecificAssetIds());
        String globalAssetId = aasWithSubmodel.getGlobalAssetId();

        repository.create(aasWithSubmodel);

        SpecificAssetId globalAssetIdAsSpecificAssetId = new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(globalAssetId)
                .build();
        // Add random specific asset id that is not in the repository
        specificAssetIds.add(new DefaultSpecificAssetId.Builder().name("my-sp-a-id").value("my-value").build());

        specificAssetIds.add(globalAssetIdAsSpecificAssetId);

        Page<String> aass = repository.getAASIdentifiers(specificAssetIds, PagingInfo.ALL);
        Assert.assertNotNull(aass);
        Assert.assertEquals(0, aass.getContent().size());
        repository.deleteAAS(aasWithSubmodel.getId());
    }


    @Test
    public void getAASIdentifiersNoSpecificAssetId() throws Exception {
        AssetAdministrationShellDescriptor aasWithSubmodel = getAASWithSubmodel();
        List<SpecificAssetId> specificAssetIds = new ArrayList<>(aasWithSubmodel.getSpecificAssetIds());
        String globalAssetId = aasWithSubmodel.getGlobalAssetId();

        repository.create(aasWithSubmodel);

        SpecificAssetId globalAssetIdAsSpecificAssetId = new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(globalAssetId)
                .build();

        specificAssetIds.clear();
        specificAssetIds.add(globalAssetIdAsSpecificAssetId);

        Page<String> aass = repository.getAASIdentifiers(specificAssetIds, PagingInfo.ALL);
        Assert.assertNotNull(aass);
        Assert.assertEquals(1, aass.getContent().size());
        repository.deleteAAS(aasWithSubmodel.getId());
        aass = repository.getAASIdentifiers(specificAssetIds, PagingInfo.ALL);
        Assert.assertNotNull(aass);
        Assert.assertEquals(0, aass.getContent().size());
    }


    @Test
    public void getAASIdentifiersNoIdsInQuery() throws Exception {
        AssetAdministrationShellDescriptor aasWithSubmodel = getAASWithSubmodel();

        repository.create(aasWithSubmodel);
        List<SpecificAssetId> specificAssetIds = List.of();
        Page<String> aass = repository.getAASIdentifiers(specificAssetIds, PagingInfo.ALL);
        Assert.assertNotNull(aass);
        Assert.assertEquals(1, aass.getContent().size());
        repository.deleteAAS(aasWithSubmodel.getId());
        aass = repository.getAASIdentifiers(specificAssetIds, PagingInfo.ALL);
        Assert.assertNotNull(aass);
        Assert.assertEquals(0, aass.getContent().size());
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
        Assert.assertNotNull(submodels);
        Assert.assertEquals(1, submodels.getContent().size());
    }


    @Test
    public void findStandAloneSubmodelById() throws Exception {
        AssetAdministrationShellDescriptor aas = getAASWithSubmodel();
        SubmodelDescriptor submodel = getSubmodel();
        repository.create(getAASWithSubmodel());
        repository.addSubmodel(submodel);

        SubmodelDescriptor findSubmodel;
        // Ensure, submodel of the AAS is not registered
        Assert.assertThrows(ResourceNotFoundException.class, () -> repository.getSubmodel(aas.getSubmodelDescriptors().get(0).getId()));
        findSubmodel = repository.getSubmodel(submodel.getId());
        Assert.assertNotNull(findSubmodel);
    }


    @Test
    public void findAASSubmodelById() throws Exception {
        AssetAdministrationShellDescriptor aas = getAASWithSubmodel();
        SubmodelDescriptor submodel = getSubmodel();
        repository.create(getAASWithSubmodel());
        repository.addSubmodel(aas.getId(), submodel);

        SubmodelDescriptor findSubmodel = repository.getSubmodel(aas.getId(), submodel.getId());
        Assert.assertNotNull(findSubmodel);
    }


    @Test
    public void deleteStandAloneSubmodel() throws Exception {
        SubmodelDescriptor submodel = getSubmodel();
        repository.addSubmodel(submodel);
        compareSubmodel(submodel, repository.getSubmodel(submodel.getId()));

        repository.deleteSubmodel(submodel.getId());
        Assert.assertThrows(ResourceNotFoundException.class, () -> repository.getSubmodel(submodel.getId()));
    }


    @Test
    public void deleteAASSubmodel() throws Exception {
        SubmodelDescriptor submodel = getSubmodel();
        AssetAdministrationShellDescriptor aas = getAASWithSubmodel();
        repository.create(aas);
        repository.addSubmodel(aas.getId(), submodel);
        compareSubmodel(submodel, repository.getSubmodel(aas.getId(), submodel.getId()));

        repository.deleteSubmodel(aas.getId(), submodel.getId());

        Assert.assertThrows(ResourceNotFoundException.class, () -> repository.getSubmodel(aas.getId(), submodel.getId()));
    }


    protected void compareSubmodel(SubmodelDescriptor expected, SubmodelDescriptor actual) {
        Assert.assertEquals(expected, actual);
    }
}
