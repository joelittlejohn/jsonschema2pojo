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

import org.jsonschema2pojo.integration.util.Jsonschema2PojoTestBase;
import org.junit.jupiter.api.Test;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.jupiter.api.Assertions.*;

public class ExtendsIT  extends Jsonschema2PojoTestBase {
    @Test
    @SuppressWarnings("rawtypes")
    public void extendsWithEmbeddedSchemaGeneratesParentType() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/extends/extendsEmbeddedSchema.json", "com.example");

        Class subtype = resultsClassLoader.loadClass("com.example.ExtendsEmbeddedSchema");
        Class supertype = resultsClassLoader.loadClass("com.example.ExtendsEmbeddedSchemaParent");

        assertThat(subtype.getSuperclass(), is(equalTo(supertype)));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void extendsWithRefToAnotherSchema() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/extends/subtypeOfA.json", "com.example");

        Class subtype = resultsClassLoader.loadClass("com.example.SubtypeOfA");
        Class supertype = resultsClassLoader.loadClass("com.example.A");

        assertThat(subtype.getSuperclass(), is(equalTo(supertype)));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void extendsWithRefToAnotherSchemaThatIsAlreadyASubtype() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/extends/subtypeOfSubtypeOfA.json", "com.example");

        Class subtype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfA");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfA");

        assertThat(subtype.getSuperclass(), is(equalTo(supertype)));

    }

    @Test
    public void extendsStringCausesNoNewTypeToBeGenerated() {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/extends/extendsString.json", "com.example");
        assertThrows(ClassNotFoundException.class, () -> resultsClassLoader.loadClass("com.example.ExtendsString"));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void extendsEquals() throws Exception {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/extends/subtypeOfSubtypeOfA.json", "com.example2");

        Class generatedType = resultsClassLoader.loadClass("com.example2.SubtypeOfSubtypeOfA");
        Object instance = generatedType.newInstance();
        Object instance2 = generatedType.newInstance();

        new PropertyDescriptor("parent", generatedType).getWriteMethod().invoke(instance, "1");
        new PropertyDescriptor("child", generatedType).getWriteMethod().invoke(instance, "2");

        new PropertyDescriptor("parent", generatedType).getWriteMethod().invoke(instance2, "not-equal");
        new PropertyDescriptor("child", generatedType).getWriteMethod().invoke(instance2, "2");

        assertNotEquals(instance, instance2);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void extendsSchemaWithinDefinitions() throws Exception {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/extends/extendsSchemaWithinDefinitions.json", "com.example");

        Class subtype = resultsClassLoader.loadClass("com.example.Child");
        assertNotNull(subtype.getDeclaredField("propertyOfChild"), "no propertyOfChild field");

        Class supertype = resultsClassLoader.loadClass("com.example.Parent");
        assertNotNull(supertype.getDeclaredField("propertyOfParent"), "no propertyOfParent field");

        assertThat(subtype.getSuperclass(), is(equalTo(supertype)));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void constructorHasParentsProperties() throws Exception {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/extends/subtypeOfB.json", "com.example", config("includeConstructors", true));

        Class type = resultsClassLoader.loadClass("com.example.SubtypeOfB");
        Class supertype = resultsClassLoader.loadClass("com.example.B");

        assertThat(type.getSuperclass(), is(equalTo(supertype)));

        assertNotNull(supertype.getConstructor(String.class), "Parent constructor is missing");
        assertNotNull(type.getConstructor(String.class, String.class), "Constructor is missing");

        Object typeInstance = type.getConstructor(String.class, String.class).newInstance("String1", "String2");

        Field chieldField = type.getDeclaredField("childProperty");
        chieldField.setAccessible(true);
        String childProp = (String) chieldField.get(typeInstance);
        Field parentField = supertype.getDeclaredField("parentProperty");
        parentField.setAccessible(true);
        String parentProp = (String) parentField.get(typeInstance);

        assertThat(childProp, is(equalTo("String1")));
        assertThat(parentProp, is(equalTo("String2")));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void constructorHasParentsParentProperties() throws Exception {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/extends/subtypeOfSubtypeOfB.json", "com.example", config("includeConstructors", true));

        Class type = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfB");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfB");
        Class superSupertype = resultsClassLoader.loadClass("com.example.B");

        assertThat(type.getSuperclass(), is(equalTo(supertype)));

        assertNotNull(superSupertype.getDeclaredConstructor(String.class), "Parent Parent constructor is missing");
        assertNotNull(supertype.getDeclaredConstructor(String.class, String.class), "Parent Constructor is missing");
        assertNotNull(type.getDeclaredConstructor(String.class, String.class, String.class), "Constructor is missing");

        Object typeInstance = type.getConstructor(String.class, String.class, String.class).newInstance("String1", "String2", "String3");

        Field chieldChildField = type.getDeclaredField("childChildProperty");
        chieldChildField.setAccessible(true);
        String childChildProp = (String) chieldChildField.get(typeInstance);
        Field chieldField = supertype.getDeclaredField("childProperty");
        chieldField.setAccessible(true);
        String childProp = (String) chieldField.get(typeInstance);
        Field parentField = superSupertype.getDeclaredField("parentProperty");
        parentField.setAccessible(true);
        String parentProp = (String) parentField.get(typeInstance);

        assertThat(childChildProp, is(equalTo("String1")));
        assertThat(childProp, is(equalTo("String2")));
        assertThat(parentProp, is(equalTo("String3")));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void constructorHasParentsParentPropertiesInCorrectOrder() throws Exception {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/extends/subtypeOfSubtypeOfBDifferentType.json", "com.example", config("includeConstructors", true));

        Class type = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfBDifferentType");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfB");
        Class superSupertype = resultsClassLoader.loadClass("com.example.B");

        assertThat(type.getSuperclass(), is(equalTo(supertype)));

        assertNotNull(superSupertype.getDeclaredConstructor(String.class), "Parent Parent constructor is missing");
        assertNotNull(supertype.getDeclaredConstructor(String.class, String.class), "Parent Constructor is missing");
        assertNotNull(type.getDeclaredConstructor(Integer.class, String.class, String.class), "Constructor is missing");

        Object typeInstance = type.getConstructor(Integer.class, String.class, String.class).newInstance(5, "String2", "String3");

        Field chieldChildField = type.getDeclaredField("childChildProperty");
        chieldChildField.setAccessible(true);
        int childChildProp = (Integer) chieldChildField.get(typeInstance);
        Field chieldField = supertype.getDeclaredField("childProperty");
        chieldField.setAccessible(true);
        String childProp = (String) chieldField.get(typeInstance);
        Field parentField = superSupertype.getDeclaredField("parentProperty");
        parentField.setAccessible(true);
        String parentProp = (String) parentField.get(typeInstance);

        assertThat(childChildProp, is(equalTo(5)));
        assertThat(childProp, is(equalTo("String2")));
        assertThat(parentProp, is(equalTo("String3")));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void constructorDoesNotDuplicateArgsFromDuplicatedParentProperties() throws Exception {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/extends/subtypeOfSubtypeOfC.json", "com.example", config("includeConstructors", true));

        Class type = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfC");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfC");
        Class superSupertype = resultsClassLoader.loadClass("com.example.C");

        assertNotNull(superSupertype.getDeclaredConstructor(String.class, Integer.class), "Parent Parent constructor is missing");
        assertNotNull(supertype.getDeclaredConstructor(String.class, Boolean.class, Integer.class), "Parent Constructor is missing");
        assertNotNull(type.getDeclaredConstructor(String.class, Integer.class, Boolean.class, Integer.class), "Constructor is missing");

        Object typeInstance = type.getConstructor(String.class, Integer.class, Boolean.class, Integer.class).newInstance("String1", 5, true, 6);

        Field chieldChildField = type.getDeclaredField("duplicatedProp");
        chieldChildField.setAccessible(true);
        String childChildProp = (String) chieldChildField.get(typeInstance);
        Field chieldField = supertype.getDeclaredField("duplicatedProp");
        chieldField.setAccessible(true);
        String childProp = (String) chieldField.get(typeInstance);
        Field parentField = superSupertype.getDeclaredField("duplicatedProp");
        parentField.setAccessible(true);
        String parentProp = (String) parentField.get(typeInstance);

        assertThat(childChildProp, is(equalTo("String1")));
        assertThat(childProp, is(equalTo("String1")));
        assertThat(parentProp, is(equalTo("String1")));
    }


    @Test
    @SuppressWarnings("rawtypes")
    public void extendsBuilderMethods() throws Exception {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/extends/subtypeOfSubtypeOfA.json", "com.example", config("generateBuilders", true));

        Class subtype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfA");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfA");

        checkBuilderMethod(subtype, supertype, "withParent");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void builderMethodsOnChildWithProperties() throws Exception {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/extends/subtypeOfB.json", "com.example", config("generateBuilders", true));

        Class type = resultsClassLoader.loadClass("com.example.SubtypeOfB");
        Class supertype = resultsClassLoader.loadClass("com.example.B");

        checkBuilderMethod(type, supertype, "withParentProperty");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void builderMethodsOnChildWithNoProperties() throws Exception {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/extends/subtypeOfBWithNoProperties.json", "com.example", config("generateBuilders", true));

        Class type = resultsClassLoader.loadClass("com.example.SubtypeOfBWithNoProperties");
        Class supertype = resultsClassLoader.loadClass("com.example.B");

        checkBuilderMethod(type, supertype, "withParentProperty");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void checkBuilderMethod(Class type, Class supertype, String builderMethodName) throws Exception {
        assertThat(type.getSuperclass(), is(equalTo(supertype)));

        Method builderMethod = supertype.getDeclaredMethod(builderMethodName, String.class);
        assertNotNull(builderMethod, "Builder method not found on super type: " + builderMethodName);
        assertThat(builderMethod.getReturnType(), is(equalTo(supertype)));

        Method builderMethodOverride = type.getDeclaredMethod(builderMethodName, String.class);
        assertNotNull(builderMethodOverride, "Builder method not overridden on type: " + builderMethodName);
        assertThat(builderMethodOverride.getReturnType(), is(equalTo(type)));
    }

}
