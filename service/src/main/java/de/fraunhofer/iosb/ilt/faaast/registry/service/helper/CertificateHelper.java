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
package de.fraunhofer.iosb.ilt.faaast.registry.service.helper;

import de.fraunhofer.iosb.ilt.faaast.service.certificate.CertificateData;
import de.fraunhofer.iosb.ilt.faaast.service.certificate.CertificateInformation;
import de.fraunhofer.iosb.ilt.faaast.service.certificate.util.KeyStoreHelper;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


/**
 * Helper class for certificate handling.
 */
public class CertificateHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateHelper.class);

    private static final CertificateInformation SELFSIGNED_CERTIFICATE_INFORMATION = CertificateInformation.builder()
            .applicationUri("urn:de:fraunhofer:iosb:ilt:faaast:registry:service")
            .commonName("FA³ST Registry Service")
            .countryCode("DE")
            .localityName("Karlsruhe")
            .organization("Fraunhofer IOSB")
            .organizationUnit("ILT")
            .build();

    private CertificateHelper() {}


    /**
     * Create a KeyStore with a self-signed certificate.
     *
     * @return The created KeyStore.
     */
    public static KeyStore generateSelfSignedCertificate() {
        try {
            LOGGER.debug("Generating self-signed certificate for HTTP endpoint...");
            CertificateData certificateData = KeyStoreHelper.generateSelfSigned(SELFSIGNED_CERTIFICATE_INFORMATION);
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry(KeyStoreHelper.DEFAULT_ALIAS, certificateData.getCertificate());
            keyStore.setKeyEntry(KeyStoreHelper.DEFAULT_ALIAS, certificateData.getKeyPair().getPrivate(), null, certificateData.getCertificateChain());
            LOGGER.debug("Self-signed certificate for Registry service successfully generated");
            return keyStore;
        }
        catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error generating self-signed certificate for service", e);
        }
    }
}
