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

import static org.hamcrest.Matchers.not;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matcher;
import org.jsonschema2pojo.integration.util.FileSearchMatcher;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

@SuppressWarnings("rawtypes")
public class UseInnerClassBuildersIT {

  @Rule
  public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

  private static Matcher<File> containsText(String searchText) {
    return new FileSearchMatcher(searchText);
  }

  /**
   * This test asserts that no methods containing 'with' appear in the generated code using the default configuration
   */
  @Test
  public void noBuilderMethodsByDefault() {
    File outputDirectory = schemaRule.generate("/schema.useInnerClassBuilders/child.json", "com.example", config());

    assertThat(outputDirectory, not(containsText("with")));
  }

  /**
   * This method confirms that if you choose to generate builders, but don't indicate that useInnerBuilders is true, they will be generated using the
   * chaining setters instead of the inner classes
   */
  @Test(expected = ClassNotFoundException.class)
  public void defaultBuilderIsChainedSetters() throws ClassNotFoundException {
    ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema.useInnerClassBuilders/child.json", "com.example",
        config("generateBuilders", true));

    Class<?> childClass = resultsClassLoader.loadClass("com.example.Child");
    boolean containsWithMethod = Stream.of(childClass.getMethods())
        .map(Method::getName)
        .anyMatch(methodName -> StringUtils.contains(methodName, "with"));

    assertTrue("Generated class missing any builders at all", containsWithMethod);

