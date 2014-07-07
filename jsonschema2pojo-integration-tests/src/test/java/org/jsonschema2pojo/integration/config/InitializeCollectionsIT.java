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

package org.jsonschema2pojo.integration.config;

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.Assert.*;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

public class InitializeCollectionsIT {

    @SuppressWarnings("rawtypes")
    private Class generatedType;
    private Object instance;
    
    @Before
    public void setUp() throws Exception {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/initializeCollectionProperties.json", "com.example", config("initializeCollections", false));

        generatedType = resultsClassLoader.loadClass("com.example.InitializeCollectionProperties");
        instance = generatedType.newInstance();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void defaultValueForCollectionsIsEmptyCollection() throws ClassNotFoundException, IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/initializeCollectionProperties.json", "com.example");

        generatedType = resultsClassLoader.loadClass("com.example.InitializeCollectionProperties");
        instance = generatedType.newInstance();

        Method getter = generatedType.getMethod("getList");

        assertThat(getter.invoke(instance), notNullValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void defaultValueForListIsNullWithProperty() throws ClassNotFoundException, IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {

        Method getter = generatedType.getMethod("getList");

        assertThat(getter.invoke(instance), nullValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void defaultValueForSetIsNullWithProperty() throws ClassNotFoundException, IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {

        Method getter = generatedType.getMethod("getSet");

        assertThat(getter.invoke(instance), nullValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void defaultValueForListWithValuesIsNotNullWithProperty() throws ClassNotFoundException, IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {

        Method getter = generatedType.getMethod("getListWithValues");

        assertThat(getter.invoke(instance), notNullValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void defaultValueForSetWithValuesIsNotNullWithProperty() throws ClassNotFoundException, IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {

        Method getter = generatedType.getMethod("getSetWithValues");

        assertThat(getter.invoke(instance), notNullValue());
    }

}
