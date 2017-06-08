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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.generateAndCompile;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonFormat;

@SuppressWarnings("rawtypes")
public class JsonFormatIT {

    @Test
    public void jsonFormatAddedWithDateOnly() throws ClassNotFoundException, NoSuchFieldException, SecurityException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsonFormat/dateOnly.json", "com.example");

        Class generatedType = resultsClassLoader.loadClass("com.example.DateOnly");
        Field field = generatedType.getDeclaredField("lastUpdated");
        assertThat(field.getAnnotation(JsonFormat.class), is(notNullValue()));
        JsonFormat annotation = field.getAnnotation(JsonFormat.class);
        assertThat(annotation.pattern(), is(equalTo("yyyy-MM-dd")));
        assertThat(annotation.shape(), is(equalTo(JsonFormat.Shape.STRING)));
    }
    
    @Test
    public void jsonFormatAddedWithDateAndTime() throws ClassNotFoundException, NoSuchFieldException, SecurityException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsonFormat/dateTime.json", "com.example");

        Class generatedType = resultsClassLoader.loadClass("com.example.DateTime");
        Field field = generatedType.getDeclaredField("lastUpdated");
        assertThat(field.getAnnotation(JsonFormat.class), is(notNullValue()));
        JsonFormat annotation = field.getAnnotation(JsonFormat.class);
        assertThat(annotation.pattern(), is(equalTo("yyyy-MM-dd HH:mm")));
        assertThat(annotation.shape(), is(equalTo(JsonFormat.Shape.STRING)));
    }
}
