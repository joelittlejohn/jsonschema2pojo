/**
 * Copyright © 2010-2020 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo.rules;

import com.fasterxml.jackson.databind.JsonNode;
import org.jsonschema2pojo.Schema;
import com.sun.codemodel.JType;

import java.util.HashMap;
import java.util.Map;

/**
 * Wraps an existing Rule and performs deduplication of identical schemas to the same output.
 */
class DeduplicateRule<T> implements Rule<T, JType> {

    private final RuleFactory ruleFactory;
    private final Map<String, JType> deduplicateCache;
    private final Rule<T, JType> rule;

    public DeduplicateRule(RuleFactory ruleFactory, Map<Class<?>, Map<String, JType>> dedupeCacheByRule, Rule<T, JType> rule) {
        this.deduplicateCache = dedupeCacheByRule.computeIfAbsent(
                rule.getClass(),
                k -> new HashMap<>());
        this.ruleFactory = ruleFactory;
        this.rule = rule;
    }

    /**
     * Deduplicates based on {@link Schema#calculateHash()} and returns identical {@code R} instances.
     *
     * @param currentSchema
     *            schema to be used for deduplication
     * @return same {@code R} instance if a previous schema with the same hash code was already processed
     */
    @Override
    public JType apply(String nodeName, JsonNode node, JsonNode parent, T generatableType, Schema currentSchema) {
        String hash = currentSchema.calculateHash();
        JType output = deduplicateCache.get(hash);
        if (output == null) {
            ruleFactory.getLogger().trace("Deduplication miss for schema " + nodeName);
            output = rule.apply(nodeName, node, parent, generatableType, currentSchema);
            deduplicateCache.put(hash, output);
        } else {
            ruleFactory.getLogger().debug("Deduplicated schema " + nodeName);
        }
        return output;
    }
}
