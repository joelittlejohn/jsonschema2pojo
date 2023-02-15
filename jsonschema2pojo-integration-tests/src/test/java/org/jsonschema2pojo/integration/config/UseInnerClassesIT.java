/**
 * Copyright © 2010-2020 Marco Herrn
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

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;

import java.io.IOException;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;


@SuppressWarnings("rawtypes")
public class UseInnerClassesIT {

  @Rule
  public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();


  private boolean canLoad(ClassLoader classLoader, final String className) {
    try {
      classLoader.loadClass(className);
      return true;
    } catch (ClassNotFoundException ex) {
      return false;
    }
  }

  /**
   * This test asserts that all classes are generated as top-level classes by default
   */
  @Test
  public void noInnerClassesByDefault() throws IOException {
    ClassLoader classLoader = schemaRule.generateAndCompile("/schema.useInnerClasses",
                                                            "com.example",
                                                            config("useInnerClasses", Boolean.FALSE));

    Assert.assertTrue(canLoad(classLoader, "com.example.BaseType1"));
    Assert.assertTrue(canLoad(classLoader, "com.example.AmbiguousType"));
    Assert.assertTrue(canLoad(classLoader, "com.example.One"));

    Assert.assertFalse(canLoad(classLoader, "com.example.BaseType1$AmbiguousType"));
    Assert.assertFalse(canLoad(classLoader, "com.example.BaseType1$One"));

    Assert.assertTrue(canLoad(classLoader, "com.example.BaseType2"));
    Assert.assertTrue(canLoad(classLoader, "com.example.AmbiguousType__1"));
    Assert.assertTrue(canLoad(classLoader, "com.example.Two"));

    Assert.assertFalse(canLoad(classLoader, "com.example.BaseType2$AmbiguousType"));
    Assert.assertFalse(canLoad(classLoader, "com.example.BaseType2$Two"));

  }

  /**
   * This test asserts that only one top level class per schema URI is generated.
   */
  @Test
  public void useInnerClasses() throws IOException {
    ClassLoader classLoader = schemaRule.generateAndCompile("/schema.useInnerClasses",
                                                            "com.example",
                                                            config("useInnerClasses", Boolean.TRUE));

    Assert.assertTrue(canLoad(classLoader, "com.example.BaseType1"));
    Assert.assertFalse(canLoad(classLoader, "com.example.AmbiguousType"));
    Assert.assertFalse(canLoad(classLoader, "com.example.One"));

    Assert.assertTrue(canLoad(classLoader, "com.example.BaseType1$AmbiguousType"));
    Assert.assertTrue(canLoad(classLoader, "com.example.BaseType1$One"));

    Assert.assertTrue(canLoad(classLoader, "com.example.BaseType2"));
    Assert.assertFalse(canLoad(classLoader, "com.example.AmbiguousType__1"));
    Assert.assertFalse(canLoad(classLoader, "com.example.Two"));

    Assert.assertTrue(canLoad(classLoader, "com.example.BaseType2$AmbiguousType"));
    Assert.assertTrue(canLoad(classLoader, "com.example.BaseType2$Two"));
  }

}
