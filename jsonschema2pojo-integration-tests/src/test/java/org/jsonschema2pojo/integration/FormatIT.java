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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;

public class FormatIT extends Jsonschema2PojoTestBase {

    private static Class<?> classWithFormattedProperties;

    public static Stream<Arguments> parameters() {
        return Stream.of(
                /* { propertyName, expectedType, jsonValue, javaValue } */
                Arguments.of("integerAsDateTime", Date.class, 123, new Date(123)),
                Arguments.of("stringAsDateTime", Date.class, "54321", new Date(54321L)),
                Arguments.of("stringAsTime", String.class, "12:30", "12:30"),
                Arguments.of("stringAsDate", String.class, "1950-10-10", "1950-10-10"),
                Arguments.of("numberAsUtcMillisec", Long.class, 555, 555L),
                Arguments.of("stringAsUtcMillisec", Long.class, "999", 999L),
                Arguments.of("customFormattedNumber", Double.class, "6.512", 6.512d),
                Arguments.of("stringAsRegex", Pattern.class, "^.*[0-9]+.*$", Pattern.compile("^.*[0-9]+.*$")),
                Arguments.of("stringAsHostname", String.class, "somehost", "somehost"),
                Arguments.of("stringAsIpAddress", String.class, "192.168.1.666", "192.168.1.666"),
                Arguments.of("stringAsIpv6", String.class, "2001:0db8:85a3:0000", "2001:0db8:85a3:0000"),
                Arguments.of("stringAsColor", String.class, "#fefefe", "#fefefe"),
                Arguments.of("stringAsStyle", String.class, "border: 1px solid red", "border: 1px solid red"),
                Arguments.of("stringAsPhone", String.class, "1-800-STARWARS", "1-800-STARWARS"),
                Arguments.of("stringAsUri", URI.class, "http://some/uri?q=abc", "http://some/uri?q=abc"),
                Arguments.of("stringAsUuid", UUID.class, "15a2a782-81b3-48ef-b35f-c2b9847b617e", "15a2a782-81b3-48ef-b35f-c2b9847b617e"),
                Arguments.of("stringAsEmail", String.class, "a@b.com", "a@b.com")
        );
    }


    @BeforeEach
    public void generateClasses() throws ClassNotFoundException {

        Map<String, String> formatMapping = new HashMap<String, String>() {{
            put("int32", "int");
        }};

        ClassLoader resultsClassLoader = generateAndCompile("/schema/format/formattedProperties.json", "com.example", config("formatTypeMapping", formatMapping));

        classWithFormattedProperties = resultsClassLoader.loadClass("com.example.FormattedProperties");

    }

    @ParameterizedTest(name = "[{0}]")
    @MethodSource("parameters")
    public void formatValueProducesExpectedType(String propertyName, Class<?> expectedType, Object jsonValue, Object javaValue) throws IntrospectionException {

        Method getter = new PropertyDescriptor(propertyName, classWithFormattedProperties).getReadMethod();

        assertThat(getter.getReturnType().getName(), is(expectedType.getName()));

    }

    @ParameterizedTest(name = "[{0}]")
    @MethodSource("parameters")
    public void valueCanBeSerializedAndDeserialized(String propertyName, Class<?> expectedType, Object jsonValue, Object javaValue) throws IOException, IntrospectionException, IllegalAccessException, InvocationTargetException {

        ObjectMapper objectMapper = new ObjectMapper();

        ObjectNode node = objectMapper.createObjectNode();
        node.put(propertyName, jsonValue.toString());

        Object pojo = objectMapper.treeToValue(node, classWithFormattedProperties);

        Method getter = new PropertyDescriptor(propertyName, classWithFormattedProperties).getReadMethod();

        assertThat(getter.invoke(pojo).toString(), is(equalTo(javaValue.toString())));

        JsonNode jsonVersion = objectMapper.valueToTree(pojo);

        assertThat(jsonVersion.get(propertyName).asText(), is(equalTo(jsonValue.toString())));

    }

}
