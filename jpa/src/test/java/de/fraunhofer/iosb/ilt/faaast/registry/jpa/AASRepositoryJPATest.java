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
package de.fraunhofer.iosb.ilt.faaast.registry.jpa;

import de.fraunhofer.iosb.ilt.faaast.registry.core.AbstractAasRepositoryTest;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import javax.persistence.EntityManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@ContextConfiguration(classes = AASRepositoryJPATest.class)
@DataJpaTest
@EntityScan(basePackages = {
        "de.fraunhofer.iosb.ilt.faaast.registry.jpa.model"
})
public class AASRepositoryJPATest extends AbstractAasRepositoryTest<AASRepositoryJPA> {

    @Autowired
    private EntityManager entityManager;

    @Before
    public void setup() {
        repository = new AASRepositoryJPA(entityManager);
    }


    @Override
    protected void compareSubmodel(SubmodelDescriptor submodelExpected, SubmodelDescriptor submodelActual) {
        // we can't compare the whole objects here, as they are not identical because of the additional database id.
        Assert.assertEquals(submodelExpected.getIdShort(), submodelActual.getIdShort());
        Assert.assertEquals(submodelExpected.getAdministration().getVersion(), submodelActual.getAdministration().getVersion());
        Assert.assertEquals(submodelExpected.getAdministration().getRevision(), submodelActual.getAdministration().getRevision());
        Assert.assertEquals(submodelExpected.getDescriptions().size(), submodelActual.getDescriptions().size());
        for (int i = 0; i < submodelExpected.getDescriptions().size(); i++) {
            Assert.assertEquals(submodelExpected.getDescriptions().get(i).getLanguage(), submodelActual.getDescriptions().get(i).getLanguage());
            Assert.assertEquals(submodelExpected.getDescriptions().get(i).getValue(), submodelActual.getDescriptions().get(i).getValue());
        }
        Assert.assertEquals(submodelExpected.getDisplayNames().size(), submodelActual.getDisplayNames().size());
        for (int i = 0; i < submodelExpected.getDisplayNames().size(); i++) {
            Assert.assertEquals(submodelExpected.getDisplayNames().get(i).getLanguage(), submodelActual.getDisplayNames().get(i).getLanguage());
            Assert.assertEquals(submodelExpected.getDisplayNames().get(i).getValue(), submodelActual.getDisplayNames().get(i).getValue());
        }
        Assert.assertEquals(submodelExpected.getEndpoints().size(), submodelActual.getEndpoints().size());
        for (int i = 0; i < submodelExpected.getEndpoints().size(); i++) {
            Assert.assertEquals(submodelExpected.getEndpoints().get(i).getInterfaceInformation(), submodelActual.getEndpoints().get(i).getInterfaceInformation());
            Assert.assertEquals(submodelExpected.getEndpoints().get(i).getProtocolInformation().getEndpointAddress(),
                    submodelActual.getEndpoints().get(i).getProtocolInformation().getEndpointAddress());
            Assert.assertEquals(submodelExpected.getEndpoints().get(i).getProtocolInformation().getEndpointProtocol(),
                    submodelActual.getEndpoints().get(i).getProtocolInformation().getEndpointProtocol());
            Assert.assertEquals(submodelExpected.getEndpoints().get(i).getProtocolInformation().getEndpointProtocolVersion(),
                    submodelActual.getEndpoints().get(i).getProtocolInformation().getEndpointProtocolVersion());
            Assert.assertEquals(submodelExpected.getEndpoints().get(i).getProtocolInformation().getSubprotocol(),
                    submodelActual.getEndpoints().get(i).getProtocolInformation().getSubprotocol());
            Assert.assertEquals(submodelExpected.getEndpoints().get(i).getProtocolInformation().getSubprotocolBody(),
                    submodelActual.getEndpoints().get(i).getProtocolInformation().getSubprotocolBody());
            Assert.assertEquals(submodelExpected.getEndpoints().get(i).getProtocolInformation().getSubprotocolBodyEncoding(),
                    submodelActual.getEndpoints().get(i).getProtocolInformation().getSubprotocolBodyEncoding());
        }
        Assert.assertEquals(submodelExpected.getIdentification().getIdType(), submodelActual.getIdentification().getIdType());
        Assert.assertEquals(submodelExpected.getIdentification().getIdentifier(), submodelActual.getIdentification().getIdentifier());
        Assert.assertEquals(submodelExpected.getSemanticId().getKeys().size(), submodelActual.getSemanticId().getKeys().size());
        for (int i = 0; i < submodelExpected.getSemanticId().getKeys().size(); i++) {
            Assert.assertEquals(submodelExpected.getSemanticId().getKeys().get(i).getIdType(), submodelActual.getSemanticId().getKeys().get(i).getIdType());
            Assert.assertEquals(submodelExpected.getSemanticId().getKeys().get(i).getType(), submodelActual.getSemanticId().getKeys().get(i).getType());
            Assert.assertEquals(submodelExpected.getSemanticId().getKeys().get(i).getValue(), submodelActual.getSemanticId().getKeys().get(i).getValue());
        }
    }

}
