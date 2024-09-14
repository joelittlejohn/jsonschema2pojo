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

package org.jsonschema2pojo.integration.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.jsonschema2pojo.integration.util.CodeGenerationHelper;
import org.jsonschema2pojo.integration.util.TestableJsonschema2PojoMojo;
import org.jsonschema2pojo.maven.Jsonschema2PojoMojo;
import org.junit.Test;

public class Jsonschema2PojoForIdRef {

	static File rootDirectory() {
		return new File("target" + File.separator + "jsonschema2pojo");
	}

	@Test
	public void sourcesAndCompile() throws ClassNotFoundException, NoSuchFieldException, SecurityException {

		Map<String, Map<String, String>> classNameWithExpectedFieldAndType = new HashMap<>();
		{
			Map<String, String> expectedFieldAndType = new HashMap<>();

			expectedFieldAndType.put("dataFirst1", "Set<DataFirst>");
			expectedFieldAndType.put("dataFirst2", "DataFirst");
			expectedFieldAndType.put("dataSecond1", "DataSecond");
			expectedFieldAndType.put("prop", "Prop");

			classNameWithExpectedFieldAndType.put("MainData", expectedFieldAndType);
		}
		{
			Map<String, String> expectedFieldAndType = new HashMap<>();

			expectedFieldAndType.put("dataSecond", "DataSecond");

			classNameWithExpectedFieldAndType.put("DataFirst", expectedFieldAndType);
		}
		{
			Map<String, String> expectedFieldAndType = new HashMap<>();

			expectedFieldAndType.put("dataFirst", "Set<DataFirst>");
			expectedFieldAndType.put("dataMain", "MainData");
			expectedFieldAndType.put("dataSecond", "DataSecond");

			classNameWithExpectedFieldAndType.put("DataSecond", expectedFieldAndType);
		}
		{
			Map<String, String> expectedFieldAndType = new HashMap<>();

			expectedFieldAndType.put("id", "Integer");
			expectedFieldAndType.put("otherMainData", "MainData");

			classNameWithExpectedFieldAndType.put("Prop", expectedFieldAndType);
		}

		try {
			@SuppressWarnings("serial")
			File rootDirectory = new File(rootDirectory(), "/test_$id");
			File generateDirectory = new File(rootDirectory, "generate");
			File compileDirectory = new File(rootDirectory, "compile");
			generateDirectory.mkdirs();
			compileDirectory.mkdirs();
			String packageName = "org.testId";

			Jsonschema2PojoMojo pluginMojo = new TestableJsonschema2PojoMojo().configure(new HashMap<String, Object>() {
				{
					put("sourceDirectory", new File("src/test/resources/json/examples_$id").toString());
					put("outputDirectory", generateDirectory);
					put("project", getMockProject());
					put("targetPackage", packageName);
				}
			});

			pluginMojo.execute();
			assertTrue("Generation Ok",true);

			CodeGenerationHelper.compile(generateDirectory, compileDirectory, Collections.EMPTY_LIST,
					Collections.EMPTY_MAP);

			assertTrue("Compile Ok",true);

			URLClassLoader classLoader = new URLClassLoader(new URL[] { compileDirectory.toURL() },
					this.getClass().getClassLoader());
			
			for (Entry<String, Map<String, String>> classes : classNameWithExpectedFieldAndType.entrySet()) {
				String classInpect = classes.getKey();
				Class<?> clazz = classLoader.loadClass(packageName+"."+classInpect);
				for (Entry<String, String> field : classes.getValue().entrySet()) {
					String filedName = field.getKey();
					Field declaredField = clazz.getDeclaredField(filedName);
					Class<?> type = declaredField.getType();
					if (Collection.class.isAssignableFrom(type)) {
						//Look for Generic
						ParameterizedType genericType = (ParameterizedType)declaredField.getGenericType();
						assertNotNull("Generic type of the collection can't be null", genericType);
						assertEquals("In "+classInpect+" the filedName "+filedName+" doesn't match", type.getSimpleName()+"<"+((Class)genericType.getActualTypeArguments()[0]).getSimpleName()+">", field.getValue());
					}else {
						assertEquals(type.getSimpleName(), field.getValue());
					}
				}
			}
			assertTrue("All field has been found",true);

		} catch (MojoExecutionException | DependencyResolutionRequiredException | MalformedURLException e) {
			throw new RuntimeException(e);
		}

	}

	private static MavenProject getMockProject() throws DependencyResolutionRequiredException {

		MavenProject project = mock(MavenProject.class);
		when(project.getCompileClasspathElements()).thenReturn(new ArrayList<String>());

		return project;
	}

}
