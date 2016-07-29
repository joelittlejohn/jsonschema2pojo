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

import com.sun.codemodel.JMod;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.lang.reflect.Constructor;
import java.util.Map;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class ConstructorsIT {

    public static void assertHasModifier(int modifier, int modifiers, String modifierName) {
        assertEquals(
                "Expected the bit " + modifierName + " (" + modifier + ")" + " to be set but got: " + modifiers,
                modifier, modifier & modifiers);
    }

    public static void assertHasOnlyDefaultConstructor(Class<?> cls) {
        Constructor<?>[] constructors = cls.getConstructors();

        assertEquals(constructors.length, 1);

        assertEquals("Expected " + cls + " to only have the default, no-args constructor",
                0, constructors[0].getParameterTypes().length);
    }

    public static class AllPropertiesIT {
        @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

        protected static Class<?> typeWithRequired;
        private static Class<?> typeWithoutProperties;
        protected static Class<?> typeWithRequiredArray;

        //xxx there's a bit of duplication here in the name of performance; if we did this step as a Before method,
        //we could factor out a super class between AllPropertiesIT and RequiredOnlyIT... but it makes the tests run
        //more slowly
        @BeforeClass
        public static void generateAndCompileConstructorClasses() throws ClassNotFoundException {

            Map<String, Object> config = config("propertyWordDelimiters", "_",
                    "includeConstructors", true
            );
            classSchemaRule.generate(
                    "/schema/constructors/requiredPropertyConstructors.json",
                    "com.example",
                    config);

            classSchemaRule.generate(
                    "/schema/constructors/noPropertiesConstructor.json",
                    "com.example",
                    config);

            classSchemaRule.generate(
                    "/schema/constructors/requiredArrayPropertyConstructors.json",
                    "com.example",
                    config);

            ClassLoader loader = classSchemaRule.compile();
            typeWithRequired = loader.loadClass("com.example.RequiredPropertyConstructors");
            typeWithoutProperties = loader.loadClass("com.example.NoPropertiesConstructor");
            typeWithRequiredArray = loader.loadClass("com.example.RequiredArrayPropertyConstructors");
        }

        @Test
        public void testGeneratesConstructorWithAllProperties() throws Exception {
            assertHasModifier(JMod.PUBLIC, getArgsConstructor(typeWithRequired).getModifiers(), "public");
        }

        @Test
            public void testGeneratesCosntructorWithAllPropertiesArrayStyle() throws Exception {
            assertHasModifier(JMod.PUBLIC, getArgsConstructor(typeWithRequiredArray).getModifiers(), "public");
        }

        public Constructor<?> getArgsConstructor(Class<?> clazz) throws NoSuchMethodException {
            return clazz.getConstructor(String.class, Integer.class, Boolean.class, String.class, String.class);
        }

        @Test
        public void testNoConstructorWithoutProperties() throws Exception {
            assertHasOnlyDefaultConstructor(typeWithoutProperties);
        }
    }

    /**
     * Tests with constructorsRequiredPropertiesOnly set to true
     */
    public static class RequiredOnlyIT  {

        @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();
        @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

        protected static Class<?> typeWithRequired;
        protected static Class<?> typeWithoutRequired;
        protected static Class<?> typeWithRequiredArray;

        @BeforeClass
        public static void generateAndCompileConstructorClasses() throws ClassNotFoundException {

            Map<String, Object> config = config("propertyWordDelimiters", "_",
                    "includeConstructors", true,
                    "constructorsRequiredPropertiesOnly", true
            );
            classSchemaRule.generate(
                    "/schema/constructors/requiredPropertyConstructors.json",
                    "com.example",
                    config);

            classSchemaRule.generate(
                    "/schema/constructors/noRequiredPropertiesConstructor.json",
                    "com.example",
                    config);

            classSchemaRule.generate(
                    "/schema/constructors/requiredArrayPropertyConstructors.json",
                    "com.example",
                    config);

            ClassLoader classLoader = classSchemaRule.compile();
            typeWithRequired = classLoader.loadClass("com.example.RequiredPropertyConstructors");
            typeWithoutRequired = classLoader.loadClass("com.example.NoRequiredPropertiesConstructor");
            typeWithRequiredArray = classLoader.loadClass("com.example.RequiredArrayPropertyConstructors");
        }

        @Test
        public void testCreatesPublicNoArgsConstructor() throws Exception {
            Constructor<?> constructor = typeWithRequired.getConstructor();

            assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
        }

        @Test
        public void testCreatesConstructorWithRequiredParams() throws Exception {
            Constructor<?> constructor = getArgsConstructor(typeWithRequired);

            assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
        }

        @Test
        public void testCreatesConstructorWithRequiredParamsArrayStyle() throws Exception {
            Constructor<?> constructor = getArgsConstructor(typeWithRequiredArray);

            assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
        }

        public Constructor<?> getArgsConstructor(Class<?> clazz) throws NoSuchMethodException {
            return clazz.getConstructor(String.class, Integer.class, Boolean.class);
        }

        @Test
        public void testConstructorAssignsFields() throws Exception {
            Object instance = getArgsConstructor(typeWithRequired).newInstance("type", 5, true);

            assertEquals("type", typeWithRequired.getMethod("getType").invoke(instance));
            assertEquals(5, typeWithRequired.getMethod("getId").invoke(instance));
            assertEquals(true, typeWithRequired.getMethod("getHasTickets").invoke(instance));
        }

        @Test
        public void testNoConstructorWithoutRequiredParams() throws Exception {
            assertHasOnlyDefaultConstructor(typeWithoutRequired);
        }

        @Test
        public void testDoesntGenerateConstructorsWithoutConfig() throws Exception {

            Class<?> noConstructors = schemaRule.generateAndCompile(
                    "/schema/constructors/requiredPropertyConstructors.json",
                    "com.example",
                    config("propertyWordDelimiters", "_",
                            "includeConstructors", false
                    ))
                    .loadClass("com.example.RequiredPropertyConstructors");
            assertHasOnlyDefaultConstructor(noConstructors);
        }
    }
}
