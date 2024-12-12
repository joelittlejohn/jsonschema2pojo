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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableMap;
import com.sun.codemodel.JType;
import org.jsonschema2pojo.Schema;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DeduplicateRuleTest {

    private Map<String, JType> dedupeCache;
    private Rule<Object, JType> dedupeRule;

    @Before
    public void setupRule() {
        dedupeCache = new HashMap<>();
        DummyRule dummyRule = new DummyRule();
        dedupeRule = new DeduplicateRule<>(new RuleFactory(), ImmutableMap.of(dummyRule.getClass(), dedupeCache), dummyRule);
    }

    @Test
    public void test() {

        ObjectMapper objectMapper = new ObjectMapper();

        ArrayNode arrayNode1 = objectMapper.createArrayNode().add(7).add(8);
        ArrayNode arrayNode2 = objectMapper.createArrayNode().add(7).add(8);
        ArrayNode arrayNode3 = objectMapper.createArrayNode().add(7).add(9);
        assertEquals(arrayNode1.toString(), arrayNode2.toString());
        assertNotEquals(arrayNode1.toString(), arrayNode3.toString());

        Schema schema1 = new Schema(null, arrayNode1, null);
        Schema schema2 = new Schema(null, arrayNode2, null);
        Schema schema3 = new Schema(null, arrayNode3, null);
        assertEquals(schema1.calculateHash(), schema1.calculateHash());
        assertEquals(schema1.calculateHash(), schema2.calculateHash());
        assertNotEquals(schema1.calculateHash(), schema3.calculateHash());

        JType type1 = dedupeRule.apply(null, null, null, null, schema1);
        JType type2 = dedupeRule.apply(null, null, null, null, schema1);
        JType type3 = dedupeRule.apply(null, null, null, null, schema2);
        JType type4 = dedupeRule.apply(null, null, null, null, schema3);

        // Comparing the mock object references from DummyRule
        assertSame(type1, type2);
        assertSame(type1, type3);
        assertNotSame(type1, type4);
    }

    public static class DummyRule implements Rule<Object, JType> {
        @Override
        public JType apply(String nodeName, JsonNode node, JsonNode parent, Object generatableType, Schema currentSchema) {
            // All we need here is a unique object to test deduplication
            // If deduplicated, object reference of this mock will be the same
            return Mockito.mock(JType.class);
        }
    }
}
