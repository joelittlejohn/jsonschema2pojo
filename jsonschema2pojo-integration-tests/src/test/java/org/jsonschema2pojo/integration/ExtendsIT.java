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

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class ExtendsIT {

    @Test
    @SuppressWarnings("rawtypes")
    public void extendsWithEmbeddedSchemaGeneratesParentType() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/extends/extendsEmbeddedSchema.json", "com.example");

        Class subtype = resultsClassLoader.loadClass("com.example.ExtendsEmbeddedSchema");
        Class supertype = resultsClassLoader.loadClass("com.example.ExtendsEmbeddedSchemaParent");

        assertThat(subtype.getSuperclass(), is(equalTo(supertype)));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void extendsWithRefToAnotherSchema() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/extends/subtypeOfA.json", "com.example");

        Class subtype = resultsClassLoader.loadClass("com.example.SubtypeOfA");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfAParent");

        assertThat(subtype.getSuperclass(), is(equalTo(supertype)));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void extendsWithRefToAnotherSchemaThatIsAlreadyASubtype() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/extends/subtypeOfSubtypeOfA.json", "com.example");

        Class subtype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfA");
        Class supertype = resultsClassLoader.loadClass("com.example.SubtypeOfSubtypeOfAParent");

        assertThat(subtype.getSuperclass(), is(equalTo(supertype)));

    }

    @Test(expected = ClassNotFoundException.class)
    public void extendsStringCausesNoNewTypeToBeGenerated() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/extends/extendsString.json", "com.example");
        resultsClassLoader.loadClass("com.example.ExtendsString");

    }

}
