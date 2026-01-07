/**
 * Copyright © 2010-2020 Nokia
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo.integration.config;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.jsonschema2pojo.integration.util.FileSearchMatcher.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.maven.plugin.MojoExecutionException;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class AnnotationStyleIT {

    @RegisterExtension
    public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void defaultAnnotationStyleIsJackson2() throws ReflectiveOperationException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/type/types.json", "com.example");
        Class<?> generatedType = resultsClassLoader.loadClass("com.example.Types");

        Method getter = generatedType.getMethod("getUniqueArrayProperty");

        assertThat(generatedType.getAnnotation(com.fasterxml.jackson.annotation.JsonPropertyOrder.class), is(notNullValue()));
        assertThat(generatedType.getAnnotation(com.fasterxml.jackson.annotation.JsonInclude.class), is(notNullValue()));
        assertThat(getter.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class), is(notNullValue()));

        Field field = generatedType.getDeclaredField("uniqueArrayProperty");
        assertThat(field.getAnnotation(com.fasterxml.jackson.databind.annotation.JsonDeserialize.class), is(notNullValue()));
        assertThat(field.getAnnotation(tools.jackson.databind.annotation.JsonDeserialize.class), is(nullValue()));
    }

    @Test
    public void annotationStyleJacksonProducesJackson2Annotations() throws ReflectiveOperationException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/type/types.json",
                "com.example", config("annotationStyle", "jackson"));
        Class<?> generatedType = resultsClassLoader.loadClass("com.example.Types");

        Method getter = generatedType.getMethod("getUniqueArrayProperty");

        assertThat(generatedType.getAnnotation(com.fasterxml.jackson.annotation.JsonPropertyOrder.class), is(notNullValue()));
        assertThat(generatedType.getAnnotation(com.fasterxml.jackson.annotation.JsonInclude.class), is(notNullValue()));
        assertThat(getter.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class), is(notNullValue()));

        Field field = generatedType.getDeclaredField("uniqueArrayProperty");
        assertThat(field.getAnnotation(com.fasterxml.jackson.databind.annotation.JsonDeserialize.class), is(notNullValue()));
        assertThat(field.getAnnotation(tools.jackson.databind.annotation.JsonDeserialize.class), is(nullValue()));
    }

    @Test
    public void annotationStyleJackson2ProducesJackson2Annotations() throws ReflectiveOperationException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/type/types.json",
                "com.example", config("annotationStyle", "jackson2"));
        Class<?> generatedType = resultsClassLoader.loadClass("com.example.Types");

        assertThat(schemaRule.getGenerateDir(), not(containsText("org.codehaus.jackson")));
        assertThat(schemaRule.getGenerateDir(), containsText("com.fasterxml.jackson"));
        assertThat(schemaRule.getGenerateDir(), not(containsText("tools.jackson.databind.annotation")));

        Method getter = generatedType.getMethod("getUniqueArrayProperty");

        assertThat(generatedType.getAnnotation(com.fasterxml.jackson.annotation.JsonPropertyOrder.class), is(notNullValue()));
        assertThat(generatedType.getAnnotation(com.fasterxml.jackson.annotation.JsonInclude.class), is(notNullValue()));
        assertThat(getter.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class), is(notNullValue()));

        Field field = generatedType.getDeclaredField("uniqueArrayProperty");
        assertThat(field.getAnnotation(com.fasterxml.jackson.databind.annotation.JsonDeserialize.class), is(notNullValue()));
        assertThat(field.getAnnotation(tools.jackson.databind.annotation.JsonDeserialize.class), is(nullValue()));
    }

    @Test
    public void annotationStyleJackson3ProducesJackson3Annotations() throws ReflectiveOperationException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/type/types.json",
                "com.example", config("annotationStyle", "jackson3"));
        Class<?> generatedType = resultsClassLoader.loadClass("com.example.Types");

        assertThat(schemaRule.getGenerateDir(), not(containsText("org.codehaus.jackson")));
        assertThat(schemaRule.getGenerateDir(), containsText("com.fasterxml.jackson"));
        assertThat(schemaRule.getGenerateDir(), containsText("tools.jackson.databind.annotation"));

        Method getter = generatedType.getMethod("getUniqueArrayProperty");

        assertThat(generatedType.getAnnotation(com.fasterxml.jackson.annotation.JsonPropertyOrder.class), is(notNullValue()));
        assertThat(generatedType.getAnnotation(com.fasterxml.jackson.annotation.JsonInclude.class), is(notNullValue()));
        assertThat(getter.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class), is(notNullValue()));

        Field field = generatedType.getDeclaredField("uniqueArrayProperty");
        assertThat(field.getAnnotation(com.fasterxml.jackson.databind.annotation.JsonDeserialize.class), is(nullValue()));
        assertThat(field.getAnnotation(tools.jackson.databind.annotation.JsonDeserialize.class), is(notNullValue()));
    }

    @Test
    public void annotationStyleJackson2ProducesJsonPropertyDescription() throws ReflectiveOperationException {
        Class<?> generatedType = schemaRule.generateAndCompile("/schema/description/description.json", "com.example", config("annotationStyle", "jackson2")).loadClass("com.example.Description");

        Field field = generatedType.getDeclaredField("description");
        assertThat(field.getAnnotation(JsonPropertyDescription.class).value(), is("A description for this property"));
    }

    @Test
    public void annotationStyleNoneProducesNoAnnotations() throws ReflectiveOperationException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("annotationStyle", "none"));

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        Method getter = generatedType.getMethod("getA");

        assertThat(generatedType.getAnnotation(JsonPropertyOrder.class), is(nullValue()));
        assertThat(generatedType.getAnnotation(JsonSerialize.class), is(nullValue()));
        assertThat(getter.getAnnotation(JsonProperty.class), is(nullValue()));

    }

    @Test
    public void invalidAnnotationStyleCausesMojoException() {
        final String schema = "/schema/properties/primitiveProperties.json";

        final RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> schemaRule.generate(schema, "com.example", config("annotationStyle", "invalidstyle")));

        assertThat(exception.getCause(), is(instanceOf(MojoExecutionException.class)));
        assertThat(exception.getCause().getMessage(), is(containsString("invalidstyle")));
    }

}
