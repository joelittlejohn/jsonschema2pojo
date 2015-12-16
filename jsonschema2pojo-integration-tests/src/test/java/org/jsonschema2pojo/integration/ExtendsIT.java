/**
 * Copyright © 2010-2014 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.jsonschema2pojo.integration;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

public class ExtendsIT {
    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    @SuppressWarnings("rawtypes")
    public void extendsWithEmbeddedSchemaGeneratesParentType() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/extendsEmbeddedSchema.json", "com.example");

        Class subtype = resultsClassLoader.loadClass("com.example.ExtendsEmbeddedSchema");
        Class supertype = resultsClassLoader.loadClass("com.example.ExtendsEmbeddedSchemaParent");

        assertThat(subtype.getSuperclass(), is(equalTo(supertype)));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void extendsWithRefToAnotherSchema() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/subtypeOfA.json", "com.example");

        Class subtype = resultsClassLoader.loadClass("com.example.SubtypeOfA");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfAParent");

        assertThat(subtype.getSuperclass(), is(equalTo(supertype)));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void extendsWithRefToAnotherSchemaThatIsAlreadyASubtype() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/subtypeOfSubtypeOfA.json", "com.example");

        Class subtype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfA");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfAParent");

        assertThat(subtype.getSuperclass(), is(equalTo(supertype)));

    }

    @Test(expected = ClassNotFoundException.class)
    public void extendsStringCausesNoNewTypeToBeGenerated() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/extendsString.json", "com.example");
        resultsClassLoader.loadClass("com.example.ExtendsString");

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void extendsEquals() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/subtypeOfSubtypeOfA.json", "com.example2");
        
        Class generatedType = resultsClassLoader.loadClass("com.example2.SubtypeOfSubtypeOfA");
        Object instance = generatedType.newInstance();
        Object instance2 = generatedType.newInstance();

        new PropertyDescriptor("parent", generatedType).getWriteMethod().invoke(instance, "1");
        new PropertyDescriptor("child", generatedType).getWriteMethod().invoke(instance, "2");        
        
        new PropertyDescriptor("parent", generatedType).getWriteMethod().invoke(instance2, "not-equal");
        new PropertyDescriptor("child", generatedType).getWriteMethod().invoke(instance2, "2");
        
        assertFalse(instance.equals(instance2));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void extendsSchemaWithinDefinitions() throws Exception {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/extendsSchemaWithinDefinitions.json", "com.example");

        Class subtype = resultsClassLoader.loadClass("com.example.Child");
        assertNotNull("no propertyOfChild field", subtype.getDeclaredField("propertyOfChild"));

        Class supertype = resultsClassLoader.loadClass("com.example.ChildParent");
        assertNotNull("no propertyOfParent field", supertype.getDeclaredField("propertyOfParent"));

        assertThat(subtype.getSuperclass(), is(equalTo(supertype)));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void extendsBuilderMethods() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/subtypeOfSubtypeOfA.json", "com.example", config("generateBuilders", true));

        Class subtype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfA");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfAParent");

        Method builderMethod = supertype.getDeclaredMethod("withParent", String.class);
        assertNotNull("no withParent method", builderMethod);
        assertThat(builderMethod.getReturnType(), is(equalTo(supertype)));

        Method builderMethodOverride = subtype.getDeclaredMethod("withParent", String.class);
        assertNotNull("no withParent method", builderMethodOverride);
        assertThat(builderMethodOverride.getReturnType(), is(equalTo(subtype)));
    }
}
