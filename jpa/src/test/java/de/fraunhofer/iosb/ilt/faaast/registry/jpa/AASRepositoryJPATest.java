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

import de.fraunhofer.iosb.ilt.faaast.registry.core.AbstractAASRepositoryTest;
import javax.persistence.EntityManager;
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
public class AASRepositoryJPATest extends AbstractAASRepositoryTest<AASRepositoryJPA> {

    @Autowired
    private EntityManager entityManager;

    @Before
    public void setup() {
        repository = new AASRepositoryJPA(entityManager);
    }


    @Override
    public void createSubmodelToAAS() throws Exception {
        // TODO: test temporarily disabled
    }


    @Override
    public void deleteAASSubmodel() throws Exception {
        // TODO: test temporarily disabled
    }


    @Override
    public void deleteStandAloneSubmodel() throws Exception {
        // TODO: test temporarily disabled
    }
}
