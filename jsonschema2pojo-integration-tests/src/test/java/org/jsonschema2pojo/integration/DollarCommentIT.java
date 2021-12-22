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

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.Type;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DollarCommentIT extends Jsonschema2PojoTestBase {

    private JavaClass classWithDescription;

    @BeforeEach
    public void generateClasses() throws IOException {

        generateAndCompile("/schema/dollar_comment/dollar_comment.json", "com.example");
        File generatedJavaFile = generated("com/example/DollarComment.java");

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(generatedJavaFile);

        classWithDescription = javaDocBuilder.getClassByName("com.example.DollarComment");
    }

    @Test
    public void dollarCommentAppearsInClassJavadoc() {

        String javaDocComment = classWithDescription.getComment();

        assertThat(javaDocComment, containsString("Class-level comment"));

    }

    @Test
    public void dollarCommentAppearsInFieldJavadoc() {

        JavaField javaField = classWithDescription.getFieldByName("withComment");
        String javaDocComment = javaField.getComment();

        assertThat(javaDocComment, containsString("JavaDoc linking to {@link #descriptionAndComment}"));

    }

    @Test
    public void dollarCommentAppearsInGetterJavadoc() {

        JavaMethod javaMethod = classWithDescription.getMethodBySignature("getWithComment", new Type[]{});
        String javaDocComment = javaMethod.getComment();

        assertThat(javaDocComment, containsString("JavaDoc linking to {@link #descriptionAndComment}"));

    }

    @Test
    public void dollarCommentAppearsInSetterJavadoc() {

        JavaMethod javaMethod = classWithDescription.getMethodBySignature("setWithComment", new Type[]{new Type("java.lang.String")});
        String javaDocComment = javaMethod.getComment();

        assertThat(javaDocComment, containsString("JavaDoc linking to {@link #descriptionAndComment}"));

    }

    @Test
    public void descriptionAndCommentCanCoexist() {

        JavaField javaField = classWithDescription.getFieldByName("descriptionAndComment");
        String javaDocComment = javaField.getComment();

        assertThat(javaDocComment, containsString("JavaDoc buddies!"));
        assertThat(javaDocComment.indexOf("JavaDoc buddies!"), is(greaterThan(javaDocComment.indexOf("This should co-exist with the value from $comment"))));

    }
}
