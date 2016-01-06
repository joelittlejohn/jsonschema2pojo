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

package org.jsonschema2pojo.integration.json;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static java.util.Arrays.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonTypesIT {
    
    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void simpleTypesInExampleAreMappedToCorrectJavaTypes() throws Exception {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/json/simpleTypes.json", "com.example",
                config("sourceType", "json"));

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.SimpleTypes");

        Object deserialisedValue = OBJECT_MAPPER.readValue(this.getClass().getResourceAsStream("/json/simpleTypes.json"), generatedType);

        assertThat((String) generatedType.getMethod("getA").invoke(deserialisedValue), is("abc"));
        assertThat((Integer) generatedType.getMethod("getB").invoke(deserialisedValue), is(123));
        assertThat((Double) generatedType.getMethod("getC").invoke(deserialisedValue), is(12999999999999999999999.99d));
        assertThat((Boolean) generatedType.getMethod("getD").invoke(deserialisedValue), is(true));
        assertThat(generatedType.getMethod("getE").invoke(deserialisedValue), is(nullValue()));

    }

    @Test(expected = ClassNotFoundException.class)
    public void simpleTypeAtRootProducesNoJavaTypes() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/json/simpleTypeAsRoot.json", "com.example",
                config("sourceType", "json"));

        resultsClassLoader.loadClass("com.example.SimpleTypeAsRoot");

    }

    @Test
    @SuppressWarnings("unchecked")
    public void complexTypesProduceObjects() throws Exception {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/json/complexObject.json", "com.example",
                config("sourceType", "json"));

        Class<?> complexObjectClass = resultsClassLoader.loadClass("com.example.ComplexObject");
        Object complexObject = OBJECT_MAPPER.readValue(this.getClass().getResourceAsStream("/json/complexObject.json"), complexObjectClass);

        Object a = complexObjectClass.getMethod("getA").invoke(complexObject);
        Object aa = a.getClass().getMethod("getAa").invoke(a);
        assertThat(aa.getClass().getMethod("getAaa").invoke(aa).toString(), is("aaaa"));

        Object b = complexObjectClass.getMethod("getB").invoke(complexObject);
        assertThat(b.getClass().getMethod("getAa").invoke(b), is(notNullValue()));

        Object _1 = complexObjectClass.getMethod("get1").invoke(complexObject);
        Object _2 = _1.getClass().getMethod("get2").invoke(_1);
        assertThat(_2, is(notNullValue()));
        Object _3 = _1.getClass().getMethod("get3").invoke(_1);
        assertThat((List<Integer>) _3, is(equalTo(asList(1, 2, 3))));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void arrayTypePropertiesProduceLists() throws Exception {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/json/array.json", "com.example",
                config("sourceType", "json"));

        Class<?> arrayType = resultsClassLoader.loadClass("com.example.Array");
        Class<?> itemType = resultsClassLoader.loadClass("com.example.A");

        Object deserialisedValue = OBJECT_MAPPER.readValue(this.getClass().getResourceAsStream("/json/array.json"), arrayType);

        List<?> valueA = (List) arrayType.getMethod("getA").invoke(deserialisedValue);
        assertThat(((ParameterizedType) arrayType.getMethod("getA").getGenericReturnType()).getActualTypeArguments()[0], is(equalTo((Type) itemType)));
        assertThat((Integer) itemType.getMethod("get0").invoke(valueA.get(0)), is(0));
        assertThat((Integer) itemType.getMethod("get1").invoke(valueA.get(1)), is(1));
        assertThat((Integer) itemType.getMethod("get2").invoke(valueA.get(2)), is(2));

        Object valueB = arrayType.getMethod("getB").invoke(deserialisedValue);
        assertThat(valueB, is(instanceOf(List.class)));
        assertThat(((ParameterizedType) arrayType.getMethod("getB").getGenericReturnType()).getActualTypeArguments()[0], is(equalTo((Type) Integer.class)));

    }

    @Test(expected = ClassNotFoundException.class)
    public void arrayAtRootProducesNoJavaTypes() throws Exception {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/json/arrayAsRoot.json", "com.example",
                config("sourceType", "json"));

        resultsClassLoader.loadClass("com.example.ArrayAsRoot");

    }

}
