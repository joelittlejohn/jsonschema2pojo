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

package org.jsonschema2pojo.integration.ref;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.jsonschema2pojo.rules.RuleFactory;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;

public class FragmentRefIT {

    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    private static Class<?> fragmentRefsClass;

    @BeforeClass
    public static void generateAndCompile() throws ClassNotFoundException {

        ClassLoader fragmentRefsClassLoader = classSchemaRule.generateAndCompile("/schema/ref/fragmentRefs.json", "com.example");

        fragmentRefsClass = fragmentRefsClassLoader.loadClass("com.example.FragmentRefs");

    }

    @Test
    public void refToFragmentOfSelfIsReadSuccessfully() throws NoSuchMethodException {

        Class<?> aClass = fragmentRefsClass.getMethod("getFragmentOfSelf").getReturnType();

        assertThat(aClass.getName(), is("com.example.A"));
        assertThat(aClass.getMethods(), hasItemInArray(hasProperty("name", equalTo("getPropertyOfA"))));
    }

    @Test
    public void refToFragmentOfAnotherSchemaIsReadSuccessfully() throws NoSuchMethodException {

        Class<?> aClass = fragmentRefsClass.getMethod("getFragmentOfA").getReturnType();

        assertThat(aClass.getName(), is("com.example.AdditionalPropertyValue"));

    }

    @Test
    public void refToFragmentOfAnotherSchemaThatAlsoHasARefIsReadSuccessfully() throws NoSuchMethodException {

        Class<?> aClass = fragmentRefsClass.getMethod("getFragmentWithAnotherRef").getReturnType();

        assertThat(aClass.getName(), is("java.lang.String"));

    }

    @Test
    public void selfRefWithoutParentFile() throws IOException {
        JCodeModel codeModel = new JCodeModel();
        JsonNode schema = new ObjectMapper().readTree("{\"type\":\"object\", \"properties\":{\"a\":{\"$ref\":\"#/b\"}}, \"b\":\"string\"}");

        JPackage p = codeModel._package("com.example");
        new RuleFactory().getSchemaRule().apply("Example", schema, null, p, new Schema(null, schema, null));
    }

    @Test
    public void refToInnerFragmentThatHasRefToOuterFragmentWithoutParentFile() throws IOException {
        JCodeModel codeModel = new JCodeModel();
        JsonNode schema = new ObjectMapper().readTree("{\n" +
                "    \"type\": \"object\",\n" +
                "    \"definitions\": {\n" +
                "        \"location\": {\n" +
                "            \"type\": \"object\",\n" +
                "            \"properties\": {\n" +
                "                \"cat\": {\n" +
                "                    \"$ref\": \"#/definitions/cat\"\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"cat\": {\n" +
                "            \"type\": \"number\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"properties\": {\n" +
                "        \"location\": {\n" +
                "            \"$ref\": \"#/definitions/location\"\n" +
                "        }\n" +
                "    }\n" +
                "}");

        JPackage p = codeModel._package("com.example");
        new RuleFactory().getSchemaRule().apply("Example", schema, null, p, new Schema(null, schema, null));
    }

    @Test
    public void refToInnerFragmentThatHasRefToAnotherFragmentWithoutParentFile() throws IOException {
        JCodeModel codeModel = new JCodeModel();
        JsonNode schema = new ObjectMapper().readTree("{\n"
                + "    \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n"
                + "    \"title\": \"Inbox Item Datalake DTO\",\n"
                + "    \"definitions\": {\n"
                + "        \"PropertyA\": {\n"
                + "            \"type\": \"object\",\n"
                + "            \"properties\": {\n"
                + "                \"value\": {\n"
                + "                    \"type\": \"string\"\n"
                + "                }\n"
                + "            }\n"
                + "        },\n"
                + "        \"PropertyB\": {\n"
                + "            \"type\": \"object\",\n"
                + "            \"properties\": {\n"
                + "                \"data\": {\n"
                + "                    \"type\": \"array\",\n"
                + "                    \"items\": {\n"
                + "                        \"$ref\": \"#/definitions/PropertyA\"\n"
                + "                    },\n"
                + "                    \"default\": []\n"
                + "                }\n"
                + "            }\n"
                + "        }\n"
                + "    },\n"
                + "    \"properties\": {\n"
                + "        \"FinalProperty\": {\n"
                + "            \"type\": \"array\",\n"
                + "            \"items\": {\n"
                + "                \"$ref\": \"#/definitions/PropertyB\"\n"
                + "            },\n"
                + "            \"default\": []\n"
                + "        }\n"
                + "    }\n"
                + "}");

        JPackage p = codeModel._package("com.example");
        new RuleFactory().getSchemaRule().apply("Example", schema, null, p, new Schema(null, schema, null));
    }


    @Test
    public void refToInnerFragmentThatHasRefToAnotherFragment() throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException {
        final ClassLoader fragmentRefsClassLoader = classSchemaRule.generateAndCompile("/schema/ref/refToRefToDefinitions.json", "com.example");

        final Class<?> finalPropertyClass = fragmentRefsClassLoader.loadClass("com.example.RefToRefToDefinitions");

        Class<?> finalPropertyType = finalPropertyClass.getMethod("getFinalProperty").getReturnType();
        assertThat(finalPropertyType.getName(), is("java.util.List"));

        Type finalPropertyItemType = ((ParameterizedType)finalPropertyClass.getMethod("getFinalProperty").getGenericReturnType()).getActualTypeArguments()[0];
        assertThat(finalPropertyItemType.getTypeName(), is("com.example.PropertyB"));

        final Class<?> propertyBClass = fragmentRefsClassLoader.loadClass("com.example.PropertyB");

        Class<?> dataType = propertyBClass.getMethod("getData").getReturnType();
        assertThat(dataType.getName(), is("java.util.List"));

        Type dataItemType = ((ParameterizedType)propertyBClass.getMethod("getData").getGenericReturnType()).getActualTypeArguments()[0];
        assertThat(dataItemType.getTypeName(), is("com.example.PropertyA"));

        final Class<?> propertyAClass = fragmentRefsClassLoader.loadClass("com.example.PropertyA");

        Class<?> valueType = propertyAClass.getMethod("getValue").getReturnType();
        assertThat(valueType.getName(), is("java.lang.String"));
    }
}
