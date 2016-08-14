/**
 * Copyright © 2010-2014 Nokia
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

import static java.lang.reflect.Modifier.*;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("rawtypes")
public class EnumIT {
    
    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();
    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private static Class parentClass;
    private static Class<Enum> enumClass;

    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void generateAndCompileEnum() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = classSchemaRule.generateAndCompile("/schema/enum/typeWithEnumProperty.json", "com.example", config("propertyWordDelimiters", "_"));

        parentClass = resultsClassLoader.loadClass("com.example.TypeWithEnumProperty");
        enumClass = (Class<Enum>) resultsClassLoader.loadClass("com.example.TypeWithEnumProperty$EnumProperty");

    }

    @Test
    public void enumPropertyCreatesAStaticInnerType() throws ClassNotFoundException {

        assertThat(enumClass.isEnum(), is(true));

        assertThat(isPublic(enumClass.getModifiers()), is(true));
        assertThat(isStatic(enumClass.getModifiers()), is(true));

    }

    @Test
    public void enumClassIncludesCorrectlyNamedConstants() {

        assertThat(enumClass.getEnumConstants()[0].name(), is("ONE"));
        assertThat(enumClass.getEnumConstants()[1].name(), is("SECOND_ONE"));
        assertThat(enumClass.getEnumConstants()[2].name(), is("_3_RD_ONE"));
        assertThat(enumClass.getEnumConstants()[3].name(), is("_4_1"));

    }

    @Test
    public void enumContainsWorkingAnnotatedSerializationMethod() throws NoSuchMethodException {

        Method toString = enumClass.getMethod("toString");

        assertThat(enumClass.getEnumConstants()[0].toString(), is("one"));
        assertThat(enumClass.getEnumConstants()[1].toString(), is("secondOne"));
        assertThat(enumClass.getEnumConstants()[2].toString(), is("3rd one"));

        assertThat(toString.isAnnotationPresent(JsonValue.class), is(true));

    }

    @Test
    public void enumContainsWorkingAnnotatedDeserializationMethod() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        Method fromValue = enumClass.getMethod("fromValue", String.class);

        assertThat((Enum) fromValue.invoke(enumClass, "one"), is(sameInstance(enumClass.getEnumConstants()[0])));
        assertThat((Enum) fromValue.invoke(enumClass, "secondOne"), is(sameInstance(enumClass.getEnumConstants()[1])));
        assertThat((Enum) fromValue.invoke(enumClass, "3rd one"), is(sameInstance(enumClass.getEnumConstants()[2])));

        assertThat(fromValue.isAnnotationPresent(JsonCreator.class), is(true));

    }

    @Test
    public void enumDeserializationMethodRejectsInvalidValues() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        Method fromValue = enumClass.getMethod("fromValue", String.class);

        try {
            fromValue.invoke(enumClass, "something invalid");
            fail();
        } catch (InvocationTargetException e) {
            assertThat(e.getCause(), is(instanceOf(IllegalArgumentException.class)));
        }

    }

    @Test
    @SuppressWarnings("unchecked")
    public void enumAtRootCreatesATopLevelType() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/enum/enumAsRoot.json", "com.example");

        Class<Enum> rootEnumClass = (Class<Enum>) resultsClassLoader.loadClass("com.example.enums.EnumAsRoot");

        assertThat(rootEnumClass.isEnum(), is(true));
        assertThat(isPublic(rootEnumClass.getModifiers()), is(true));

    }

    @Test
    @SuppressWarnings("unchecked")
    public void enumWithEmptyStringAsValue() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/enum/enumWithEmptyString.json", "com.example");

        Class<Enum> emptyEnumClass = (Class<Enum>) resultsClassLoader.loadClass("com.example.EnumWithEmptyString");

        assertThat(emptyEnumClass.isEnum(), is(true));
        assertThat(emptyEnumClass.getEnumConstants()[0].name(), is("__EMPTY__"));

    }

    @Test
    @SuppressWarnings("unchecked")
    public void enumWithNullValue() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/enum/enumWithNullValue.json", "com.example");

        Class<Enum> nullEnumClass = (Class<Enum>) resultsClassLoader.loadClass("com.example.EnumWithNullValue");

        assertThat(nullEnumClass.isEnum(), is(true));
        assertThat(nullEnumClass.getEnumConstants().length, is(1));

    }

    @Test
    public void enumWithUppercaseProperty() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/enum/enumWithUppercaseProperty.json", "com.example");

        resultsClassLoader.loadClass("com.example.EnumWithUppercaseProperty");
        resultsClassLoader.loadClass("com.example.EnumWithUppercaseProperty$TimeFormat");
    }

    @Test
    public void enumWithExtendedCharacters() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/enum/enumWithExtendedCharacters.json", "com.example");

        resultsClassLoader.loadClass("com.example.EnumWithExtendedCharacters");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void enumWithNoValidCharactersForIdentifier() throws Exception {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/enum/enumWithNoValidCharactersForIdentifier.json", "com.example");

        Class<Enum> enumClass = (Class<Enum>) resultsClassLoader.loadClass("com.example.EnumWithNoValidCharactersForIdentifier");

        assertThat(enumClass.getEnumConstants()[0].name(), is("__EMPTY__"));
        assertThat(enumClass.getEnumConstants()[1].name(), is("__EMPTY___"));
        assertThat(enumClass.getEnumConstants()[2].name(), is("__EMPTY____"));

        assertThat(enumClass.getEnumConstants()[0].toString(), is(":"));
        assertThat(enumClass.getEnumConstants()[1].toString(), is("\u2265"));
        assertThat(enumClass.getEnumConstants()[2].toString(), is("<"));
    }

    @Test
    public void multipleEnumArraysWithSameName() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/enum/multipleEnumArraysWithSameName.json", "com.example");

        resultsClassLoader.loadClass("com.example.MultipleEnumArraysWithSameName");
        resultsClassLoader.loadClass("com.example.Status");
        resultsClassLoader.loadClass("com.example.Status_");
    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void enumWithCustomJavaNames() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/enum/enumWithCustomJavaNames.json", "com.example");

        Class<?> typeWithEnumProperty = resultsClassLoader.loadClass("com.example.EnumWithCustomJavaNames");
        Class<Enum> enumClass = (Class<Enum>) resultsClassLoader.loadClass("com.example.EnumWithCustomJavaNames$EnumProperty");

        Object valueWithEnumProperty = typeWithEnumProperty.newInstance();
        Method enumSetter = typeWithEnumProperty.getMethod("setEnumProperty", enumClass);
        enumSetter.invoke(valueWithEnumProperty, enumClass.getEnumConstants()[2]);
        assertThat(enumClass.getEnumConstants()[0].name(), is("ONE"));
        assertThat(enumClass.getEnumConstants()[1].name(), is("TWO"));
        assertThat(enumClass.getEnumConstants()[2].name(), is("THREE"));
        assertThat(enumClass.getEnumConstants()[3].name(), is("FOUR"));

        ObjectMapper objectMapper = new ObjectMapper();

        String jsonString = objectMapper.writeValueAsString(valueWithEnumProperty);
        JsonNode jsonTree = objectMapper.readTree(jsonString);

        assertThat(jsonTree.size(), is(1));
        assertThat(jsonTree.has("enum_Property"), is(true));
        assertThat(jsonTree.get("enum_Property").isTextual(), is(true));
        assertThat(jsonTree.get("enum_Property").asText(), is("3"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void jacksonCanMarshalEnums() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {

        Object valueWithEnumProperty = parentClass.newInstance();
        Method enumSetter = parentClass.getMethod("setEnumProperty", enumClass);
        enumSetter.invoke(valueWithEnumProperty, enumClass.getEnumConstants()[2]);

        ObjectMapper objectMapper = new ObjectMapper();

        String jsonString = objectMapper.writeValueAsString(valueWithEnumProperty);
        JsonNode jsonTree = objectMapper.readTree(jsonString);

        assertThat(jsonTree.size(), is(1));
        assertThat(jsonTree.has("enum_Property"), is(true));
        assertThat(jsonTree.get("enum_Property").isTextual(), is(true));
        assertThat(jsonTree.get("enum_Property").asText(), is("3rd one"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void jacksonCanUnmarshalEnums() throws IOException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {

        String jsonString = "{\"enum_Property\" : \"3rd one\"}";

        Object result = new ObjectMapper().readValue(jsonString, parentClass);

        Method enumGetter = parentClass.getMethod("getEnumProperty");

        assertThat((Enum) enumGetter.invoke(result), is(equalTo(enumClass.getEnumConstants()[2])));

    }

}
