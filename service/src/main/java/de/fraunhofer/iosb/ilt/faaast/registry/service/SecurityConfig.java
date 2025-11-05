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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.AclFilter;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;


/**
 * Security configuration.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String JWT_TYP = "typ";
    private static final String JWT_AT_JWT = "at+jwt";
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${service.security.aclFolder}")
    private String aclFolder;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

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

        AclFilter aclFilter = new AclFilter(aclFolder);

        // when security is enabled, CSRF is automatically activated, which means, that all
        // modifying requests (e.g. POST) require a token. We don't want that by default.
        // That's why CSRF is disabled
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterAfter(aclFilter, BasicAuthenticationFilter.class)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults()));
        return http.build();
    }


    /**
     * A JWT Decoder.
     *
     * @return The JWT Decoder.
     */
    @Bean
    public JwtDecoder jwtDecoder() {

        NimbusJwtDecoder jwtDecoderDefault = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);
        JwtDecoder jwtDecoderAt = NimbusJwtDecoder.withIssuerLocation(issuerUri).jwtProcessorCustomizer(customizer -> {
            customizer.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType("at+jwt")));
        }).build();

        return new JwtDecoder() {
            @Override
            public Jwt decode(String token) throws JwtException {
                try {
                    LOGGER.info("token: {}", token);
                    String[] chunks = token.split("\\.");
                    String header = EncodingHelper.base64UrlDecode(chunks[0]);
                    LOGGER.info("Header: {}", header);
                    Map<String, Object> headerMapping = new ObjectMapper().readValue(header, HashMap.class);
                    Jwt jwt;
                    if (headerMapping.containsKey(JWT_TYP)) {
                        String typ = headerMapping.get(JWT_TYP).toString();
                        if (JWT_AT_JWT.equals(typ)) {
                            LOGGER.debug("jwtDecoder: use at+jwt Decoder (Typ: {})", typ);
                            jwt = jwtDecoderAt.decode(token);
                        }
                        else {
                            LOGGER.debug("jwtDecoder: use default Decoder (Typ: {})", typ);
                            jwt = jwtDecoderDefault.decode(token);
                        }
                    }
                    else {
                        LOGGER.debug("jwtDecoder: use default Decoder (unknown Typ");
                        jwt = jwtDecoderDefault.decode(token);
                    }

                    LOGGER.debug("jwt ID: {}", jwt.getId());
                    LOGGER.debug("jwt Subject: {}", jwt.getSubject());
                    LOGGER.debug("jwt token expires: {}", LocalDateTime.ofInstant(jwt.getExpiresAt(), ZoneId.systemDefault()));
                    //for (var c: jwt.getClaims().entrySet()) {
                    //    LOGGER.info("Claim: Key: {}; Value: {}", c.getKey(), c.getValue());
                    //}
                    return jwt;
                }
                catch (JwtException ex) {
                    LOGGER.error("exception: ", ex);
                    throw ex;
                }
                catch (JsonProcessingException ex) {
                    LOGGER.error("exception: ", ex);
                    throw new JwtException(ex.getMessage());
                }
            }
        };
    }
}
