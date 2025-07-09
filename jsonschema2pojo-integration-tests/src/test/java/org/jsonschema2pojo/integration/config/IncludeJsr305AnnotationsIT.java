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

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.hamcrest.Matcher;
import org.jsonschema2pojo.integration.util.FileSearchMatcher;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

@ParameterizedClass
@MethodSource("data")
public class IncludeJsr305AnnotationsIT {

    private final boolean useJakartaValidation;
    @RegisterExtension
    public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    public static Collection<Object> data() {
        return asList(true, false);
    }

    public IncludeJsr305AnnotationsIT(boolean useJakartaValidation) {
        this.useJakartaValidation = useJakartaValidation;
    }

    @Test
    public void jsrAnnotationsAreNotIncludedByDefault() {
        File outputDirectory = schemaRule.generate("/schema/jsr303/all.json", "com.example",
                config("useJakartaValidation", useJakartaValidation));

        final String validationPackageName = useJakartaValidation ? "jakarta.validation" : "javax.validation";
        assertThat(outputDirectory, not(containsText(validationPackageName)));
    }

    @Test
    public void jsrAnnotationsAreNotIncludedWhenSwitchedOff() {
        File outputDirectory = schemaRule.generate("/schema/jsr303/all.json", "com.example",
                config("includeJsr305Annotations", false, "useJakartaValidation", useJakartaValidation));

        final String validationPackageName = useJakartaValidation ? "jakarta.validation" : "javax.validation";
        assertThat(outputDirectory, not(containsText(validationPackageName)));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void jsr305NonnullAnnotationIsAddedForSchemaRuleRequired() throws ReflectiveOperationException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/required.json", "com.example",
                                                                       config("includeJsr305Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.Required");

        validateNonnullField(generatedType.getDeclaredField("required"));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void jsr305NullableAnnotationIsAddedByDefault() throws ReflectiveOperationException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/required/required.json", "com.example",
                                                                       config("includeJsr305Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.Required");

        validateNonnullField(generatedType.getDeclaredField("requiredProperty"));
        validateNullableField(generatedType.getDeclaredField("nonRequiredProperty"));
        validateNullableField(generatedType.getDeclaredField("defaultNotRequiredProperty"));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void jsr305RequiredArrayIsTakenIntoConsideration() throws ReflectiveOperationException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/required/requiredArray.json", "com.example",
                                                                       config("includeJsr305Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.RequiredArray");

        validateNonnullField(generatedType.getDeclaredField("requiredProperty"));
        validateNullableField(generatedType.getDeclaredField("nonRequiredProperty"));
        validateNullableField(generatedType.getDeclaredField("defaultNotRequiredProperty"));
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

        assertThat("Expected @Nonnull annotation is missing.", nonnullAnnotation, is(notNullValue()));
        assertThat("Unexpected @Nullable annotation found.", nullableAnnotation, is(nullValue()));
    }

    private static void validateNullableField(Field nullableField) {
        Nonnull nonnullAnnotation = nullableField.getAnnotation(Nonnull.class);
        Nullable nullableAnnotation = nullableField.getAnnotation(Nullable.class);

        assertThat("Unexpected @Nonnull annotation found.", nonnullAnnotation, is(nullValue()));
        assertThat("Expected @Nullable annotation is missing.", nullableAnnotation, is(notNullValue()));
    }

    private static Matcher<File> containsText(String searchText) {
        return new FileSearchMatcher(searchText);
    }
}
