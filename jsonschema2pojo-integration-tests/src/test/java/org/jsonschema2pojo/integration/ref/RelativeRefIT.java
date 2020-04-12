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

package org.jsonschema2pojo.integration.ref;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class RelativeRefIT {

    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    private static Class<?> relativeRefsClass;

    @BeforeClass
    public static void generateAndCompileEnum() throws ClassNotFoundException {

        ClassLoader relativeRefsClassLoader = classSchemaRule.generateAndCompile("/schema/ref/refsToA.json", "com.example");

        relativeRefsClass = relativeRefsClassLoader.loadClass("com.example.RefsToA");

    }

    @Test
    public void relativeRefUsedInAPropertyIsReadSuccessfully() throws NoSuchMethodException {

        Class<?> aClass = relativeRefsClass.getMethod("getA").getReturnType();

        assertThat(aClass.getName(), is("com.example.A"));
        assertThat(aClass.getMethods(), hasItemInArray(hasProperty("name", equalTo("getPropertyOfA"))));
    }

    @Test
    public void relativeRefUsedAsArrayItemsIsReadSuccessfully() throws NoSuchMethodException {

        Type listOfAType = relativeRefsClass.getMethod("getArrayOfA").getGenericReturnType();
        Class<?> listEntryClass = (Class<?>) ((ParameterizedType) listOfAType).getActualTypeArguments()[0];

        assertThat(listEntryClass.getName(), is("com.example.A"));
    }

    @Test
    public void relativeRefUsedForAdditionalPropertiesIsReadSuccessfully() throws NoSuchMethodException {

        Type additionalPropertiesType = relativeRefsClass.getMethod("getAdditionalProperties").getGenericReturnType();
        Class<?> mapEntryClass = (Class<?>) ((ParameterizedType) additionalPropertiesType).getActualTypeArguments()[1];

        assertThat(mapEntryClass.getName(), is("com.example.A"));

    }

}