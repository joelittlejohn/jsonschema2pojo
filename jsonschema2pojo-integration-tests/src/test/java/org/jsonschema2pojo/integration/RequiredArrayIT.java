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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.impl.DefaultJavaClass;

public class RequiredArrayIT extends RequiredIT {
    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    private static JavaClass classWithRequired;

    @BeforeClass
    public static void generateClasses() throws IOException {

        classSchemaRule.generateAndCompile("/schema/required/requiredArray.json", "com.example");
        File generatedJavaFile = classSchemaRule.generated("com/example/RequiredArray.java");

        JavaProjectBuilder javaDocBuilder = new JavaProjectBuilder();
        javaDocBuilder.addSource(generatedJavaFile);

        classWithRequired = javaDocBuilder.getClassByName("com.example.RequiredArray");
    }

    @Test
    public void requiredAppearsInFieldJavadoc() {

        JavaField javaField = classWithRequired.getFieldByName("requiredProperty");
        String javaDocComment = javaField.getComment();

        assertThat(javaDocComment, containsString("(Required)"));

    }

    @Test
    public void requiredAppearsInGetterJavadoc() {

        JavaMethod javaMethod = classWithRequired.getMethodBySignature("getRequiredProperty", Collections.emptyList());
        String javaDocComment = javaMethod.getComment();

        assertThat(javaDocComment, containsString("(Required)"));

    }

    @Test
    public void requiredAppearsInSetterJavadoc() {

        final List<JavaType> parameterTypes = Collections.singletonList(new DefaultJavaClass("java.lang.String"));
        JavaMethod javaMethod = classWithRequired.getMethodBySignature("setRequiredProperty", parameterTypes);
        String javaDocComment = javaMethod.getComment();

        assertThat(javaDocComment, containsString("(Required)"));

    }

    @Test
    public void nonRequiredFieldHasNoRequiredText() {

        JavaField javaField = classWithRequired.getFieldByName("nonRequiredProperty");
        String javaDocComment = javaField.getComment();

        assertThat(javaDocComment, not(containsString("(Required)")));

    }

    @Test
    public void notRequiredIsTheDefault() {

        JavaField javaField = classWithRequired.getFieldByName("defaultNotRequiredProperty");
        String javaDocComment = javaField.getComment();

        assertThat(javaDocComment, not(containsString("(Required)")));

    }

}
