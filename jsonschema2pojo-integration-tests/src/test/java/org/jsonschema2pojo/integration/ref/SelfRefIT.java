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

package org.jsonschema2pojo.integration.ref;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.junit.BeforeClass;
import org.junit.Test;

public class SelfRefIT {

    private static Class<?> selfRefsClass;

    @BeforeClass
    public static void generateAndCompileEnum() throws ClassNotFoundException {

        ClassLoader selfRefsClassLoader = generateAndCompile("/schema/ref/selfRefs.json", "com.example");

        selfRefsClass = selfRefsClassLoader.loadClass("com.example.SelfRefs");

    }

    @Test
    public void selfRefUsedInAPropertyIsReadSuccessfully() throws NoSuchMethodException {

        Class<?> aClass = selfRefsClass.getMethod("getChildOfSelf").getReturnType();

        assertThat(aClass.getName(), is("com.example.SelfRefs"));
    }

    @Test
    public void selfRefUsedAsArrayItemsIsReadSuccessfully() throws NoSuchMethodException {

        Type listOfAType = selfRefsClass.getMethod("getArrayOfSelf").getGenericReturnType();
        Class<?> listEntryClass = (Class<?>) ((ParameterizedType) listOfAType).getActualTypeArguments()[0];

        assertThat(listEntryClass.getName(), is("com.example.SelfRefs"));
    }

    @Test
    public void selfRefUsedForAdditionalPropertiesIsReadSuccessfully() throws NoSuchMethodException {

        Type additionalPropertiesType = selfRefsClass.getMethod("getAdditionalProperties").getGenericReturnType();
        Class<?> mapEntryClass = (Class<?>) ((ParameterizedType) additionalPropertiesType).getActualTypeArguments()[1];

        assertThat(mapEntryClass.getName(), is("com.example.SelfRefs"));

    }

}