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

package com.googlecode.jsonschema2pojo.integration;

import static com.googlecode.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonschema2pojo.Schema;

public class PropertiesIT {

    @Before
    public void clearSchemaCache() {
        Schema.clearCache();
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void propertiesWithNullValuesAreOmittedWhenSerialized() throws ClassNotFoundException, IntrospectionException, InstantiationException, IllegalAccessException, InvocationTargetException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/nullProperties.json", "com.example");

        Class generatedType = resultsClassLoader.loadClass("com.example.NullProperties");
        Object instance = generatedType.newInstance();

        Method setter = new PropertyDescriptor("property", generatedType).getWriteMethod();
        setter.invoke(instance, "value");

        ObjectMapper mapper = new ObjectMapper();

        assertThat(mapper.valueToTree(instance).toString(), containsString("property"));

        setter.invoke(instance, (Object) null);

        assertThat(mapper.valueToTree(instance).toString(), not(containsString("property")));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void propertiesAreSerializedInCorrectOrder() throws ClassNotFoundException, IntrospectionException, InstantiationException, IllegalAccessException, InvocationTargetException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/orderedProperties.json", "com.example");

        Class generatedType = resultsClassLoader.loadClass("com.example.OrderedProperties");
        Object instance = generatedType.newInstance();

        new PropertyDescriptor("type", generatedType).getWriteMethod().invoke(instance, "1");
        new PropertyDescriptor("id", generatedType).getWriteMethod().invoke(instance, "2");
        new PropertyDescriptor("name", generatedType).getWriteMethod().invoke(instance, "3");
        new PropertyDescriptor("hastickets", generatedType).getWriteMethod().invoke(instance, true);
        new PropertyDescriptor("starttime", generatedType).getWriteMethod().invoke(instance, "4");

        String serialized = new ObjectMapper().valueToTree(instance).toString();

        assertThat("Properties are not in expected order", serialized.indexOf("type"), is(lessThan(serialized.indexOf("id"))));
        assertThat("Properties are not in expected order", serialized.indexOf("id"), is(lessThan(serialized.indexOf("name"))));
        assertThat("Properties are not in expected order", serialized.indexOf("name"), is(lessThan(serialized.indexOf("hastickets"))));
        assertThat("Properties are not in expected order", serialized.indexOf("hastickets"), is(lessThan(serialized.indexOf("starttime"))));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void usePrimitivesArgumentCausesPrimitiveTypes() throws ClassNotFoundException, IntrospectionException, InstantiationException, IllegalAccessException, InvocationTargetException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/primitiveProperties.json", "com.example", config("usePrimitives", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        assertThat(new PropertyDescriptor("a", generatedType).getReadMethod().getReturnType().getName(), is("int"));
        assertThat(new PropertyDescriptor("b", generatedType).getReadMethod().getReturnType().getName(), is("double"));
        assertThat(new PropertyDescriptor("c", generatedType).getReadMethod().getReturnType().getName(), is("boolean"));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void wordDelimitersCausesCamelCase() throws ClassNotFoundException, IntrospectionException, InstantiationException, IllegalAccessException, InvocationTargetException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/propertiesWithWordDelimiters.json", "com.example",
                config("usePrimitives", true, "propertyWordDelimiters", "_ -"));

        Class generatedType = resultsClassLoader.loadClass("com.example.WordDelimit");

        Object instance = generatedType.newInstance();

        new PropertyDescriptor("propertyWithUnderscores", generatedType).getWriteMethod().invoke(instance, "a_b_c");
        new PropertyDescriptor("propertyWithHyphens", generatedType).getWriteMethod().invoke(instance, "a-b-c");
        new PropertyDescriptor("propertyWithMixedDelimiters", generatedType).getWriteMethod().invoke(instance, "a b_c-d");

        JsonNode jsonified = new ObjectMapper().valueToTree(instance);

        assertThat(jsonified.has("property_with_underscores"), is(true));
        assertThat(jsonified.has("property-with-hyphens"), is(true));
        assertThat(jsonified.has("property_with mixed-delimiters"), is(true));
    }
}
