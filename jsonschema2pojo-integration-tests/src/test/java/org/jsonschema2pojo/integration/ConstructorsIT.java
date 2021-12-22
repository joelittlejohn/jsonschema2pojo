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

import com.sun.codemodel.JMod;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConstructorsIT extends Jsonschema2PojoTestBase {

    public static void assertHasModifier(int modifier, int modifiers, String modifierName) {
        assertEquals(modifier, modifier & modifiers, "Expected the bit " + modifierName + " (" + modifier + ")" + " to be set but got: " + modifiers);
    }

    public static void assertHasOnlyDefaultConstructor(Class<?> cls) {
        Constructor<?>[] constructors = cls.getConstructors();

        assertEquals(constructors.length, 1);

        assertEquals(0, constructors[0].getParameterTypes().length, "Expected " + cls + " to only have the default, no-args constructor");
    }

    public static Constructor<?> getAllPropertiesConstructor(Class<?> clazz) throws NoSuchMethodException {
        return clazz.getConstructor(String.class, Integer.class, Boolean.class, String.class, String.class);
    }

    public static Constructor<?> getRequiredPropertiesConstructor(Class<?> clazz) throws NoSuchMethodException {
        return clazz.getConstructor(String.class, Integer.class, Boolean.class);
    }

    public static Object getInstance(Class<?> clazz, Object... args)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?>[] parameterTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }

        Constructor<?> constructor = clazz.getConstructor(parameterTypes);
        return constructor.newInstance(args);
    }

    public static Object getValue(Object instance, String methodName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return instance.getClass()
                .getMethod(methodName)
                .invoke(instance);
    }


    public static class ConstructorTestClasses extends Jsonschema2PojoTestBase {
        protected Class<?> typeWithoutProperties;
        protected Class<?> typeWithoutRequiredProperties;
        protected Class<?> typeWithRequiredArray;
        protected Class<?> typeWithRequired;

        protected void generateClasses(Map<String, Object> config) throws ClassNotFoundException {
            generate("/schema/constructors/noPropertiesConstructor.json", "com.example", config);

            generate("/schema/constructors/noRequiredPropertiesConstructor.json", "com.example", config);

            generate("/schema/constructors/requiredArrayPropertyConstructors.json", "com.example", config);

            generate("/schema/constructors/requiredPropertyConstructors.json", "com.example", config);

            ClassLoader loader = compile();
            typeWithoutProperties = loader.loadClass("com.example.NoPropertiesConstructor");
            typeWithoutRequiredProperties = loader.loadClass("com.example.NoRequiredPropertiesConstructor");
            typeWithRequiredArray = loader.loadClass("com.example.RequiredArrayPropertyConstructors");
            typeWithRequired = loader.loadClass("com.example.RequiredPropertyConstructors");

        }
    }

    /**
     * Tests what happens when includeConstructors is set to true
     */
    @Nested
    public static class DefaultInlcudeConstructorsIT extends ConstructorTestClasses {

        @BeforeEach
        public void generateAndCompileConstructorClasses() throws ClassNotFoundException {
            // @formatter:off
            generateClasses(config(
                    "propertyWordDelimiters", "_",
                    "includeConstructors", true));
            // @formatter:on
        }

        @Test
        public void testGeneratesConstructorWithAllProperties() throws Exception {
            assertHasModifier(JMod.PUBLIC, getAllPropertiesConstructor(typeWithRequired).getModifiers(), "public");
        }

        @Test
        public void testGeneratesCosntructorWithAllPropertiesArrayStyle() throws Exception {
            assertHasModifier(JMod.PUBLIC, getAllPropertiesConstructor(typeWithRequiredArray).getModifiers(), "public");
        }

        @Test
        public void testNoConstructorWithoutProperties() {
            assertHasOnlyDefaultConstructor(typeWithoutProperties);
        }
    }

    /**
     * Tests with constructorsRequiredPropertiesOnly set to true
     */
    @Nested
    public static class RequiredOnlyIT extends ConstructorTestClasses {

        @BeforeEach
        public void generateAndCompileConstructorClasses() throws ClassNotFoundException {
            // @formatter:off
            generateClasses(
                    config(
                            "propertyWordDelimiters", "_",
                            "includeConstructors", true,
                            "constructorsRequiredPropertiesOnly", true));
            // @formatter:on
        }

        @Test
        public void testCreatesPublicNoArgsConstructor() throws Exception {
            Constructor<?> constructor = typeWithRequired.getConstructor();

            assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
        }

        @Test
        public void testCreatesConstructorWithRequiredParams() throws Exception {
            Constructor<?> constructor = getRequiredPropertiesConstructor(typeWithRequired);

            assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
        }

        @Test
        public void testCreatesConstructorWithRequiredParamsArrayStyle() throws Exception {
            Constructor<?> constructor = getRequiredPropertiesConstructor(typeWithRequiredArray);

            assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
        }

        @Test
        public void testConstructorAssignsFields() throws Exception {
            Object instance = getInstance(typeWithRequired, "type", 5, true);

            assertEquals("type", getValue(instance, "getType"));
            assertEquals(5, getValue(instance, "getId"));
            assertEquals(true, getValue(instance, "getHasTickets"));
        }

        @Test
        public void testNoConstructorWithoutRequiredParams() {
            assertHasOnlyDefaultConstructor(typeWithoutRequiredProperties);
        }

        @Test
        public void testDoesntGenerateConstructorsWithoutConfig() throws Exception {
            assertHasOnlyDefaultConstructor(typeWithoutProperties);
        }
    }

    /**
     * Tests what happens when includeConstructors is set to true
     */
    @Nested
    public static class IncludeRequiredConstructorsIT extends ConstructorTestClasses {

        @BeforeEach
        public void generateAndCompileConstructorClasses() throws ClassNotFoundException {
            // @formatter:off
            generateClasses(
                    config(
                            "propertyWordDelimiters", "_",
                            "includeConstructors", true,
                            "includeRequiredPropertiesConstructor", true));
            // @formatter:on
        }

        @Test
        public void testGeneratesConstructorWithAllProperties() throws Exception {
            assertHasModifier(JMod.PUBLIC, getAllPropertiesConstructor(typeWithRequired).getModifiers(), "public");
        }

        @Test
        public void testGeneratesCosntructorWithAllPropertiesArrayStyle() throws Exception {
            assertHasModifier(JMod.PUBLIC, getAllPropertiesConstructor(typeWithRequiredArray).getModifiers(), "public");
        }

        @Test
        public void testCreatesPublicNoArgsConstructor() throws Exception {
            Constructor<?> constructor = typeWithRequired.getConstructor();

            assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
        }

        @Test
        public void testCreatesConstructorWithRequiredParams() throws Exception {
            Constructor<?> constructor = getAllPropertiesConstructor(typeWithRequired);

            assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
        }

        @Test
        public void testCreatesConstructorWithRequiredParamsArrayStyle() throws Exception {
            Constructor<?> constructor = getAllPropertiesConstructor(typeWithRequiredArray);

            assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
        }

        @Test
        public void testRequiredFieldsConstructorAssignsFields() throws Exception {
            Object instance = getInstance(typeWithRequired, "type", 5, true);

            assertEquals("type", getValue(instance, "getType"));
            assertEquals(5, getValue(instance, "getId"));
            assertEquals(true, getValue(instance, "getHasTickets"));
        }

        @Test
        public void testAllFieldsConstructorAssignsFields() throws Exception {
            Object instance = getInstance(typeWithRequired, "type", 5, true, "provider", "startTime");

            assertEquals("type", getValue(instance, "getType"));
            assertEquals(5, getValue(instance, "getId"));
            assertEquals(true, getValue(instance, "getHasTickets"));
            assertEquals("provider", getValue(instance, "getProvider"));
            assertEquals("startTime", getValue(instance, "getStarttime"));
        }
    }

    /**
     * Tests what happens when includeConstructors is set to true
     */
    @Nested
    public static class IncludeCopyConstructorsIT extends ConstructorTestClasses {

        @BeforeEach
        public void generateAndCompileConstructorClasses() throws ClassNotFoundException {
            // @formatter:off
            generateClasses(
                    config("propertyWordDelimiters", "_",
                            "includeConstructors", true,
                            "includeCopyConstructor", true));
            // @formatter:on
        }

        @Test
        public void testGeneratesConstructorWithAllProperties() throws Exception {
            assertHasModifier(JMod.PUBLIC, getAllPropertiesConstructor(typeWithRequired).getModifiers(), "public");
        }

        @Test
        public void testGeneratesCosntructorWithAllPropertiesArrayStyle() throws Exception {
            assertHasModifier(JMod.PUBLIC, getAllPropertiesConstructor(typeWithRequiredArray).getModifiers(), "public");
        }

        @Test
        public void testCreatesPublicNoArgsConstructor() throws Exception {
            Constructor<?> constructor = typeWithRequired.getConstructor();

            assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
        }

        @Test
        public void testCreatesConstructorWithCopyParams() throws Exception {
            Constructor<?> constructor = typeWithRequired.getConstructor(typeWithRequired);

            assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
        }

        @Test
        public void testCopyConstructorAssignsFields() throws Exception {
            Object instance = getInstance(typeWithRequired, "type", 5, true, "provider", "startTime");
            Object copyInstance = getInstance(typeWithRequired, instance);

            assertEquals("type", getValue(copyInstance, "getType"));
            assertEquals(5, getValue(copyInstance, "getId"));
            assertEquals(true, getValue(copyInstance, "getHasTickets"));
            assertEquals("provider", getValue(copyInstance, "getProvider"));
            assertEquals("startTime", getValue(copyInstance, "getStarttime"));
        }

        @Test
        public void testAllFieldsConstructorAssignsFields() throws Exception {
            Object instance = getInstance(typeWithRequired, "type", 5, true, "provider", "startTime");

            assertEquals("type", getValue(instance, "getType"));
            assertEquals(5, getValue(instance, "getId"));
            assertEquals(true, getValue(instance, "getHasTickets"));
            assertEquals("provider", getValue(instance, "getProvider"));
            assertEquals("startTime", getValue(instance, "getStarttime"));
        }
    }

}
