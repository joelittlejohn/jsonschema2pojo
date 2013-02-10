/**
 * Copyright © 2010-2013 Nokia
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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

public class ArrayIT {

    private static Class<?> classWithArrayProperties;

    @BeforeClass
    public static void generateAndCompileClass() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/array/typeWithArrayProperties.json", "com.example");

        classWithArrayProperties = resultsClassLoader.loadClass("com.example.TypeWithArrayProperties");

    }

    @Test
    public void nonUniqueArraysAreLists() throws NoSuchMethodException {

        Method getterMethod = classWithArrayProperties.getMethod("getNonUniqueArray");

        assertThat(getterMethod.getReturnType().getName(), is(List.class.getName()));
        assertThat(getterMethod.getGenericReturnType(), is(instanceOf(ParameterizedType.class)));

        Type genericType = ((ParameterizedType) getterMethod.getGenericReturnType()).getActualTypeArguments()[0];
        assertThat(genericType, is(instanceOf(Class.class)));
        assertThat(((Class<?>) genericType).getName(), is(Integer.class.getName()));

    }

    @Test
    public void uniqueArraysAreSets() throws NoSuchMethodException {

        Method getterMethod = classWithArrayProperties.getMethod("getUniqueArray");

        assertThat(getterMethod.getReturnType().getName(), is(Set.class.getName()));
        assertThat(getterMethod.getGenericReturnType(), is(instanceOf(ParameterizedType.class)));

        Type genericType = ((ParameterizedType) getterMethod.getGenericReturnType()).getActualTypeArguments()[0];
        assertThat(genericType, is(instanceOf(Class.class)));
        assertThat(((Class<?>) genericType).getName(), is(Boolean.class.getName()));

    }

    @Test
    public void arraysAreNonUniqueByDefault() throws NoSuchMethodException {

        Method getterMethod = classWithArrayProperties.getMethod("getNonUniqueArrayByDefault");

        assertThat(getterMethod.getReturnType().getName(), is(List.class.getName()));

    }

    @Test
    public void arraysCanHaveComplexTypes() throws NoSuchMethodException {

        Method getterMethod = classWithArrayProperties.getMethod("getComplexTypesArray");

        assertThat(getterMethod.getGenericReturnType(), is(instanceOf(ParameterizedType.class)));

        Type genericType = ((ParameterizedType) getterMethod.getGenericReturnType()).getActualTypeArguments()[0];
        assertThat(genericType, is(instanceOf(Class.class)));
        assertThat(((Class<?>) genericType).getName(), is("com.example.ComplexTypesArray"));

    }

    @Test
    public void arrayItemTypeIsObjectByDefault() throws NoSuchMethodException {

        Method getterMethod = classWithArrayProperties.getMethod("getDefaultTypesArray");

        assertThat(getterMethod.getGenericReturnType(), is(instanceOf(ParameterizedType.class)));

        Type genericType = ((ParameterizedType) getterMethod.getGenericReturnType()).getActualTypeArguments()[0];
        assertThat(genericType, is(instanceOf(Class.class)));
        assertThat(((Class<?>) genericType).getName(), is(Object.class.getName()));

    }

    @Test
    public void arraysCanBeMultiDimensional() throws NoSuchMethodException {

        Method getterMethod = classWithArrayProperties.getMethod("getMultiDimensionalArray");

        // assert List
        assertThat(getterMethod.getReturnType().getName(), is(List.class.getName()));
        assertThat(getterMethod.getGenericReturnType(), is(instanceOf(ParameterizedType.class)));

        // assert List<List>
        Type genericType = ((ParameterizedType) getterMethod.getGenericReturnType()).getActualTypeArguments()[0];
        assertThat(genericType, is(instanceOf(ParameterizedType.class)));
        assertThat(((Class<?>) ((ParameterizedType) genericType).getRawType()).getName(), is(List.class.getName()));

        // assert List<List<Object>>
        Type itemsType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
        assertThat(itemsType, is(instanceOf(Class.class)));
        assertThat(((Class<?>) itemsType).getName(), is(Object.class.getName()));

    }

    @Test
    public void arrayItemTypeIsSingularFormOfPropertyName() throws NoSuchMethodException {

        Method getterMethod = classWithArrayProperties.getMethod("getThings");

        // assert List<Thing>
        Class<?> genericType = (Class<?>) ((ParameterizedType) getterMethod.getGenericReturnType()).getActualTypeArguments()[0];
        assertThat(genericType.getName(), is("com.example.Thing"));

    }

    @Test
    public void arrayItemTypeIsSingularFormOfPropertyNameWhenNameEndsInIES() throws NoSuchMethodException {

        Method getterMethod = classWithArrayProperties.getMethod("getProperties");

        // assert List<Property>
        Class<?> genericType = (Class<?>) ((ParameterizedType) getterMethod.getGenericReturnType()).getActualTypeArguments()[0];
        assertThat(genericType.getName(), is("com.example.Property"));

    }

    /**
     * @see <a
     *      href="http://code.google.com/p/jsonschema2pojo/issues/detail?id=76">issue
     *      76</a>
     */
    @Test
    public void propertiesThatReferenceAnArraySchemaAlwaysHaveCorrectCollectionType() throws NoSuchMethodException, ClassNotFoundException {

        Method array1GetterMethod = classWithArrayProperties.getMethod("getRefToArray1");

        // assert List<RootArrayItem>
        Class<?> array1GenericType = (Class<?>) ((ParameterizedType) array1GetterMethod.getGenericReturnType()).getActualTypeArguments()[0];
        assertThat(array1GenericType.getName(), is("com.example.RootArrayItem"));

        Method array2GetterMethod = classWithArrayProperties.getMethod("getRefToArray2");

        // assert List<RootArrayItem>
        Class<?> array2GenericType = (Class<?>) ((ParameterizedType) array2GetterMethod.getGenericReturnType()).getActualTypeArguments()[0];
        assertThat(array2GenericType.getName(), is("com.example.RootArrayItem"));
    }

}
