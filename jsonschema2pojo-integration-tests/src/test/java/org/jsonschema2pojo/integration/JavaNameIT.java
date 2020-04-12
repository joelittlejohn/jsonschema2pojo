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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;

/**
 * Created by cmb on 31.01.16.
 */
public class JavaNameIT {

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void generateAndCompileClass() {


    }

    @Test
    public void propertiesHaveCorrectNames() throws IllegalAccessException, InstantiationException, ClassNotFoundException {

        ClassLoader javaNameClassLoader = schemaRule.generateAndCompile("/schema/javaName/javaName.json", "com.example.javaname");
        Class<?> classWithJavaNames = javaNameClassLoader.loadClass("com.example.javaname.JavaName");
        Object instance = classWithJavaNames.newInstance();

        assertThat(instance, hasProperty("javaProperty"));
        assertThat(instance, hasProperty("propertyWithoutJavaName"));
        assertThat(instance, hasProperty("javaEnum"));
        assertThat(instance, hasProperty("enumWithoutJavaName"));
        assertThat(instance, hasProperty("javaObject"));
        assertThat(instance, hasProperty("objectWithoutJavaName"));

    }

    @Test
    public void propertiesHaveCorrectTypes() throws IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchFieldException {

        ClassLoader javaNameClassLoader = schemaRule.generateAndCompile("/schema/javaName/javaName.json", "com.example.javaname");
        Class<?> classWithJavaNames = javaNameClassLoader.loadClass("com.example.javaname.JavaName");

        classWithJavaNames.newInstance();

        assertThat(classWithJavaNames.getDeclaredField("javaEnum").getType(), typeCompatibleWith(javaNameClassLoader.loadClass("com.example.javaname.JavaName$JavaEnum")));
        assertThat(classWithJavaNames.getDeclaredField("enumWithoutJavaName").getType(), typeCompatibleWith(javaNameClassLoader.loadClass("com.example.javaname.JavaName$EnumWithoutJavaName")));
        assertThat(classWithJavaNames.getDeclaredField("javaObject").getType(), typeCompatibleWith(javaNameClassLoader.loadClass("com.example.javaname.JavaObject")));
        assertThat(classWithJavaNames.getDeclaredField("objectWithoutJavaName").getType(), typeCompatibleWith(javaNameClassLoader.loadClass("com.example.javaname.ObjectWithoutJavaName")));

    }

    @Test
    public void gettersHaveCorrectNames() throws NoSuchMethodException, ClassNotFoundException {

        ClassLoader javaNameClassLoader = schemaRule.generateAndCompile("/schema/javaName/javaName.json", "com.example.javaname");
        Class<?> classWithJavaNames = javaNameClassLoader.loadClass("com.example.javaname.JavaName");

        classWithJavaNames.getMethod("getJavaProperty");
        classWithJavaNames.getMethod("getPropertyWithoutJavaName");
        classWithJavaNames.getMethod("getJavaEnum");
        classWithJavaNames.getMethod("getEnumWithoutJavaName");
        classWithJavaNames.getMethod("getJavaObject");
        classWithJavaNames.getMethod("getObjectWithoutJavaName");

    }

    @Test
    public void settersHaveCorrectNamesAndArgumentTypes() throws NoSuchMethodException, ClassNotFoundException {

        ClassLoader javaNameClassLoader = schemaRule.generateAndCompile("/schema/javaName/javaName.json", "com.example.javaname");
        Class<?> classWithJavaNames = javaNameClassLoader.loadClass("com.example.javaname.JavaName");

        classWithJavaNames.getMethod("setJavaProperty", String.class);
        classWithJavaNames.getMethod("setPropertyWithoutJavaName", String.class);

        classWithJavaNames.getMethod("setJavaEnum", javaNameClassLoader.loadClass("com.example.javaname.JavaName$JavaEnum"));
        classWithJavaNames.getMethod("setEnumWithoutJavaName", javaNameClassLoader.loadClass("com.example.javaname.JavaName$EnumWithoutJavaName"));

        classWithJavaNames.getMethod("setJavaObject", javaNameClassLoader.loadClass("com.example.javaname.JavaObject"));
        classWithJavaNames.getMethod("setObjectWithoutJavaName", javaNameClassLoader.loadClass("com.example.javaname.ObjectWithoutJavaName"));

    }

    @Test
    public void serializedPropertiesHaveCorrectNames() throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException, ClassNotFoundException {

        ClassLoader javaNameClassLoader = schemaRule.generateAndCompile("/schema/javaName/javaName.json", "com.example.javaname");
        Class<?> classWithJavaNames = javaNameClassLoader.loadClass("com.example.javaname.JavaName");
        Object instance = classWithJavaNames.newInstance();

        new PropertyDescriptor("javaProperty", classWithJavaNames).getWriteMethod().invoke(instance, "abc");
        new PropertyDescriptor("propertyWithoutJavaName", classWithJavaNames).getWriteMethod().invoke(instance, "abc");

        JsonNode serialized = mapper.valueToTree(instance);

        assertThat(serialized.has("propertyWithJavaName"), is(true));
        assertThat(serialized.has("propertyWithoutJavaName"), is(true));

    }

