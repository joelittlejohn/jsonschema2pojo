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

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.Type;

public class RequiredIT {

    private static JavaClass classWithRequired;

    @BeforeClass
    public static void generateClasses() throws ClassNotFoundException, IOException {

        File outputDirectory = generate("/schema/required/required.json", "com.example");
        File generatedJavaFile = new File(outputDirectory, "com/example/Required.java");

        compile(outputDirectory);

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(generatedJavaFile);

        classWithRequired = javaDocBuilder.getClassByName("com.example.Required");
    }

    @Test
    public void requiredAppearsInFieldJavadoc() throws IOException {

        JavaField javaField = classWithRequired.getFieldByName("requiredProperty");
        String javaDocComment = javaField.getComment();

        assertThat(javaDocComment, containsString("(Required)"));

    }

    @Test
    public void requiredAppearsInGetterJavadoc() throws IOException {

        JavaMethod javaMethod = classWithRequired.getMethodBySignature("getRequiredProperty", new Type[] {});
        String javaDocComment = javaMethod.getComment();

        assertThat(javaDocComment, containsString("(Required)"));

    }

    @Test
    public void requiredAppearsInSetterJavadoc() throws IOException {

        JavaMethod javaMethod = classWithRequired.getMethodBySignature("setRequiredProperty", new Type[] { new Type("java.lang.String") });
        String javaDocComment = javaMethod.getComment();

        assertThat(javaDocComment, containsString("(Required)"));

    }

    @Test
    public void nonRequiredFiedHasNoRequiredText() throws IOException {

        JavaField javaField = classWithRequired.getFieldByName("nonRequiredProperty");
        String javaDocComment = javaField.getComment();

        assertThat(javaDocComment, not(containsString("(Required)")));

    }

    @Test
    public void notRequiredIsTheDefault() throws IOException {

        JavaField javaField = classWithRequired.getFieldByName("defaultNotRequiredProperty");
        String javaDocComment = javaField.getComment();

        assertThat(javaDocComment, not(containsString("(Required)")));

    }

}
