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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.Assert.*;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class IncludeTypeInfoIT
{
    @Rule
    public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void defaultConfig() throws ClassNotFoundException
    {

        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/typeInfo/typeInfo.json", "com.example");

        Class<?> classWithTypeInfo = classLoader.loadClass("com.example.TypeInfo");

        assertNull(classWithTypeInfo.getAnnotation(JsonTypeInfo.class));
    }

    @Test
    public void defaultWithSchemaProperty() throws ClassNotFoundException
    {

        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/typeInfo/typeInfoWithSchemaProperty.json", "com.example");

        Class<?> classWithTypeInfo = classLoader.loadClass("com.example.TypeInfoWithSchemaProperty");

        assertNotNull(classWithTypeInfo.getAnnotation(JsonTypeInfo.class));

        JsonTypeInfo jsonTypeInfo = classWithTypeInfo.getAnnotation(JsonTypeInfo.class);
        assertThat(jsonTypeInfo.use(), is(JsonTypeInfo.Id.CLASS));
        assertThat(jsonTypeInfo.include(), is(JsonTypeInfo.As.PROPERTY));
        assertThat(jsonTypeInfo.property(), is("@clazz"));
    }

    @Test
    public void disabled() throws ClassNotFoundException
    {

        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/typeInfo/typeInfo.json", "com.example",
                                                                config("includeTypeInfo", false));

        Class<?> classWithTypeInfo = classLoader.loadClass("com.example.TypeInfo");

        assertNull(classWithTypeInfo.getAnnotation(JsonTypeInfo.class));
    }

    @Test
    public void disabledWithSchemaProperty() throws ClassNotFoundException
    {

        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/typeInfo/typeInfoWithSchemaProperty.json", "com.example",
                                                                config("includeTypeInfo", false));
        Class<?> classWithTypeInfo = classLoader.loadClass("com.example.TypeInfoWithSchemaProperty");

        assertNotNull(classWithTypeInfo.getAnnotation(JsonTypeInfo.class));

        JsonTypeInfo jsonTypeInfo = classWithTypeInfo.getAnnotation(JsonTypeInfo.class);
        assertThat(jsonTypeInfo.use(), is(JsonTypeInfo.Id.CLASS));
        assertThat(jsonTypeInfo.include(), is(JsonTypeInfo.As.PROPERTY));
        assertThat(jsonTypeInfo.property(), is("@clazz"));
    }

    @Test
    public void enabled() throws ClassNotFoundException
    {

        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/typeInfo/typeInfo.json", "com.example",
                                                                config("includeTypeInfo", true));

        Class<?> classWithTypeInfo = classLoader.loadClass("com.example.TypeInfo");

        assertNotNull(classWithTypeInfo.getAnnotation(JsonTypeInfo.class));

        JsonTypeInfo jsonTypeInfo = classWithTypeInfo.getAnnotation(JsonTypeInfo.class);
        assertThat(jsonTypeInfo.use(), is(JsonTypeInfo.Id.CLASS));
        assertThat(jsonTypeInfo.include(), is(JsonTypeInfo.As.PROPERTY));
        assertThat(jsonTypeInfo.property(), is("@class"));
    }

    @Test
    public void enabledWithSchemaProperty() throws ClassNotFoundException
    {

        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/typeInfo/typeInfoWithSchemaProperty.json", "com.example",
                                                                config("includeTypeInfo", true));
        Class<?> classWithTypeInfo = classLoader.loadClass("com.example.TypeInfoWithSchemaProperty");

        assertNotNull(classWithTypeInfo.getAnnotation(JsonTypeInfo.class));

        JsonTypeInfo jsonTypeInfo = classWithTypeInfo.getAnnotation(JsonTypeInfo.class);
        assertThat(jsonTypeInfo.use(), is(JsonTypeInfo.Id.CLASS));
        assertThat(jsonTypeInfo.include(), is(JsonTypeInfo.As.PROPERTY));
        assertThat(jsonTypeInfo.property(), is("@clazz"));
    }
}
