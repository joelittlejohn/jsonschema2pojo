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

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class ExcludedFromEqualsAndHashCodeIT {
    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    private static ClassLoader resultsClassLoader;

    @BeforeClass
    public static void generateAndCompileEnum() {
        classSchemaRule.generate("/schema/excludedFromEqualsAndHashCode/excludedFromEqualsAndHashCode.json", "com.example");
        classSchemaRule.generate("/schema/excludedFromEqualsAndHashCode/x-excludedFromEqualsAndHashCode.json", "com.example");
        resultsClassLoader = classSchemaRule.compile();
    }

    @Parameterized.Parameters(name="{0}")
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] {
                /* { className } */
                { "com.example.ExcludedFromEqualsAndHashCode" },
                { "com.example.XExcludedFromEqualsAndHashCode" },
        });
    }

    private Class<?> clazz;

    public ExcludedFromEqualsAndHashCodeIT(String className) throws ClassNotFoundException {
        this.clazz = resultsClassLoader.loadClass(className);
    }

    @Test
    public void hashCodeTest() throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {

        Object instance = clazz.newInstance();

        setProperty(instance, "excludedByProperty", "one");
        setProperty(instance, "excludedByArray", "two");
        setProperty(instance, "notExcluded", "three");
        setProperty(instance, "notExcludedByProperty", "four");

        int hashCodeBefore;
        int hashCodeAfter;

        hashCodeBefore = instance.hashCode();
        setProperty(instance, "excludedByProperty", "five");
        hashCodeAfter = instance.hashCode();

        assertThat(hashCodeBefore, is(equalTo(hashCodeAfter)));

        hashCodeBefore = hashCodeAfter;
        setProperty(instance, "excludedByArray", "six");
        hashCodeAfter = instance.hashCode();

        assertThat(hashCodeBefore, is(equalTo(hashCodeAfter)));

        hashCodeBefore = hashCodeAfter;
        setProperty(instance, "notExcluded", "seven");
        hashCodeAfter = instance.hashCode();

        assertThat(hashCodeBefore, is(not(equalTo(hashCodeAfter))));

        hashCodeBefore = hashCodeAfter;
        setProperty(instance, "notExcludedByProperty", "eight");
        hashCodeAfter = instance.hashCode();

        assertThat(hashCodeBefore, is(not(equalTo(hashCodeAfter))));
    }

    @Test
    public void equalsSelf() throws IllegalAccessException, InstantiationException {
        Object instance = clazz.newInstance();

        assertThat(instance.equals(instance), is(true));
    }

    @Test
    public void excludedByPropertyTest() throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {
        Object instanceOne = clazz.newInstance();
        Object instanceTwo = clazz.newInstance();

        setProperty(instanceOne, "excludedByProperty", "one");
        setProperty(instanceOne, "excludedByArray", "two");
        setProperty(instanceOne, "notExcluded", "three");
        setProperty(instanceOne, "notExcludedByProperty", "four");

        setProperty(instanceTwo, "excludedByProperty", "differentValue");
        setProperty(instanceTwo, "excludedByArray", "two");
        setProperty(instanceTwo, "notExcluded", "three");
        setProperty(instanceTwo, "notExcludedByProperty", "four");

        assertThat(instanceOne.equals(instanceTwo), is(true));
    }

    @Test
    public void excludedByArrayTest() throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {
        Object instanceOne = clazz.newInstance();
        Object instanceTwo = clazz.newInstance();

        setProperty(instanceOne, "excludedByProperty", "one");
        setProperty(instanceOne, "excludedByArray", "two");
        setProperty(instanceOne, "notExcluded", "three");
        setProperty(instanceOne, "notExcludedByProperty", "four");

        setProperty(instanceTwo, "excludedByProperty", "one");
        setProperty(instanceTwo, "excludedByArray", "differentValue");
        setProperty(instanceTwo, "notExcluded", "three");
        setProperty(instanceTwo, "notExcludedByProperty", "four");

        assertThat(instanceOne.equals(instanceTwo), is(true));
    }

    @Test
    public void notExcludedTest() throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {
        Object instanceOne = clazz.newInstance();
        Object instanceTwo = clazz.newInstance();

        setProperty(instanceOne, "excludedByProperty", "one");
        setProperty(instanceOne, "excludedByArray", "two");
        setProperty(instanceOne, "notExcluded", "three");
        setProperty(instanceOne, "notExcludedByProperty", "four");

        setProperty(instanceTwo, "excludedByProperty", "one");
        setProperty(instanceTwo, "excludedByArray", "two");
        setProperty(instanceTwo, "notExcluded", "differentValue");
        setProperty(instanceTwo, "notExcludedByProperty", "four");

        assertThat(instanceOne.equals(instanceTwo), is(false));
    }

    @Test
    public void notExcludedByPropertyTest() throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {
        Object instanceOne = clazz.newInstance();
        Object instanceTwo = clazz.newInstance();

        setProperty(instanceOne, "excludedByProperty", "one");
        setProperty(instanceOne, "excludedByArray", "two");
        setProperty(instanceOne, "notExcluded", "three");
        setProperty(instanceOne, "notExcludedByProperty", "four");

        setProperty(instanceTwo, "excludedByProperty", "one");
        setProperty(instanceTwo, "excludedByArray", "two");
        setProperty(instanceTwo, "notExcluded", "three");
        setProperty(instanceTwo, "notExcludedByProperty", "differentValue");

        assertThat(instanceOne.equals(instanceTwo), is(false));
    }

    private void setProperty(Object instance, String property, String value) throws IllegalAccessException, InvocationTargetException, IntrospectionException {
        new PropertyDescriptor(property, clazz).getWriteMethod().invoke(instance, value);
    }

}
