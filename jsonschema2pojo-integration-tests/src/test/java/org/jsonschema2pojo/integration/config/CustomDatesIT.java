/**
 * Copyright Â© 2010-2014 Nokia
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
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.Assert.*;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.jsonschema2pojo.exception.GenerationException;
import org.junit.Test;

public class CustomDatesIT {

    @Test
    public void defaultTypesAreNotCustom() throws ClassNotFoundException, IntrospectionException {
        ClassLoader classLoader = generateAndCompile("/schema/format/formattedProperties.json", "com.example");

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
    public void dateTimeTypeCausesCustomDateTimeType() throws IntrospectionException, ClassNotFoundException {
        String clazz="org.joda.time.LocalDateTime";
        ClassLoader classLoader = generateAndCompile("/schema/format/formattedProperties.json", "com.example",
                config("dateTimeType", clazz));
        Class<?> classWithDate = classLoader.loadClass("com.example.FormattedProperties");
        assertTypeIsExpected(classWithDate, "stringAsDateTime", clazz);
    }

    @Test
    public void disablingDateTimeTypeCausesDefault() throws ClassNotFoundException, IntrospectionException {
        ClassLoader classLoader = generateAndCompile("/schema/format/formattedProperties.json", "com.example",
                config("dateTimeType", null));
        Class<?> classWithDate = classLoader.loadClass("com.example.FormattedProperties");
        assertTypeIsExpected(classWithDate, "stringAsDateTime", "java.util.Date");
    }

    @Test
    public void dateTypeCausesCustomDateTimeType() throws IntrospectionException, ClassNotFoundException {
        String clazz="org.joda.time.LocalDate";
        ClassLoader classLoader = generateAndCompile("/schema/format/formattedProperties.json", "com.example",
                config("dateType", clazz));
        Class<?> classWithDate = classLoader.loadClass("com.example.FormattedProperties");
        assertTypeIsExpected(classWithDate, "stringAsDate", clazz);
    }

    @Test
    public void disablingDateTypeCausesDefault() throws ClassNotFoundException, IntrospectionException {
        ClassLoader classLoader = generateAndCompile("/schema/format/formattedProperties.json", "com.example",
                config("dateType", null));
        Class<?> classWithDate = classLoader.loadClass("com.example.FormattedProperties");
        assertTypeIsExpected(classWithDate, "stringAsDate", "java.lang.String");
    }

    @Test
    public void timeTypeCausesCustomTimeType() throws IntrospectionException, ClassNotFoundException {
        String clazz="org.joda.time.LocalTime";
        ClassLoader classLoader = generateAndCompile("/schema/format/formattedProperties.json", "com.example",
                config("timeType", clazz));
        Class<?> classWithTime = classLoader.loadClass("com.example.FormattedProperties");
        assertTypeIsExpected(classWithTime, "stringAsTime", clazz);
    }

    @Test
    public void disablingTimeTypeCausesDefault() throws ClassNotFoundException, IntrospectionException {
        ClassLoader classLoader = generateAndCompile("/schema/format/formattedProperties.json", "com.example",
                config("timeType", null));
        Class<?> classWithTime = classLoader.loadClass("com.example.FormattedProperties");
        assertTypeIsExpected(classWithTime, "stringAsTime", "java.lang.String");
    }

    @Test(expected=GenerationException.class)
    public void throwsGenerationExceptionForUnknownDateTimeType() {
        generateAndCompile("/schema/format/formattedProperties.json", "com.example",
                config("dateTimeType", "org.jsonschema2pojo.integration.config.UnknownType"));
    }

    @Test(expected=GenerationException.class)
    public void throwsGenerationExceptionForUnknownDateType() {
        generateAndCompile("/schema/format/formattedProperties.json", "com.example",
                config("dateType", "org.jsonschema2pojo.integration.config.UnknownType"));
    }

    @Test(expected=GenerationException.class)
    public void throwsGenerationExceptionForUnknownTimeType() {
        generateAndCompile("/schema/format/formattedProperties.json", "com.example",
                config("timeType", "org.jsonschema2pojo.integration.config.UnknownType"));
    }

    private void assertTypeIsExpected(Class<?> classInstance, String propertyName, String expectedType)
            throws IntrospectionException {
        Method getter = new PropertyDescriptor(propertyName, classInstance).getReadMethod();
        assertThat(getter.getReturnType().getName(), is(expectedType));
    }

}
