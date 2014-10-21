/**
 * Copyright © 2010-2013 Nokia
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

package org.jsonschema2pojo.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.generateAndCompile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ImmutableBuildersIT {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    @SuppressWarnings("unchecked")
    public void canGenerateImmutablePojoWithBuilder() throws Exception {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/immutable/immutable.json", "com.example",
                config("immutable", true, "generateBuilderClasses", true));

        // Check that the generated fields are all final.
        Class generatedType = resultsClassLoader.loadClass("com.example.Immutable");
        assertNotEquals(0, Modifier.FINAL & generatedType.getDeclaredField("foo").getModifiers());
        assertNotEquals(0, Modifier.FINAL & generatedType.getDeclaredField("bar").getModifiers());
        assertNotEquals(0, Modifier.FINAL & generatedType.getDeclaredField("baz").getModifiers());
        assertNotEquals(0, Modifier.FINAL & generatedType.getDeclaredField("qux").getModifiers());

        // Check that the generated type has no setters.
        for (String method : ImmutableList.of("setFoo", "setBar", "setBaz", "setQux", "setAdditionalProperty")) {
            try {
                generatedType.getMethod(method);
                fail("Immutable generated POJO should not have setter " + method);
            } catch (NoSuchMethodException e) { }
        }

        // Build an instance.
        Method newBuilder = generatedType.getMethod("newBuilder");
        Object builder = newBuilder.invoke(generatedType);
        Class builderType = builder.getClass();
        assertTrue(builderType.getMethod("withBar", Boolean.class).invoke(builder, true) == builder);
        builderType.getMethod("withQux", List.class).invoke(builder, Lists.newArrayList(1, 2, 3));
        builderType.getMethod("withAdditionalProperty", String.class, Object.class)
                .invoke(builder, "hello", "world");
        Object instance = builderType.getMethod("build").invoke(builder);

        // Check that any fields with Collections as values are immutable.
        try {
            ((List<Integer>) generatedType.getMethod("getQux").invoke(instance)).add(5);
            fail("It should not be possible to modify a List in an immutable POJO");
        } catch (UnsupportedOperationException e) { }
        try {
            ((Map<String, Object>) generatedType.getMethod("getAdditionalProperties").invoke(instance)).put("a", 0);
            fail("It should not be possible to modify a Map in an immutable POJO");
        } catch (UnsupportedOperationException e) { }

        // Check that the instance has the expected values.
        assertEquals("HELLO WORLD", generatedType.getMethod("getFoo").invoke(instance));
        assertEquals(true, generatedType.getMethod("getBar").invoke(instance));
        assertNull(generatedType.getMethod("getBaz").invoke(instance));
        assertEquals(Lists.newArrayList(1, 2, 3), generatedType.getMethod("getQux").invoke(instance));
        assertEquals(ImmutableMap.of("hello", "world"),
                generatedType.getMethod("getAdditionalProperties").invoke(instance));

        // Check that we can copy construct a builder
        Method newCopyBuilder = generatedType.getMethod("newBuilder", generatedType);
        Object builder2 = newCopyBuilder.invoke(generatedType, instance);
        Object instance2 = builder2.getClass().getMethod("build").invoke(builder2);
        assertEquals("HELLO WORLD", generatedType.getMethod("getFoo").invoke(instance2));
        assertEquals(true, generatedType.getMethod("getBar").invoke(instance2));
        assertNull(generatedType.getMethod("getBaz").invoke(instance2));
        assertEquals(Lists.newArrayList(1, 2, 3), generatedType.getMethod("getQux").invoke(instance2));

        // Check that the instance has the expected values when serialized.
        JsonNode jsonified = mapper.valueToTree(instance);
        assertEquals("HELLO WORLD", jsonified.get("foo").asText());
        assertEquals(true, jsonified.get("bar").asBoolean());
        assertNull(jsonified.get("baz"));
        assertEquals(3, jsonified.get("qux").size());
        assertEquals(1, jsonified.get("qux").get(0).asInt());
        assertEquals(2, jsonified.get("qux").get(1).asInt());
        assertEquals(3, jsonified.get("qux").get(2).asInt());
        assertEquals("world", jsonified.get("hello").asText());

        // Check that deserialization works.
        assertEquals(instance, mapper.readValue(mapper.writeValueAsString(instance), generatedType));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void canGenerateImmutablePojoWithBuilderAndPublicFields() throws Exception {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/immutable/immutable.json", "com.example",
                config("immutable", true, "generateBuilderClasses", true, "usePublicFields", true));

        // Check that the generated fields are all final.
        Class generatedType = resultsClassLoader.loadClass("com.example.Immutable");
        assertNotEquals(0, Modifier.FINAL & generatedType.getField("foo").getModifiers());
        assertNotEquals(0, Modifier.FINAL & generatedType.getField("bar").getModifiers());
        assertNotEquals(0, Modifier.FINAL & generatedType.getField("baz").getModifiers());
        assertNotEquals(0, Modifier.FINAL & generatedType.getField("qux").getModifiers());

        // Check that the generated type has no getters or setters (except for getAdditionalProperties, which Jackson
        // currently requires for serialization.
        for (String method : ImmutableList.of("getFoo", "getBar", "getBaz", "getQux")) {
            try {
                generatedType.getMethod(method);
                fail("POJO generated with usePublicFields should not have getter " + method);
            } catch (NoSuchMethodException e) { }
        }
        for (String method : ImmutableList.of("setFoo", "setBar", "setBaz", "setQux", "setAdditionalProperty")) {
            try {
                generatedType.getMethod(method);
                fail("POJO generated with usePublicFields should not have setter " + method);
            } catch (NoSuchMethodException e) { }
        }

        // Build an instance.
        Method newBuilder = generatedType.getMethod("newBuilder");
        Object builder = newBuilder.invoke(generatedType);
        Class builderType = builder.getClass();
        assertTrue(builderType.getMethod("withBar", Boolean.class).invoke(builder, true) == builder);
        builderType.getMethod("withQux", List.class).invoke(builder, Lists.newArrayList(1, 2, 3));
        builderType.getMethod("withAdditionalProperty", String.class, Object.class)
                .invoke(builder, "hello", "world");
        Object instance = builderType.getMethod("build").invoke(builder);

        // Check that any fields with Collections as values are immutable.
        try {
            ((List<Integer>) generatedType.getField("qux").get(instance)).add(5);
            fail("It should not be possible to modify a List in an immutable POJO");
        } catch (UnsupportedOperationException e) { }
        try {
            ((Map<String, Object>) generatedType.getField("additionalProperties").get(instance)).put("a", 0);
            fail("It should not be possible to modify a Map in an immutable POJO");
        } catch (UnsupportedOperationException e) { }

        // Check that the instance has the expected values.
        assertEquals("HELLO WORLD", generatedType.getField("foo").get(instance));
        assertEquals(true, generatedType.getField("bar").get(instance));
        assertNull(generatedType.getField("baz").get(instance));
        assertEquals(Lists.newArrayList(1, 2, 3), generatedType.getField("qux").get(instance));
        assertEquals(ImmutableMap.of("hello", "world"),
                generatedType.getField("additionalProperties").get(instance));

        // Check that the instance has the expected values when serialized.
        JsonNode jsonified = mapper.valueToTree(instance);
        assertEquals("HELLO WORLD", jsonified.get("foo").asText());
        assertEquals(true, jsonified.get("bar").asBoolean());
        assertNull(jsonified.get("baz"));
        assertEquals(3, jsonified.get("qux").size());
        assertEquals(1, jsonified.get("qux").get(0).asInt());
        assertEquals(2, jsonified.get("qux").get(1).asInt());
        assertEquals(3, jsonified.get("qux").get(2).asInt());
        assertEquals("world", jsonified.get("hello").asText());
        // Check that the generate type has no getters or setters (except for getAdditionalProperties, which Jackson
        // currently requires for serialization.

        // Check that deserialization works.
        Object deserialized = mapper.readValue(mapper.writeValueAsString(instance), generatedType);
        assertEquals(instance, deserialized);
        try {
            ((List<Integer>) generatedType.getField("qux").get(deserialized)).add(5);
            fail("It should not be possible to modify a List in an immutable POJO");
        } catch (UnsupportedOperationException e) { }
    }
}
