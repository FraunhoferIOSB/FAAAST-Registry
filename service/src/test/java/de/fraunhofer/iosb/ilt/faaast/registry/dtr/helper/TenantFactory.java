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
package de.fraunhofer.iosb.ilt.faaast.registry.dtr.helper;

public class TenantFactory {

    private static final String TENANT_ONE = "TENANT_ONE";
    private static final String TENANT_TWO = "TENANT_TWO";
    private static final String TENANT_THREE = "TENANT_THREE";

    private TenantFactory() {

    }


    public static Tenant tenantOne() {
        return new Tenant("publicClientId", TENANT_ONE);
    }


    public static Tenant tenantTwo() {
        return new Tenant("publicClientId", TENANT_TWO);
    }


    public static Tenant tenantThree() {
        return new Tenant("publicClientId", TENANT_THREE);
    }


    public static Tenant tenantOne(String publicClientId) {
        return new Tenant(publicClientId, TENANT_ONE);
    }


    public static Tenant tenantTwo(String publicClientId) {
        return new Tenant(publicClientId, TENANT_TWO);
    }


    public static Tenant tenantThree(String publicClientId) {
        return new Tenant(publicClientId, TENANT_THREE);
    }

}
