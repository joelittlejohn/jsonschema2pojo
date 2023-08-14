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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class ExcludedFromToStringIT {
    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    private static Class<?> clazz;

    @BeforeClass
    public static void generateAndCompileEnum() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = classSchemaRule.generateAndCompile("/schema/excludedFromToString/excludedFromToString.json", "com.example");

        clazz = resultsClassLoader.loadClass("com.example.ExcludedFromToString");
    }

    @Test
    public void toStringTest() throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {

        Object instance = clazz.newInstance();

        setProperty(instance, "excludedByProperty", "one");
        setProperty(instance, "excludedByArray", "two");
        setProperty(instance, "notExcluded", "three");
        setProperty(instance, "notExcludedByProperty", "four");

        String toString = instance.toString();
        assertThat(toString, not(containsString("excludedByProperty")));
        assertThat(toString, not(containsString("one")));
        assertThat(toString, not(containsString("excludedByArray")));
        assertThat(toString, not(containsString("two")));
        assertThat(toString, containsString("notExcluded"));
        assertThat(toString, containsString("three"));
        assertThat(toString, containsString("notExcludedByProperty"));
        assertThat(toString, containsString("four"));
    }

    private static void setProperty(Object instance, String property, String value) throws IllegalAccessException, InvocationTargetException, IntrospectionException {
        new PropertyDescriptor(property, clazz).getWriteMethod().invoke(instance, value);
    }

}
