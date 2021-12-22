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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.jsonschema2pojo.integration.util.FileSearchMatcher.*;
import static org.jsonschema2pojo.integration.util.JsonAssert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.Gson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

public class Moshi1IT extends Jsonschema2PojoTestBase {

    private Moshi moshi;

    @BeforeEach
    public void setUp() {
        moshi = new Moshi.Builder().build();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void annotationStyleMoshi1ProducesMoshi1Annotations() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        Class generatedType = generateAndCompile("/json/examples/torrent.json", "com.example",
                config("annotationStyle", "moshi1",
                        "propertyWordDelimiters", "_",
                        "sourceType", "json"))
                .loadClass("com.example.Torrent");

        assertThat(getGenerateDir(), not(containsText("org.codehaus.jackson")));
        assertThat(getGenerateDir(), not(containsText("com.fasterxml.jackson")));
        assertThat(getGenerateDir(), not(containsText("com.google.gson")));
        assertThat(getGenerateDir(), not(containsText("@SerializedName")));
        assertThat(getGenerateDir(), containsText("com.squareup.moshi"));
        assertThat(getGenerateDir(), containsText("@com.squareup.moshi.Json"));

        Method getter = generatedType.getMethod("getBuild");

        assertThat(generatedType.getAnnotation(JsonPropertyOrder.class), is(nullValue()));
        assertThat(generatedType.getAnnotation(JsonInclude.class), is(nullValue()));
        assertThat(getter.getAnnotation(JsonProperty.class), is(nullValue()));
    }

    @Test
    public void annotationStyleMoshi1MakesTypesThatWorkWithMoshi1() throws ClassNotFoundException, SecurityException, IOException {

        ClassLoader resultsClassLoader = generateAndCompile("/json/examples/", "com.example",
                config("annotationStyle", "moshi1",
                        "propertyWordDelimiters", "_",
                        "sourceType", "json",
                        "useLongIntegers", true));

        assertJsonRoundTrip(resultsClassLoader, "com.example.Torrent", "/json/examples/torrent.json");
        assertJsonRoundTrip(resultsClassLoader, "com.example.GetUserData", "/json/examples/GetUserData.json");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void enumValuesAreSerializedCorrectly() throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/enum/typeWithEnumProperty.json", "com.example",
                config("annotationStyle", "moshi1",
                        "propertyWordDelimiters", "_"));

        Class generatedType = resultsClassLoader.loadClass("com.example.TypeWithEnumProperty");
        Class enumType = resultsClassLoader.loadClass("com.example.TypeWithEnumProperty$EnumProperty");
        Object instance = generatedType.newInstance();

        Method setter = generatedType.getMethod("setEnumProperty", enumType);
        setter.invoke(instance, enumType.getEnumConstants()[3]);

        JsonAdapter jsonAdapter = moshi.adapter(generatedType);
        String json = jsonAdapter.toJson(instance);

        Map<String, String> jsonAsMap = new Gson().fromJson(json, Map.class);
        assertThat(jsonAsMap.get("enum_Property"), is("4 ! 1"));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void assertJsonRoundTrip(ClassLoader resultsClassLoader, String className, String jsonResource) throws ClassNotFoundException, IOException {
        Class generatedType = resultsClassLoader.loadClass(className);

        String expectedJson = IOUtils.toString(getClass().getResource(jsonResource));
        JsonAdapter<Object> jsonAdapter = moshi.adapter(generatedType);
        Object javaInstance = jsonAdapter.fromJson(expectedJson);
        String actualJson = jsonAdapter.toJson(javaInstance);

        assertEqualsJson(expectedJson, actualJson);
    }

}