    @Test
    public void originalPropertyNamesAppearInJavaDoc() throws IOException {

        schemaRule.generateAndCompile("/schema/javaName/javaName.json", "com.example.javaname");
        File generatedJavaFile = schemaRule.generated("com/example/javaname/JavaName.java");

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(generatedJavaFile);

        JavaClass classWithDescription = javaDocBuilder.getClassByName("com.example.javaname.JavaName");

        JavaField javaPropertyField = classWithDescription.getFieldByName("javaProperty");
        assertThat(javaPropertyField.getComment(), containsString("Corresponds to the \"propertyWithJavaName\" property."));

        JavaField javaEnumField = classWithDescription.getFieldByName("javaEnum");
        assertThat(javaEnumField.getComment(), containsString("Corresponds to the \"enumWithJavaName\" property."));

        JavaField javaObjectField = classWithDescription.getFieldByName("javaObject");
        assertThat(javaObjectField.getComment(), containsString("Corresponds to the \"objectWithJavaName\" property."));

    }

    @Test(expected = IllegalArgumentException.class)
    public void doesNotAllowDuplicateNames() {

        schemaRule.generateAndCompile("/schema/javaName/duplicateName.json", "com.example");

    }

    @Test(expected = IllegalArgumentException.class)
    public void doesNotAllowDuplicateDefaultNames() {

        schemaRule.generateAndCompile("/schema/javaName/duplicateDefaultName.json", "com.example");

    }

    @Test
    public void arrayRequiredAppearsInFieldJavadoc() throws IOException {

        schemaRule.generateAndCompile("/schema/javaName/javaNameWithRequiredProperties.json", "com.example.required");
        File generatedJavaFileWithRequiredProperties = schemaRule.generated("com/example/required/JavaNameWithRequiredProperties.java");

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(generatedJavaFileWithRequiredProperties);

        JavaClass classWithRequiredProperties = javaDocBuilder.getClassByName("com.example.required.JavaNameWithRequiredProperties");

        JavaField javaFieldWithoutJavaName = classWithRequiredProperties.getFieldByName("requiredPropertyWithoutJavaName");
        JavaField javaFieldWithJavaName = classWithRequiredProperties.getFieldByName("requiredPropertyWithoutJavaName");

        assertThat(javaFieldWithoutJavaName.getComment(), containsString("(Required)"));
        assertThat(javaFieldWithJavaName.getComment(), containsString("(Required)"));
    }

    @Test
    public void inlineRequiredAppearsInFieldJavadoc() throws IOException {

        schemaRule.generateAndCompile("/schema/javaName/javaNameWithRequiredProperties.json", "com.example.required");
        File generatedJavaFileWithRequiredProperties = schemaRule.generated("com/example/required/JavaNameWithRequiredProperties.java");

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(generatedJavaFileWithRequiredProperties);

        JavaClass classWithRequiredProperties = javaDocBuilder.getClassByName("com.example.required.JavaNameWithRequiredProperties");

        JavaField javaFieldWithoutJavaName = classWithRequiredProperties.getFieldByName("inlineRequiredPropertyWithoutJavaName");
        JavaField javaFieldWithJavaName = classWithRequiredProperties.getFieldByName("inlineRequiredPropertyWithoutJavaName");

        assertThat(javaFieldWithoutJavaName.getComment(), containsString("(Required)"));
        assertThat(javaFieldWithJavaName.getComment(), containsString("(Required)"));
    }

    @Test
    public void generateClassInTargetPackage() throws IllegalAccessException, InstantiationException, ClassNotFoundException {

        ClassLoader javaNameClassLoader = schemaRule.generateAndCompile("/schema/javaName/AuthorizeRequest_v1p0.json", "com.example.javaname");
        Class<?> classWithTargetPackage = javaNameClassLoader.loadClass("com.example.javaname.OCSPRequestData");

        assertEquals("com.example.javaname.OCSPRequestData", classWithTargetPackage.getName());
    }

    @Test
    public void generateClassInJavaTypePackage() throws IllegalAccessException, InstantiationException, ClassNotFoundException {

        ClassLoader javaNameClassLoader = schemaRule.generateAndCompile("/schema/javaName/AuthorizeRequest_v1p0.json", "com.example.javaname");
        Class<?> classWithJavaTypePackage = javaNameClassLoader.loadClass("com.apetecan.javaname.AdditionalInfo");

        assertEquals("com.apetecan.javaname.AdditionalInfo", classWithJavaTypePackage.getName());
    }

    @Test
    public void generateClassInTargetPackageReferencingClassInJavaTypePackage() throws IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchFieldException {

        ClassLoader javaNameClassLoader = schemaRule.generateAndCompile("/schema/javaName/AuthorizeRequest_v1p0_2.json", "com.example.javaname");
        Class<?> classWithTargetPackage = javaNameClassLoader.loadClass("com.example.javaname.AuthorizeRequestV1p02");

        assertEquals("com.example.javaname.AuthorizeRequestV1p02", classWithTargetPackage.getName());

        classWithTargetPackage.newInstance();

        assertThat(classWithTargetPackage.getDeclaredField("idToken").getType(), typeCompatibleWith(javaNameClassLoader.loadClass("com.apetecan.javaname.IdToken")));
    }

    @Test
    public void generateReferencedClassUsesParentClassPackage() throws IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchFieldException {

        ClassLoader javaNameClassLoader = schemaRule.generateAndCompile("/schema/javaName/AuthorizeRequest_v1p0_2.json", "com.example.javaname");
        Class<?> classWithJavaTypePackage = javaNameClassLoader.loadClass("com.apetecan.javaname.IdToken");

        assertEquals("com.apetecan.javaname.IdToken", classWithJavaTypePackage.getName());

        classWithJavaTypePackage.newInstance();

        assertThat((Class<?>)((ParameterizedType)classWithJavaTypePackage.getDeclaredField("additionalInfo").getGenericType()).getActualTypeArguments()[0], typeCompatibleWith(javaNameClassLoader.loadClass("com.apetecan.javaname.AdditionalInfo")));
    }
}
