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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.AllAccessPermissionRulesRoot;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.Attribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.Rule;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.GenericFilterBean;


/**
 * Security configuration.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String apiPrefix = "/api/v3.0/";
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    private final String abortMessage = "Invalid ACL folder path, AAS Security will not enforce rules.)";

    private Map<Path, AllAccessPermissionRulesRoot> aclList;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${service.security.aclFolder}")
    private String aclFolder;

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
        readAccessRules();

        // currently only test
        // when security is enabled, CSRF is automatically activated, which means, that all
        // modifying requests (e.g. POST) require a token. We don't want that by default.
        // That's why CSRF is disabled
        http
                .csrf(AbstractHttpConfigurer::disable)
                //.authorizeHttpRequests((authorize) -> authorize
                //        .requestMatchers("/**").permitAll())
                //        .anyRequest().permitAll())
                //        .requestMatchers(HttpMethod.GET).permitAll()
                //.anyRequest().authenticated())
                .addFilterAfter(new AclFilter(), BasicAuthenticationFilter.class)
                .oauth2ResourceServer((oauth2) -> oauth2
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

        JwtDecoder jwtDecoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();

        return new JwtDecoder() {
            @Override
            public Jwt decode(String token) throws JwtException {
                try {
                    //LOGGER.info("token: {}", token);
                    Jwt jwt = jwtDecoder.decode(token);
                    //LOGGER.info("jwt: {}", jwt);
                    //LOGGER.info("jwt ID: {}", jwt.getId());
                    //LOGGER.info("jwt Subject: {}", jwt.getSubject());
                    //for (var c: jwt.getClaims().entrySet()) {
                    //    LOGGER.info("Claim: Key: {}; Value: {}", c.getKey(), c.getValue());
                    //}
                    return jwt;
                }
                catch (JwtException ex) {
                    LOGGER.error("exception: ", ex);
                    throw ex;
                }
            }
        };
    }


    private void readAccessRules() {
        aclList = new HashMap<>();

        if (aclFolder == null
                || aclFolder.trim().isEmpty()
                || !new File(aclFolder.trim()).isDirectory()) {
            LOGGER.error(abortMessage);
            return;
        }

        File folder = new File(aclFolder.trim());
        File[] jsonFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        ObjectMapper mapper = new ObjectMapper();
        if (jsonFiles != null) {
            for (File file: jsonFiles) {
                Path filePath = file.toPath();
                String content = null;
                try {
                    content = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
                    aclList.put(filePath, mapper.readValue(
                            content, AllAccessPermissionRulesRoot.class));
                }
                catch (IOException e) {
                    LOGGER.error(abortMessage, e);
                }
            }
        }
    }

    /**
     * Custom filter for applying our access rules.
     */
    public class AclFilter extends GenericFilterBean {

        @Override
        public void doFilter(
                             ServletRequest servletRequest,
                             ServletResponse servletResponse,
                             FilterChain chain)
                throws IOException, ServletException {
            LOGGER.info("doFilter called: Request: {}", servletRequest);
            if (servletRequest instanceof HttpServletRequest request) {
                HttpServletResponse response = (HttpServletResponse)servletResponse;
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                Map<String, Object> claims = new HashMap<>();
                if ((authentication != null) && (authentication.getCredentials() instanceof Jwt jwt)) {
                    claims = jwt.getClaims();
                }
                boolean allowed = filterRules(aclList, claims, request);
                LOGGER.info("doFilter called: Request: {}; authentication: {}; allowed: {}", request, authentication, allowed);
                if (!allowed) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                }
                else {
                    chain.doFilter(servletRequest, servletResponse);
                }
            }
            else {
                chain.doFilter(servletRequest, servletResponse);
            }
        }


        /**
         * Check all rules that explicitly allows the request.
         * If a rule exists after all filters, true is returned
         *
         * @param claims
         * @param request
         * @return
         */
        private static boolean filterRules(Map<Path, AllAccessPermissionRulesRoot> aclList, Map<String, Object> claims, HttpServletRequest request) {
            String requestPath = request.getRequestURI();
            String path = requestPath.startsWith(apiPrefix) ? requestPath.substring(9) : requestPath;
            String method = request.getMethod();
            List<AllAccessPermissionRulesRoot> relevantRules = aclList.values().stream()
                    .filter(a -> a.getAllAccessPermissionRules()
                            .getRules().stream()
                            .anyMatch(r -> r.getACL() != null
                                    && r.getACL().getATTRIBUTES() != null
                                    && r.getACL().getRIGHTS() != null
                                    && r.getOBJECTS() != null
                                    && r.getOBJECTS().stream().anyMatch(attr -> {
                                        if (attr.getROUTE() != null) {
                                            return "*".equals(attr.getROUTE()) || attr.getROUTE().contains(path);
                                        }
                                        else if (attr.getDESCRIPTOR() != null) {
                                            return checkDescriptor(path, attr.getDESCRIPTOR());
                                        }
                                        else {
                                            return false;
                                        }
                                    })
                                    && "ALLOW".equals(r.getACL().getACCESS())
                                    && r.getACL().getRIGHTS().contains(getRequiredRight(method))
                                    && verifyAllClaims(claims, r)))
                    .collect(Collectors.toList());
            return !relevantRules.isEmpty();
        }


        private static boolean verifyAllClaims(Map<String, Object> claims, Rule rule) {
            if (rule.getACL().getATTRIBUTES().stream()
                    .anyMatch(attr -> "ANONYMOUS".equals(attr.getGLOBAL())
                            && Boolean.TRUE.equals(rule.getFORMULA().get("$boolean")))) {
                return true;
            }
            if (claims == null) {
                return false;
            }
            List<String> claimValues = rule.getACL().getATTRIBUTES().stream()
                    .filter(attr -> attr.getGLOBAL() == null)
                    .map(Attribute::getCLAIM)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return !claimValues.isEmpty()
                    && claimValues.stream()
                            .allMatch(value -> {
                                Object claim = claims.get(value);
                                return claim != null
                                        && evaluateSimpleEQFormula(rule.getFORMULA(), value, claim.toString());
                            });
        }


        private static boolean evaluateSimpleEQFormula(Map<String, Object> formula, String value, String claimValue) {
            if (formula.size() != 1 || !formula.containsKey("$eq")) {
                LOGGER.error("Unsupported ACL formula.");
                return false;
            }
            List<LinkedHashMap> eqList = (List<LinkedHashMap>) formula.get("$eq");
            LinkedHashMap attribute = (LinkedHashMap) eqList.get(0).get("$attribute");
            String strVal = (String) eqList.get(1).get("$strVal");
            if (attribute.get("CLAIM").equals(value) && strVal.equals(claimValue)) {
                return true;
            }
            return false;
        }


        private static String getRequiredRight(String method) {
            switch (method) {
                case "GET":
                    return "READ";
                case "POST":
                    return "WRITE";
                case "PUT":
                    return "UPDATE";
                case "DELETE":
                    return "DELETE";
                default:
                    throw new IllegalArgumentException("Unsupported method: " + method);
            }
        }


        private static boolean checkDescriptor(String path, String descriptor) {
            if (descriptor.startsWith("(aasDesc)")) {
                if (!path.startsWith("/shell-descriptors")) {
                    return false;
                }
                if (descriptor.equals("(aasDesc)*")) {
                    return true;
                }
                else if (descriptor.startsWith("(aasDesc)")) {
                    String id = descriptor.substring(9);
                    return path.contains(EncodingHelper.base64UrlEncode(id));
                }
            }
            else if (descriptor.startsWith("(smDesc)")) {
                if (!path.startsWith("/submodel-descriptors")) {
                    return false;
                }
                if (descriptor.equals("(smDesc)*")) {
                    return true;
                }
                else if (descriptor.startsWith("(smDesc)")) {
                    String id = descriptor.substring(8);
                    return path.contains(EncodingHelper.base64UrlEncode(id));
                }
            }
            return false;
        }
    }
}
