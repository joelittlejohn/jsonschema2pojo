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

package org.jsonschema2pojo.integration.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.annotation.JsonInclude;

public class InclusionLevelIT {

    @RegisterExtension
    public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void Jackson2InclusionLevelAlways() throws ClassNotFoundException, SecurityException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("annotationStyle", "jackson2", "inclusionLevel", "ALWAYS"));

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        assertThat(generatedType.getAnnotation(JsonInclude.class), is(notNullValue()));
        assertThat(((JsonInclude) generatedType.getAnnotation(JsonInclude.class)).value(), is(JsonInclude.Include.ALWAYS));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void Jackson2InclusionLevelNonAbsent() throws ClassNotFoundException, SecurityException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("annotationStyle", "jackson2", "inclusionLevel", "NON_ABSENT"));

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        assertThat(generatedType.getAnnotation(JsonInclude.class), is(notNullValue()));
        assertThat(((JsonInclude) generatedType.getAnnotation(JsonInclude.class)).value(), is(JsonInclude.Include.NON_ABSENT));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void Jackson2InclusionLevelNonDefault() throws ClassNotFoundException, SecurityException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("annotationStyle", "jackson2", "inclusionLevel", "NON_DEFAULT"));

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        assertThat(generatedType.getAnnotation(JsonInclude.class), is(notNullValue()));
        assertThat(((JsonInclude) generatedType.getAnnotation(JsonInclude.class)).value(), is(JsonInclude.Include.NON_DEFAULT));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void Jackson2InclusionLevelNonEmpty() throws ClassNotFoundException, SecurityException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("annotationStyle", "jackson2", "inclusionLevel", "NON_EMPTY"));

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        assertThat(generatedType.getAnnotation(JsonInclude.class), is(notNullValue()));
        assertThat(((JsonInclude) generatedType.getAnnotation(JsonInclude.class)).value(), is(JsonInclude.Include.NON_EMPTY));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void Jackson2InclusionLevelNonNull() throws ClassNotFoundException, SecurityException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("annotationStyle", "jackson2", "inclusionLevel", "NON_NULL"));

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        assertThat(generatedType.getAnnotation(JsonInclude.class), is(notNullValue()));
        assertThat(((JsonInclude) generatedType.getAnnotation(JsonInclude.class)).value(), is(JsonInclude.Include.NON_NULL));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void Jackson2InclusionLevelUseDefault() throws ClassNotFoundException, SecurityException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("annotationStyle", "jackson2", "inclusionLevel", "USE_DEFAULTS"));

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        assertThat(generatedType.getAnnotation(JsonInclude.class), is(notNullValue()));
        assertThat(((JsonInclude) generatedType.getAnnotation(JsonInclude.class)).value(), is(JsonInclude.Include.USE_DEFAULTS));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void Jackson2InclusionLevelNotSet() throws ClassNotFoundException, SecurityException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("annotationStyle", "jackson2"));

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        assertThat(generatedType.getAnnotation(JsonInclude.class), is(notNullValue()));
        assertThat(((JsonInclude) generatedType.getAnnotation(JsonInclude.class)).value(), is(JsonInclude.Include.NON_NULL));
    }

}
