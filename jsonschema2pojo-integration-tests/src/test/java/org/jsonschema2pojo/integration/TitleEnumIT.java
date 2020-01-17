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
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

/*
  Enums are treated differently to schemas of type object and we want to ensure that a title
  added to root-level enums is added to the javadoc.
 */
public class TitleEnumIT {
    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    private static JavaClass classWithTitle;

    @BeforeClass
    public static void generateClasses() throws IOException {

        classSchemaRule.generateAndCompile("/schema/title/titleEnum.json", "com.example");
        File generatedJavaFile = classSchemaRule.generated("com/example/TitleEnum.java");

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(generatedJavaFile);

        classWithTitle = javaDocBuilder.getClassByName("com.example.TitleEnum");
    }

    @Test
    public void descriptionAppearsInEnumJavadoc() {

        String javaDocComment = classWithTitle.getComment();

        assertThat(javaDocComment, containsString("A title for this enum"));

    }
}
