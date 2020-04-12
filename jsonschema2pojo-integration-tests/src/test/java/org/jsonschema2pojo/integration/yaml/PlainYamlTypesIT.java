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

package org.jsonschema2pojo.integration.yaml;

import static java.util.Arrays.*;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.List;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class PlainYamlTypesIT {

    @Rule
    public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

    @Test
    public void simpleTypesInExampleAreMappedToCorrectJavaTypes() throws Exception {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/yaml/simpleTypes.yaml", "com.example",
                config("sourceType", "yaml"));

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.SimpleTypes");

        Object deserialisedValue = OBJECT_MAPPER.readValue(this.getClass().getResourceAsStream("/yaml/simpleTypes.yaml"), generatedType);

        assertThat((String) generatedType.getMethod("getA").invoke(deserialisedValue), is("abc"));
        assertThat((Integer) generatedType.getMethod("getB").invoke(deserialisedValue), is(123));
        assertThat((Double) generatedType.getMethod("getC").invoke(deserialisedValue), is(12999999999999999999999.99d));
        assertThat((Boolean) generatedType.getMethod("getD").invoke(deserialisedValue), is(true));
        assertThat(generatedType.getMethod("getE").invoke(deserialisedValue), is(nullValue()));

    }

    @Test
    public void integerIsMappedToBigInteger() throws Exception {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/yaml/simpleTypes.yaml", "com.example", config("sourceType", "yaml", "useBigIntegers", true));

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.SimpleTypes");

        Object deserialisedValue = OBJECT_MAPPER.readValue(this.getClass().getResourceAsStream("/yaml/simpleTypes.yaml"), generatedType);

        assertThat((BigInteger) generatedType.getMethod("getB").invoke(deserialisedValue), is(new BigInteger("123")));
    }

    @Test(expected = ClassNotFoundException.class)
    public void simpleTypeAtRootProducesNoJavaTypes() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/yaml/simpleTypeAsRoot.yaml", "com.example",
                config("sourceType", "yaml"));

        resultsClassLoader.loadClass("com.example.SimpleTypeAsRoot");

    }

    @Test
    @SuppressWarnings("unchecked")
    public void complexTypesProduceObjects() throws Exception {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/yaml/complexObject.yaml", "com.example",
                config("sourceType", "yaml"));

        Class<?> complexObjectClass = resultsClassLoader.loadClass("com.example.ComplexObject");
        Object complexObject = OBJECT_MAPPER.readValue(this.getClass().getResourceAsStream("/yaml/complexObject.yaml"), complexObjectClass);

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

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/yaml/array.yaml", "com.example",
                config("sourceType", "yaml"));

        Class<?> arrayType = resultsClassLoader.loadClass("com.example.Array");
        Class<?> itemType = resultsClassLoader.loadClass("com.example.A");

        Object deserialisedValue = OBJECT_MAPPER.readValue(this.getClass().getResourceAsStream("/yaml/array.yaml"), arrayType);

        List<?> valueA = (List) arrayType.getMethod("getA").invoke(deserialisedValue);
        assertThat(((ParameterizedType) arrayType.getMethod("getA").getGenericReturnType()).getActualTypeArguments()[0], is(equalTo((Type) itemType)));
        assertThat((Integer) itemType.getMethod("get0").invoke(valueA.get(0)), is(0));
        assertThat((Integer) itemType.getMethod("get1").invoke(valueA.get(1)), is(1));
        assertThat((Integer) itemType.getMethod("get2").invoke(valueA.get(2)), is(2));

        Object valueB = arrayType.getMethod("getB").invoke(deserialisedValue);
        assertThat(valueB, is(instanceOf(List.class)));
        assertThat(((ParameterizedType) arrayType.getMethod("getB").getGenericReturnType()).getActualTypeArguments()[0], is(equalTo((Type) Integer.class)));

    }

    @Test
    public void arrayItemsAreRecursivelyMerged() throws Exception {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/yaml/complexPropertiesInArrayItem.yaml", "com.example", config("sourceType", "yaml"));
        Class<?> genType = resultsClassLoader.loadClass("com.example.ComplexPropertiesInArrayItem");
        Class<?> listItemType = resultsClassLoader.loadClass("com.example.List");
        Class<?> objItemType = resultsClassLoader.loadClass("com.example.Obj");

        Object[] items = (Object[]) OBJECT_MAPPER.readValue(this.getClass().getResourceAsStream("/yaml/complexPropertiesInArrayItem.yaml"), Array.newInstance(genType, 0).getClass());
        {
            Object item = items[0];

            List<?> itemList = (List<?>) genType.getMethod("getList").invoke(item);
            assertThat((Integer) listItemType.getMethod("getA").invoke(itemList.get(0)), is(1));
            assertThat((String) listItemType.getMethod("getC").invoke(itemList.get(0)), is("hey"));
            assertNull(listItemType.getMethod("getB").invoke(itemList.get(0)));

            Object itemObj = genType.getMethod("getObj").invoke(item);
            assertThat((String) objItemType.getMethod("getName").invoke(itemObj), is("k"));
            assertNull(objItemType.getMethod("getIndex").invoke(itemObj));
        }
        {
            Object item = items[1];

            List<?> itemList = (List<?>) genType.getMethod("getList").invoke(item);
            assertThat((Integer) listItemType.getMethod("getB").invoke(itemList.get(0)), is(177));
            assertThat((String) listItemType.getMethod("getC").invoke(itemList.get(0)), is("hey again"));
            assertNull(listItemType.getMethod("getA").invoke(itemList.get(0)));

            Object itemObj = genType.getMethod("getObj").invoke(item);
            assertThat((Integer) objItemType.getMethod("getIndex").invoke(itemObj), is(8));
            assertNull(objItemType.getMethod("getName").invoke(itemObj));
        }
    }

    @Test
    public void arrayItemsAreNotRecursivelyMerged() throws Exception {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/yaml/simplePropertiesInArrayItem.yaml", "com.example", config("sourceType", "yaml"));
        Class<?> genType = resultsClassLoader.loadClass("com.example.SimplePropertiesInArrayItem");

        // Different array items use different types for the same property;
        // we don't support union types, so we have to pick one
        assertEquals(Integer.class, genType.getMethod("getScalar").getReturnType());

        thrown.expect(InvalidFormatException.class);
        thrown.expectMessage(startsWith("Cannot deserialize value of type `java.lang.Integer` from String \"what\": not a valid Integer value"));
        OBJECT_MAPPER.readValue(this.getClass().getResourceAsStream("/yaml/simplePropertiesInArrayItem.yaml"), Array.newInstance(genType, 0).getClass());
    }

    @Test(expected = ClassNotFoundException.class)
    public void arrayAtRootProducesNoJavaTypes() throws Exception {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/yaml/arrayAsRoot.yaml", "com.example",
                config("sourceType", "yaml"));

        resultsClassLoader.loadClass("com.example.ArrayAsRoot");

    }

}
