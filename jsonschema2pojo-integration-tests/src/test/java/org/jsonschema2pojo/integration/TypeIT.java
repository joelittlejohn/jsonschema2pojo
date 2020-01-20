/**
 * Copyright © 2010-2017 Nokia
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
import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Related to {@link TypeMiscIT}
 */
@RunWith(Parameterized.class)
public class TypeIT {
    
    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();
    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private static ClassLoader classLoader;

    @BeforeClass
    public static void generateAndCompileClass() {
        classSchemaRule.generate("/schema/type/types.json","com.example");
        classSchemaRule.generate("/schema/type/x-types.json","com.example");
        classLoader = classSchemaRule.compile();
    }

    @Parameterized.Parameters(name="{0}")
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] {
                /* { className } */
                { "com.example.Types" },
                { "com.example.XTypes" },
        });
    }

    private Class<?> classWithManyTypes;

    public TypeIT(String className) throws ClassNotFoundException {
        classWithManyTypes = classLoader.loadClass(className);
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
    public void arrayTypeProducesCollection() throws NoSuchMethodException {

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
