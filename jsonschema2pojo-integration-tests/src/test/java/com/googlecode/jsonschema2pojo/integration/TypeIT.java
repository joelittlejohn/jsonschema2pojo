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

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.googlecode.jsonschema2pojo.Schema;

public class TypeIT {

    private static File generatedTypesDirectory;
    private static Class<?> classWithManyTypes;

    @BeforeClass
    public static void generateAndCompileClass() throws ClassNotFoundException {

        generatedTypesDirectory = generate("/schema/type/types.json", "com.example");
        classWithManyTypes = compile(generatedTypesDirectory).loadClass("com.example.Types");

    }

    @Before
    public void clearSchemaCache() {
        Schema.clearCache();
    }

    @Test
    public void booleanTypeProducesBooleans() throws NoSuchMethodException {

        Method getterMethod = classWithManyTypes.getMethod("getBooleanProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Boolean"));

    }

    @Test
    public void stringTypeProducesStrings() throws NoSuchMethodException {

        Method getterMethod = classWithManyTypes.getMethod("getStringProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.String"));

    }

    @Test
    public void integerTypeProducesInts() throws NoSuchMethodException {

        Method getterMethod = classWithManyTypes.getMethod("getIntegerProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Integer"));

    }

    @Test
    public void numberTypeProducesDouble() throws NoSuchMethodException {

        Method getterMethod = classWithManyTypes.getMethod("getNumberProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Double"));

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
    public void objectTypeProducesNewType() throws NoSuchMethodException {

        Method getterMethod = classWithManyTypes.getMethod("getObjectProperty");

        assertThat(getterMethod.getReturnType().getName(), is("com.example.ObjectProperty"));
        assertThat(getterMethod.getReturnType().getMethod("getProperty"), is(notNullValue()));

    }

    @Test
    public void defaultTypeProducesObject() throws NoSuchMethodException {

        Method getterMethod = classWithManyTypes.getMethod("getDefaultProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Object"));

    }

    @Test
    public void reusingTypeFromClasspathProducesNoNewType() throws NoSuchMethodException {

        Method getterMethod = classWithManyTypes.getMethod("getReusedClasspathType");

        assertThat(getterMethod.getReturnType().getName(), is("java.util.Locale"));
        assertThat(new File(generatedTypesDirectory, "java/util/Locale.java").exists(), is(false));

    }

    @Test
    public void reusingTypeFromGeneratedTypesProducesNoNewType() throws NoSuchMethodException {

        Method getterMethod = classWithManyTypes.getMethod("getReusedGeneratedType");

        assertThat(getterMethod.getReturnType().getName(), is("com.example.ObjectProperty"));
        assertThat(getterMethod.getReturnType().getMethod("getProperty"), is(notNullValue()));

    }

    @Test
    public void javaTypeSupportsPrimitiveTypes() throws NoSuchMethodException {

        Method getterMethod = classWithManyTypes.getMethod("getPrimitiveJavaType");

        assertThat(getterMethod.getReturnType().getName(), is("long"));

    }

    @Test
    public void useLongIntegersParameterCausesIntegersToBecomeLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        File generatedTypesDirectory = generate("/schema/type/integerAsLong.json", "com.example", config("useLongIntegers", true));
        Class<?> classWithLongProperty = compile(generatedTypesDirectory).loadClass("com.example.IntegerAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Long"));

    }

    @Test
    public void useLongIntegersParameterCausesPrimitiveIntsToBecomeLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        File generatedTypesDirectory = generate("/schema/type/integerAsLong.json", "com.example",
                config("useLongIntegers", true, "usePrimitives", true));
        Class<?> classWithLongProperty = compile(generatedTypesDirectory).loadClass("com.example.IntegerAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("long"));
    }

    @Test
    public void unionTypesChooseFirstTypePresent() throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        File generatedTypesDirectory = generate("/schema/type/unionTypes.json", "com.example");
        Class<?> classWithUnionProperties = compile(generatedTypesDirectory).loadClass("com.example.UnionTypes");

        Method booleanGetter = classWithUnionProperties.getMethod("getBooleanProperty");

        assertThat(booleanGetter.getReturnType().getName(), is("java.lang.Boolean"));

        Method stringGetter = classWithUnionProperties.getMethod("getStringProperty");

        assertThat(stringGetter.getReturnType().getName(), is("java.lang.String"));

        Method integerGetter = classWithUnionProperties.getMethod("getIntegerProperty");

        assertThat(integerGetter.getReturnType().getName(), is("java.lang.Integer"));
    }

}
