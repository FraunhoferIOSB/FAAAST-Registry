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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Accumulates SQL fragments during query translation.
 */
public class TranslationContext {

    private final Set<String> joins = new LinkedHashSet<>();
    private final Set<String> laterals = new LinkedHashSet<>();
    private final List<Object> parameters = new ArrayList<>();
    private final AtomicInteger matchCounter = new AtomicInteger(0);

    /**
     * Adds a join clause.
     *
     * @param joinClause The desired join clause.
     */
    public void addJoin(String joinClause) {
        joins.add(joinClause);
    }


    /**
     * Adds a lateral clause.
     *
     * @param lateralClause The desired lateral.
     */
    public void addLateral(String lateralClause) {
        laterals.add(lateralClause);
    }


    /**
     * Adds a parameter.
     *
     * @param param The desired parameter.
     */
    public void addParameter(Object param) {
        parameters.add(param);
    }


    public Set<String> getJoins() {
        return joins;
    }


    public Set<String> getLaterals() {
        return laterals;
    }


    public List<Object> getParameters() {
        return parameters;
    }


    /**
     * Gets the next match id.
     * 
     * @return The next match id.
     */
    public int nextMatchId() {
        return matchCounter.incrementAndGet();
    }
}
