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

package com.googlecode.jsonschema2pojo.integration.ref;

import static com.googlecode.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class HttpRefIT {

    private static Class<?> httpRefsClass;

    @BeforeClass
    public static void generateAndCompileEnum() throws ClassNotFoundException {

        ClassLoader httpRefsClassLoader = generateAndCompile("/schema/ref/httpRefs.json", "com.example");

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

        Class<?> cardClass = httpRefsClass.getMethod("getCard").getReturnType();

        assertThat(cardClass.getName(), is("com.example.Card"));
        assertThat(cardClass.getMethods(), hasItemInArray(hasProperty("name", equalTo("getAdr"))));

        Class<?> adrClass = cardClass.getMethod("getAdr").getReturnType();

        assertThat(adrClass.getName(), is("com.example.Adr"));
        assertThat(adrClass.getMethods(), hasItemInArray(hasProperty("name", equalTo("getRegion"))));

        Class<?> geoClass = cardClass.getMethod("getGeo").getReturnType();

        assertThat(geoClass.getName(), is("com.example.Geo"));
        assertThat(geoClass.getMethods(), hasItemInArray(hasProperty("name", equalTo("getLatitude"))));

    }

}
