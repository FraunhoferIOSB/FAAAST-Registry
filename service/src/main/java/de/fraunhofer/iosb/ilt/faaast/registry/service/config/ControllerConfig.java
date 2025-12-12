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

import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.AssetKindConverter;
import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.Constants;
import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.SpecificAssetIdListConverter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.filter.UrlHandlerFilter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * Class with configuration for enum converters. They are necessary for the request parameter.
 */
@Configuration
@EnableTransactionManagement
public class ControllerConfig implements WebMvcConfigurer {

    private static final String URL_PATH1 = "/**";
    private static final String URL_PATH2 = "%s%s/**";

    @Value("${cors.enabled:false}")
    private boolean corsEnabled;

    @Value("${cors.allowCredentials:false}")
    private boolean corsAllowCredentials;

    @Value("${cors.allowedOrigins:}")
    private List<String> corsAllowedOrigins;

    @Value("${cors.allowedMethods:}")
    private List<String> corsAllowedMethods;

    @Value("${cors.allowedHeaders:}")
    private List<String> corsAllowedHeaders;

    @Value("${cors.exposedHeaders:}")
    private List<String> corsExposedHeaders;

    @Value("${cors.maxAge:1800}")
    private long corsMaxAge;

    @Value("${server.servlet.context-path}")
    private String apiPrefix;

    /**
     * The conversion service.
     *
     * @return the conversion service
     */
    @Bean
    protected ConversionService conversionService() {
        return new DefaultConversionService();
    }


    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .ignoreAcceptHeader(true)
                .defaultContentType(MediaType.APPLICATION_JSON);
    }


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (!corsEnabled) {
            return;
        }
        CorsRegistration registration = registry.addMapping(URL_PATH1);
        String[] origins = (corsAllowedOrigins == null)
                ? new String[0]
                : corsAllowedOrigins.stream().map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);

        boolean containsWildcard = java.util.Arrays.stream(origins).anyMatch("*"::equals);
        if (corsAllowCredentials) {
            // With credentials, never use allowedOrigins("*"). Use patterns or explicit origins.
            if (containsWildcard) {
                registration.allowedOriginPatterns("*");
            }
            else if (origins.length > 0) {
                registration.allowedOriginPatterns(origins);
            }
            // else: no origin configured -> none allowed (safe default)
        }
        else {
            // No credentials: "*" is allowed with allowedOrigins
            if (origins.length == 0 || containsWildcard) {
                registration.allowedOrigins("*");
            }
            else {
                registration.allowedOrigins(origins);
            }
        }
        registration.allowedMethods(corsAllowedMethods.toArray(String[]::new));
        registration.allowedHeaders(corsAllowedHeaders.toArray(String[]::new));
        registration.exposedHeaders(corsExposedHeaders.toArray(String[]::new));
        registration.allowCredentials(corsAllowCredentials);
        registration.maxAge(corsMaxAge);
    }


    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new AssetKindConverter());
        registry.addConverter(new SpecificAssetIdListConverter());
    }


    /**
     * Use Filter to enable trailing slashes in requests.
     *
     * @return The extended FilterRegistrationBean.
     */
    @Bean
    public FilterRegistrationBean urlHandlerFilterRegistrationBean() {
        FilterRegistrationBean<OncePerRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(UrlHandlerFilter
                .trailingSlashHandler(String.format(URL_PATH2, apiPrefix, Constants.SHELL_REQUEST_PATH)).wrapRequest()
                .trailingSlashHandler(String.format(URL_PATH2, apiPrefix, Constants.SUBMODEL_REQUEST_PATH)).wrapRequest()
                .trailingSlashHandler(String.format(URL_PATH2, apiPrefix, Constants.DESCRIPTION_REQUEST_PATH)).wrapRequest()
                .trailingSlashHandler(String.format(URL_PATH2, apiPrefix, Constants.BULK_REQUEST_PATH)).wrapRequest()
                .build());
        return registrationBean;
    }
}
