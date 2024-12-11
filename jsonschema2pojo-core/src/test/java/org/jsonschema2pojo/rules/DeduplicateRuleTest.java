/**
 * Copyright © 2024 Nokia
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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.RuleLogger;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.util.NameHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

public class DeduplicateRuleTest {

    private Map<String, UUID> dedupeCache;
    private Rule<Object, UUID> dedupeRule;

    @Before
    public void setupRule() {
        dedupeCache = new HashMap<>();
        DummyRule dummyRule = new DummyRule();
        dedupeRule = new DeduplicateRule<>(ImmutableMap.of(dummyRule.getClass(), dedupeCache), dummyRule);
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

        UUID uuid1 = dedupeRule.apply(null, null, null, null, schema1);
        UUID uuid2 = dedupeRule.apply(null, null, null, null, schema1);
        UUID uuid3 = dedupeRule.apply(null, null, null, null, schema2);
        UUID uuid4 = dedupeRule.apply(null, null, null, null, schema3);

        assertEquals(uuid1, uuid2);
        assertEquals(uuid1, uuid3);
        assertNotEquals(uuid1, uuid4);
    }

    public static class DummyRule implements Rule<Object, UUID> {
        @Override
        public UUID apply(String nodeName, JsonNode node, JsonNode parent, Object generatableType, Schema currentSchema) {
            return UUID.randomUUID();
        }
    }
}
