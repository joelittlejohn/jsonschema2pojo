/**
 * Copyright © 2010-2017 Nokia
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo.integration.config;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.assertThat;

public class CustomArraysIT {

	@Rule
	public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

	@Test
	public void defaultTypesAreNotCustom() throws IntrospectionException, ClassNotFoundException {

		ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/array/typeWithArrayProperties.json", "com.example");

		Class<?> classWithArrayProperties = resultsClassLoader.loadClass("com.example.TypeWithArrayProperties");

		List<String[]> defaultTypes = Arrays.asList(
				new String[]{"nonUniqueArray", "java.util.List"},
				new String[]{"uniqueArray", "java.util.Set"},
				new String[]{"nonUniqueArrayByDefault", "java.util.List"},
				new String[]{"complexTypesArray", "java.util.List"},
				new String[]{"defaultTypesArray", "java.util.List"},
				new String[]{"multiDimensionalArray", "java.util.List"},
				new String[]{"refToArray1", "java.util.List"}
		);

		for (String[] defaultType : defaultTypes) {
			assertTypeIsExpected(classWithArrayProperties, defaultType[0], defaultType[1]);
		}
	}

	@Test
	public void listTypeCausesCustomListType() throws IntrospectionException, ClassNotFoundException {
		String clazz = scala.collection.immutable.List.class.getName();
		ClassLoader classLoader = schemaRule.generateAndCompile("/schema/array/typeWithArrayProperties.json", "com.example",
				config("listType", clazz));
		Class<?> classWithArray = classLoader.loadClass("com.example.TypeWithArrayProperties");
		assertTypeIsExpected(classWithArray, "nonUniqueArray", clazz);
	}

	@Test
	public void disablingListTypeCausesDefault() throws ClassNotFoundException, IntrospectionException {
		ClassLoader classLoader = schemaRule.generateAndCompile("/schema/array/typeWithArrayProperties.json", "com.example",
				config("listType", null));
		Class<?> classWithDate = classLoader.loadClass("com.example.TypeWithArrayProperties");
		assertTypeIsExpected(classWithDate, "nonUniqueArray", "java.util.List");
	}

	@Test
	public void setTypeCausesCustomSetType() throws IntrospectionException, ClassNotFoundException {
		String clazz = scala.collection.immutable.Set.class.getName();
		ClassLoader classLoader = schemaRule.generateAndCompile("/schema/array/typeWithArrayProperties.json", "com.example",
				config("setType", clazz));
		Class<?> classWithArray = classLoader.loadClass("com.example.TypeWithArrayProperties");
		assertTypeIsExpected(classWithArray, "uniqueArray", clazz);
	}

	@Test
	public void disablingSetTypeCausesDefault() throws ClassNotFoundException, IntrospectionException {
		ClassLoader classLoader = schemaRule.generateAndCompile("/schema/array/typeWithArrayProperties.json", "com.example",
				config("setType", null));
		Class<?> classWithDate = classLoader.loadClass("com.example.TypeWithArrayProperties");
		assertTypeIsExpected(classWithDate, "uniqueArray", "java.util.Set");
	}

	private void assertTypeIsExpected(Class<?> classInstance, String propertyName, String expectedType)
			throws IntrospectionException {
		Method getter = new PropertyDescriptor(propertyName, classInstance).getReadMethod();
		assertThat(getter.getReturnType().getName(), is(expectedType));
	}

}
