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

package org.jsonschema2pojo.integration.json;

import static java.util.Arrays.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

@RunWith(Parameterized.class)
public class JsonTypesIT {

    @Parameters(name="{0}")
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "json", new ObjectMapper()},
                { "yaml", new ObjectMapper(new YAMLFactory()) }
        });
    }

    @Rule
    public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final String format;
    private final ObjectMapper objectMapper;

    public JsonTypesIT(final String format, final ObjectMapper objectMapper) {
        this.format = format;
        this.objectMapper = objectMapper;
    }

    private String filePath(String baseName) {
        return "/" + format + "/" + baseName + "." + format;
    }

    @Test
    public void simpleTypesInExampleAreMappedToCorrectJavaTypes() throws Exception {

        final String filePath = filePath("simpleTypes");
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(filePath, "com.example", config("sourceType", format));

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.SimpleTypes");

        Object deserializedValue = objectMapper.readValue(this.getClass().getResourceAsStream(filePath), generatedType);

        assertThat((String) generatedType.getMethod("getA").invoke(deserializedValue), is("abc"));
        assertThat((Integer) generatedType.getMethod("getB").invoke(deserializedValue), is(123));
        assertThat(generatedType.getMethod("getB").getReturnType().getName(), is("java.lang.Integer"));
        assertThat((Double) generatedType.getMethod("getC").invoke(deserializedValue), is(12999999999999999999999.99d));
        assertThat((Boolean) generatedType.getMethod("getD").invoke(deserializedValue), is(true));
        assertThat(generatedType.getMethod("getE").invoke(deserializedValue), is(nullValue()));
        assertThat(generatedType.getMethod("getF").invoke(deserializedValue), is(21474836470L));

    }

    @Test
    public void integerIsMappedToBigInteger() throws Exception {

        final String filePath = filePath("simpleTypes");
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(filePath, "com.example", config("sourceType", format, "useBigIntegers", true));

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.SimpleTypes");

        Object deserializedValue = objectMapper.readValue(this.getClass().getResourceAsStream(filePath), generatedType);

        assertThat((BigInteger) generatedType.getMethod("getB").invoke(deserializedValue), is(new BigInteger("123")));
    }

    @Test
    public void numberIsMappedToBigDecimal() throws Exception {

        final String filePath = filePath("simpleTypes");
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(filePath, "com.example", config("sourceType", format, "useBigDecimals", true));

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.SimpleTypes");

        Object deserializedValue = objectMapper.readValue(this.getClass().getResourceAsStream(filePath), generatedType);

        assertThat((BigDecimal) generatedType.getMethod("getC").invoke(deserializedValue), is(new BigDecimal("12999999999999999999999.99")));
    }

    @Test(expected = ClassNotFoundException.class)
    public void simpleTypeAtRootProducesNoJavaTypes() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(filePath("simpleTypeAsRoot"), "com.example", config("sourceType", format));

        resultsClassLoader.loadClass("com.example.SimpleTypeAsRoot");

    }

    @Test
    @SuppressWarnings("unchecked")
    public void complexTypesProduceObjects() throws Exception {

        final String filePath = filePath("complexObject");
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(filePath, "com.example", config("sourceType", format));

        Class<?> complexObjectClass = resultsClassLoader.loadClass("com.example.ComplexObject");
        Object complexObject = objectMapper.readValue(this.getClass().getResourceAsStream(filePath), complexObjectClass);

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

        final String filePath = filePath("array");
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(filePath, "com.example", config("sourceType", format));

        Class<?> arrayType = resultsClassLoader.loadClass("com.example.Array");
        Class<?> itemType = resultsClassLoader.loadClass("com.example.A");

        Object deserializedValue = objectMapper.readValue(this.getClass().getResourceAsStream(filePath), arrayType);

        List<?> valueA = (List) arrayType.getMethod("getA").invoke(deserializedValue);
        assertThat(((ParameterizedType) arrayType.getMethod("getA").getGenericReturnType()).getActualTypeArguments()[0], is(equalTo((Type) itemType)));
        assertThat((Integer) itemType.getMethod("get0").invoke(valueA.get(0)), is(0));
        assertThat((Integer) itemType.getMethod("get1").invoke(valueA.get(1)), is(1));
        assertThat((Integer) itemType.getMethod("get2").invoke(valueA.get(2)), is(2));

        Object valueB = arrayType.getMethod("getB").invoke(deserializedValue);
        assertThat(valueB, is(instanceOf(List.class)));
        assertThat(((ParameterizedType) arrayType.getMethod("getB").getGenericReturnType()).getActualTypeArguments()[0], is(equalTo((Type) Integer.class)));

    }

    @Test
    public void arrayItemsAreRecursivelyMerged() throws Exception {

        final String filePath = filePath("complexPropertiesInArrayItem");
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(filePath, "com.example", config("sourceType", format));
        Class<?> genType = resultsClassLoader.loadClass("com.example.ComplexPropertiesInArrayItem");
        Class<?> listItemType = resultsClassLoader.loadClass("com.example.List");
        Class<?> objItemType = resultsClassLoader.loadClass("com.example.Obj");

        Object[] items = (Object[]) objectMapper.readValue(this.getClass().getResourceAsStream(filePath), Array.newInstance(genType, 0).getClass());
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

        final String filePath = filePath("simplePropertiesInArrayItem");
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(filePath, "com.example", config("sourceType", format));
        Class<?> genType = resultsClassLoader.loadClass("com.example.SimplePropertiesInArrayItem");

        // Different array items use different types for the same property;
        // we don't support union types, so we have to pick one
        assertEquals(Integer.class, genType.getMethod("getScalar").getReturnType());

        thrown.expect(InvalidFormatException.class);
        thrown.expectMessage(startsWith("Cannot deserialize value of type `java.lang.Integer` from String \"what\": not a valid `java.lang.Integer` value"));
        objectMapper.readValue(this.getClass().getResourceAsStream(filePath), Array.newInstance(genType, 0).getClass());
    }

    @Test(expected = ClassNotFoundException.class)
    public void arrayAtRootWithSimpleTypeProducesNoJavaTypes() throws Exception {

        final String filePath = filePath("arrayAsRoot");
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(filePath, "com.example", config("sourceType", format));

        resultsClassLoader.loadClass("com.example.ArrayAsRoot");

    }

    @Test
    public void propertiesWithSameNameOnDifferentObjects() throws Exception {

        final String filePath = "/" + format + "/propertiesSameName";
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(filePath, "com.example", config("sourceType", format));

        Class<?> aType = resultsClassLoader.loadClass("com.example.A");
        Class<?> bType = resultsClassLoader.loadClass("com.example.B");
        Class<?> aFieldsType = resultsClassLoader.loadClass("com.example.Fields");
        Class<?> bFieldsType = resultsClassLoader.loadClass("com.example.Fields__1");

        Object deserializedValueA = objectMapper.readValue(this.getClass().getResourceAsStream(filePath("propertiesSameName/a")), aType);
        Object deserializedValueB = objectMapper.readValue(this.getClass().getResourceAsStream(filePath("propertiesSameName/b")), bType);

        Object aFields = aType.getMethod("getFields").invoke(deserializedValueA);
        Object onlyOnA = aFieldsType.getMethod("getOnlyOnA").invoke(aFields);
        assertThat(onlyOnA, is("aaa"));

        Object bFields = bType.getMethod("getFields").invoke(deserializedValueB);
        Object onlyOnB = bFieldsType.getMethod("getOnlyOnB").invoke(bFields);
        assertThat(onlyOnB, is("bbb"));

    }

    @Test
    public void propertiesWithSameNameOnDifferentObjectsWithDedupe() throws Exception {

        final String filePath = "/" + format + "/commonSubClasses";
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(filePath, "com.example", config("sourceType", format, "useDeduplication", true));

        Class<?> aType = resultsClassLoader.loadClass("com.example.A");
        Class<?> bType = resultsClassLoader.loadClass("com.example.B");

        Object deserializedValueA = objectMapper.readValue(this.getClass().getResourceAsStream(filePath("commonSubClasses/a")), aType);
        Object deserializedValueB = objectMapper.readValue(this.getClass().getResourceAsStream(filePath("commonSubClasses/b")), bType);

        assertNotEquals(aType.getMethod("getAa").getReturnType().getName(),
                bType.getMethod("getAa").getReturnType().getName());
        assertEquals(aType.getMethod("get1").getReturnType().getName(),
                bType.getMethod("get1").getReturnType().getName());

        Object a1 = aType.getMethod("get1").invoke(deserializedValueA);
        Object b1 = bType.getMethod("get1").invoke(deserializedValueB);

        assertEquals(a1.getClass().getMethod("get3").getReturnType().getName(),
                b1.getClass().getMethod("get3").getReturnType().getName());
    }
}
