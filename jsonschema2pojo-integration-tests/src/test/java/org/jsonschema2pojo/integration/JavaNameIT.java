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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.typeCompatibleWith;
import static org.junit.Assert.assertThat;

/**
 * Created by cmb on 31.01.16.
 */
public class JavaNameIT {

    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    private final ObjectMapper mapper = new ObjectMapper();

    private static Class<?> classWithJavaNames;
    private static ClassLoader resultsClassLoader;

    @BeforeClass
    public static void generateAndCompileClass() throws ClassNotFoundException {

        resultsClassLoader = classSchemaRule.generateAndCompile("/schema/javaName/javaName.json", "com.example");

        classWithJavaNames = resultsClassLoader.loadClass("com.example.JavaName");

    }

    @Test
    public void propertiesHaveCorrectNames() throws IllegalAccessException, InstantiationException {

        Object instance = classWithJavaNames.newInstance();

        assertThat(instance, hasProperty("javaProperty"));
        assertThat(instance, hasProperty("propertyWithoutJavaName"));
        assertThat(instance, hasProperty("javaEnum"));
        assertThat(instance, hasProperty("enumWithoutJavaName"));
        assertThat(instance, hasProperty("javaObject"));
        assertThat(instance, hasProperty("objectWithoutJavaName"));

    }

    @Test
    public void propertiesHaveCorrectTypes() throws IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchFieldException, IntrospectionException {

        assertThat(classWithJavaNames.getDeclaredField("javaEnum").getType(), typeCompatibleWith(resultsClassLoader.loadClass("com.example.JavaName$JavaEnum")));
        assertThat(classWithJavaNames.getDeclaredField("enumWithoutJavaName").getType(), typeCompatibleWith(resultsClassLoader.loadClass("com.example.JavaName$EnumWithoutJavaName")));
        assertThat(classWithJavaNames.getDeclaredField("javaObject").getType(), typeCompatibleWith(resultsClassLoader.loadClass("com.example.JavaObject")));
        assertThat(classWithJavaNames.getDeclaredField("objectWithoutJavaName").getType(), typeCompatibleWith(resultsClassLoader.loadClass("com.example.ObjectWithoutJavaName")));

    }

    @Test
    public void gettersHaveCorrectNames() throws NoSuchMethodException {

        classWithJavaNames.getMethod("getJavaProperty");
        classWithJavaNames.getMethod("getPropertyWithoutJavaName");
        classWithJavaNames.getMethod("getJavaEnum");
        classWithJavaNames.getMethod("getEnumWithoutJavaName");
        classWithJavaNames.getMethod("getJavaObject");
        classWithJavaNames.getMethod("getObjectWithoutJavaName");

    }

    @Test
    public void settersHaveCorrectNamesAndArgumentTypes() throws NoSuchMethodException, ClassNotFoundException {

        classWithJavaNames.getMethod("setJavaProperty", String.class);
        classWithJavaNames.getMethod("setPropertyWithoutJavaName", String.class);

        classWithJavaNames.getMethod("setJavaEnum", resultsClassLoader.loadClass("com.example.JavaName$JavaEnum"));
        classWithJavaNames.getMethod("setEnumWithoutJavaName", resultsClassLoader.loadClass("com.example.JavaName$EnumWithoutJavaName"));

        classWithJavaNames.getMethod("setJavaObject", resultsClassLoader.loadClass("com.example.JavaObject"));
        classWithJavaNames.getMethod("setObjectWithoutJavaName", resultsClassLoader.loadClass("com.example.ObjectWithoutJavaName"));

    }

    @Test
    public void serializedPropertiesHaveCorrectNames() throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {

        Object instance = classWithJavaNames.newInstance();

        new PropertyDescriptor("javaProperty", classWithJavaNames).getWriteMethod().invoke(instance, "abc");
        new PropertyDescriptor("propertyWithoutJavaName", classWithJavaNames).getWriteMethod().invoke(instance, "abc");

        JsonNode serialized = mapper.valueToTree(instance);

        assertThat(serialized.has("propertyWithJavaName"), is(true));
        assertThat(serialized.has("propertyWithoutJavaName"), is(true));

    }

    @Test
    public void originalPropertyNamesAppearInJavaDoc() throws NoSuchFieldException, IOException {

        File generatedJavaFile = classSchemaRule.generated("com/example/JavaName.java");

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(generatedJavaFile);

        JavaClass classWithDescription = javaDocBuilder.getClassByName("com.example.JavaName");

        JavaField javaPropertyField = classWithDescription.getFieldByName("javaProperty");
        assertThat(javaPropertyField.getComment(), containsString("Corresponds to the \"propertyWithJavaName\" property."));

        JavaField javaEnumField = classWithDescription.getFieldByName("javaEnum");
        assertThat(javaEnumField.getComment(), containsString("Corresponds to the \"enumWithJavaName\" property."));

        JavaField javaObjectField = classWithDescription.getFieldByName("javaObject");
        assertThat(javaObjectField.getComment(), containsString("Corresponds to the \"objectWithJavaName\" property."));

    }

    @Test(expected = IllegalArgumentException.class)
    public void doesNotAllowDuplicateNames() {

        classSchemaRule.generateAndCompile("/schema/javaName/duplicateName.json", "com.example");

    }

    @Test(expected = IllegalArgumentException.class)
    public void doesNotAllowDuplicateDefaultNames() {

        classSchemaRule.generateAndCompile("/schema/javaName/duplicateDefaultName.json", "com.example");

    }

}
