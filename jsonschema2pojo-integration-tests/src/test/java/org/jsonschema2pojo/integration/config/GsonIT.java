/**
 * Copyright © 2010-2014 Nokia
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
import static org.jsonschema2pojo.integration.util.JsonAssert.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.Gson;

public class GsonIT {

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void annotationStyleGsonProducesGsonAnnotations() throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException {

        File generatedOutputDirectory = generate("/json/examples/torrent.json", "com.example",
                config("annotationStyle", "gson",
                        "propertyWordDelimiters", "_",
                        "sourceType", "json"));

        assertThat(generatedOutputDirectory, not(containsText("org.codehaus.jackson")));
        assertThat(generatedOutputDirectory, not(containsText("com.fasterxml.jackson")));
        assertThat(generatedOutputDirectory, containsText("com.google.gson"));
        assertThat(generatedOutputDirectory, containsText("@SerializedName"));

        ClassLoader resultsClassLoader = compile(generatedOutputDirectory);

        Class generatedType = resultsClassLoader.loadClass("com.example.Torrent");
        Method getter = generatedType.getMethod("getBuild");

        assertThat(generatedType.getAnnotation(JsonPropertyOrder.class), is(nullValue()));
        assertThat(generatedType.getAnnotation(JsonInclude.class), is(nullValue()));
        assertThat(getter.getAnnotation(JsonProperty.class), is(nullValue()));
    }

    @Test
    public void annotationStyleGsonMakesTypesThatWorkWithGson() throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException, IOException {

        File generatedOutputDirectory = generate("/json/examples/", "com.example",
                config("annotationStyle", "gson",
                        "propertyWordDelimiters", "_",
                        "sourceType", "json",
                        "useLongIntegers", true));

        ClassLoader resultsClassLoader = compile(generatedOutputDirectory);

        assertJsonRoundTrip(resultsClassLoader, "com.example.Torrent", "/json/examples/torrent.json");
        assertJsonRoundTrip(resultsClassLoader, "com.example.GetUserData", "/json/examples/GetUserData.json");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void enumValuesAreSerializedCorrectly() throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/enum/typeWithEnumProperty.json", "com.example",
                config("annotationStyle", "gson",
                        "propertyWordDelimiters", "_"));

        Class generatedType = resultsClassLoader.loadClass("com.example.TypeWithEnumProperty");
        Class enumType = resultsClassLoader.loadClass("com.example.TypeWithEnumProperty$EnumProperty");
        Object instance = generatedType.newInstance();

        Method setter = generatedType.getMethod("setEnumProperty", enumType);
        setter.invoke(instance, enumType.getEnumConstants()[3]);

        String json = new Gson().toJson(instance);
        Map<String, String> jsonAsMap = new Gson().fromJson(json, Map.class);

        assertThat(jsonAsMap.get("enum_Property"), is("4 ! 1"));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void assertJsonRoundTrip(ClassLoader resultsClassLoader, String className, String jsonResource) throws ClassNotFoundException, IOException {
        Class generatedType = resultsClassLoader.loadClass(className);

        String expectedJson = IOUtils.toString(getClass().getResource(jsonResource));
        Object javaInstance = new Gson().fromJson(expectedJson, generatedType);
        String actualJson = new Gson().toJson(javaInstance);

        assertEqualsJson(expectedJson, actualJson);
    }

}
