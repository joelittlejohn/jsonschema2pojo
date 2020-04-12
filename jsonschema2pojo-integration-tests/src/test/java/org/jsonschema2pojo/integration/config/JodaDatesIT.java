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

package org.jsonschema2pojo.integration.config;

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.*;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

public class JodaDatesIT {

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void defaultTypesAreNotJoda() throws ClassNotFoundException, IntrospectionException {
        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/format/formattedProperties.json", "com.example");

        Class<?> classWithDate = classLoader.loadClass("com.example.FormattedProperties");

        List<String[]> nonJodaTypes = Arrays.asList(
            new String[] {"stringAsDateTime", "java.util.Date"},
            new String[] {"stringAsDate", "java.lang.String"},
            new String[] {"stringAsTime", "java.lang.String"}
        );

        for (String[] nonJodaType : nonJodaTypes) {
            assertTypeIsExpected(classWithDate, nonJodaType[0], nonJodaType[1]);
        }
    }

    @Test
    public void useJodaDatesCausesJodaDateTimeDates() throws IntrospectionException, ClassNotFoundException {
        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/format/formattedProperties.json", "com.example",
                config("useJodaDates", true));

        Class<?> classWithDate = classLoader.loadClass("com.example.FormattedProperties");

        assertTypeIsExpected(classWithDate, "stringAsDateTime", "org.joda.time.DateTime");
    }

    @Test
    public void disablingJodaDatesCausesJavaUtilDates() throws ClassNotFoundException, IntrospectionException {
        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/format/formattedProperties.json", "com.example",
                config("useJodaDates", false));

        Class<?> classWithDate = classLoader.loadClass("com.example.FormattedProperties");

        assertTypeIsExpected(classWithDate, "stringAsDateTime", "java.util.Date");
    }

    @Test
    public void useJodaLocalDatesCausesJodaLocalDateDates() throws IntrospectionException, ClassNotFoundException {
        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/format/formattedProperties.json", "com.example", config
                ("useJodaLocalDates", true));

        Class<?> classWithDate = classLoader.loadClass("com.example.FormattedProperties");

        assertTypeIsExpected(classWithDate, "stringAsDate", "org.joda.time.LocalDate");
    }

    @Test
    public void disablingJodaLocalDatesCausesStrings() throws ClassNotFoundException, IntrospectionException {
        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/format/formattedProperties.json", "com.example",
                config("useJodaLocalDates", false));

        Class<?> classWithDate = classLoader.loadClass("com.example.FormattedProperties");

        assertTypeIsExpected(classWithDate, "stringAsDate", "java.lang.String");
    }

    @Test
    public void useJodaLocalTimesCausesJodaLocalTimeDates() throws IntrospectionException, ClassNotFoundException {
        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/format/formattedProperties.json", "com.example",
                config("useJodaLocalTimes", true));

        Class<?> classWithDate = classLoader.loadClass("com.example.FormattedProperties");

        assertTypeIsExpected(classWithDate, "stringAsTime", "org.joda.time.LocalTime");
    }

    @Test
    public void disablingJodaLocalTimesCausesStrings() throws ClassNotFoundException, IntrospectionException {
        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/format/formattedProperties.json", "com.example",
                config("useJodaLocalTimes", false));

        Class<?> classWithDate = classLoader.loadClass("com.example.FormattedProperties");

        assertTypeIsExpected(classWithDate, "stringAsTime", "java.lang.String");
    }

    @Test
    public void useJodaDatesCausesDateTimeDefaultValues() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException {
        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/default/default.json", "com.example",
                config("useJodaDates", true));

        Class<?> classWithDefaults = classLoader.loadClass("com.example.Default");

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getDateTimeWithDefault");

        assertThat((DateTime) getter.invoke(instance), is(equalTo(new DateTime(123456789))));
    }

    @Test
    public void useJodaDatesCausesDateTimeAsStringDefaultValues() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException {
        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/default/default.json", "com.example",
                config("useJodaDates", true));

        Class<?> classWithDefaults = classLoader.loadClass("com.example.Default");

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getDateTimeAsStringWithDefault");

        assertThat((DateTime) getter.invoke(instance), is(equalTo(new DateTime("2011-02-24T09:25:23.112+0000"))));
    }

    @Test
    public void useJodaLocalDatesCausesLocalDateDefaultValues() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException {
        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/default/default.json", "com.example",
                config("useJodaLocalDates", true));

        Class<?> classWithDefaults = classLoader.loadClass("com.example.Default");

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getDateAsStringWithDefault");

        assertThat((LocalDate) getter.invoke(instance), is(equalTo(new LocalDate("2015-03-04"))));
    }

    @Test
    public void useJodaLocalTimesCausesLocalTimeDefaultValues() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException {
        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/default/default.json", "com.example",
                config("useJodaLocalTimes", true));

        Class<?> classWithDefaults = classLoader.loadClass("com.example.Default");

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getTimeAsStringWithDefault");

        assertThat((LocalTime) getter.invoke(instance), is(equalTo(new LocalTime("16:15:00"))));
    }

    private void assertTypeIsExpected(Class<?> classInstance, String propertyName, String expectedType)
            throws IntrospectionException {
        Method getter = new PropertyDescriptor(propertyName, classInstance).getReadMethod();
        assertThat(getter.getReturnType().getName(), is(expectedType));
    }

}
