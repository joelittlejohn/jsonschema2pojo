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

package org.jsonschema2pojo.integration.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;

import java.lang.reflect.Method;

import org.jsonschema2pojo.SourceSortOrder;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class SourceSortOrderIT {

    @RegisterExtension
    public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void generatedClassesInCorrectPackageForFilesFirstSort() throws ClassNotFoundException, SecurityException,
            NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/schema/sourceSortOrder/", "com.example", config("sourceSortOrder", SourceSortOrder.FILES_FIRST
                        .toString()));

        Class generatedTypeA = resultsClassLoader.loadClass("com.example.A");
        Class generatedTypeZ = resultsClassLoader.loadClass("com.example.Z");

        Method getterTypeA = generatedTypeA.getMethod("getRefToA");
        final Class<?> returnTypeA = getterTypeA.getReturnType();

        Method getterTypeZ = generatedTypeZ.getMethod("getRefToZ");
        final Class<?> returnTypeZ = getterTypeZ.getReturnType();

        assertInPackage("com.example", generatedTypeA);
        assertInPackage("com.example", generatedTypeZ);
        assertInPackage("com.example", returnTypeA);
        assertInPackage("com.example", returnTypeZ);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void generatedClassesInCorrectPackageForDirectoriesFirstSort() throws ClassNotFoundException,
            SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/schema/sourceSortOrder/", "com.example", config("sourceSortOrder", SourceSortOrder.SUBDIRS_FIRST
                        .toString()));

        Class generatedTypeA = resultsClassLoader.loadClass("com.example.A");
        Class generatedTypeZ = resultsClassLoader.loadClass("com.example.Z");

        Method getterTypeA = generatedTypeA.getMethod("getRefToA");
        final Class<?> returnTypeA = getterTypeA.getReturnType();

        Method getterTypeZ = generatedTypeZ.getMethod("getRefToZ");
        final Class<?> returnTypeZ = getterTypeZ.getReturnType();

        assertInPackage("com.example", generatedTypeA);
        assertInPackage("com.example", generatedTypeZ);
        assertInPackage("com.example.includes", returnTypeA);
        assertInPackage("com.example.includes", returnTypeZ);
    }

    private void assertInPackage(String expectedPackage, Class<?> generatedClass) {
        assertThat("Unexpected package", generatedClass.getPackage().getName(), is(equalTo(expectedPackage)));
    }
}
