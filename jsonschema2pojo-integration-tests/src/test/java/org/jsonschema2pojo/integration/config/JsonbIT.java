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
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.jsonschema2pojo.integration.util.FileSearchMatcher.containsText;
import static org.junit.Assert.assertThat;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

import javax.json.bind.annotation.JsonbDateFormat;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbPropertyOrder;

import java.lang.reflect.Method;

public class JsonbIT {

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void annotationStyleJsonb1ProducesJsonb1Annotations() throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        Class generatedType = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("annotationStyle", "jsonb1"))
            .loadClass("com.example.PrimitiveProperties");

        assertThat(schemaRule.getGenerateDir(), not(containsText("org.codehaus.jackson")));
        assertThat(schemaRule.getGenerateDir(), not(containsText("com.fasterxml.jackson")));
        assertThat(schemaRule.getGenerateDir(), not(containsText("com.google.gson")));
        assertThat(schemaRule.getGenerateDir(), not(containsText("jakarta.json.bind.annotation")));
        assertThat(schemaRule.getGenerateDir(), containsText("javax.json.bind.annotation"));

        Method getter = generatedType.getMethod("getA");

        assertThat(generatedType.getAnnotation(JsonbPropertyOrder.class), is(notNullValue()));
        assertThat(getter.getAnnotation(JsonbProperty.class), is(notNullValue()));
    }

    @Test
    @SuppressWarnings({ "rawtypes"})
    public void annotationStyleJsonb1ProducesDateFormatAnnotation() throws ClassNotFoundException, SecurityException, NoSuchFieldException {

        Class generatedType = schemaRule.generateAndCompile("/schema/format/customDateTimeFormat.json", "com.example",
            config("annotationStyle", "jsonb1"))
            .loadClass("com.example.CustomDateTimeFormat");

        assertThat(generatedType.getDeclaredField("defaultFormat").getAnnotation(JsonbDateFormat.class), is(notNullValue()));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void annotationStyleJsonb2ProducesJsonb2Annotations() throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        Class generatedType = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("annotationStyle", "jsonb2"))
            .loadClass("com.example.PrimitiveProperties");

        assertThat(schemaRule.getGenerateDir(), not(containsText("org.codehaus.jackson")));
        assertThat(schemaRule.getGenerateDir(), not(containsText("com.fasterxml.jackson")));
        assertThat(schemaRule.getGenerateDir(), not(containsText("com.google.gson")));
        assertThat(schemaRule.getGenerateDir(), not(containsText("javax.json.bind.annotation")));
        assertThat(schemaRule.getGenerateDir(), containsText("jakarta.json.bind.annotation"));

        Method getter = generatedType.getMethod("getA");

        assertThat(generatedType.getAnnotation(jakarta.json.bind.annotation.JsonbPropertyOrder.class), is(notNullValue()));
        assertThat(getter.getAnnotation(jakarta.json.bind.annotation.JsonbProperty.class), is(notNullValue()));
    }

    @Test
    @SuppressWarnings({ "rawtypes"})
    public void annotationStyleJsonb2ProducesDateFormatAnnotation() throws ClassNotFoundException, SecurityException, NoSuchFieldException {

        Class generatedType = schemaRule.generateAndCompile("/schema/format/customDateTimeFormat.json", "com.example",
                config("annotationStyle", "jsonb2"))
            .loadClass("com.example.CustomDateTimeFormat");

        assertThat(generatedType.getDeclaredField("defaultFormat").getAnnotation(jakarta.json.bind.annotation.JsonbDateFormat.class), is(notNullValue()));
    }

}
