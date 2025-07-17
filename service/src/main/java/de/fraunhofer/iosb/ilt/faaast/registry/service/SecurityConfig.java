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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;


/**
 * Security configuration.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    //@Bean
    //SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    //    http
    //            .authorizeExchange(exchanges -> exchanges
    //                    .anyExchange().authenticated())
    //            .oauth2ResourceServer(oauth2 -> oauth2
    //                    .opaqueToken(Customizer.withDefaults()));
    //    return http.build();
    //}

    /**
     * Configure access.
     *
     * @param http The security configuration.
     * @return The security filter chain.
     * @throws Exception When an erro occurs.
     */
    @Bean
    @Profile("!test")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // currently only test
        // when security is enabled, CSRF is automatically activated, which means, that all
        // modifying requests (e.g. POST) require a token. We don't want that by default.
        // That's why CSRF is disabled
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) -> authorize
                        //        .requestMatchers("/**").permitAll())
                        .anyRequest().permitAll())
                //        .requestMatchers(HttpMethod.GET).permitAll()
                //.anyRequest().authenticated())
                .oauth2ResourceServer((oauth2) -> oauth2
                        .opaqueToken(Customizer.withDefaults()));
        return http.build();
    }
}
