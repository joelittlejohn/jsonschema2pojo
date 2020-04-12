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

package org.jsonschema2pojo.integration;

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

public class JacksonViewIT {
    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void javaJsonViewWithJackson1x() throws Exception {

        org.codehaus.jackson.map.annotate.JsonView jsonViewAnnotation
                = (org.codehaus.jackson.map.annotate.JsonView) jsonViewTest("jackson1", org.codehaus.jackson.map.annotate.JsonView.class);

        assertThat(jsonViewAnnotation.value()[0].getSimpleName(), equalTo("MyJsonViewClass"));
    }

    @Test
    public void javaJsonViewWithJackson2x() throws Exception {

        com.fasterxml.jackson.annotation.JsonView jsonViewAnnotation
                = (com.fasterxml.jackson.annotation.JsonView) jsonViewTest("jackson2", com.fasterxml.jackson.annotation.JsonView.class);

        assertThat(jsonViewAnnotation.value()[0].getSimpleName(), equalTo("MyJsonViewClass"));

    }

    private Annotation jsonViewTest(String annotationStyle, Class<? extends Annotation> annotationType) throws ClassNotFoundException, NoSuchFieldException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/schema/views/views.json",
                "com.example",
                config("annotationStyle", annotationStyle));

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.Views");
        Field fieldInView = generatedType.getDeclaredField("inView");
        assertThat(fieldInView.getAnnotation(annotationType), notNullValue());

        Field fieldNotInView = generatedType.getDeclaredField("notInView");
        assertThat(fieldNotInView.getAnnotation(annotationType), nullValue());

        return fieldInView.getAnnotation(annotationType);
    }

}
