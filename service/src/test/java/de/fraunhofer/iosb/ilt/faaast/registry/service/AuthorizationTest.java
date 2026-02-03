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

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.AclFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = App.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthorizationTest {

    private static final String ACL_JSON = """
            {
              "AllAccessPermissionRules": {
                "rules": [{
                  "ACL": {
                    "ATTRIBUTES": [{ "GLOBAL": "ANONYMOUS" }],
                    "RIGHTS":     ["READ"],
                    "ACCESS":     "ALLOW"
                  },
                  "OBJECTS": [{ "ROUTE": "*" }],
                  "FORMULA": { "$boolean": true }
                }]
              }
            }""";

    @Autowired
    private WebApplicationContext context;

    //@Autowired
    //private Filter springSecurityFilterChain;

    private MockMvc mvc;

    //@Rule
    //public TemporaryFolder tmp = new TemporaryFolder();
    @TempDir
    Path tempDir;

    private Path aclDir;
    //private AclFilter filter;

    @BeforeEach
    void setup() throws IOException {
        aclDir = Files.createDirectory(tempDir.resolve("acl"));
        AclFilter filter = new AclFilter(aclDir.toString());
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilter(filter)
                .build();
    }


    @Test
    void testAnonymousAccessDependsOnAclFile() throws IOException, Exception {
        mvc.perform(MockMvcRequestBuilders.get("/shell-descriptors")).andExpect(status().isUnauthorized());

        Path rule = aclDir.resolve("allow.json");
        Path tmpRule = aclDir.resolve("allow.json.tmp");
        Files.writeString(tmpRule, ACL_JSON, StandardCharsets.UTF_8);
        Files.move(tmpRule, rule, StandardCopyOption.ATOMIC_MOVE);

        await().atMost(5, TimeUnit.SECONDS)
                .with()
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    mvc.perform(MockMvcRequestBuilders.get("/shell-descriptors")).andExpect(status().isOk());
                });

        Files.delete(rule);
        await().atMost(5, TimeUnit.SECONDS)
                .with()
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    mvc.perform(MockMvcRequestBuilders.get("/shell-descriptors")).andExpect(status().isUnauthorized());
                });
    }
}
