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

import static org.assertj.core.api.Assertions.assertThat;

import de.fraunhofer.iosb.ilt.faaast.registry.core.AbstractAasRepositoryTest;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@ContextConfiguration(classes = AasRepositoryJpaTest.class)
@DataJpaTest
@EntityScan(basePackages = {
        "de.fraunhofer.iosb.ilt.faaast.registry.jpa.model"
})
class AasRepositoryJpaTest extends AbstractAasRepositoryTest<AasRepositoryJpa> {

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        repository = new AasRepositoryJpa(entityManager);
    }


    @Override
    protected void compareSubmodel(SubmodelDescriptor expected, SubmodelDescriptor actual) {
        RecursiveComparisonConfiguration ignoreIdConfig = new RecursiveComparisonConfiguration();
        ignoreIdConfig.ignoreFields("id");
        assertThat(expected)
                .usingRecursiveComparison(ignoreIdConfig)
                .isEqualTo(actual);
    }

}
