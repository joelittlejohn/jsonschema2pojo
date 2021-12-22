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

import org.hamcrest.Matcher;
import org.jsonschema2pojo.integration.util.FileSearchMatcher;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Field;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.jupiter.api.Assertions.*;

public class IncludeJsr305AnnotationsIT extends Jsonschema2PojoTestBase {

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void jsrAnnotationsAreNotIncludedByDefault(boolean useJakartaValidation) {
        File outputDirectory = generate("/schema/jsr303/all.json", "com.example",
                config("useJakartaValidation", useJakartaValidation));

        final String validationPackageName = useJakartaValidation ? "jakarta.validation" : "javax.validation";
        assertThat(outputDirectory, not(containsText(validationPackageName)));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void jsrAnnotationsAreNotIncludedWhenSwitchedOff(boolean useJakartaValidation) {
        File outputDirectory = generate("/schema/jsr303/all.json", "com.example",
                config("includeJsr305Annotations", false, "useJakartaValidation", useJakartaValidation));

        final String validationPackageName = useJakartaValidation ? "jakarta.validation" : "javax.validation";
        assertThat(outputDirectory, not(containsText(validationPackageName)));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void jsr305NonnullAnnotationIsAddedForSchemaRuleRequired() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/required.json", "com.example",
                config("includeJsr305Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.Required");

        assertDoesNotThrow(
                () -> validateNonnullField(generatedType.getDeclaredField("required")),
                "Field is missing in generated class.");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void jsr305NullableAnnotationIsAddedByDefault() throws ClassNotFoundException {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/required/required.json", "com.example",
                config("includeJsr305Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.Required");

        assertDoesNotThrow(
                () -> validateNonnullField(generatedType.getDeclaredField("requiredProperty")),
                "Expected field is missing in generated class."
        );
        assertDoesNotThrow(
                () -> validateNullableField(generatedType.getDeclaredField("nonRequiredProperty")),
                "Expected field is missing in generated class.");
        assertDoesNotThrow(
                () -> validateNullableField(generatedType.getDeclaredField("defaultNotRequiredProperty")),
                "Expected field is missing in generated class.");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void jsr305RequiredArrayIsTakenIntoConsideration() throws ClassNotFoundException {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/required/requiredArray.json", "com.example",
                config("includeJsr305Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.RequiredArray");

        assertDoesNotThrow(
                () -> validateNonnullField(generatedType.getDeclaredField("requiredProperty")),
                "Expected field is missing in generated class.");
        assertDoesNotThrow(
                () -> validateNullableField(generatedType.getDeclaredField("nonRequiredProperty")),
                "Expected field is missing in generated class.");
        assertDoesNotThrow(
                () -> validateNullableField(generatedType.getDeclaredField("defaultNotRequiredProperty")),
                "Expected field is missing in generated class.");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void jsr305AnnotationsGeneratedProperlyInNestedArray() throws ClassNotFoundException, NoSuchFieldException {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/required/requiredNestedInArray.json", "com.example",
                config("includeJsr305Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.Nested");

        validateNonnullField(generatedType.getDeclaredField("requiredProperty"));
        validateNullableField(generatedType.getDeclaredField("nonRequiredProperty"));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void jsr305AnnotationsGeneratedProperlyInNestedObject() throws ClassNotFoundException, NoSuchFieldException {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/required/requiredNestedInObject.json", "com.example",
                config("includeJsr305Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.Nested");

        validateNonnullField(generatedType.getDeclaredField("requiredProperty"));
        validateNullableField(generatedType.getDeclaredField("nonRequiredProperty"));
    }

    private static void validateNonnullField(Field nonnullField) {
        Nonnull nonnullAnnotation = nonnullField.getAnnotation(Nonnull.class);
        Nullable nullableAnnotation = nonnullField.getAnnotation(Nullable.class);

        assertNotNull(nonnullAnnotation, "Expected @Nonnull annotation is missing.");
        assertNull(nullableAnnotation, "Unexpected @Nullable annotation found.");
    }

    private static void validateNullableField(Field nullableField) {
        Nonnull nonnullAnnotation = nullableField.getAnnotation(Nonnull.class);
        Nullable nullableAnnotation = nullableField.getAnnotation(Nullable.class);

        assertNull(nonnullAnnotation, "Unexpected @Nonnull annotation found.");
        assertNotNull(nullableAnnotation, "Expected @Nullable annotation is missing.");
    }

    private static Matcher<File> containsText(String searchText) {
        return new FileSearchMatcher(searchText);
    }
}
