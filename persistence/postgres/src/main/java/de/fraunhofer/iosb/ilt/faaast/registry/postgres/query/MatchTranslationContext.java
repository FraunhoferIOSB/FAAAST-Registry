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
package de.fraunhofer.iosb.ilt.faaast.registry.postgres.query;

/**
 * Context for $match expression translation.
 * Ensures all field identifiers with [] inside a $match reference the SAME array element.
 */
public class MatchTranslationContext {

    private final TranslationContext parent;
    private final String sharedLateralAlias;
    private String lateralFrom;
    private boolean hasLateral = false;

    public MatchTranslationContext(TranslationContext parent, String sharedLateralAlias) {
        this.parent = parent;
        this.sharedLateralAlias = sharedLateralAlias;
    }


    public TranslationContext getParent() {
        return parent;
    }


    public String getSharedLateralAlias() {
        return sharedLateralAlias;
    }


    /**
     * Sets lateral from value.
     *
     * @param from The desired value.
     */
    public void setLateralFrom(String from) {
        this.lateralFrom = from;
        this.hasLateral = true;
    }


    public String getLateralFrom() {
        return lateralFrom;
    }


    /**
     * Get a value indicating whether a lateral is available.
     *
     * @return True if it has a lateral, false otherwise.
     */
    public boolean hasLateral() {
        return hasLateral;
    }
}
