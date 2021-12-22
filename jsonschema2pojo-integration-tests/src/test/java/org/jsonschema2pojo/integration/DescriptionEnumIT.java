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
import org.jsonschema2pojo.integration.util.Jsonschema2PojoTestBase;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

/*
  Enums are treated differently to schemas of type object and we want to ensure that a description
  added to root-level enums is added to the javadoc.
 */
public class DescriptionEnumIT extends Jsonschema2PojoTestBase {

    @Test
    public void descriptionAppearsInEnumJavadoc() throws IOException {

        generateAndCompile("/schema/description/descriptionEnum.json", "com.example");
        File generatedJavaFile = generated("com/example/DescriptionEnum.java");

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(generatedJavaFile);

        JavaClass classWithDescription = javaDocBuilder.getClassByName("com.example.DescriptionEnum");

        String javaDocComment = classWithDescription.getComment();

        assertThat(javaDocComment, containsString("A description for this enum"));

    }

}
