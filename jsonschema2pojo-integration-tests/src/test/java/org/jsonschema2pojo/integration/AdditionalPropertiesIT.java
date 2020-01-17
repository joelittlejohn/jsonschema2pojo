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

package org.jsonschema2pojo.integration;

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matcher;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

public class AdditionalPropertiesIT {

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    @SuppressWarnings("unchecked")
    public void jacksonCanDeserializeOurAdditionalProperties() throws ClassNotFoundException, IOException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/additionalProperties/defaultAdditionalProperties.json", "com.example");

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
    @SuppressWarnings("unchecked")
    public void jacksonCanDeserializeOurAdditionalPropertiesWithoutIncludeAccessors() throws ClassNotFoundException, IOException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/additionalProperties/defaultAdditionalProperties.json", "com.example", config("includeGetters", false));

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
    public void jacksonCanSerializeOurAdditionalProperties() throws ClassNotFoundException, IOException, SecurityException, IllegalArgumentException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/additionalProperties/defaultAdditionalProperties.json", "com.example");

        Class<?> classWithAdditionalProperties = resultsClassLoader.loadClass("com.example.DefaultAdditionalProperties");
        String jsonWithAdditionalProperties = "{\"a\":1, \"b\":2};";
        Object instanceWithAdditionalProperties = mapper.readValue(jsonWithAdditionalProperties, classWithAdditionalProperties);

        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(instanceWithAdditionalProperties));

        assertThat(jsonNode.path("a").asText(), is("1"));
        assertThat(jsonNode.path("b").asInt(), is(2));
    }

    @Test
    public void jacksonCanSerializeOurAdditionalPropertiesWithoutIncludeAccessors() throws ClassNotFoundException, IOException, SecurityException, IllegalArgumentException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/additionalProperties/defaultAdditionalProperties.json", "com.example", config("includeGetters", false));

        Class<?> classWithAdditionalProperties = resultsClassLoader.loadClass("com.example.DefaultAdditionalProperties");
        String jsonWithAdditionalProperties = "{\"a\":1, \"b\":2};";
        Object instanceWithAdditionalProperties = mapper.readValue(jsonWithAdditionalProperties, classWithAdditionalProperties);

        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(instanceWithAdditionalProperties));

        assertThat(jsonNode.path("a").asText(), is("1"));
        assertThat(jsonNode.path("b").asInt(), is(2));
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void additionalPropertiesAreNotDeserializableWhenDisallowed() throws ClassNotFoundException, SecurityException, IOException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/additionalProperties/noAdditionalProperties.json", "com.example");

        Class<?> classWithNoAdditionalProperties = resultsClassLoader.loadClass("com.example.NoAdditionalProperties");

        mapper.readValue("{\"a\":\"1\", \"b\":2}", classWithNoAdditionalProperties);

    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void additionalPropertiesAreNotDeserializableWhenDisabledGlobally() throws ClassNotFoundException, SecurityException, IOException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/additionalProperties/defaultAdditionalProperties.json", "com.example", config("includeAdditionalProperties", false));

        Class<?> classWithNoAdditionalProperties = resultsClassLoader.loadClass("com.example.DefaultAdditionalProperties");

        mapper.readValue("{\"a\":\"1\", \"b\":2}", classWithNoAdditionalProperties);

    }

    @Test
    public void additionalPropertiesOfStringTypeOnly() throws SecurityException, NoSuchMethodException, ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/additionalProperties/additionalPropertiesString.json", "com.example", config("generateBuilders", true));

        Class<?> classWithNoAdditionalProperties = resultsClassLoader.loadClass("com.example.AdditionalPropertiesString");
        Method getter = classWithNoAdditionalProperties.getMethod("getAdditionalProperties");

        assertThat(((ParameterizedType) getter.getGenericReturnType()).getActualTypeArguments()[1], is(equalTo((Type) String.class)));

        // setter with these types should exist:
        classWithNoAdditionalProperties.getMethod("setAdditionalProperty", String.class, String.class);

        // builder with these types should exist:
        Method builderMethod = classWithNoAdditionalProperties.getMethod("withAdditionalProperty", String.class, String.class);
        assertThat("the builder method returns this type", builderMethod.getReturnType(), typeEqualTo(classWithNoAdditionalProperties));

    }

    @Test
    public void additionalPropertiesOfObjectTypeCreatesNewClassForPropertyValues() throws SecurityException, NoSuchMethodException, ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/additionalProperties/additionalPropertiesObject.json", "com.example", config("generateBuilders", true));

        Class<?> classWithNoAdditionalProperties = resultsClassLoader.loadClass("com.example.AdditionalPropertiesObject");
        Class<?> propertyValueType = resultsClassLoader.loadClass("com.example.AdditionalPropertiesObjectProperty");

        Method getter = classWithNoAdditionalProperties.getMethod("getAdditionalProperties");

        assertThat(((ParameterizedType) getter.getGenericReturnType()).getActualTypeArguments()[1], is(equalTo((Type) propertyValueType)));

        // setter with these types should exist:
        classWithNoAdditionalProperties.getMethod("setAdditionalProperty", String.class, propertyValueType);

        // builder with these types should exist:
        Method builderMethod = classWithNoAdditionalProperties.getMethod("withAdditionalProperty", String.class, propertyValueType);
        assertThat("the builder method returns this type", builderMethod.getReturnType(), typeEqualTo(classWithNoAdditionalProperties));

    }

    @Test(expected = NoSuchMethodException.class)
    public void additionalPropertiesBuilderAbsentIfNotConfigured() throws SecurityException, NoSuchMethodException, ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/additionalProperties/additionalPropertiesObject.json", "com.example");

        Class<?> classWithNoAdditionalProperties = resultsClassLoader.loadClass("com.example.AdditionalPropertiesObject");
        Class<?> propertyValueType = resultsClassLoader.loadClass("com.example.AdditionalPropertiesObjectProperty");

        // builder with these types should not exist:
        Method builderMethod = classWithNoAdditionalProperties.getMethod("withAdditionalProperty", String.class, propertyValueType);
        assertThat("the builder method returns this type", builderMethod.getReturnType(), typeEqualTo(classWithNoAdditionalProperties));

        fail("additional properties builder found when not requested");
    }

    @Test
    public void additionalPropertiesOfStringArrayTypeOnly() throws SecurityException, NoSuchMethodException, ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/additionalProperties/additionalPropertiesArraysOfStrings.json", "com.example", config("generateBuilders", true));

        Class<?> classWithNoAdditionalProperties = resultsClassLoader.loadClass("com.example.AdditionalPropertiesArraysOfStrings");
        Method getter = classWithNoAdditionalProperties.getMethod("getAdditionalProperties");

        ParameterizedType listType = (ParameterizedType) ((ParameterizedType) getter.getGenericReturnType()).getActualTypeArguments()[1];
        assertThat(listType.getActualTypeArguments()[0], is(equalTo((Type) String.class)));

        // setter with these types should exist:
        classWithNoAdditionalProperties.getMethod("setAdditionalProperty", String.class, List.class);

        // builder with these types should exist:
        Method builderMethod = classWithNoAdditionalProperties.getMethod("withAdditionalProperty", String.class, List.class);
        assertThat("the builder method returns this type", builderMethod.getReturnType(), typeEqualTo(classWithNoAdditionalProperties));

    }

    @Test
    public void additionalPropertiesOfBooleanTypeOnly() throws SecurityException, NoSuchMethodException, ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/additionalProperties/additionalPropertiesPrimitiveBoolean.json", "com.example", config("usePrimitives", true, "generateBuilders", true));

        Class<?> classWithNoAdditionalProperties = resultsClassLoader.loadClass("com.example.AdditionalPropertiesPrimitiveBoolean");
        Method getter = classWithNoAdditionalProperties.getMethod("getAdditionalProperties");

        assertThat(((ParameterizedType) getter.getGenericReturnType()).getActualTypeArguments()[1], is(equalTo((Type) Boolean.class)));

        // setter with these types should exist:
        classWithNoAdditionalProperties.getMethod("setAdditionalProperty", String.class, boolean.class);

        // builder with these types should exist:
        Method builderMethod = classWithNoAdditionalProperties.getMethod("withAdditionalProperty", String.class, boolean.class);
        assertThat("the builder method returns this type", builderMethod.getReturnType(), typeEqualTo(classWithNoAdditionalProperties));

    }

    @Test
    public void withAdditionalPropertyStoresValue() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/additionalProperties/additionalPropertiesString.json", "com.example", config("generateBuilders", true));

        Class<?> classWithNoAdditionalProperties = resultsClassLoader.loadClass("com.example.AdditionalPropertiesString");
        Method getter = classWithNoAdditionalProperties.getMethod("getAdditionalProperties");
        Method builderMethod = classWithNoAdditionalProperties.getMethod("withAdditionalProperty", String.class, String.class);

        Object value = "value";
        Object instance = classWithNoAdditionalProperties.newInstance();
        Object result = builderMethod.invoke(instance, "prop", value);
        Object stored = ((Map<?, ?>) getter.invoke(instance)).get("prop");

        assertThat("the builder returned the instance", result, sameInstance(instance));
        assertThat("the getter returned the value", stored, sameInstance(value));

    }

    @Test
    public void additionalPropertiesWorkWithAllVisibility() throws ClassNotFoundException, SecurityException, IOException {
        mapper.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
        mapper.configure(MapperFeature.AUTO_DETECT_SETTERS, false);
        mapper.setVisibility(mapper.getVisibilityChecker().with(Visibility.ANY));

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/additionalProperties/defaultAdditionalProperties.json", "com.example");

        Class<?> classWithAdditionalProperties = resultsClassLoader.loadClass("com.example.DefaultAdditionalProperties");
        String jsonWithAdditionalProperties = "{\"a\":1, \"b\":2};";
        Object instanceWithAdditionalProperties = mapper.readValue(jsonWithAdditionalProperties, classWithAdditionalProperties);

        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(instanceWithAdditionalProperties));

        assertThat(jsonNode.path("a").asText(), is("1"));
        assertThat(jsonNode.path("b").asInt(), is(2));
        assertThat(jsonNode.has("additionalProperties"), is(false));
    }

    @SuppressWarnings("rawtypes")
    public static Matcher<Class> typeEqualTo(Class<?> type) {
        return equalTo((Class) type);
    }

}
