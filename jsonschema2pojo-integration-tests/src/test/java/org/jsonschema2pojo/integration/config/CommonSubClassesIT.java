/**
 * Copyright © 2025 Matus Faro
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo.integration.config;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CommonSubClassesIT {

    @Rule
    public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void deduplicateSubClasses() throws ClassNotFoundException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/schema/commonSubClasses/", "com.example", config("useDeduplication", true));

        Class generatedTypeA = resultsClassLoader.loadClass("com.example.A");
        Class generatedTypeZ = resultsClassLoader.loadClass("com.example.Z");

        assertSameReturnType(generatedTypeA, generatedTypeZ, "getRefToA");
        assertSameReturnType(generatedTypeA, generatedTypeZ, "getArrayOfRefToZ");
        assertSameReturnType(generatedTypeA, generatedTypeZ, "getObjectOfRefToZ");
        assertSameReturnType(generatedTypeA, generatedTypeZ, "getInlineToA");
        assertSameReturnType(generatedTypeA, generatedTypeZ, "getArrayOfInlineToZ");
        assertSameReturnType(generatedTypeA, generatedTypeZ, "getObjectOfInlineToZ");
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void doNotDeduplicateSubClasses() throws ClassNotFoundException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/schema/commonSubClasses/", "com.example", config("useDeduplication", false));

        Class generatedTypeA = resultsClassLoader.loadClass("com.example.A");
        Class generatedTypeZ = resultsClassLoader.loadClass("com.example.Z");

        assertSameReturnType(generatedTypeA, generatedTypeZ, "getRefToA");
        assertSameReturnType(generatedTypeA, generatedTypeZ, "getArrayOfRefToZ");
        assertDifferentReturnType(generatedTypeA, generatedTypeZ, "getObjectOfRefToZ");
        assertDifferentReturnType(generatedTypeA, generatedTypeZ, "getInlineToA");
        assertDifferentReturnType(generatedTypeA, generatedTypeZ, "getArrayOfInlineToZ");
        assertDifferentReturnType(generatedTypeA, generatedTypeZ, "getObjectOfInlineToZ");
    }

    private void assertSameReturnType(Class<?> generatedTypeA, Class<?> generatedTypeZ, String methodName) throws NoSuchMethodException {
        assertEquals(
                "Method " + methodName + " should have the same return type",
                generatedTypeA.getMethod(methodName).getGenericReturnType().getTypeName(),
                generatedTypeZ.getMethod(methodName).getGenericReturnType().getTypeName());
    }

    private void assertDifferentReturnType(Class<?> generatedTypeA, Class<?> generatedTypeZ, String methodName) throws NoSuchMethodException {
        assertNotEquals(
                "Method " + methodName + " should have the same return type",
                generatedTypeA.getMethod(methodName).getGenericReturnType().getTypeName(),
                generatedTypeZ.getMethod(methodName).getGenericReturnType().getTypeName());
    }
}
