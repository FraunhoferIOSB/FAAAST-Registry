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

import helper.CertificateHelper;
import java.security.KeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ssl.DefaultSslBundleRegistry;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslStoreBundle;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;


/**
 * SSL configuration with dynmic keystore.
 */
@Component
public class SslConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SslConfig.class);

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        // if no SSL Bundle is provided, we generate a self-signed certificate
        if (factory.getSsl().getBundle() == null) {
            LOGGER.info("Generating self-signed certificate for HTTPS (reason: no SSL-Bundle provided)");
            KeyStore keyStore = CertificateHelper.generateSelfSignedCertificate();
            SslStoreBundle storeBundle = SslStoreBundle.of(keyStore, null, null);
            SslBundle bundle = SslBundle.of(storeBundle);

            DefaultSslBundleRegistry defaultSslBundleRegistry = new DefaultSslBundleRegistry();
            String bundleName = "default-bundle";
            defaultSslBundleRegistry.registerBundle(bundleName, bundle);
            factory.setSslBundles(defaultSslBundleRegistry);
            factory.getSsl().setBundle(bundleName);
        }
    }

}
