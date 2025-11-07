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
package de.fraunhofer.iosb.ilt.faaast.registry.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;


/**
 * Configuration for disabled Security.
 */
@Configuration
@ConditionalOnProperty(name = "security.enabled", havingValue = "false", matchIfMissing = true)
public class DisableSecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DisableSecurityConfig.class);

    /**
     * Configure general access.
     *
     * @param http The security configuration.
     * @return The security filter chain.
     * @throws Exception When an error occurs.
     */
    @Bean
    @Profile("!test")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        LOGGER.info("Registry is running without Security!");
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request.anyRequest()
                        .permitAll());
        return http.build();
    }
}
