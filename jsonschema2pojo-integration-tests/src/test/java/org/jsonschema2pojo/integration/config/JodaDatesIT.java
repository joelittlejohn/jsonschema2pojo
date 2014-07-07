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

package org.jsonschema2pojo.integration.config;

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.Assert.*;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.joda.time.DateTime;
import org.junit.Test;

public class JodaDatesIT {

    @Test
    public void defaultDateTypeIsJavaUtilDate() throws ClassNotFoundException, IntrospectionException {
        ClassLoader classLoader = generateAndCompile("/schema/format/formattedProperties.json", "com.example");

        Class<?> classWithDate = classLoader.loadClass("com.example.FormattedProperties");

        Method getter = new PropertyDescriptor("stringAsDateTime", classWithDate).getReadMethod();

        assertThat(getter.getReturnType().getName(), is("java.util.Date"));
    }

    @Test
    public void useJodaDatesCausesJodaDateTimeDates() throws IntrospectionException, ClassNotFoundException {
        ClassLoader classLoader = generateAndCompile("/schema/format/formattedProperties.json", "com.example", config("useJodaDates", true));

        Class<?> classWithDate = classLoader.loadClass("com.example.FormattedProperties");

        Method getter = new PropertyDescriptor("stringAsDateTime", classWithDate).getReadMethod();

        assertThat(getter.getReturnType().getName(), is("org.joda.time.DateTime"));
    }

    @Test
    public void disablingJodaDatesCausesJavaUtilDates() throws ClassNotFoundException, IntrospectionException {
        ClassLoader classLoader = generateAndCompile("/schema/format/formattedProperties.json", "com.example", config("useJodaDates", false));

        Class<?> classWithDate = classLoader.loadClass("com.example.FormattedProperties");

        Method getter = new PropertyDescriptor("stringAsDateTime", classWithDate).getReadMethod();

        assertThat(getter.getReturnType().getName(), is("java.util.Date"));
    }

    @Test
    public void useJodaDatesCausesDateTimeDefaultValues() throws ClassNotFoundException, IntrospectionException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException {
        ClassLoader classLoader = generateAndCompile("/schema/default/default.json", "com.example", config("useJodaDates", true));

        Class<?> classWithDefaults = classLoader.loadClass("com.example.Default");

        Object instance = classWithDefaults.newInstance();

        Method getter = classWithDefaults.getMethod("getDateWithDefault");

        assertThat((DateTime) getter.invoke(instance), is(equalTo(new DateTime(123456789))));
    }

}
