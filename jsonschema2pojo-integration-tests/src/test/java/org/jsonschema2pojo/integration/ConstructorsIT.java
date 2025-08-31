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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.helger.jcodemodel.JMod;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

// Using @TestInstance(TestInstance.Lifecycle.PER_CLASS) and 'public static Jsonschema2PojoRule classSchemaRule' on top level
// as Java8 does not support static methods in nested classes
public class ConstructorsIT {

  @RegisterExtension
  public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

  public static void assertHasModifier(int modifier, int modifiers, String modifierName) {
    assertThat(
            "Expected the bit " + modifierName + " (" + modifier + ")" + " to be set but got: " + modifiers,
            modifier & modifiers,
            is(equalTo(modifier)));
  }

  public static void assertHasOnlyDefaultConstructor(Class<?> cls) {
    Constructor<?>[] constructors = cls.getConstructors();

    assertThat(constructors.length, is(equalTo(1)));

    assertThat(
            "Expected " + cls + " to only have the default, no-args constructor",
            constructors[0].getParameterTypes().length,
            is(equalTo(0)));
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

  private static class ConstructorTestClasses {

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
  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class DefaultIncludeConstructorsIT {

    private ConstructorTestClasses testClasses;

    @BeforeAll
    public void generateAndCompileConstructorClasses() throws ClassNotFoundException {
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
    public void testGeneratesConstructorWithAllPropertiesArrayStyle() throws Exception {
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
  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class RequiredOnlyIT {

    private ConstructorTestClasses testClasses = null;

    @BeforeAll
    public void generateAndCompileConstructorClasses() throws ClassNotFoundException {
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

      assertThat(getValue(instance, "getType"), is(equalTo("type")));
      assertThat(getValue(instance, "getId"), is(equalTo(5)));
      assertThat(getValue(instance, "getHasTickets"), is(true));
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
  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class IncludeRequiredConstructorsIT {

    private ConstructorTestClasses testClasses = null;

    @BeforeAll
    public void generateAndCompileConstructorClasses() throws ClassNotFoundException {
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
    public void testGeneratesConstructorWithAllPropertiesArrayStyle() throws Exception {
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

      assertThat(getValue(instance, "getType"), is(equalTo("type")));
      assertThat(getValue(instance, "getId"), is(equalTo(5)));
      assertThat(getValue(instance, "getHasTickets"), is(equalTo(true)));
    }

    @Test
    public void testAllFieldsConstructorAssignsFields() throws Exception {
      Object instance = getInstance(testClasses.typeWithRequired, "type", 5, true, "provider", "startTime");

      assertThat(getValue(instance, "getType"), is(equalTo("type")));
      assertThat(getValue(instance, "getId"), is(equalTo(5)));
      assertThat(getValue(instance, "getHasTickets"), is(true));
      assertThat(getValue(instance, "getProvider"), is(equalTo("provider")));
      assertThat(getValue(instance, "getStarttime"), is(equalTo("startTime")));
    }

    /**
     * Test that duplicate constructors are not generated (compile time error is not thrown) when:
     * <ul>
     *     <li>all properties are required</li>
     *     <li>{@code includeAllPropertiesConstructor} configuration property is {@code true}</li>
     *     <li>{@code includeRequiredPropertiesConstructor} configuration property is {@code true}</li>
     */
    @Test
    public void testGeneratesConstructorWithAllPropertiesRequired() throws Exception {
      classSchemaRule.generate(
          "/schema/constructors/allPropertiesRequiredConstructor.json",
          "com.example",
          config("includeConstructors", true, "includeAllPropertiesConstructor", true, "includeRequiredPropertiesConstructor", true));
      Class<?> type = classSchemaRule.compile().loadClass("com.example.AllPropertiesRequiredConstructor");
      assertHasModifier(JMod.PUBLIC, getAllPropertiesConstructor(type).getModifiers(), "public");
    }

  }

  /**
   * Tests what happens when includeConstructors is set to true
   */
  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class IncludeCopyConstructorsIT {

    private ConstructorTestClasses testClasses = null;

    @BeforeAll
    public void generateAndCompileConstructorClasses() throws ClassNotFoundException {
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
    public void testGeneratesConstructorWithAllPropertiesArrayStyle() throws Exception {
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

      assertThat(getValue(copyInstance, "getType"), is(equalTo("type")));
      assertThat(getValue(copyInstance, "getId"), is(equalTo(5)));
      assertThat(getValue(copyInstance, "getHasTickets"), is(true));
      assertThat(getValue(copyInstance, "getProvider"), is(equalTo("provider")));
      assertThat(getValue(copyInstance, "getStarttime"), is(equalTo("startTime")));
    }

    @Test
    public void testAllFieldsConstructorAssignsFields() throws Exception {
      Object instance = getInstance(testClasses.typeWithRequired, "type", 5, true, "provider", "startTime");

      assertThat(getValue(instance, "getType"), is(equalTo("type")));
      assertThat(getValue(instance, "getId"), is(equalTo(5)));
      assertThat(getValue(instance, "getHasTickets"), is(true));
      assertThat(getValue(instance, "getProvider"), is(equalTo("provider")));
      assertThat(getValue(instance, "getStarttime"), is(equalTo("startTime")));
    }
  }

}
