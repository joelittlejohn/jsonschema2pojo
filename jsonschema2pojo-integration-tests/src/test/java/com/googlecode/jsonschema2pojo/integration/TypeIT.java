/**
 * Copyright © 2010-2011 Nokia
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
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

public class TypeIT {

    private static Class<?> classWithManyTypes;

    @BeforeClass
    public static void generateAndCompileClass() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/type/types.json", "com.example", true);

        classWithManyTypes = resultsClassLoader.loadClass("com.example.Types");

    }

    @Test
    public void booleanTypeProducesBooleans() throws NoSuchMethodException {

        Method getterMethod = classWithManyTypes.getMethod("isBooleanProperty");

        assertThat(getterMethod.getReturnType().getName(), is("boolean"));

    }

    @Test
    public void stringTypeProducesStrings() throws NoSuchMethodException {

        Method getterMethod = classWithManyTypes.getMethod("getStringProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.String"));

    }

    @Test
    public void integerTypeProducesInts() throws NoSuchMethodException {

        Method getterMethod = classWithManyTypes.getMethod("getIntegerProperty");

        assertThat(getterMethod.getReturnType().getName(), is("int"));

    }

    @Test
    public void numberTypeProducesDouble() throws NoSuchMethodException {

        Method getterMethod = classWithManyTypes.getMethod("getNumberProperty");

        assertThat(getterMethod.getReturnType().getName(), is("double"));

    }

    @Test
    public void arrayTypeProducesCollection() throws NoSuchMethodException, ClassNotFoundException {

        Method getterMethod = classWithManyTypes.getMethod("getArrayProperty");

        assertThat(Collection.class.isAssignableFrom(getterMethod.getReturnType()), is(true));

    }

    @Test
    public void nullTypeProducesObject() throws NoSuchMethodException {

        Method getterMethod = classWithManyTypes.getMethod("getNullProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Object"));

    }

    @Test
    public void anyTypeProducesObject() throws NoSuchMethodException {

        Method getterMethod = classWithManyTypes.getMethod("getAnyProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Object"));

    }

    @Test
    public void defaultTypeProducesObject() throws NoSuchMethodException {

        Method getterMethod = classWithManyTypes.getMethod("getDefaultProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Object"));

    }

}
