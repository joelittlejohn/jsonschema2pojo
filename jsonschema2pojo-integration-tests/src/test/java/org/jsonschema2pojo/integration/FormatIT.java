/**
 * Copyright © 2010-2014 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless optional by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo.integration;

import static java.util.Arrays.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RunWith(Parameterized.class)
public class FormatIT {
    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    private static Class<?> classWithFormattedProperties;

    @Parameters(name="{0}")
    public static List<Object[]> data() {
        return asList(new Object[][] {
                /* { propertyName, expectedType, jsonValue, javaValue } */
                { "integerAsDateTime", Date.class, 123, new Date(123) },
                { "stringAsDateTime", Date.class, "54321", new Date(54321L) },
                { "stringAsTime", String.class, "12:30", "12:30" },
                { "stringAsDate", String.class, "1950-10-10", "1950-10-10" },
                { "numberAsUtcMillisec", Long.class, 555, 555L },
                { "stringAsUtcMillisec", Long.class, "999", 999L },
                { "customFormattedNumber", Double.class, "6.512", 6.512d },
                { "stringAsRegex", Pattern.class, "^.*[0-9]+.*$", Pattern.compile("^.*[0-9]+.*$") },
                { "stringAsHostname", String.class, "somehost", "somehost" },
                { "stringAsIpAddress", String.class, "192.168.1.666", "192.168.1.666" },
                { "stringAsIpv6", String.class, "2001:0db8:85a3:0000", "2001:0db8:85a3:0000" },
                { "stringAsColor", String.class, "#fefefe", "#fefefe" },
                { "stringAsStyle", String.class, "border: 1px solid red", "border: 1px solid red" },
                { "stringAsPhone", String.class, "1-800-STARWARS", "1-800-STARWARS" },
                { "stringAsUri", URI.class, "http://some/uri?q=abc", "http://some/uri?q=abc" },
                { "stringAsUuid", UUID.class, "15a2a782-81b3-48ef-b35f-c2b9847b617e", "15a2a782-81b3-48ef-b35f-c2b9847b617e" },
                { "stringAsEmail", String.class, "a@b.com", "a@b.com" } });
    }

    private String propertyName;
    private Class<?> expectedType;
    private Object jsonValue;
    private Object javaValue;

    public FormatIT(String propertyName, Class<?> expectedType, Object jsonValue, Object javaValue) {
        this.propertyName = propertyName;
        this.expectedType = expectedType;
        this.jsonValue = jsonValue;
        this.javaValue = javaValue;
    }

    @BeforeClass
    public static void generateClasses() throws ClassNotFoundException, IOException {

        ClassLoader resultsClassLoader = classSchemaRule.generateAndCompile("/schema/format/formattedProperties.json", "com.example");

        classWithFormattedProperties = resultsClassLoader.loadClass("com.example.FormattedProperties");

    }

    @Test
    public void formatValueProducesExpectedType() throws NoSuchMethodException, IntrospectionException {

        Method getter = new PropertyDescriptor(propertyName, classWithFormattedProperties).getReadMethod();

        assertThat(getter.getReturnType().getName(), is(this.expectedType.getName()));

    }

    @Test
    public void valueCanBeSerializedAndDeserialized() throws NoSuchMethodException, IOException, IntrospectionException, IllegalAccessException, InvocationTargetException {

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
