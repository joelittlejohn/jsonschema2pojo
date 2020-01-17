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

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.hamcrest.Matcher;
import org.jsonschema2pojo.integration.util.FileSearchMatcher;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

public class IncludeJsr305AnnotationsIT {

    @Rule
    public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void jsrAnnotationsAreNotIncludedByDefault() {
        File outputDirectory = schemaRule.generate("/schema/jsr303/all.json", "com.example");

        assertThat(outputDirectory, not(containsText("javax.validation")));
    }

    @Test
    public void jsrAnnotationsAreNotIncludedWhenSwitchedOff() {
        File outputDirectory = schemaRule.generate("/schema/jsr303/all.json", "com.example",
                config("includeJsr305Annotations", false));

        assertThat(outputDirectory, not(containsText("javax.validation")));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void jsr305NonnullAnnotationIsAddedForSchemaRuleRequired() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/required.json", "com.example",
                config("includeJsr305Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.Required");

        try {
            validateNonnullField(generatedType.getDeclaredField("required"));
        } catch (NoSuchFieldException e) {
            fail("Field is missing in generated class.");
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void jsr305NullableAnnotationIsAddedByDefault() throws ClassNotFoundException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/required/required.json", "com.example",
                config("includeJsr305Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.Required");

        try {
            validateNonnullField(generatedType.getDeclaredField("requiredProperty"));
            validateNullableField(generatedType.getDeclaredField("nonRequiredProperty"));
            validateNullableField(generatedType.getDeclaredField("defaultNotRequiredProperty"));
        } catch (NoSuchFieldException e) {
            fail("Expected field is missing in generated class.");
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void jsr305RequiredArrayIsTakenIntoConsideration() throws ClassNotFoundException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/required/requiredArray.json", "com.example",
                config("includeJsr305Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.RequiredArray");

        try {
            validateNonnullField(generatedType.getDeclaredField("requiredProperty"));
            validateNullableField(generatedType.getDeclaredField("nonRequiredProperty"));
            validateNullableField(generatedType.getDeclaredField("defaultNotRequiredProperty"));
        } catch (NoSuchFieldException e) {
            fail("Expected field is missing in generated class.");
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void jsr305AnnotationsGeneratedProperlyInNestedArray() throws ClassNotFoundException, NoSuchFieldException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/required/requiredNestedInArray.json", "com.example",
                config("includeJsr305Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.Nested");

        validateNonnullField(generatedType.getDeclaredField("requiredProperty"));
        validateNullableField(generatedType.getDeclaredField("nonRequiredProperty"));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void jsr305AnnotationsGeneratedProperlyInNestedObject() throws ClassNotFoundException, NoSuchFieldException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/required/requiredNestedInObject.json", "com.example",
                config("includeJsr305Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.Nested");

        validateNonnullField(generatedType.getDeclaredField("requiredProperty"));
        validateNullableField(generatedType.getDeclaredField("nonRequiredProperty"));
    }

    private static void validateNonnullField(Field nonnullField) {
        Nonnull nonnullAnnotation = nonnullField.getAnnotation(Nonnull.class);
        Nullable nullableAnnotation = nonnullField.getAnnotation(Nullable.class);

        assertNotNull("Expected @Nonnull annotation is missing.", nonnullAnnotation);
        assertNull("Unexpected @Nullable annotation found.", nullableAnnotation);
    }

    private static void validateNullableField(Field nullableField) {
        Nonnull nonnullAnnotation = nullableField.getAnnotation(Nonnull.class);
        Nullable nullableAnnotation = nullableField.getAnnotation(Nullable.class);

        assertNull("Unexpected @Nonnull annotation found.", nonnullAnnotation);
        assertNotNull("Expected @Nullable annotation is missing.", nullableAnnotation);
    }

    private static Matcher<File> containsText(String searchText) {
        return new FileSearchMatcher(searchText);
    }
}
