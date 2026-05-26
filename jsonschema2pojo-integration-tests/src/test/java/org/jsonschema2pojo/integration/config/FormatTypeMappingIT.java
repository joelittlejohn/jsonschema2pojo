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

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class FormatTypeMappingIT {

    @RegisterExtension public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void canOverrideDateRelatedTypes() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/format/formattedProperties.json", "com.example",
                config("formatTypeMapping", mapping("date", LocalDate.class, "time", LocalTime.class, "date-time", DateTime.class)));

        Class generatedType = resultsClassLoader.loadClass("com.example.FormattedProperties");

        Method dateTime = generatedType.getMethod("getStringAsDateTime");
        Method time = generatedType.getMethod("getStringAsTime");
        Method date = generatedType.getMethod("getStringAsDate");
        assertThat(dateTime.getReturnType(), typeCompatibleWith(DateTime.class));
        assertThat(time.getReturnType(), typeCompatibleWith(LocalTime.class));
        assertThat(date.getReturnType(), typeCompatibleWith(LocalDate.class));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void canOverrideTypes() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/format/formattedProperties.json", "com.example",
                config("formatTypeMapping", mapping("uri", URL.class)));

        Class generatedType = resultsClassLoader.loadClass("com.example.FormattedProperties");

        Method getter = generatedType.getMethod("getStringAsUri");
        assertThat(getter.getReturnType(), typeCompatibleWith(URL.class));
    }

    @Test
    public void canOverrideNonStandardTypes() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/format/nonStandard.json", "com.example",
                config("formatTypeMapping", mapping("non-standard", URL.class)));

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.NonStandard");

        Method getter = generatedType.getMethod("getStringAsNonStandard");
        assertThat(getter.getReturnType(), typeCompatibleWith(URL.class));
    }

    @Test
    public void canOverrideArrayTypes() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/format/arrayFormat.json", "com.example",
                config("formatTypeMapping", mapping("base64", byte[].class)));

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.ArrayFormat");

        Method getter = generatedType.getMethod("getArrayFormat");
        assertThat(getter.getReturnType(), typeCompatibleWith(byte[].class));
    }

    private static Map<String, String> mapping(Object... keyValues) {
        return config(keyValues)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Entry::getKey, e -> ((Class<?>) e.getValue()).getName()));
    }

}
