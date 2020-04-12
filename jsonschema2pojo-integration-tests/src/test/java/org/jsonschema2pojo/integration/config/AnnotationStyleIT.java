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
import static org.jsonschema2pojo.integration.util.FileSearchMatcher.*;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.maven.plugin.MojoExecutionException;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class AnnotationStyleIT {

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void defaultAnnotationStyeIsJackson2() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example");

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        Method getter = generatedType.getMethod("getA");

        assertThat(generatedType.getAnnotation(JsonPropertyOrder.class), is(notNullValue()));
        assertThat(generatedType.getAnnotation(JsonInclude.class), is(notNullValue()));
        assertThat(getter.getAnnotation(JsonProperty.class), is(notNullValue()));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void annotationStyleJacksonProducesJackson2Annotations() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("annotationStyle", "jackson"));

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        Method getter = generatedType.getMethod("getA");

        assertThat(generatedType.getAnnotation(JsonPropertyOrder.class), is(notNullValue()));
        assertThat(generatedType.getAnnotation(JsonInclude.class), is(notNullValue()));
        assertThat(getter.getAnnotation(JsonProperty.class), is(notNullValue()));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void annotationStyleJackson2ProducesJackson2Annotations() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        Class generatedType = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("annotationStyle", "jackson2"))
                .loadClass("com.example.PrimitiveProperties");

        assertThat(schemaRule.getGenerateDir(), not(containsText("org.codehaus.jackson")));
        assertThat(schemaRule.getGenerateDir(), containsText("com.fasterxml.jackson"));

        Method getter = generatedType.getMethod("getA");

        assertThat(generatedType.getAnnotation(JsonPropertyOrder.class), is(notNullValue()));
        assertThat(generatedType.getAnnotation(JsonInclude.class), is(notNullValue()));
        assertThat(getter.getAnnotation(JsonProperty.class), is(notNullValue()));
    }

    @Test
    public void annotationStyleJackson2ProducesJsonPropertyDescription() throws Exception {
        Class<?> generatedType = schemaRule.generateAndCompile("/schema/description/description.json", "com.example", config("annotationStyle", "jackson2")).loadClass("com.example.Description");

        Field field = generatedType.getDeclaredField("description");
        assertThat(field.getAnnotation(JsonPropertyDescription.class).value(), is("A description for this property"));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void annotationStyleJackson1ProducesJackson1Annotations() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        Class generatedType = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("annotationStyle", "jackson1"))
                .loadClass("com.example.PrimitiveProperties");

        assertThat(schemaRule.getGenerateDir(), not(containsText("com.fasterxml.jackson")));
        assertThat(schemaRule.getGenerateDir(), containsText("org.codehaus.jackson"));

        Method getter = generatedType.getMethod("getA");

        assertThat(generatedType.getAnnotation(org.codehaus.jackson.annotate.JsonPropertyOrder.class), is(notNullValue()));
        assertThat(generatedType.getAnnotation(org.codehaus.jackson.map.annotate.JsonSerialize.class), is(notNullValue()));
        assertThat(getter.getAnnotation(org.codehaus.jackson.annotate.JsonProperty.class), is(notNullValue()));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void annotationStyleNoneProducesNoAnnotations() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("annotationStyle", "none"));

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        Method getter = generatedType.getMethod("getA");

        assertThat(generatedType.getAnnotation(JsonPropertyOrder.class), is(nullValue()));
        assertThat(generatedType.getAnnotation(JsonSerialize.class), is(nullValue()));
        assertThat(getter.getAnnotation(JsonProperty.class), is(nullValue()));

    }

    @Test
    public void invalidAnnotationStyleCausesMojoException() {

        try {
            schemaRule.generate("/schema/properties/primitiveProperties.json", "com.example", config("annotationStyle", "invalidstyle"));
            fail();
        } catch (RuntimeException e) {
            assertThat(e.getCause(), is(instanceOf(MojoExecutionException.class)));
            assertThat(e.getCause().getMessage(), is(containsString("invalidstyle")));
        }

    }

}
