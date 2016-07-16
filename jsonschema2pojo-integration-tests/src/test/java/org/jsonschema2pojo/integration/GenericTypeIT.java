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
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class GenericTypeIT {
    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    private static Class<?> classWithGenericTypes;

    @BeforeClass
    public static void generateAndCompileClass() throws ClassNotFoundException {

        classWithGenericTypes = classSchemaRule.generateAndCompile("/schema/type/genericJavaType.json", "com.example").loadClass("com.example.GenericJavaType");

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void genericTypeCanBeIncludedInJavaType() throws ClassNotFoundException, NoSuchMethodException, SecurityException {

        Method getterMethod = classWithGenericTypes.getMethod("getA");
        assertThat((Class<Map>) getterMethod.getReturnType(), is(equalTo(Map.class)));
        assertThat(getterMethod.getGenericReturnType(), is(instanceOf(ParameterizedType.class)));

        Type[] typeArguments = ((ParameterizedType) getterMethod.getGenericReturnType()).getActualTypeArguments();
        assertThat(typeArguments[0], is(equalTo((Type)String.class)));
        assertThat(typeArguments[1], is(equalTo((Type)Integer.class)));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void genericTypeCanBeIncludedWhenTypeObjectIsOmitted() throws ClassNotFoundException, NoSuchMethodException, SecurityException {

        Method getterMethod = classWithGenericTypes.getMethod("getD");
        assertThat((Class<Map>) getterMethod.getReturnType(), is(equalTo(Map.class)));
        assertThat(getterMethod.getGenericReturnType(), is(instanceOf(ParameterizedType.class)));

        Type[] typeArguments = ((ParameterizedType) getterMethod.getGenericReturnType()).getActualTypeArguments();
        assertThat(typeArguments[0], is(equalTo((Type) String.class)));
        assertThat(typeArguments[1], is(equalTo((Type) Double.class)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void genericTypeInJavaTypeCanBeNested() throws ClassNotFoundException, NoSuchMethodException, SecurityException {

        Method getterMethod = classWithGenericTypes.getMethod("getB");
        assertThat((Class<Map>) getterMethod.getReturnType(), is(equalTo(Map.class)));
        assertThat(getterMethod.getGenericReturnType(), is(instanceOf(ParameterizedType.class)));

        Type[] typeArguments = ((ParameterizedType) getterMethod.getGenericReturnType()).getActualTypeArguments();
        assertThat(typeArguments[0], is(instanceOf(ParameterizedType.class)));
        assertThat(((ParameterizedType)typeArguments[0]).getActualTypeArguments().length, is(2));
        assertThat(((ParameterizedType)typeArguments[0]).getActualTypeArguments()[0], is((Type)String.class));
        assertThat(((ParameterizedType)typeArguments[0]).getActualTypeArguments()[1], is((Type)Integer.class));
        assertThat(typeArguments[1], is(instanceOf(ParameterizedType.class)));
        assertThat(((ParameterizedType)typeArguments[1]).getActualTypeArguments().length, is(1));
        assertThat(((ParameterizedType)typeArguments[1]).getActualTypeArguments()[0], is(instanceOf(ParameterizedType.class)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void genericTypeInJavaTypeCanIncludeArrays() throws ClassNotFoundException, NoSuchMethodException, SecurityException {

        Method getterMethod = classWithGenericTypes.getMethod("getC");
        assertThat((Class<Map>) getterMethod.getReturnType(), is(equalTo(Map.class)));
        assertThat(getterMethod.getGenericReturnType(), is(instanceOf(ParameterizedType.class)));

        Type[] typeArguments = ((ParameterizedType) getterMethod.getGenericReturnType()).getActualTypeArguments();
        assertThat(typeArguments[0], is(equalTo((Type)String.class)));
        assertThat(typeArguments[1], is(equalTo((Type)String[][].class)));
    }

}
