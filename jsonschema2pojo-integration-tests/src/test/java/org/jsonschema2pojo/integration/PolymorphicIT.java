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

/**
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
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class PolymorphicIT {

    @RegisterExtension public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void extendsWithPolymorphicDeserializationWithDefaultAnnotationStyle() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/polymorphic/extendsSchema.json", "com.example");

        Class<?> subtype = resultsClassLoader.loadClass("com.example.ExtendsSchema");
        Class<?> supertype = subtype.getSuperclass();

        assertThat(supertype.getAnnotation(JsonTypeInfo.class), is(notNullValue()));
    }

    @Test
    public void extendsWithPolymorphicDeserializationWithJackson2() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/polymorphic/extendsSchema.json", "com.example",
                                                                       config("annotationStyle", "JACKSON2"));

        Class<?> subtype = resultsClassLoader.loadClass("com.example.ExtendsSchema");
        Class<?> supertype = subtype.getSuperclass();

        assertThat(supertype.getAnnotation(JsonTypeInfo.class), is(notNullValue()));
    }
}
