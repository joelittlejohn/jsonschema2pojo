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

public class DescriptionIT extends Jsonschema2PojoTestBase {
    private JavaClass classWithDescription;

//    private static JavaClass classWithDescription;

    @BeforeEach
    public void generateClasses() throws IOException {

        generateAndCompile("/schema/description/description.json", "com.example");
        File generatedJavaFile = generated("com/example/Description.java");

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(generatedJavaFile);

        classWithDescription = javaDocBuilder.getClassByName("com.example.Description");
    }

    @Test
    public void descriptionAppearsInClassJavadoc() {

        String javaDocComment = classWithDescription.getComment();

        assertThat(javaDocComment, containsString("A description for this type"));

    }

    @Test
    public void descriptionAppearsInFieldJavadoc() {

        JavaField javaField = classWithDescription.getFieldByName("description");
        String javaDocComment = javaField.getComment();

        assertThat(javaDocComment, containsString("A description for this property"));

    }

    @Test
    public void descriptionAppearsInGetterJavadoc() {

        JavaMethod javaMethod = classWithDescription.getMethodBySignature("getDescription", new Type[]{});
        String javaDocComment = javaMethod.getComment();

        assertThat(javaDocComment, containsString("A description for this property"));

    }

    @Test
    public void descriptionAppearsInSetterJavadoc() {

        JavaMethod javaMethod = classWithDescription.getMethodBySignature("setDescription", new Type[]{new Type("java.lang.String")});
        String javaDocComment = javaMethod.getComment();

        assertThat(javaDocComment, containsString("A description for this property"));

    }

    @Test
    public void descriptionAppearsAfterTitleInJavadoc() {

        JavaField javaField = classWithDescription.getFieldByName("descriptionAndTitle");
        String javaDocComment = javaField.getComment();

        assertThat(javaDocComment, containsString("A title for this property"));
        assertThat(javaDocComment.indexOf("A description for this property"), is(greaterThan(javaDocComment.indexOf("A title for this property"))));

    }
}
