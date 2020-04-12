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

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.assertEquals;

import com.sun.codemodel.JMod;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class ConstructorsIT {

  public static void assertHasModifier(int modifier, int modifiers, String modifierName) {
    assertEquals("Expected the bit " + modifierName + " (" + modifier + ")" + " to be set but got: " + modifiers, modifier, modifier & modifiers);
  }

  public static void assertHasOnlyDefaultConstructor(Class<?> cls) {
    Constructor<?>[] constructors = cls.getConstructors();

    assertEquals(constructors.length, 1);

    assertEquals("Expected " + cls + " to only have the default, no-args constructor", 0, constructors[0].getParameterTypes().length);
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


  @Ignore
  public static class ConstructorTestClasses {

    protected Class<?> typeWithoutProperties;
    protected Class<?> typeWithoutRequiredProperties;
    protected Class<?> typeWithRequiredArray;
    protected Class<?> typeWithRequired;


    public ConstructorTestClasses(Jsonschema2PojoRule classSchemaRule, Map<String, Object> config) throws ClassNotFoundException {
      classSchemaRule.generate("/schema/constructors/noPropertiesConstructor.json", "com.example", config);

      classSchemaRule.generate("/schema/constructors/noRequiredPropertiesConstructor.json", "com.example", config);

      classSchemaRule.generate("/schema/constructors/requiredArrayPropertyConstructors.json", "com.example", config);

      classSchemaRule.generate("/schema/constructors/requiredPropertyConstructors.json", "com.example", config);

      ClassLoader loader = classSchemaRule.compile();
      typeWithoutProperties = loader.loadClass("com.example.NoPropertiesConstructor");
      typeWithoutRequiredProperties = loader.loadClass("com.example.NoRequiredPropertiesConstructor");
      typeWithRequiredArray = loader.loadClass("com.example.RequiredArrayPropertyConstructors");
      typeWithRequired = loader.loadClass("com.example.RequiredPropertyConstructors");

    }
  }

  /**
   * Tests what happens when includeConstructors is set to true
   */
  public static class DefaultInlcudeConstructorsIT {

    @ClassRule
    public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    private static ConstructorTestClasses testClasses = null;

    @BeforeClass
    public static void generateAndCompileConstructorClasses() throws ClassNotFoundException {
      // @formatter:off
      testClasses = new ConstructorTestClasses(classSchemaRule, config(
          "propertyWordDelimiters", "_",
          "includeConstructors", true));
      // @formatter:on
    }

    @Test
    public void testGeneratesConstructorWithAllProperties() throws Exception {
      assertHasModifier(JMod.PUBLIC, getAllPropertiesConstructor(testClasses.typeWithRequired).getModifiers(), "public");
    }

    @Test
    public void testGeneratesCosntructorWithAllPropertiesArrayStyle() throws Exception {
      assertHasModifier(JMod.PUBLIC, getAllPropertiesConstructor(testClasses.typeWithRequiredArray).getModifiers(), "public");
    }

    @Test
    public void testNoConstructorWithoutProperties() {
      assertHasOnlyDefaultConstructor(testClasses.typeWithoutProperties);
    }
  }

  /**
   * Tests with constructorsRequiredPropertiesOnly set to true
   */
  public static class RequiredOnlyIT {

    @ClassRule
    public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    private static ConstructorTestClasses testClasses = null;

    @BeforeClass
    public static void generateAndCompileConstructorClasses() throws ClassNotFoundException {
      // @formatter:off
      testClasses = new ConstructorTestClasses(classSchemaRule,
          config(
              "propertyWordDelimiters", "_",
              "includeConstructors", true,
              "constructorsRequiredPropertiesOnly", true));
      // @formatter:on
    }

    @Test
    public void testCreatesPublicNoArgsConstructor() throws Exception {
      Constructor<?> constructor = testClasses.typeWithRequired.getConstructor();

      assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
    }

    @Test
    public void testCreatesConstructorWithRequiredParams() throws Exception {
      Constructor<?> constructor = getRequiredPropertiesConstructor(testClasses.typeWithRequired);

      assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
    }

    @Test
    public void testCreatesConstructorWithRequiredParamsArrayStyle() throws Exception {
      Constructor<?> constructor = getRequiredPropertiesConstructor(testClasses.typeWithRequiredArray);

      assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
    }

    @Test
    public void testConstructorAssignsFields() throws Exception {
      Object instance = getInstance(testClasses.typeWithRequired, "type", 5, true);

      assertEquals("type", getValue(instance, "getType"));
      assertEquals(5, getValue(instance, "getId"));
      assertEquals(true, getValue(instance, "getHasTickets"));
    }

    @Test
    public void testNoConstructorWithoutRequiredParams() {
      assertHasOnlyDefaultConstructor(testClasses.typeWithoutRequiredProperties);
    }

    @Test
    public void testDoesntGenerateConstructorsWithoutConfig() throws Exception {
      assertHasOnlyDefaultConstructor(testClasses.typeWithoutProperties);
    }
  }

  /**
   * Tests what happens when includeConstructors is set to true
   */
  public static class IncludeRequiredConstructorsIT {

    @ClassRule
    public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    private static ConstructorTestClasses testClasses = null;

    @BeforeClass
    public static void generateAndCompileConstructorClasses() throws ClassNotFoundException {
      // @formatter:off
      testClasses = new ConstructorTestClasses(classSchemaRule,
          config(
              "propertyWordDelimiters", "_",
              "includeConstructors", true,
              "includeRequiredPropertiesConstructor", true));
      // @formatter:on
    }

    @Test
    public void testGeneratesConstructorWithAllProperties() throws Exception {
      assertHasModifier(JMod.PUBLIC, getAllPropertiesConstructor(testClasses.typeWithRequired).getModifiers(), "public");
    }

    @Test
    public void testGeneratesCosntructorWithAllPropertiesArrayStyle() throws Exception {
      assertHasModifier(JMod.PUBLIC, getAllPropertiesConstructor(testClasses.typeWithRequiredArray).getModifiers(), "public");
    }

    @Test
    public void testCreatesPublicNoArgsConstructor() throws Exception {
      Constructor<?> constructor = testClasses.typeWithRequired.getConstructor();

      assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
    }

    @Test
    public void testCreatesConstructorWithRequiredParams() throws Exception {
      Constructor<?> constructor = getAllPropertiesConstructor(testClasses.typeWithRequired);

      assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
    }

    @Test
    public void testCreatesConstructorWithRequiredParamsArrayStyle() throws Exception {
      Constructor<?> constructor = getAllPropertiesConstructor(testClasses.typeWithRequiredArray);

      assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
    }

    @Test
    public void testRequiredFieldsConstructorAssignsFields() throws Exception {
      Object instance = getInstance(testClasses.typeWithRequired, "type", 5, true);

      assertEquals("type", getValue(instance, "getType"));
      assertEquals(5, getValue(instance, "getId"));
      assertEquals(true, getValue(instance, "getHasTickets"));
    }

    @Test
    public void testAllFieldsConstructorAssignsFields() throws Exception {
      Object instance = getInstance(testClasses.typeWithRequired, "type", 5, true, "provider", "startTime");

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
  public static class IncludeCopyConstructorsIT {

    @ClassRule
    public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    private static ConstructorTestClasses testClasses = null;

    @BeforeClass
    public static void generateAndCompileConstructorClasses() throws ClassNotFoundException {
      // @formatter:off
      testClasses = new ConstructorTestClasses(classSchemaRule,
          config("propertyWordDelimiters", "_",
              "includeConstructors", true,
              "includeCopyConstructor", true));
      // @formatter:on
    }

    @Test
    public void testGeneratesConstructorWithAllProperties() throws Exception {
      assertHasModifier(JMod.PUBLIC, getAllPropertiesConstructor(testClasses.typeWithRequired).getModifiers(), "public");
    }

    @Test
    public void testGeneratesCosntructorWithAllPropertiesArrayStyle() throws Exception {
      assertHasModifier(JMod.PUBLIC, getAllPropertiesConstructor(testClasses.typeWithRequiredArray).getModifiers(), "public");
    }

    @Test
    public void testCreatesPublicNoArgsConstructor() throws Exception {
      Constructor<?> constructor = testClasses.typeWithRequired.getConstructor();

      assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
    }

    @Test
    public void testCreatesConstructorWithCopyParams() throws Exception {
      Constructor<?> constructor = testClasses.typeWithRequired.getConstructor(testClasses.typeWithRequired);

      assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
    }

    @Test
    public void testCopyConstructorAssignsFields() throws Exception {
      Object instance = getInstance(testClasses.typeWithRequired, "type", 5, true, "provider", "startTime");
      Object copyInstance = getInstance(testClasses.typeWithRequired, instance);

      assertEquals("type", getValue(copyInstance, "getType"));
      assertEquals(5, getValue(copyInstance, "getId"));
      assertEquals(true, getValue(copyInstance, "getHasTickets"));
      assertEquals("provider", getValue(copyInstance, "getProvider"));
      assertEquals("startTime", getValue(copyInstance, "getStarttime"));
    }

    @Test
    public void testAllFieldsConstructorAssignsFields() throws Exception {
      Object instance = getInstance(testClasses.typeWithRequired, "type", 5, true, "provider", "startTime");

      assertEquals("type", getValue(instance, "getType"));
      assertEquals(5, getValue(instance, "getId"));
      assertEquals(true, getValue(instance, "getHasTickets"));
      assertEquals("provider", getValue(instance, "getProvider"));
      assertEquals("startTime", getValue(instance, "getStarttime"));
    }
  }

}
