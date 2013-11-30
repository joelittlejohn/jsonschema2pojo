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

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

public class AdditionalPropertiesIT {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    @SuppressWarnings("unchecked")
    public void jacksonCanDeserializeOurAdditionalProperties() throws ClassNotFoundException, IOException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/additionalProperties/defaultAdditionalProperties.json", "com.example");

        Class<?> classWithAdditionalProperties = resultsClassLoader.loadClass("com.example.DefaultAdditionalProperties");

        Object deserialized = mapper.readValue("{\"a\":\"1\", \"b\":2}", classWithAdditionalProperties);

        Method getter = classWithAdditionalProperties.getMethod("getAdditionalProperties");

        assertThat(getter.invoke(deserialized), is(notNullValue()));
        assertThat(((Map<String, Object>) getter.invoke(deserialized)).containsKey("a"), is(true));
        assertThat((String) ((Map<String, Object>) getter.invoke(deserialized)).get("a"), is("1"));
        assertThat(((Map<String, Object>) getter.invoke(deserialized)).containsKey("b"), is(true));
        assertThat((Integer) ((Map<String, Object>) getter.invoke(deserialized)).get("b"), is(2));

    }

    @Test
    public void jacksonCanSerializeOurAdditionalProperties() throws ClassNotFoundException, IOException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/additionalProperties/defaultAdditionalProperties.json", "com.example");

        Class<?> classWithAdditionalProperties = resultsClassLoader.loadClass("com.example.DefaultAdditionalProperties");
        String jsonWithAdditionalProperties = "{\"a\":1, \"b\":2};";
        Object instanceWithAdditionalProperties = mapper.readValue(jsonWithAdditionalProperties, classWithAdditionalProperties);

        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(instanceWithAdditionalProperties));
        
        assertThat(jsonNode.path("a").asText(), is("1"));
        assertThat(jsonNode.path("b").asInt(), is(2));
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void additionalPropertiesAreNotDeserializableWhenDisallowed() throws ClassNotFoundException, SecurityException, NoSuchMethodException, IOException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/additionalProperties/noAdditionalProperties.json", "com.example");

        Class<?> classWithNoAdditionalProperties = resultsClassLoader.loadClass("com.example.NoAdditionalProperties");

        mapper.readValue("{\"a\":\"1\", \"b\":2}", classWithNoAdditionalProperties);

    }

    @Test
    public void additionalPropertiesOfStringTypeOnly() throws SecurityException, NoSuchMethodException, ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/additionalProperties/additionalPropertiesString.json", "com.example");

        Class<?> classWithNoAdditionalProperties = resultsClassLoader.loadClass("com.example.AdditionalPropertiesString");
        Method getter = classWithNoAdditionalProperties.getMethod("getAdditionalProperties");

        assertThat(((ParameterizedType) getter.getGenericReturnType()).getActualTypeArguments()[1], is(equalTo((Type) String.class)));

        // setter with these types should exist:
        classWithNoAdditionalProperties.getMethod("setAdditionalProperty", String.class, String.class);

    }

    public void additionalPropertiesOfObjectTypeCreatesNewClassForPropertyValues() throws SecurityException, NoSuchMethodException, ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/additionalProperties/additionalPropertiesObject.json", "com.example");

        Class<?> classWithNoAdditionalProperties = resultsClassLoader.loadClass("com.example.AdditionalPropertiesObject");
        Class<?> propertyValueType = resultsClassLoader.loadClass("com.example.AdditionalPropertiesObjectProperty");

        Method getter = classWithNoAdditionalProperties.getMethod("getAdditionalProperties");

        assertThat(((ParameterizedType) getter.getGenericReturnType()).getActualTypeArguments()[1], is(equalTo((Type) propertyValueType)));

        // setter with these types should exist:
        classWithNoAdditionalProperties.getMethod("setAdditionalProperty", String.class, propertyValueType);

    }

    @Test
    public void additionalPropertiesOfStringArrayTypeOnly() throws SecurityException, NoSuchMethodException, ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/additionalProperties/additionalPropertiesArraysOfStrings.json", "com.example");

        Class<?> classWithNoAdditionalProperties = resultsClassLoader.loadClass("com.example.AdditionalPropertiesArraysOfStrings");
        Method getter = classWithNoAdditionalProperties.getMethod("getAdditionalProperties");

        ParameterizedType listType = (ParameterizedType) ((ParameterizedType) getter.getGenericReturnType()).getActualTypeArguments()[1];
        assertThat(listType.getActualTypeArguments()[0], is(equalTo((Type) String.class)));

        // setter with these types should exist:
        classWithNoAdditionalProperties.getMethod("setAdditionalProperty", String.class, List.class);

    }

    @Test
    public void additionalPropertiesOfBooleanTypeOnly() throws SecurityException, NoSuchMethodException, ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/additionalProperties/additionalPropertiesPrimitiveBoolean.json", "com.example",
                config("usePrimitives", true));

        Class<?> classWithNoAdditionalProperties = resultsClassLoader.loadClass("com.example.AdditionalPropertiesPrimitiveBoolean");
        Method getter = classWithNoAdditionalProperties.getMethod("getAdditionalProperties");

        assertThat(((ParameterizedType) getter.getGenericReturnType()).getActualTypeArguments()[1], is(equalTo((Type) Boolean.class)));

        // setter with these types should exist:
        classWithNoAdditionalProperties.getMethod("setAdditionalProperty", String.class, boolean.class);

    }

}
