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
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class TypeIT {
    
    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();
    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private static Class<?> classWithManyTypes;

    @BeforeClass
    public static void generateAndCompileClass() throws ClassNotFoundException {

        classWithManyTypes = classSchemaRule.generateAndCompile("/schema/type/types.json", "com.example")
                .loadClass("com.example.Types");

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
        assertThat(classSchemaRule.generated("java/util/Locale.java").exists(), is(false));

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
    public void correctTypeIsChosenForNullableType() throws NoSuchMethodException {

        Method getterMethod = classWithManyTypes.getMethod("getNullableStringProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.String"));

    }

    @Test
    public void javaTypeCanBeUsedForAnyShemaType() throws NoSuchMethodException {

        assertThat(classWithManyTypes.getMethod("getIntegerWithJavaType").getReturnType().getName(), is("java.math.BigDecimal"));
        assertThat(classWithManyTypes.getMethod("getNumberWithJavaType").getReturnType().getName(), is("java.util.UUID"));
        assertThat(classWithManyTypes.getMethod("getStringWithJavaType").getReturnType().getName(), is("java.lang.Boolean"));
        assertThat(classWithManyTypes.getMethod("getBooleanWithJavaType").getReturnType().getName(), is("long"));
        assertThat(classWithManyTypes.getMethod("getDateWithJavaType").getReturnType().getName(), is("int"));

    }

    @Test
    public void maximumGreaterThanIntegerMaxCausesIntegersToBecomeLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithLongProperty = schemaRule.generateAndCompile("/schema/type/integerWithLongMaximumAsLong.json", "com.example")
                .loadClass("com.example.IntegerWithLongMaximumAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Long"));

    }

    @Test
    public void maximumGreaterThanIntegerMaxCausesIntegersToBecomePrimitiveLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithLongProperty = schemaRule.generateAndCompile("/schema/type/integerWithLongMaximumAsLong.json", "com.example", config("usePrimitives", true))
                .loadClass("com.example.IntegerWithLongMaximumAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("long"));

    }

    @Test
    public void minimumLessThanIntegerMinCausesIntegersToBecomeLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithLongProperty = schemaRule.generateAndCompile("/schema/type/integerWithLongMinimumAsLong.json", "com.example")
                .loadClass("com.example.IntegerWithLongMinimumAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Long"));

    }

    @Test
    public void minimumLessThanIntegerMinCausesIntegersToBecomePrimitiveLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithLongProperty = schemaRule.generateAndCompile("/schema/type/integerWithLongMinimumAsLong.json", "com.example", config("usePrimitives", true))
                .loadClass("com.example.IntegerWithLongMinimumAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("long"));

    }

    @Test
    public void useLongIntegersParameterCausesIntegersToBecomeLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithLongProperty = schemaRule.generateAndCompile("/schema/type/integerAsLong.json", "com.example", config("useLongIntegers", true))
                .loadClass("com.example.IntegerAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Long"));

    }

    @Test
    public void useLongIntegersParameterCausesPrimitiveIntsToBecomeLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithLongProperty = schemaRule.generateAndCompile("/schema/type/integerAsLong.json", "com.example", config("useLongIntegers", true, "usePrimitives", true))
                .loadClass("com.example.IntegerAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("long"));
    }

    @Test
    public void useDoubleNumbersFalseCausesNumbersToBecomeFloats() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithDoubleProperty = schemaRule.generateAndCompile("/schema/type/numberAsFloat.json", "com.example", config("useDoubleNumbers", false))
                .loadClass("com.example.NumberAsFloat");

        Method getterMethod = classWithDoubleProperty.getMethod("getFloatProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Float"));

    }

    @Test
    public void useDoubleNumbersFalseCausesPrimitiveNumbersToBecomeFloats() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithDoubleProperty = schemaRule.generateAndCompile("/schema/type/numberAsFloat.json", "com.example", config("useDoubleNumbers", false, "usePrimitives", true))
                .loadClass("com.example.NumberAsFloat");

        Method getterMethod = classWithDoubleProperty.getMethod("getFloatProperty");

        assertThat(getterMethod.getReturnType().getName(), is("float"));
    }

    @Test
    public void unionTypesChooseFirstTypePresent() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        Class<?> classWithUnionProperties = schemaRule.generateAndCompile("/schema/type/unionTypes.json", "com.example").loadClass("com.example.UnionTypes");

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
        Class<?> classWithNameConflict = schemaRule.generateAndCompile("/schema/type/typeNameConflict.json", "com.example").loadClass("com.example.TypeNameConflict");

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

    @Test
    @SuppressWarnings("rawtypes")
    public void typeImplementsInterfacesWithGenericArgsCorrectly() throws NoSuchMethodException, SecurityException {
        Method getterMethod = classWithManyTypes.getMethod("getTypeWithGenericInterface");

        assertThat(getterMethod.getReturnType().getName(), is("com.example.TypeWithGenericInterface"));
        assertThat(getterMethod.getReturnType().getInterfaces().length, is(1));
        assertThat((Class[]) getterMethod.getReturnType().getInterfaces(), hasItemInArray((Class) InterfaceWithGenerics.class));
    }

    public interface InterfaceWithGenerics<T, U, V> {
    }

    @Test
    public void typeExtendsJavaClass() throws NoSuchMethodException {
        Method getterMethod = classWithManyTypes.getMethod("getTypeWithInheritedClass");

        final Class<?> generatedClass = getterMethod.getReturnType();
        assertThat(generatedClass.getName(), is("com.example.TypeWithInheritedClass"));
        assertThat(generatedClass.getSuperclass().equals(InheritedClass.class), equalTo(true));
    }

    public static class InheritedClass {
    }

    @Test
    public void typeExtendsJavaClassWithGenerics() throws NoSuchMethodException {
        Method getterMethod = classWithManyTypes.getMethod("getTypeWithInheritedClassWithGenerics");

        final Class<?> generatedClass = getterMethod.getReturnType();
        assertThat(generatedClass.getName(), is("com.example.TypeWithInheritedClassWithGenerics"));
        assertThat(generatedClass.getSuperclass().equals(InheritedClassWithGenerics.class), equalTo(true));
        assertThat(((ParameterizedType) generatedClass.getGenericSuperclass()).getActualTypeArguments(), equalTo(new Type[]
                {
                        String.class, Integer.class, Boolean.class
                }));
    }

    public static class InheritedClassWithGenerics<X, Y, Z> {
    }

}
