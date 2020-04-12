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

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class HttpRefIT {

    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    private static Class<?> httpRefsClass;

    @BeforeClass
    public static void generateAndCompileEnum() throws ClassNotFoundException {

        ClassLoader httpRefsClassLoader = classSchemaRule.generateAndCompile("/schema/ref/httpRefs.json", "com.example");

        httpRefsClass = httpRefsClassLoader.loadClass("com.example.HttpRefs");

    }

    @Test
    public void refToHttpResourceReadSuccessfully() throws NoSuchMethodException {

        Class<?> aClass = httpRefsClass.getMethod("getAddress").getReturnType();

        assertThat(aClass.getName(), is("com.example.Address"));
        assertThat(aClass.getMethods(), hasItemInArray(hasProperty("name", equalTo("getRegion"))));
    }

    @Test
    public void relativeRefToHttpResourceWithinHttpResource() throws NoSuchMethodException {

        Class<?> transitiveRefClass = httpRefsClass.getMethod("getRefsToA").getReturnType();

        assertThat(transitiveRefClass.getName(), is("com.example.RefsToA"));
        assertThat(transitiveRefClass.getMethods(), hasItemInArray(hasProperty("name", equalTo("getA"))));

        Class<?> aClass = transitiveRefClass.getMethod("getA").getReturnType();

        assertThat(aClass.getName(), is("com.example.A"));
        assertThat(aClass.getMethods(), hasItemInArray(hasProperty("name", equalTo("getPropertyOfA"))));

    }

}
