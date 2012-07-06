/**
 * Copyright © 2010-2011 Nokia
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

package com.googlecode.jsonschema2pojo.integration;

import static com.googlecode.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static java.lang.reflect.Modifier.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EnumIT {

    @SuppressWarnings("rawtypes")
    private static Class parentClass;

    @SuppressWarnings("rawtypes")
    private static Class<Enum> enumClass;

    @BeforeClass
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void generateAndCompileEnum() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/enum/typeWithEnumProperty.json", "com.example");

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
    @SuppressWarnings("rawtypes")
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void enumAtRootCreatesATopLevelType() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/enum/enumAsRoot.json", "com.example");

        Class<Enum> rootEnumClass = (Class<Enum>) resultsClassLoader.loadClass("com.example.EnumAsRoot");

        assertThat(rootEnumClass.isEnum(), is(true));
        assertThat(isPublic(rootEnumClass.getModifiers()), is(true));

    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void enumWithEmptyStringAsValue() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/enum/enumWithEmptyString.json", "com.example");

        Class<Enum> emptyEnumClass = (Class<Enum>) resultsClassLoader.loadClass("com.example.EnumWithEmptyString");

        assertThat(emptyEnumClass.getEnumConstants()[0].name(), is("__EMPTY__"));

    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void enumWithNullValue() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/enum/enumWithNullValue.json", "com.example");

        Class<Enum> emptyEnumClass = (Class<Enum>) resultsClassLoader.loadClass("com.example.EnumWithNullValue");

        assertThat(emptyEnumClass.getEnumConstants().length, is(1));

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
        assertThat(jsonTree.has("enumProperty"), is(true));
        assertThat(jsonTree.get("enumProperty").isTextual(), is(true));
        assertThat(jsonTree.get("enumProperty").asText(), is("3rd one"));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void jacksonCanUnmarshalEnums() throws IOException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {

        String jsonString = "{\"enumProperty\" : \"3rd one\"}";

        Object result = new ObjectMapper().readValue(jsonString, parentClass);

        Method enumGetter = parentClass.getMethod("getEnumProperty");

        assertThat((Enum) enumGetter.invoke(result), is(equalTo(enumClass.getEnumConstants()[2])));

    }

}
