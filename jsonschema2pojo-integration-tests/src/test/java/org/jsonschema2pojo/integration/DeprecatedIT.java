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
import java.util.Collections;
import java.util.List;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.impl.DefaultJavaClass;

public class DeprecatedIT {

    @RegisterExtension public static Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private static JavaClass classWithDeprecated;

    @BeforeAll
    public static void generateClasses() throws IOException {
        schemaRule.generateAndCompile("/schema/deprecated/deprecated.json", "com.example");
        File generatedJavaFile = schemaRule.generated("com/example/Deprecated.java");

        JavaProjectBuilder javaDocBuilder = new JavaProjectBuilder();
        javaDocBuilder.addSource(generatedJavaFile);

        classWithDeprecated = javaDocBuilder.getClassByName("com.example.Deprecated");
    }

    @Test
    public void deprecatedAppearsInFieldJavadoc() {

        JavaField javaField = classWithDeprecated.getFieldByName("deprecatedProperty");
        String javaDocComment = javaField.getComment();

        assertThat(javaDocComment, containsString("@deprecated"));

    }

    @Test
    public void deprecatedAppearsInGetterJavadoc() {

        JavaMethod javaMethod = classWithDeprecated.getMethodBySignature("getDeprecatedProperty", Collections.emptyList());
        String javaDocComment = javaMethod.getComment();

        assertThat(javaDocComment, containsString("@deprecated"));

    }

    @Test
    public void deprecatedAppearsInSetterJavadoc() {

        final List<JavaType> parameterTypes = Collections.singletonList(new DefaultJavaClass("java.lang.String"));
        JavaMethod javaMethod = classWithDeprecated.getMethodBySignature("setDeprecatedProperty", parameterTypes);
        String javaDocComment = javaMethod.getComment();

        assertThat(javaDocComment, containsString("@deprecated"));

    }

}
