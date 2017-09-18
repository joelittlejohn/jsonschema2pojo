/**
 * Copyright © 2010-2017 Nokia
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

package org.jsonschema2pojo.integration.yaml;

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.Assert.*;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class YamlPropertiesIT {
	
    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @SuppressWarnings("rawtypes")
    public void propertiesWithNullValuesAreOmittedWhenSerialized() throws ClassNotFoundException, IntrospectionException, InstantiationException, IllegalAccessException, InvocationTargetException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/yaml/properties/nullProperties.yaml", "com.example", config("sourceType", "yamlschema"));

        Class generatedType = resultsClassLoader.loadClass("com.example.NullProperties");
        Object instance = generatedType.newInstance();

        Method setter = new PropertyDescriptor("property", generatedType).getWriteMethod();
        setter.invoke(instance, "value");

        assertThat(mapper.valueToTree(instance).toString(), containsString("property"));

        setter.invoke(instance, (Object) null);

        assertThat(mapper.valueToTree(instance).toString(), not(containsString("property")));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void usePrimitivesArgumentCausesPrimitiveTypes() throws ClassNotFoundException, IntrospectionException, InstantiationException, IllegalAccessException, InvocationTargetException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/yaml/properties/primitiveProperties.yaml", "com.example", config("usePrimitives", true, "sourceType", "yamlschema"));

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        assertThat(new PropertyDescriptor("a", generatedType).getReadMethod().getReturnType().getName(), is("int"));
        assertThat(new PropertyDescriptor("b", generatedType).getReadMethod().getReturnType().getName(), is("double"));
        assertThat(new PropertyDescriptor("c", generatedType).getReadMethod().getReturnType().getName(), is("boolean"));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void wordDelimitersCausesCamelCase() throws ClassNotFoundException, IntrospectionException, InstantiationException, IllegalAccessException, InvocationTargetException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/yaml/properties/propertiesWithWordDelimiters.yaml", "com.example",
                config("usePrimitives", true, "propertyWordDelimiters", "_ -", "sourceType", "yamlschema"));

        Class generatedType = resultsClassLoader.loadClass("com.example.WordDelimit");

        Object instance = generatedType.newInstance();

        new PropertyDescriptor("propertyWithUnderscores", generatedType).getWriteMethod().invoke(instance, "a_b_c");
        new PropertyDescriptor("propertyWithHyphens", generatedType).getWriteMethod().invoke(instance, "a-b-c");
        new PropertyDescriptor("propertyWithMixedDelimiters", generatedType).getWriteMethod().invoke(instance, "a b_c-d");

        JsonNode jsonified = mapper.valueToTree(instance);

        assertThat(jsonified.has("property_with_underscores"), is(true));
        assertThat(jsonified.has("property-with-hyphens"), is(true));
        assertThat(jsonified.has("property_with mixed-delimiters"), is(true));
    }

    @Test
    public void propertyNamesThatAreJavaKeywordsCanBeSerialized() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/yaml/properties/propertiesThatAreJavaKeywords.yaml", "com.example", config("sourceType", "yamlschema"));

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.PropertiesThatAreJavaKeywords");

        String valuesAsJsonString = "{\"public\":\"a\",\"void\":\"b\",\"enum\":\"c\",\"abstract\":\"d\"}";
        Object valuesAsObject = mapper.readValue(valuesAsJsonString, generatedType);
        JsonNode valueAsJsonNode = mapper.valueToTree(valuesAsObject);

        assertThat(valueAsJsonNode.path("public").asText(), is("a"));
        assertThat(valueAsJsonNode.path("void").asText(), is("b"));
        assertThat(valueAsJsonNode.path("enum").asText(), is("c"));
        assertThat(valueAsJsonNode.path("abstract").asText(), is("d"));

    }

    @Test
    public void propertyCalledClassCanBeSerialized() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/yaml/properties/propertyCalledClass.yaml", "com.example", config("sourceType", "yamlschema"));

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.PropertyCalledClass");

        String valuesAsJsonString = "{\"class\":\"a\"}";
        Object valuesAsObject = mapper.readValue(valuesAsJsonString, generatedType);
        JsonNode valueAsJsonNode = mapper.valueToTree(valuesAsObject);

        assertThat(valueAsJsonNode.path("class").asText(), is("a"));

    }

    @Test
    public void propertyNamesAreLowerCamelCase() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/yaml/properties/propertiesAreUpperCamelCase.yaml", "com.example", config("sourceType", "yamlschema"));
        Class<?> generatedType = resultsClassLoader.loadClass("com.example.UpperCase");

        Object instance = generatedType.newInstance();

        new PropertyDescriptor("property1", generatedType).getWriteMethod().invoke(instance, "1");
        new PropertyDescriptor("propertyTwo", generatedType).getWriteMethod().invoke(instance, 2);
        new PropertyDescriptor("propertyThreeWithSpace", generatedType).getWriteMethod().invoke(instance, "3");
        new PropertyDescriptor("propertyFour", generatedType).getWriteMethod().invoke(instance, "4");

        JsonNode jsonified = mapper.valueToTree(instance);

        assertNotNull(generatedType.getDeclaredField("property1"));
        assertNotNull(generatedType.getDeclaredField("propertyTwo"));
        assertNotNull(generatedType.getDeclaredField("propertyThreeWithSpace"));
        assertNotNull(generatedType.getDeclaredField("propertyFour"));

        assertThat(jsonified.has("Property1"), is(true));
        assertThat(jsonified.has("PropertyTwo"), is(true));
        assertThat(jsonified.has(" PropertyThreeWithSpace"), is(true));
        assertThat(jsonified.has("propertyFour"), is(true));
    }

}
