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

import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.Base64;


/**
 * Class with helper methods for the Registry.
 */
public class RegistryHelper {

    private RegistryHelper() {}


    /**
     * Decodes the given Base64 URL encoded value.
     *
     * @param encoded The Base64 URL encoded value.
     * @return The decoded value.
     */
    public static String decode(String encoded) {
        if (encoded == null) {
            return null;
        }
        return new String(Base64.getUrlDecoder().decode(encoded));
    }


    /**
     * Encodes the given value with a Base64 URL encoding.
     *
     * @param value The desired value.
     * @return The Base64URL encoded value.
     */
    public static String encode(String value) {
        Ensure.requireNonNull(value);
        return new String(Base64.getUrlEncoder().encode(value.getBytes()));
    }

}
