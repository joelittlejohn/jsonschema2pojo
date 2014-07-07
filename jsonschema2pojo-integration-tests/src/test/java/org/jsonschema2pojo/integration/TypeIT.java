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

package org.jsonschema2pojo.integration;

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

public class TypeIT {

    private static File generatedTypesDirectory;
    private static Class<?> classWithManyTypes;

    @BeforeClass
    public static void generateAndCompileClass() throws ClassNotFoundException {

        generatedTypesDirectory = generate("/schema/type/types.json", "com.example");
        classWithManyTypes = compile(generatedTypesDirectory).loadClass("com.example.Types");

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
    public void presenceOfPropertiesImpliesTypeObject() throws NoSuchMethodException {

        Method getterMethod = classWithManyTypes.getMethod("getImpliedObjectProperty");

        assertThat(getterMethod.getReturnType().getName(), is("com.example.ImpliedObjectProperty"));

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
    public void useDoubleNumbersFalseCausesNumbersToBecomeFloats() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        File generatedTypesDirectory = generate("/schema/type/numberAsFloat.json", "com.example", config("useDoubleNumbers", false));
        Class<?> classWithDoubleProperty = compile(generatedTypesDirectory).loadClass("com.example.NumberAsFloat");

        Method getterMethod = classWithDoubleProperty.getMethod("getFloatProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Float"));

    }

    @Test
    public void useDoubleNumbersFalseCausesPrimitiveNumbersToBecomeFloats() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        File generatedTypesDirectory = generate("/schema/type/numberAsFloat.json", "com.example",
                config("useDoubleNumbers", false, "usePrimitives", true));
        Class<?> classWithDoubleProperty = compile(generatedTypesDirectory).loadClass("com.example.NumberAsFloat");

        Method getterMethod = classWithDoubleProperty.getMethod("getFloatProperty");

        assertThat(getterMethod.getReturnType().getName(), is("float"));
    }

    @Test
    public void unionTypesChooseFirstTypePresent() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        Class<?> classWithUnionProperties = generateAndCompile("/schema/type/unionTypes.json", "com.example").loadClass("com.example.UnionTypes");

        Method booleanGetter = classWithUnionProperties.getMethod("getBooleanProperty");

        assertThat(booleanGetter.getReturnType().getName(), is("java.lang.Boolean"));

        Method stringGetter = classWithUnionProperties.getMethod("getStringProperty");

        assertThat(stringGetter.getReturnType().getName(), is("java.lang.String"));

        Method integerGetter = classWithUnionProperties.getMethod("getIntegerProperty");

        assertThat(integerGetter.getReturnType().getName(), is("java.lang.Integer"));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void typeNameConflictDoesNotCauseTypeReuse() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithNameConflict = generateAndCompile("/schema/type/typeNameConflict.json", "com.example").loadClass("com.example.TypeNameConflict");

        Method getterMethod = classWithNameConflict.getMethod("getTypeNameConflict");

        assertThat((Class) getterMethod.getReturnType(), is(not((Class) classWithNameConflict)));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void typeImplementsAdditionalJavaInterfaces() throws NoSuchMethodException {
        Method getterMethod = classWithManyTypes.getMethod("getTypeWithInterfaces");

        assertThat(getterMethod.getReturnType().getName(), is("com.example.TypeWithInterfaces"));
        assertThat(getterMethod.getReturnType().getInterfaces().length, is(2));
        assertThat((Class[]) getterMethod.getReturnType().getInterfaces(), hasItemInArray((Class) Cloneable.class));
        assertThat((Class[]) getterMethod.getReturnType().getInterfaces(), hasItemInArray((Class) Serializable.class));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void genericTypeCanBeIncludedInJavaType() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithNameConflict = generateAndCompile("/schema/type/genericJavaType.json", "com.example").loadClass("com.example.GenericJavaType");

        Method getterMethod = classWithNameConflict.getMethod("getA");

        assertThat((Class<Map>) getterMethod.getReturnType(), is(equalTo(Map.class)));
        assertThat(getterMethod.getGenericReturnType(), is(instanceOf(ParameterizedType.class)));
        assertThat(((ParameterizedType)getterMethod.getGenericReturnType()).getActualTypeArguments()[0], is(equalTo((Type)String.class)));
        assertThat(((ParameterizedType)getterMethod.getGenericReturnType()).getActualTypeArguments()[1], is(equalTo((Type)Integer.class)));
    }

}
