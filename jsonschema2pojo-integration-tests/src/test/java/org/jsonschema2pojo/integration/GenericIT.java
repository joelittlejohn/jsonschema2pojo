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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.generateAndCompile;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class GenericIT {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    @SuppressWarnings("unchecked")
    public void canGenerifyTypes() throws Exception {
        ClassLoader resultsClassLoader =
                generateAndCompile("/schema/generic/generic.json", "com.example");

        Class generatedType = resultsClassLoader.loadClass("com.example.Generic");

        // Check that the generated type has the correct type parameters
        assertEquals(3, generatedType.getTypeParameters().length);

        assertEquals("T",
                generatedType.getMethod("getGenericProperty").getGenericReturnType().toString());

        assertEquals("java.util.List<U>",
                generatedType.getMethod("getGenericArray").getGenericReturnType().toString());

        assertEquals("java.util.Map<java.lang.String, V>",
                generatedType.getMethod("getGenericMap").getGenericReturnType().toString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void canGenerifyTypesWithImmutableBuilders() throws Exception {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/generic/generic.json", "com.example",
                config("immutable", true, "generateBuilderClasses", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.Generic");

        // Check that the generated type has the correct type parameters
        assertEquals(3, generatedType.getTypeParameters().length);

        assertEquals("T",
                generatedType.getDeclaredField("genericProperty").getGenericType().toString());

        assertEquals("java.util.List<U>",
                generatedType.getDeclaredField("genericArray").getGenericType().toString());

        assertEquals("java.util.Map<java.lang.String, V>",
                generatedType.getDeclaredField("genericMap").getGenericType().toString());

        assertEquals("com.example.Generic.com.example.Generic$Builder<T, U, V>",
                generatedType.getMethod("newBuilder").getGenericReturnType().toString());

        assertEquals("com.example.Generic.com.example.Generic$Builder<T, U, V>",
                generatedType.getMethod("newBuilder", generatedType)
                        .getGenericReturnType().toString());
    }
}
