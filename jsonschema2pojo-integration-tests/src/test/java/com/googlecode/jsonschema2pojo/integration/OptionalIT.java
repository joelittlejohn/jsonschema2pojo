/**
 * Copyright © 2011 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless optional by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.jsonschema2pojo.integration;

import static com.googlecode.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
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

public class OptionalIT {

    private static JavaClass classWithOptional;

    @BeforeClass
    public static void generateClasses() throws ClassNotFoundException, IOException {

        File outputDirectory = generate("/schema/optional/optional.json", "com.example", true);
        File generatedJavaFile = new File(outputDirectory, "com/example/Optional.java");

        compile(outputDirectory);

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(generatedJavaFile);

        classWithOptional = javaDocBuilder.getClassByName("com.example.Optional");
    }

    @Test
    public void optionalAppearsInFieldJavadoc() throws IOException {

        JavaField javaField = classWithOptional.getFieldByName("optionalProperty");
        String javaDocComment = javaField.getComment();

        assertThat(javaDocComment, containsString("(Optional)"));

    }

    @Test
    public void optionalAppearsInGetterJavadoc() throws IOException {

        JavaMethod javaMethod = classWithOptional.getMethodBySignature("getOptionalProperty", new Type[] {});
        String javaDocComment = javaMethod.getComment();

        assertThat(javaDocComment, containsString("(Optional)"));

    }

    @Test
    public void optionalAppearsInSetterJavadoc() throws IOException {

        JavaMethod javaMethod = classWithOptional.getMethodBySignature("setOptionalProperty", new Type[] {new Type("java.lang.String")});
        String javaDocComment = javaMethod.getComment();

        assertThat(javaDocComment, containsString("(Optional)"));

    }

    @Test
    public void nonOptionalFiedHasNoOptionalText() throws IOException {

        JavaField javaField = classWithOptional.getFieldByName("nonOptionalProperty");
        String javaDocComment = javaField.getComment();

        assertThat(javaDocComment, not(containsString("(Optional)")));

    }

    @Test
    public void notOptionalIsTheDefault() throws IOException {

        JavaField javaField = classWithOptional.getFieldByName("defaultNotOptionalProperty");
        String javaDocComment = javaField.getComment();

        assertThat(javaDocComment, not(containsString("(Optional)")));

    }

}
