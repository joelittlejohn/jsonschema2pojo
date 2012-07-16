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

package com.googlecode.jsonschema2pojo.integration.config;

import static com.googlecode.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.googlecode.jsonschema2pojo.AnnotationStyle;
import com.googlecode.jsonschema2pojo.Schema;

public class AnnotationStyleIT {

    @Before
    public void clearSchemaCache() {
        Schema.clearCache();
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void defaultAnnotationStyeIsJackson() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/primitiveProperties.json", "com.example");

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        Method getter = generatedType.getMethod("getA");

        assertThat(generatedType.getAnnotation(JsonPropertyOrder.class), is(notNullValue()));
        assertThat(generatedType.getAnnotation(JsonSerialize.class), is(notNullValue()));
        assertThat(getter.getAnnotation(JsonProperty.class), is(notNullValue()));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void annotationStyleJacksonProducesJacksonAnnotations() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("annotationStyle", AnnotationStyle.JACKSON));

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        Method getter = generatedType.getMethod("getA");

        assertThat(generatedType.getAnnotation(JsonPropertyOrder.class), is(notNullValue()));
        assertThat(generatedType.getAnnotation(JsonSerialize.class), is(notNullValue()));
        assertThat(getter.getAnnotation(JsonProperty.class), is(notNullValue()));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void annotationStyleNoneProducesNoAnnotations() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("annotationStyle", AnnotationStyle.NONE));

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        Method getter = generatedType.getMethod("getA");

        assertThat(generatedType.getAnnotation(JsonPropertyOrder.class), is(nullValue()));
        assertThat(generatedType.getAnnotation(JsonSerialize.class), is(nullValue()));
        assertThat(getter.getAnnotation(JsonProperty.class), is(nullValue()));

    }

}
