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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.maven.plugin.MojoExecutionException;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoTestBase;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.jsonschema2pojo.integration.util.FileSearchMatcher.containsText;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AnnotationStyleIT extends Jsonschema2PojoTestBase {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void defaultAnnotationStyeIsJackson2() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/primitiveProperties.json", "com.example");

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        Method getter = generatedType.getMethod("getA");

        assertThat(generatedType.getAnnotation(JsonPropertyOrder.class), is(notNullValue()));
        assertThat(generatedType.getAnnotation(JsonInclude.class), is(notNullValue()));
        assertThat(getter.getAnnotation(JsonProperty.class), is(notNullValue()));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void annotationStyleJacksonProducesJackson2Annotations() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("annotationStyle", "jackson"));

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        Method getter = generatedType.getMethod("getA");

        assertThat(generatedType.getAnnotation(JsonPropertyOrder.class), is(notNullValue()));
        assertThat(generatedType.getAnnotation(JsonInclude.class), is(notNullValue()));
        assertThat(getter.getAnnotation(JsonProperty.class), is(notNullValue()));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void annotationStyleJackson2ProducesJackson2Annotations() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        Class generatedType = generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("annotationStyle", "jackson2"))
                .loadClass("com.example.PrimitiveProperties");

        assertThat(getGenerateDir(), not(containsText("org.codehaus.jackson")));
        assertThat(getGenerateDir(), containsText("com.fasterxml.jackson"));

        Method getter = generatedType.getMethod("getA");

        assertThat(generatedType.getAnnotation(JsonPropertyOrder.class), is(notNullValue()));
        assertThat(generatedType.getAnnotation(JsonInclude.class), is(notNullValue()));
        assertThat(getter.getAnnotation(JsonProperty.class), is(notNullValue()));
    }

    @Test
    public void annotationStyleJackson2ProducesJsonPropertyDescription() throws Exception {
        Class<?> generatedType = generateAndCompile("/schema/description/description.json", "com.example", config("annotationStyle", "jackson2")).loadClass("com.example.Description");

        Field field = generatedType.getDeclaredField("description");
        assertThat(field.getAnnotation(JsonPropertyDescription.class).value(), is("A description for this property"));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void annotationStyleNoneProducesNoAnnotations() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("annotationStyle", "none"));

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        Method getter = generatedType.getMethod("getA");

        assertThat(generatedType.getAnnotation(JsonPropertyOrder.class), is(nullValue()));
        assertThat(generatedType.getAnnotation(JsonSerialize.class), is(nullValue()));
        assertThat(getter.getAnnotation(JsonProperty.class), is(nullValue()));

    }

    @Test
    public void invalidAnnotationStyleCausesMojoException() {


        RuntimeException e = assertThrows(RuntimeException.class, () ->
                generate("/schema/properties/primitiveProperties.json", "com.example", config("annotationStyle", "invalidstyle")));
        assertThat(e.getCause(), is(instanceOf(MojoExecutionException.class)));
        assertThat(e.getCause().getMessage(), is(containsString("invalidstyle")));
    }

}
