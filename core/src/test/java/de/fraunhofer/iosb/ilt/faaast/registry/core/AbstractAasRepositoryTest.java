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
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultAssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultEndpoint;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultProtocolInformation;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultSubmodelDescriptor;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.impl.DefaultAdministrativeInformation;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultIdentifierKeyValuePair;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        return DefaultSubmodelDescriptor.builder()
                .idShort("Submodel2")
                .identification(new DefaultIdentifier.Builder()
                        .idType(IdentifierType.CUSTOM)
                        .identifier("TestSubmodel2")
                        .build())
                .description(new LangString("some submodel", "en-US"))
                .displayName(new LangString("Submodel 2 Name", "de-DE"))
                .semanticId(new DefaultReference.Builder()
                        .key(new DefaultKey.Builder()
                                .idType(KeyType.IRI)
                                .type(KeyElements.SUBMODEL)
                                .value("http://example.org/smTest2")
                                .build())
                        .build())
                .administration(new DefaultAdministrativeInformation.Builder()
                        .revision("1")
                        .version("1.1")
                        .build())
                .endpoint(DefaultEndpoint.builder()
                        .interfaceInformation("http")
                        .protocolInformation(DefaultProtocolInformation.builder()
                                .endpointAddress("localhost:8080/factory1/submodel2")
                                .endpointProtocol("http")
                                .build())
                        .build())
                .build();
    }


    protected AssetAdministrationShellDescriptor getAASWithSubmodel() {
        AssetAdministrationShellDescriptor aas = DefaultAssetAdministrationShellDescriptor.builder()
                .idShort("Test1")
                .identification(new DefaultIdentifier.Builder()
                        .idType(IdentifierType.CUSTOM)
                        .identifier("TestAAS1")
                        .build())
                .description(new LangString("some aas", "en-US"))
                .displayName(new LangString("Test 1 AAS", "en-US"))
                .administration(new DefaultAdministrativeInformation.Builder()
                        .revision("1")
                        .version("1.1")
                        .build())
                .specificAssetIds(new ArrayList<>(Arrays.asList(new DefaultIdentifierKeyValuePair.Builder()
                        .externalSubjectId(new DefaultReference.Builder()
                                .key(new DefaultKey.Builder()
                                        .idType(KeyType.IRI)
                                        .type(KeyElements.ASSET)
                                        .value("http://example.org/aasTest1")
                                        .build())
                                .build())
                        .semanticId(new DefaultReference.Builder()
                                .key(new DefaultKey.Builder()
                                        .idType(KeyType.IRI)
                                        .type(KeyElements.ASSET)
                                        .value("http://example.org/aasTest1")
                                        .build())
                                .build())
                        .key("TestKey")
                        .value("ValueTest")
                        .build())))
                .globalAssetId(new DefaultReference.Builder()
                        .key(new DefaultKey.Builder()
                                .idType(KeyType.IRI)
                                .type(KeyElements.ASSET)
                                .value("http://example.org/aasTest1")
                                .build())
                        .build())
                .endpoint(DefaultEndpoint.builder()
                        .interfaceInformation("http")
                        .protocolInformation(DefaultProtocolInformation.builder()
                                .endpointAddress("localhost:8080/factory1")
                                .endpointProtocol("http")
                                .build())
                        .build())
                .build();
        List<SubmodelDescriptor> submodels = new ArrayList<>();
        submodels.add(DefaultSubmodelDescriptor.builder()
                .idShort("Submodel1")
                .identification(new DefaultIdentifier.Builder()
                        .idType(IdentifierType.CUSTOM)
                        .identifier("TestSubmodel1")
                        .build())
                .description(new LangString("some submodel", "en-US"))
                .displayName(new LangString("Submodel 1 Name", "en-US"))
                .semanticId(new DefaultReference.Builder()
                        .key(new DefaultKey.Builder()
                                .idType(KeyType.IRI)
                                .type(KeyElements.SUBMODEL)
                                .value("http://example.org/smTest1")
                                .build())
                        .build())
                .administration(new DefaultAdministrativeInformation.Builder()
                        .revision("1")
                        .version("1.1")
                        .build())
                .endpoint(DefaultEndpoint.builder()
                        .interfaceInformation("http")
                        .protocolInformation(DefaultProtocolInformation.builder()
                                .endpointAddress("localhost:8080/factory1/submodel")
                                .endpointProtocol("http")
                                .build())
                        .build())
                .build());
        aas.setSubmodels(submodels);
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
        aas.getSubmodels().get(0).setIdShort("NewSubmodelIdShort");
        repository.update(aasId, aas);
        aas = repository.getAAS(aas.getIdentification().getIdentifier());
        Assert.assertEquals("NewIdShort", aas.getIdShort());
        Assert.assertEquals("NewSubmodelIdShort", aas.getSubmodels().get(0).getIdShort());
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
        repository.addSubmodel(aas.getIdentification().getIdentifier(), submodel);
        compareSubmodel(submodel, repository.getSubmodel(aas.getIdentification().getIdentifier(), submodel.getIdentification().getIdentifier()));
    }


    @Test
    public void listAllSubmodels() throws Exception {
        repository.create(getAASWithSubmodel());
        repository.addSubmodel(getSubmodel());
        List<SubmodelDescriptor> submodels = repository.getSubmodels();
        Assert.assertEquals(2, submodels.size());
    }


    @Test
    public void findStandAloneSubmodelById() throws Exception {
        AssetAdministrationShellDescriptor aas = getAASWithSubmodel();
        SubmodelDescriptor submodel = getSubmodel();
        repository.create(getAASWithSubmodel());
        repository.addSubmodel(submodel);

        SubmodelDescriptor findSubmodel = repository.getSubmodel(aas.getSubmodels().get(0).getIdentification().getIdentifier());
        Assert.assertNotNull(findSubmodel);
        findSubmodel = repository.getSubmodel(submodel.getIdentification().getIdentifier());
        Assert.assertNotNull(findSubmodel);
    }


    @Test
    public void findAASSubmodelById() throws Exception {
        AssetAdministrationShellDescriptor aas = getAASWithSubmodel();
        SubmodelDescriptor submodel = getSubmodel();
        repository.create(getAASWithSubmodel());
        repository.addSubmodel(aas.getIdentification().getIdentifier(), submodel);

        SubmodelDescriptor findSubmodel = repository.getSubmodel(aas.getIdentification().getIdentifier(), submodel.getIdentification().getIdentifier());
        Assert.assertNotNull(findSubmodel);
    }


    @Test
    public void deleteStandAloneSubmodel() throws Exception {
        SubmodelDescriptor submodel = getSubmodel();
        repository.addSubmodel(submodel);
        compareSubmodel(submodel, repository.getSubmodel(submodel.getIdentification().getIdentifier()));

        repository.deleteSubmodel(submodel.getIdentification().getIdentifier());
        Assert.assertThrows(ResourceNotFoundException.class, () -> repository.getSubmodel(submodel.getIdentification().getIdentifier()));
    }


    @Test
    public void deleteAASSubmodel() throws Exception {
        SubmodelDescriptor submodel = getSubmodel();
        AssetAdministrationShellDescriptor aas = getAASWithSubmodel();
        repository.create(aas);
        repository.addSubmodel(aas.getIdentification().getIdentifier(), submodel);
        compareSubmodel(submodel, repository.getSubmodel(aas.getIdentification().getIdentifier(), submodel.getIdentification().getIdentifier()));

        repository.deleteSubmodel(aas.getIdentification().getIdentifier(), submodel.getIdentification().getIdentifier());

        Assert.assertThrows(ResourceNotFoundException.class, () -> repository.getSubmodel(aas.getIdentification().getIdentifier(), submodel.getIdentification().getIdentifier()));
    }


    protected void compareSubmodel(SubmodelDescriptor submodelExpected, SubmodelDescriptor submodelActual) {
        Assert.assertEquals(submodelExpected, submodelActual);
    }
}
