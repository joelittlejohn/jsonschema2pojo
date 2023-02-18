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

public class TitleIT {
    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    private static JavaClass classWithTitle;

    @BeforeClass
    public static void generateClasses() throws IOException {

        classSchemaRule.generateAndCompile("/schema/title/title.json", "com.example");
        File generatedJavaFile = classSchemaRule.generated("com/example/Title.java");

        JavaProjectBuilder javaDocBuilder = new JavaProjectBuilder();
        javaDocBuilder.addSource(generatedJavaFile);

        classWithTitle = javaDocBuilder.getClassByName("com.example.Title");
    }

    @Test
    public void descriptionAppearsInClassJavadoc() {

        String javaDocComment = classWithTitle.getComment();

        assertThat(javaDocComment, containsString("A title for this type"));

    }

    @Test
    public void descriptionAppearsInFieldJavadoc() {

        JavaField javaField = classWithTitle.getFieldByName("title");
        String javaDocComment = javaField.getComment();

        assertThat(javaDocComment, containsString("A title for this property"));

    }

    @Test
    public void descriptionAppearsInGetterJavadoc() {

        JavaMethod javaMethod = classWithTitle.getMethodBySignature("getTitle", Collections.emptyList());
        String javaDocComment = javaMethod.getComment();

        assertThat(javaDocComment, containsString("A title for this property"));

    }

    @Test
    public void descriptionAppearsInSetterJavadoc() {

        final List<JavaType> parameterTypes = Collections.singletonList(new DefaultJavaClass("java.lang.String"));
        JavaMethod javaMethod = classWithTitle.getMethodBySignature("setTitle", parameterTypes);
        String javaDocComment = javaMethod.getComment();

        assertThat(javaDocComment, containsString("A title for this property"));

    }

    @Test
    public void titleAppearsBeforeDescriptionInJavadoc() {

        JavaField javaField = classWithTitle.getFieldByName("titleAndDescription");
        String javaDocComment = javaField.getComment();

        assertThat(javaDocComment, containsString("A description for this property"));
        assertThat(javaDocComment.indexOf("A title for this property"), is(lessThan(javaDocComment.indexOf("A description for this property"))));

    }
}
