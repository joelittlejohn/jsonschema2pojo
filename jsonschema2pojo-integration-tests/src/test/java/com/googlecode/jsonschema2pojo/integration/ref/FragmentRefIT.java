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

import com.googlecode.jsonschema2pojo.Schema;

public class FragmentRefIT {

    private static Class<?> fragmentRefsClass;

    @BeforeClass
    public static void generateAndCompileEnum() throws ClassNotFoundException {

        Schema.clearCache();

        ClassLoader fragmentRefsClassLoader = generateAndCompile("/schema/ref/fragmentRefs.json", "com.example");

        fragmentRefsClass = fragmentRefsClassLoader.loadClass("com.example.FragmentRefs");

    }

    @Test
    public void refToFragmentOfSelfIsReadSuccessfully() throws NoSuchMethodException {

        Class<?> aClass = fragmentRefsClass.getMethod("getFragmentOfSelf").getReturnType();

        assertThat(aClass.getName(), is("com.example.A"));
        assertThat(aClass.getMethods(), hasItemInArray(hasProperty("name", equalTo("getPropertyOfA"))));
    }

    @Test
    public void refToFragmentOfAnotherSchemaIsReadSuccessfully() throws NoSuchMethodException {

        Class<?> aClass = fragmentRefsClass.getMethod("getFragmentOfA").getReturnType();

        assertThat(aClass.getName(), is("com.example.AdditionalPropertyValue"));

    }

}