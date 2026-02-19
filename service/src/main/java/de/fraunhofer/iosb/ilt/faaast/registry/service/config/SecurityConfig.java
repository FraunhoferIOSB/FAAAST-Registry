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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.AclFilter;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.SslHelper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTypeValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;


/**
 * Security configuration.
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "service.security.enabled", havingValue = "true")
public class SecurityConfig {

    private static final String JWT_TYP = "typ";
    //private static final String JWT_AT_JWT = "at+jwt";
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${service.security.aclFolder}")
    private String aclFolder;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${service.security.tokenExchangeUrl}")
    private String tokenExchangeUrl;

    /**
     * Configure access.
     *
     * @param http The security configuration.
     * @return The security filter chain.
     * @throws Exception When an error occurs.
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
        jwtDecoderDefault.setJwtValidator(JwtValidators.createDefaultWithValidators(
                new JwtIssuerValidator(issuerUri), new JwtTypeValidator("JWT", "at+jwt")));
        //NimbusJwtDecoder jwtDecoderAt = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);
        //jwtDecoderAt.setJwtValidator(JwtValidators.createDefaultWithValidators(
        //        new JwtIssuerValidator(issuerUri), new JwtTypeValidator("JWT", "at+jwt")));

        return new JwtDecoder() {
            @Override
            public Jwt decode(String token) throws JwtException {
                try {
                    Jwt jwt;

                    // handle Token Exxchange if configured
                    if ((tokenExchangeUrl != null) && (!tokenExchangeUrl.isEmpty())) {
                        try {
                            HttpClient client = SslHelper.newClientAcceptingAllCertificates();

                            String form = "grant_type=" + URLEncoder.encode("urn:ietf:params:oauth:grant-type:token-exchange", StandardCharsets.UTF_8) +
                                    "&subject_token_type=" + URLEncoder.encode("urn:ietf:params:oauth:token-type:jwt", StandardCharsets.UTF_8) +
                                    "&requested_token_type=" + URLEncoder.encode("urn:ietf:params:oauth:token-type:jwt", StandardCharsets.UTF_8) +
                            //        "&requested_token_type=" + URLEncoder.encode("urn:ietf:params:oauth:token-type:access_token", StandardCharsets.UTF_8) +
                                    "&subject_token=" + token;
                            //"&audience=" + URLEncoder.encode("fa3st", StandardCharsets.UTF_8);

                            HttpRequest request = HttpRequest.newBuilder()
                                    .header("Content-Type", "application/x-www-form-urlencoded")
                                    .POST(HttpRequest.BodyPublishers.ofString(form))
                                    .uri(URI.create(tokenExchangeUrl))
                                    //.uri(URI.create(tokenExchangeUrl + "/token"))
                                    .build();

                            // Send request and get response body as String
                            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                            if ((response.statusCode() >= 200) && (response.statusCode() < 300)) {
                                String exchangedToken = extractToken(response);
                                jwt = decodeToken(exchangedToken);
                                LOGGER.debug("exchanged jwt ID: {}", jwt.getId());
                                LOGGER.debug("exchanged jwt Subject: {}", jwt.getSubject());
                                LOGGER.debug("exchanged jwt token expires: {}", LocalDateTime.ofInstant(jwt.getExpiresAt(), ZoneId.systemDefault()));
                            }
                            else {
                                LOGGER.error("Token exchange failed, try previous token.");
                                jwt = decodeToken(token);
                            }
                        }
                        catch (InterruptedException | KeyManagementException | NoSuchAlgorithmException | IOException e) {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException("Could not exchange token with provider sts.", e);
                        }
                    }
                    else {
                        jwt = decodeToken(token);
                        LOGGER.debug("jwt ID: {}", jwt.getId());
                        LOGGER.debug("jwt Subject: {}", jwt.getSubject());
                        LOGGER.debug("jwt token expires: {}", LocalDateTime.ofInstant(jwt.getExpiresAt(), ZoneId.systemDefault()));
                    }

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


            private String extractToken(HttpResponse<String> response) {
                String json = response.body();

                if (json == null) {
                    return null;
                }
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                String retval = obj.get("access_token").getAsString();
                return retval;
            }


            private Jwt decodeToken(String token) throws JwtException, JsonProcessingException {
                LOGGER.info("token: {}", token);
                String[] chunks = token.split("\\.");
                String header = EncodingHelper.base64UrlDecode(chunks[0]);
                LOGGER.info("Header: {}", header);
                Map<String, Object> headerMapping = new ObjectMapper().readValue(header, HashMap.class);
                Jwt jwt;
                if (headerMapping.containsKey(JWT_TYP)) {
                    String typ = headerMapping.get(JWT_TYP).toString();
                    //if (JWT_AT_JWT.equals(typ)) {
                    //    LOGGER.debug("jwtDecoder: use at+jwt Decoder (Typ: {})", typ);
                    //    jwt = jwtDecoderAt.decode(token);
                    //}
                    //else {
                    LOGGER.debug("jwtDecoder: use default Decoder (Typ: {})", typ);
                    jwt = jwtDecoderDefault.decode(token);
                    //}
                }
                else {
                    LOGGER.debug("jwtDecoder: use default Decoder (unknown Typ");
                    jwt = jwtDecoderDefault.decode(token);
                }
                return jwt;
            }
        };
    }

    /**
     * Class required for token exchange.
     *
     * @return The token exchange provider.
     */
    //@Bean
    //@Profile("!test")
    //public OAuth2AuthorizedClientProvider tokenExchange() {
    //    return new TokenExchangeOAuth2AuthorizedClientProvider();
    //}
}
