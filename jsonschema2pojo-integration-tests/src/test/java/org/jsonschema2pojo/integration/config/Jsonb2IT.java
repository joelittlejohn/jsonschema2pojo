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
import static org.jsonschema2pojo.integration.util.JsonAssert.assertEqualsJson;
import static org.junit.Assert.assertThat;

import org.apache.commons.io.IOUtils;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbPropertyOrder;

public class Jsonb2IT {

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private Jsonb jsonb;

    @Before
    public void setUp() {
        jsonb = JsonbBuilder.create();
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

        assertThat(generatedType.getAnnotation(JsonbPropertyOrder.class), is(notNullValue()));
        assertThat(getter.getAnnotation(JsonbProperty.class), is(notNullValue()));
    }

    @Test
    @SuppressWarnings({ "rawtypes"})
    public void annotationStyleJsonb2ProducesDateFormatAnnotation() throws ClassNotFoundException, SecurityException, NoSuchFieldException {

        Class generatedType = schemaRule.generateAndCompile("/schema/format/customDateTimeFormat.json", "com.example",
                config("annotationStyle", "jsonb2"))
            .loadClass("com.example.CustomDateTimeFormat");

        assertThat(generatedType.getDeclaredField("defaultFormat").getAnnotation(JsonbDateFormat.class), is(notNullValue()));
    }

    @Test
    public void annotationStyleJsonb2MakesTypesThatWorkWithJsonb2() throws ClassNotFoundException, SecurityException, IOException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/json/examples/", "com.example",
            config("annotationStyle", "jsonb2",
                "propertyWordDelimiters", "_",
                "sourceType", "json",
                "useLongIntegers", true));

        assertJsonRoundTrip(resultsClassLoader, "com.example.Torrent", "/json/examples/torrent.json");
        assertJsonRoundTrip(resultsClassLoader, "com.example.GetUserData", "/json/examples/GetUserData.json");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void assertJsonRoundTrip(ClassLoader resultsClassLoader, String className, String jsonResource) throws ClassNotFoundException, IOException, IOException {
        Class generatedType = resultsClassLoader.loadClass(className);

        String expectedJson = IOUtils.toString(getClass().getResource(jsonResource), Charset.forName("UTF-8"));
        Object javaInstance = jsonb.fromJson(expectedJson, generatedType);
        String actualJson = jsonb.toJson(javaInstance);

        assertEqualsJson(expectedJson, actualJson);
    }

}
