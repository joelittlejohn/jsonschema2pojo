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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoTestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.Type;

public class RequiredIT extends Jsonschema2PojoTestBase {

    private static JavaClass classWithRequired;

    @BeforeEach
    public void generateClasses() throws IOException {

        generateAndCompile("/schema/required/required.json", "com.example");
        File generatedJavaFile = generated("com/example/Required.java");

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(generatedJavaFile);

        classWithRequired = javaDocBuilder.getClassByName("com.example.Required");
    }

    @Test
    public void requiredAppearsInFieldJavadoc() {

        JavaField javaField = classWithRequired.getFieldByName("requiredProperty");
        String javaDocComment = javaField.getComment();

        assertThat(javaDocComment, containsString("(Required)"));

    }

    @Test
    public void requiredAppearsInGetterJavadoc() {

        JavaMethod javaMethod = classWithRequired.getMethodBySignature("getRequiredProperty", new Type[] {});
        String javaDocComment = javaMethod.getComment();

        assertThat(javaDocComment, containsString("(Required)"));

    }

    @Test
    public void requiredAppearsInSetterJavadoc() {

        JavaMethod javaMethod = classWithRequired.getMethodBySignature("setRequiredProperty", new Type[] { new Type("java.lang.String") });
        String javaDocComment = javaMethod.getComment();

        assertThat(javaDocComment, containsString("(Required)"));

    }

    @Test
    public void nonRequiredFiedHasNoRequiredText() {

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
