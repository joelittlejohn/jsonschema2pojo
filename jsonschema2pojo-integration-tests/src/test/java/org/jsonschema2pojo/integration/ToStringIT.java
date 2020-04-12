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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

public class ToStringIT {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Rule
    public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void testScalars() throws Exception {
        Class<?> scalarTypesClass = schemaRule.generateAndCompile("/schema/toString/scalarTypes.json", "com.example")
                .loadClass("com.example.ScalarTypes");

        assertEquals("com.example.ScalarTypes@<ref>[stringField=<null>,numberField=<null>,integerField=<null>,booleanField=<null>,nullField=<null>,bytesField=<null>]",
                toStringAndReplaceAddress(Collections.emptyMap(), scalarTypesClass));

        Map<String, Object> scalarTypes = new HashMap<>();
        scalarTypes.put("stringField", "hello");
        scalarTypes.put("numberField", 4.25);
        scalarTypes.put("integerField", 42);
        scalarTypes.put("booleanField", true);
        scalarTypes.put("bytesField", "YWJj");
        scalarTypes.put("nullField", null);

        assertEquals("com.example.ScalarTypes@<ref>[stringField=hello,numberField=4.25,integerField=42,booleanField=true,nullField=<null>,bytesField={97,98,99}]",
                toStringAndReplaceAddress(scalarTypes, scalarTypesClass));
    }

    @Test
    public void testComposites() throws Exception {
        Class<?> compositeTypesClass = schemaRule.generateAndCompile("/schema/toString/compositeTypes.json", "com.example")
                .loadClass("com.example.CompositeTypes");

        assertEquals("com.example.CompositeTypes@<ref>[mapField=<null>,objectField=<null>,arrayField=[],uniqueArrayField=[]]",
                toStringAndReplaceAddress(Collections.emptyMap(), compositeTypesClass));

        Map<String, Integer> intPair = new HashMap<>();
        intPair.put("l", 0);
        intPair.put("r", 1);

        Map<String, Object> compositeTypes = new HashMap<>();
        compositeTypes.put("mapField", Collections.singletonMap("intPair", intPair));
        compositeTypes.put("objectField", intPair);
        compositeTypes.put("arrayField", Collections.singleton(intPair));
        compositeTypes.put("uniqueArrayField", Collections.singleton(intPair));

        assertEquals("com.example.CompositeTypes@<ref>"
                        + "[mapField={intPair=com.example.IntPair@<ref>[l=0,r=1]}"
                        + ",objectField=com.example.IntPair@<ref>[l=0,r=1]"
                        + ",arrayField=[com.example.IntPair@<ref>[l=0,r=1]]"
                        + ",uniqueArrayField=[com.example.IntPair@<ref>[l=0,r=1]]]",
                toStringAndReplaceAddress(compositeTypes, compositeTypesClass));
    }

    @Test
    public void testArrayOfArrays() throws Exception {
        Class<?> arrayOfArraysClass = schemaRule.generateAndCompile("/schema/toString/arrayOfArrays.json", "com.example")
                .loadClass("com.example.ArrayOfArrays");

        Map<String, ?> arrayOfNullArrays = Collections.singletonMap("grid", Arrays.asList(null, null));

        assertEquals("com.example.ArrayOfArrays@<ref>[grid=[null, null]]",
                toStringAndReplaceAddress(arrayOfNullArrays, arrayOfArraysClass));

        Map<String, ?> arrayOfArrays = Collections.singletonMap("grid", Arrays.asList(
                Arrays.asList(1, 2, 3),
                Arrays.asList(4, 5, 6),
                Arrays.asList(7, 8, 9)));

        assertEquals("com.example.ArrayOfArrays@<ref>[grid=[[1.0, 2.0, 3.0], [4.0, 5.0, 6.0], [7.0, 8.0, 9.0]]]",
                toStringAndReplaceAddress(arrayOfArrays, arrayOfArraysClass));
    }

    @Test
    public void testInheritance() throws Exception {
        Class<?> squareClass = schemaRule.generateAndCompile("/schema/toString/square.json", "com.example")
                .loadClass("com.example.Square");

        Map<String, Object> square = new HashMap<>();
        square.put("sides", 4);
        square.put("diagonals", Arrays.asList(Math.sqrt(2.0), Math.sqrt(2.0)));
        square.put("length", 1.0);

        assertEquals("com.example.Square@<ref>[sides=4,diagonals=[1.4142135623730951, 1.4142135623730951],length=1.0]",
                toStringAndReplaceAddress(square, squareClass));
    }

    private static String toStringAndReplaceAddress(Object object, Class<?> clazz) {
        Object converted = OBJECT_MAPPER.convertValue(object, clazz);
        String convertedString = null;

        if (converted != null) {
            convertedString = converted.toString();
        }

        if (convertedString != null) {
            convertedString = convertedString.replaceAll("@[0-9a-f]+", "@<ref>");
        }

        return convertedString;
    }
}
