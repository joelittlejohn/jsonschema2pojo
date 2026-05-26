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

package org.jsonschema2pojo.integration.ref;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;

import java.lang.reflect.Field;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class RefSiblingIT {

    @RegisterExtension public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void refSiblingDescriptionIsUsedForPropertyDescription() throws ReflectiveOperationException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/ref/refWithSiblingDescription.json",
                "com.example", config("annotationStyle", "jackson2"));
        Class<?> generatedType = resultsClassLoader.loadClass("com.example.RefWithSiblingDescription");

        Field field = generatedType.getDeclaredField("address");

        assertThat(field.getType().getName(), is("com.example.Address"));
        assertThat(field.getAnnotation(JsonPropertyDescription.class).value(), is("The preferred contact address."));
    }
}
