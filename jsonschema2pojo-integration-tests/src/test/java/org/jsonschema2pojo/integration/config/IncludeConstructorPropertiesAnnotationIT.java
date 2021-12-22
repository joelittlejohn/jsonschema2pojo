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

package org.jsonschema2pojo.integration.config;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoTestBase;
import org.junit.jupiter.api.Test;

import java.beans.ConstructorProperties;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.StringJoiner;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.jupiter.api.Assertions.*;

public class IncludeConstructorPropertiesAnnotationIT extends Jsonschema2PojoTestBase {

    private static final String[] expectedValueForAllValuesConstructor = {"x", "y", "z"};
    private static final String[] expectedValueForRequiredValuesConstructor = {"x", "y"};
    private static final String testObjectPackage = "com.example";
    private static final String testObjectName = testObjectPackage + "." + "TestObject";
    private static final String testObjectSchema = "/schema/includeConstructorPropertiesAnnotation/testObject.json";

    @Test
    public void defaultConfig() throws ClassNotFoundException {
        ClassLoader classLoader = generateAndCompile(testObjectSchema, testObjectPackage);

        Class<?> testObjectClass = classLoader.loadClass(testObjectName);

        validateNoAnnotationPresentOnAnyConstructors(testObjectClass);
    }

    @Test
    public void defaultWithConstructors() throws ClassNotFoundException {
        ClassLoader classLoader = generateAndCompile(testObjectSchema, testObjectPackage,
                config("includeConstructors", true));

        Class<?> testObjectClass = classLoader.loadClass(testObjectName);

        validateNoAnnotationPresentOnAnyConstructors(testObjectClass);
    }

    @Test
    public void disabled() throws ClassNotFoundException {
        ClassLoader classLoader = generateAndCompile(testObjectSchema, testObjectPackage,
                config("includeConstructorPropertiesAnnotation", false));

        Class<?> testObjectClass = classLoader.loadClass(testObjectName);

        validateNoAnnotationPresentOnAnyConstructors(testObjectClass);
    }

    @Test
    public void disabledWithConstructors() throws ClassNotFoundException {
        ClassLoader classLoader = generateAndCompile(testObjectSchema, testObjectPackage,
                config("includeConstructors", true,
                        "constructorsRequiredPropertiesOnly", true,
                        "includeConstructorPropertiesAnnotation", false));

        Class<?> testObjectClass = classLoader.loadClass(testObjectName);

        validateNoAnnotationPresentOnAnyConstructors(testObjectClass);
    }

    @Test
    public void enabled() throws ClassNotFoundException {
        ClassLoader classLoader = generateAndCompile(testObjectSchema, testObjectPackage,
                config("includeConstructors", true,
                        "includeConstructorPropertiesAnnotation", true));

        Class<?> testObjectClass = classLoader.loadClass(testObjectName);

        validateConstructor(3, expectedValueForAllValuesConstructor, testObjectClass);
    }

    @Test
    public void enabledWithRequiredPropertiesOnly() throws ClassNotFoundException {
        ClassLoader classLoader = generateAndCompile(testObjectSchema, testObjectPackage,
                config("includeConstructors", true,
                        "constructorsRequiredPropertiesOnly", true,
                        "includeConstructorPropertiesAnnotation", true));

        Class<?> testObjectClass = classLoader.loadClass(testObjectName);

        validateConstructor(2, expectedValueForRequiredValuesConstructor, testObjectClass);
    }

    @Test
    public void enabledWithoutConstructors() throws ClassNotFoundException {
        ClassLoader classLoader = generateAndCompile(testObjectSchema, testObjectPackage,
                config("includeConstructors", false,
                        "includeConstructorPropertiesAnnotation", true));

        Class<?> testObjectClass = classLoader.loadClass(testObjectName);

        validateNoAnnotationPresentOnAnyConstructors(testObjectClass);
    }

    private String expectedValueForFail(String[] expectedValue) {
        StringJoiner joiner = new StringJoiner(",", "{", "}");

        for (String v : expectedValue) {
            joiner.add("\"" + v + "\"");
        }

        return joiner.toString();
    }

    private String paramTypesJoin(Class<?>[] parameterTypes) {
        StringJoiner joiner = new StringJoiner(",");

        for (Class<?> clazz : parameterTypes) {
            joiner.add(clazz.getCanonicalName());
        }

        return joiner.toString();
    }

    private void validateConstructor(int paramCount, String[] expectedValue, Class<?> testObjectClass) {
        Constructor<?>[] constructors = testObjectClass.getConstructors();
        assertAll(
                "Could not find " + paramCount + " parameter constructor which was expect to have " + expectedValueForFail(expectedValue),
                Stream.of(constructors)
                        .filter(constructor -> constructor.getParameterCount() == paramCount)
                        .map(constructor -> () -> {
                            ConstructorProperties constructorPropertiesAnnotation = constructor.getAnnotation(ConstructorProperties.class);
                            assertAll(
                                    () -> assertNotNull(constructorPropertiesAnnotation),
                                    () -> assertThat(constructorPropertiesAnnotation.value(), is(expectedValue))
                            );
                        })
        );
    }

    private void validateNoAnnotationPresentOnAnyConstructors(Class<?> testObjectClass) {
        Constructor<?>[] constructors = testObjectClass.getConstructors();

        for (Constructor<?> constructor : constructors) {
            Annotation annotation = constructor.getAnnotation(ConstructorProperties.class);

            assertNull(annotation, "Found constructor with annotation expected to not be present. The Constructor " + constructor.getName() + " with " + paramTypesJoin(constructor.getParameterTypes()) + " had annotation.");
        }
    }
}
