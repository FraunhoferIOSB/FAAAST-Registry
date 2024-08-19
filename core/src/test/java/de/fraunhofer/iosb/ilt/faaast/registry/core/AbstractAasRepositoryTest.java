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
        AssetAdministrationShellDescriptor aas = new DefaultAssetAdministrationShellDescriptor.Builder()
                .idShort("Test1")
                .id("TestAAS1")
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
                .id("TestSubmodel1")
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
        List<AssetAdministrationShellDescriptor> aass = repository.getAASs();
        Assert.assertEquals(1, aass.size());
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
        List<AssetAdministrationShellDescriptor> aass = repository.getAASs();
        Assert.assertEquals(1, aass.size());
        repository.deleteAAS("TestAAS1");
        aass = repository.getAASs();
        Assert.assertEquals(0, aass.size());
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
        List<SubmodelDescriptor> submodels = repository.getSubmodels();
        Assert.assertEquals(1, submodels.size());
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