    resultsClassLoader.loadClass("com.example.Child.ChildBuilder");
  }

  /**
   * This method confirms that if you choose to use inner class builders then the chaining setters will be removed from the generated class
   */
  @Test
  public void innerBuildersRemoveChainedSetters() throws ClassNotFoundException {
    ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema.useInnerClassBuilders/child.json", "com.example",
        config("generateBuilders", true, "useInnerClassBuilders", true));

    Class<?> childClass = resultsClassLoader.loadClass("com.example.Child");
    boolean containsWithMethod = Stream.of(childClass.getMethods())
        .map(Method::getName)
        .anyMatch(methodName -> StringUtils.contains(methodName, "with"));

    assertFalse("Generated contains unexpected builders", containsWithMethod);

    assertNotNull(resultsClassLoader.loadClass("com.example.Child$ChildBuilder"));
  }

  /**
   * This methods confirms that the builders can be constructed using the empty constructor and possess a 'build' method that will return a non-null
   * object
   */
  @Test
  public void innerBuildersInvokeBuild()
      throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
    ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema.useInnerClassBuilders/child.json", "com.example",
        config("generateBuilders", true, "useInnerClassBuilders", true));

    Class<?> builderClass = resultsClassLoader.loadClass("com.example.Child$ChildBuilder");
    Method buildMethod = builderClass.getMethod("build");

    Object builder = builderClass.newInstance();
    assertNotNull(builder);

    assertNotNull(buildMethod.invoke(builder));
  }

  /**
   * This method walks through invoking the various incremental 'with' methods to build out an entire object, then invokes build on the constructed
   * object and confirms all values on the object match those provided to the with methods
   */
  @Test
  public void innerBuildersBuildObjectIncrementally()
      throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
    ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema.useInnerClassBuilders/child.json", "com.example",
        config("generateBuilders", true, "useInnerClassBuilders", true));

    Class<?> builderClass = resultsClassLoader.loadClass("com.example.Child$ChildBuilder");
    Method buildMethod = builderClass.getMethod("build");
    Method withChildProperty = builderClass.getMethod("withChildProperty", Integer.class);
    Method withParentProperty = builderClass.getMethod("withParentProperty", String.class);
    Method withSharedProperty = builderClass.getMethod("withSharedProperty", String.class);

    int childProperty = 1;
    String parentProperty = "parentProperty";
    String sharedProperty = "sharedProperty";

    Object builder = builderClass.newInstance();
    withChildProperty.invoke(builder, childProperty);
    withParentProperty.invoke(builder, parentProperty);
    withSharedProperty.invoke(builder, sharedProperty);
    Object childObject = buildMethod.invoke(builder);

    Class<?> childClass = resultsClassLoader.loadClass("com.example.Child");
    Method getChildProperty = childClass.getMethod("getChildProperty");
    Method getParentProperty = childClass.getMethod("getParentProperty");
    Method getSharedProperty = childClass.getMethod("getSharedProperty");

    assertEquals(childProperty, getChildProperty.invoke(childObject));
    assertEquals(parentProperty, getParentProperty.invoke(childObject));
    assertEquals(sharedProperty, getSharedProperty.invoke(childObject));
  }

  /**
   * This method confirms that by default the only constructor available to a builder is the empty argument constructor
   */
  @Test
  public void innerBuilderExtraConstructorsRequireConfig() throws ClassNotFoundException {
    ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema.useInnerClassBuilders/child.json", "com.example",
        config("generateBuilders", true, "useInnerClassBuilders", true));

    Class<?> builderClass = resultsClassLoader.loadClass("com.example.Child$ChildBuilder");
    assertEquals(1, builderClass.getConstructors().length);

    Constructor<?> constructor = builderClass.getConstructors()[0];
    assertEquals(0, constructor.getParameterCount());
  }

  /**
   * This method confirms that if constructors are enabled then a builder constructor which takes all properties will be created
   */
  @Test
  public void innerBuilderWithAllPropertyConstructor()
      throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
    ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema.useInnerClassBuilders/child.json", "com.example",
        config("generateBuilders", true, "useInnerClassBuilders", true, "includeConstructors", true));

    Class<?> builderClass = resultsClassLoader.loadClass("com.example.Child$ChildBuilder");
    Constructor<?> constructor = builderClass.getConstructor(Integer.class, String.class, String.class);
    Method buildMethod = builderClass.getMethod("build");

    int childProperty = 1;
    String parentProperty = "parentProperty";
    String sharedProperty = "sharedProperty";

    Object builder = constructor.newInstance(childProperty, sharedProperty, parentProperty);
    Object childObject = buildMethod.invoke(builder);

    Class<?> childClass = resultsClassLoader.loadClass("com.example.Child");
    Method getChildProperty = childClass.getMethod("getChildProperty");
    Method getParentProperty = childClass.getMethod("getParentProperty");
    Method getSharedProperty = childClass.getMethod("getSharedProperty");

    assertEquals(childProperty, getChildProperty.invoke(childObject));
    assertEquals(parentProperty, getParentProperty.invoke(childObject));
    assertEquals(sharedProperty, getSharedProperty.invoke(childObject));
  }

  /**
   * This method confirms that if constructors are enabled and constructorsRequiredPropertiesOnly is set to true then a only a builder constructor
   * with the required properties will be created
   */
  @Test
  public void innerBuilderWithRequiredPropertyConstructor()
      throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
    ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema.useInnerClassBuilders/child.json", "com.example",
        config("generateBuilders", true, "useInnerClassBuilders", true, "includeConstructors", true, "constructorsRequiredPropertiesOnly", true));

    Class<?> builderClass = resultsClassLoader.loadClass("com.example.Child$ChildBuilder");
    Constructor<?> constructor = builderClass.getConstructor(String.class);
    Method buildMethod = builderClass.getMethod("build");
    Method withChildProperty = builderClass.getMethod("withChildProperty", Integer.class);
    Method withSharedProperty = builderClass.getMethod("withSharedProperty", String.class);

    int childProperty = 1;
    String parentProperty = "parentProperty";
    String sharedProperty = "sharedProperty";

    Object builder = constructor.newInstance(parentProperty);
    withChildProperty.invoke(builder, childProperty);
    withSharedProperty.invoke(builder, sharedProperty);
    Object childObject = buildMethod.invoke(builder);

    Class<?> childClass = resultsClassLoader.loadClass("com.example.Child");
    Method getChildProperty = childClass.getMethod("getChildProperty");
    Method getParentProperty = childClass.getMethod("getParentProperty");
    Method getSharedProperty = childClass.getMethod("getSharedProperty");

    assertEquals(childProperty, getChildProperty.invoke(childObject));
    assertEquals(parentProperty, getParentProperty.invoke(childObject));
    assertEquals(sharedProperty, getSharedProperty.invoke(childObject));
  }
}
