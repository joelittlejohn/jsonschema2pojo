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

package org.jsonschema2pojo.integration;

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class DefaultIT {
    
    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();
    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private static Class<?> classWithDefaults;

    @BeforeClass
    public static void generateAndCompileClass() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = classSchemaRule.generateAndCompile("/schema/default/default.json", "com.example");

        classWithDefaults = resultsClassLoader.loadClass("com.example.Default");

    }

    @Test
    public void emptyStringPropertyHasCorrectDefaultValue() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getEmptyStringWithDefault");

        assertThat((String) getter.invoke(instance), is(equalTo("")));

    }

    @Test
    public void stringPropertyHasCorrectDefaultValue() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getStringWithDefault");

        assertThat((String) getter.invoke(instance), is(equalTo("abc")));

    }

    @Test
    public void integerPropertyHasCorrectDefaultValue() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getIntegerWithDefault");

        assertThat((Integer) getter.invoke(instance), is(equalTo(1337)));

    }

    @Test
    public void integerPropertyHasCorrectDefaultBigIntegerValue() throws Exception {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/default/default.json", "com.example", config("useBigIntegers", true));
        Class<?> c = resultsClassLoader.loadClass("com.example.Default");

        Object instance = c.newInstance();
        Method getter = c.getMethod("getIntegerWithDefault");
        assertThat((BigInteger) getter.invoke(instance), is(equalTo(new BigInteger("1337"))));

    }

    @Test
    public void numberPropertyHasCorrectDefaultValue() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getNumberWithDefault");

        assertThat((Double) getter.invoke(instance), is(equalTo(Double.valueOf("1.337"))));

    }

    @Test
    public void numberPropertyHasCorrectDefaultBigDecimalValue() throws Exception {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/default/default.json", "com.example", config("useBigDecimals", true));
        Class<?> c = resultsClassLoader.loadClass("com.example.Default");

        Object instance = c.newInstance();
        Method getter = c.getMethod("getNumberWithDefault");
        assertThat((BigDecimal) getter.invoke(instance), is(equalTo(new BigDecimal("1.337"))));

    }

    @Test
    public void booleanPropertyHasCorrectDefaultValue() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getBooleanWithDefault");

        assertThat((Boolean) getter.invoke(instance), is(equalTo(true)));

    }

    @Test
    public void dateTimeAsMillisecPropertyHasCorrectDefaultValue() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getDateTimeWithDefault");

        assertThat((Date) getter.invoke(instance), is(equalTo(new Date(123456789))));

    }

    @Test
    public void dateTimeAsStringPropertyHasCorrectDefaultValue() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getDateTimeAsStringWithDefault");

        assertThat((Date) getter.invoke(instance), is(equalTo(new Date(1298539523112L))));

    }

    @Test
    public void utcmillisecPropertyHasCorrectDefaultValue() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getUtcmillisecWithDefault");

        assertThat((Long) getter.invoke(instance), is(equalTo(123456789L)));

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void enumPropertyHasCorrectDefaultValue() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {

        Object instance = classWithDefaults.newInstance();

        Class<Enum> enumClass = (Class<Enum>) classWithDefaults.getClassLoader().loadClass("com.example.Default$EnumWithDefault");

        Method getter = classWithDefaults.getMethod("getEnumWithDefault");

        assertThat((Enum) getter.invoke(instance), is(equalTo(enumClass.getEnumConstants()[1])));

    }

    @Test
    public void complexPropertyHasCorrectDefaultValue() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getComplexPropertyWithDefault");

        assertThat(getter.invoke(instance), is(nullValue()));

    }

    @Test
    public void simplePropertyCanHaveNullDefaultValue() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getSimplePropertyWithNullDefault");

        assertThat(getter.invoke(instance), is(nullValue()));

    }
    
    @Test
    public void arrayPropertyCanHaveNullDefaultValue() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getArrayPropertyWithNullDefault");

        assertThat(getter.invoke(instance), is(nullValue()));

    }

    @Test
    @SuppressWarnings("unchecked")
    public void arrayPropertyHasCorrectDefaultValue() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getArrayWithDefault");

        assertThat(getter.invoke(instance), is(instanceOf(List.class)));

        List<String> defaultList = (List<String>) getter.invoke(instance);

        assertThat(defaultList.size(), is(3));
        assertThat(defaultList.get(0), is(equalTo("one")));
        assertThat(defaultList.get(1), is(equalTo("two")));
        assertThat(defaultList.get(2), is(equalTo("three")));

        // list should be mutable
        assertThat(defaultList.add("anotherString"), is(true));

    }

    @Test
    @SuppressWarnings("unchecked")
    public void arrayPropertyCanHaveEmptyDefaultArray() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getArrayWithEmptyDefault");

        assertThat(getter.invoke(instance), is(instanceOf(List.class)));

        List<String> defaultList = (List<String>) getter.invoke(instance);

        assertThat(defaultList.size(), is(0));

        // list should be mutable
        assertThat(defaultList.add("anotherString"), is(true));

    }

    @Test
    @SuppressWarnings("unchecked")
    public void uniqueArrayPropertyHasCorrectDefaultValue() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getUniqueArrayWithDefault");

        assertThat(getter.invoke(instance), is(instanceOf(Set.class)));

        Set<Integer> defaultSet = (Set<Integer>) getter.invoke(instance);
        Iterator<Integer> defaultSetIterator = defaultSet.iterator();

        assertThat(defaultSet.size(), is(3));
        assertThat(defaultSetIterator.next(), is(equalTo(100)));
        assertThat(defaultSetIterator.next(), is(equalTo(200)));
        assertThat(defaultSetIterator.next(), is(equalTo(300)));

        // set should be mutable
        assertThat(defaultSet.add(400), is(true));

    }

    @Test
    @SuppressWarnings("unchecked")
    public void arrayPropertyWithoutDefaultIsEmptyList() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getArrayWithoutDefault");

        assertThat(getter.invoke(instance), is(instanceOf(List.class)));

        List<String> defaultList = (List<String>) getter.invoke(instance);

        assertThat(defaultList.size(), is(0));

        // list should be mutable
        assertThat(defaultList.add("anotherString"), is(true));

    }

    @Test
    @SuppressWarnings("unchecked")
    public void uniqueArrayPropertyWithoutDefaultIsEmptySet() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getUniqueArrayWithoutDefault");

        assertThat(getter.invoke(instance), is(instanceOf(Set.class)));

        Set<Boolean> defaultSet = (Set<Boolean>) getter.invoke(instance);

        assertThat(defaultSet.size(), is(0));

        // set should be mutable
        assertThat(defaultSet.add(true), is(true));

    }

}
