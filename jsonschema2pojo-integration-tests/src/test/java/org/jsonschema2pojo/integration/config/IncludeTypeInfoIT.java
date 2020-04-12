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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

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
		assertNull(classWithTypeInfo.getAnnotation(org.codehaus.jackson.annotate.JsonTypeInfo.class));
	}

	@Test
	public void defaultJackson1() throws ClassNotFoundException
	{

		ClassLoader classLoader = schemaRule.generateAndCompile("/schema/typeInfo/typeInfo.json", "com.example",
																config("annotationStyle", "JACKSON1"));

		Class<?> classWithTypeInfo = classLoader.loadClass("com.example.TypeInfo");

		assertNull(classWithTypeInfo.getAnnotation(JsonTypeInfo.class));
		assertNull(classWithTypeInfo.getAnnotation(org.codehaus.jackson.annotate.JsonTypeInfo.class));
	}

	@Test
	public void defaultJackson1WithSchemaProperty() throws ClassNotFoundException
	{

		ClassLoader classLoader = schemaRule.generateAndCompile("/schema/typeInfo/typeInfoWithSchemaProperty.json", "com.example",
																config("annotationStyle", "JACKSON1"));
		Class<?> classWithTypeInfo = classLoader.loadClass("com.example.TypeInfoWithSchemaProperty");

		assertNull(classWithTypeInfo.getAnnotation(JsonTypeInfo.class));
		assertNotNull(classWithTypeInfo.getAnnotation(org.codehaus.jackson.annotate.JsonTypeInfo.class));

		org.codehaus.jackson.annotate.JsonTypeInfo jsonTypeInfo = classWithTypeInfo.getAnnotation(org.codehaus.jackson.annotate.JsonTypeInfo.class);
		assertThat(jsonTypeInfo.use(), is(org.codehaus.jackson.annotate.JsonTypeInfo.Id.CLASS));
		assertThat(jsonTypeInfo.include(), is(org.codehaus.jackson.annotate.JsonTypeInfo.As.PROPERTY));
		assertThat(jsonTypeInfo.property(), is("@clazz"));
	}

	@Test
	public void defaultWithSchemaProperty() throws ClassNotFoundException
	{

		ClassLoader classLoader = schemaRule.generateAndCompile("/schema/typeInfo/typeInfoWithSchemaProperty.json", "com.example");

		Class<?> classWithTypeInfo = classLoader.loadClass("com.example.TypeInfoWithSchemaProperty");

		assertNotNull(classWithTypeInfo.getAnnotation(JsonTypeInfo.class));
		assertNull(classWithTypeInfo.getAnnotation(org.codehaus.jackson.annotate.JsonTypeInfo.class));

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
		assertNull(classWithTypeInfo.getAnnotation(org.codehaus.jackson.annotate.JsonTypeInfo.class));
	}

	@Test
	public void disabledJackson1() throws ClassNotFoundException
	{

		ClassLoader classLoader = schemaRule.generateAndCompile("/schema/typeInfo/typeInfo.json", "com.example",
																config("annotationStyle", "JACKSON1",
																	   "includeTypeInfo", false));

		Class<?> classWithTypeInfo = classLoader.loadClass("com.example.TypeInfo");

		assertNull(classWithTypeInfo.getAnnotation(JsonTypeInfo.class));
		assertNull(classWithTypeInfo.getAnnotation(org.codehaus.jackson.annotate.JsonTypeInfo.class));
	}

	@Test
	public void disabledJackson1WithSchemaProperty() throws ClassNotFoundException
	{

		ClassLoader classLoader = schemaRule.generateAndCompile("/schema/typeInfo/typeInfoWithSchemaProperty.json", "com.example",
																config("annotationStyle", "JACKSON1",
																	   "includeTypeInfo", false));
		Class<?> classWithTypeInfo = classLoader.loadClass("com.example.TypeInfoWithSchemaProperty");

		assertNull(classWithTypeInfo.getAnnotation(JsonTypeInfo.class));
		assertNotNull(classWithTypeInfo.getAnnotation(org.codehaus.jackson.annotate.JsonTypeInfo.class));

		org.codehaus.jackson.annotate.JsonTypeInfo jsonTypeInfo = classWithTypeInfo.getAnnotation(org.codehaus.jackson.annotate.JsonTypeInfo.class);
		assertThat(jsonTypeInfo.use(), is(org.codehaus.jackson.annotate.JsonTypeInfo.Id.CLASS));
		assertThat(jsonTypeInfo.include(), is(org.codehaus.jackson.annotate.JsonTypeInfo.As.PROPERTY));
		assertThat(jsonTypeInfo.property(), is("@clazz"));
	}

	@Test
	public void disabledWithSchemaProperty() throws ClassNotFoundException
	{

		ClassLoader classLoader = schemaRule.generateAndCompile("/schema/typeInfo/typeInfoWithSchemaProperty.json", "com.example",
																config("includeTypeInfo", false));
		Class<?> classWithTypeInfo = classLoader.loadClass("com.example.TypeInfoWithSchemaProperty");

		assertNotNull(classWithTypeInfo.getAnnotation(JsonTypeInfo.class));
		assertNull(classWithTypeInfo.getAnnotation(org.codehaus.jackson.annotate.JsonTypeInfo.class));

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
		assertNull(classWithTypeInfo.getAnnotation(org.codehaus.jackson.annotate.JsonTypeInfo.class));

		JsonTypeInfo jsonTypeInfo = classWithTypeInfo.getAnnotation(JsonTypeInfo.class);
		assertThat(jsonTypeInfo.use(), is(JsonTypeInfo.Id.CLASS));
		assertThat(jsonTypeInfo.include(), is(JsonTypeInfo.As.PROPERTY));
		assertThat(jsonTypeInfo.property(), is("@class"));
	}

	@Test
	public void enabledJackson1() throws ClassNotFoundException
	{

		ClassLoader classLoader = schemaRule.generateAndCompile("/schema/typeInfo/typeInfo.json", "com.example",
																config("annotationStyle", "JACKSON1",
																	   "includeTypeInfo", true));

		Class<?> classWithTypeInfo = classLoader.loadClass("com.example.TypeInfo");

		assertNull(classWithTypeInfo.getAnnotation(JsonTypeInfo.class));
		assertNotNull(classWithTypeInfo.getAnnotation(org.codehaus.jackson.annotate.JsonTypeInfo.class));

		org.codehaus.jackson.annotate.JsonTypeInfo jsonTypeInfo = classWithTypeInfo.getAnnotation(org.codehaus.jackson.annotate.JsonTypeInfo.class);
		assertThat(jsonTypeInfo.use(), is(org.codehaus.jackson.annotate.JsonTypeInfo.Id.CLASS));
		assertThat(jsonTypeInfo.include(), is(org.codehaus.jackson.annotate.JsonTypeInfo.As.PROPERTY));
		assertThat(jsonTypeInfo.property(), is("@class"));
	}

	@Test
	public void enabledJackson1WithSchemaProperty() throws ClassNotFoundException
	{

		ClassLoader classLoader = schemaRule.generateAndCompile("/schema/typeInfo/typeInfoWithSchemaProperty.json", "com.example",
																config("annotationStyle", "JACKSON1",
																	   "includeTypeInfo", true));
		Class<?> classWithTypeInfo = classLoader.loadClass("com.example.TypeInfoWithSchemaProperty");

		assertNull(classWithTypeInfo.getAnnotation(JsonTypeInfo.class));
		assertNotNull(classWithTypeInfo.getAnnotation(org.codehaus.jackson.annotate.JsonTypeInfo.class));

		org.codehaus.jackson.annotate.JsonTypeInfo jsonTypeInfo = classWithTypeInfo.getAnnotation(org.codehaus.jackson.annotate.JsonTypeInfo.class);
		assertThat(jsonTypeInfo.use(), is(org.codehaus.jackson.annotate.JsonTypeInfo.Id.CLASS));
		assertThat(jsonTypeInfo.include(), is(org.codehaus.jackson.annotate.JsonTypeInfo.As.PROPERTY));
		assertThat(jsonTypeInfo.property(), is("@clazz"));
	}

	@Test
	public void enabledWithSchemaProperty() throws ClassNotFoundException
	{

		ClassLoader classLoader = schemaRule.generateAndCompile("/schema/typeInfo/typeInfoWithSchemaProperty.json", "com.example",
																config("includeTypeInfo", true));
		Class<?> classWithTypeInfo = classLoader.loadClass("com.example.TypeInfoWithSchemaProperty");

		assertNotNull(classWithTypeInfo.getAnnotation(JsonTypeInfo.class));
		assertNull(classWithTypeInfo.getAnnotation(org.codehaus.jackson.annotate.JsonTypeInfo.class));

		JsonTypeInfo jsonTypeInfo = classWithTypeInfo.getAnnotation(JsonTypeInfo.class);
		assertThat(jsonTypeInfo.use(), is(JsonTypeInfo.Id.CLASS));
		assertThat(jsonTypeInfo.include(), is(JsonTypeInfo.As.PROPERTY));
		assertThat(jsonTypeInfo.property(), is("@clazz"));
	}
}
