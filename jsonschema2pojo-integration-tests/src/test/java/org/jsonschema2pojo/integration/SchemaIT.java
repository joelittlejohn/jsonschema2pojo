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
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class SchemaIT {

    @RegisterExtension
    private Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    void subschemasDefinedInDefsAreGenerated_when_generateDefinitions_isTrue() throws ReflectiveOperationException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/schema/definitions/schemaDefsStorage.json",
                "com.example",
                config("generateDefinitions", true));

        Class<?> schemaDefsStorageType = resultsClassLoader.loadClass("com.example.SchemaDefsStorage");
        Class<?> referencedDefinitionsStorageType = resultsClassLoader.loadClass("com.example.ReferencedDefsStorage");

        assertThat(schemaDefsStorageType.getDeclaredFields(), is(emptyArray()));
        assertThat(referencedDefinitionsStorageType.getDeclaredField("name"), is(notNullValue()));
        assertInlinePropertyTypes(resultsClassLoader);
    }

    @Test
    void subschemasNotProcessed_when_schemaDoesNotContainOverridenDefinitionsPath() throws ReflectiveOperationException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/schema/definitions/schemaDefsStorage.json",
                "com.example",
                config("generateDefinitions", true, "definitionsPath", "/components/schemas"));

        Class<?> schemaDefsStorageType = resultsClassLoader.loadClass("com.example.SchemaDefsStorage");
        assertThat(schemaDefsStorageType.getDeclaredFields(), is(emptyArray()));
        assertThrows(ClassNotFoundException.class, () -> resultsClassLoader.loadClass("com.example.ReferencedDefsStorage"));
    }

    @Test
    void definitionsFromCustomPathProcessed_when_definitionsNodePathIsOverridden() throws ReflectiveOperationException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/schema/definitions/schemaDefinitionsStorageWithCustomSection.json",
                "com.example",
                config("generateDefinitions", true, "definitionsPath", "/components/schemas"));

        Class<?> statusTypeRaw = resultsClassLoader.loadClass("com.example.Status");
        assertThat(statusTypeRaw.isEnum(), is(true));
        @SuppressWarnings("unchecked")
        Class<Enum<?>> statusType = (Class<Enum<?>>) statusTypeRaw;
        assertThat(statusType.getEnumConstants()[0].name(), is("ACTIVE"));
        assertThat(statusType.getEnumConstants()[1].name(), is("INACTIVE"));

        Class<?> userType = resultsClassLoader.loadClass("com.example.User");
        assertThat(userType.getDeclaredField("id"), is(notNullValue()));
        assertThat(userType.getDeclaredField("id").getType(), is(equalTo(Integer.class)));
        assertThat(userType.getDeclaredField("name"), is(notNullValue()));
        assertThat(userType.getDeclaredField("name").getType(), is(equalTo(String.class)));

        assertThrows(ClassNotFoundException.class, () -> resultsClassLoader.loadClass("com.example.Unexpected"));
    }

    private void assertInlinePropertyTypes(ClassLoader resultsClassLoader) throws ReflectiveOperationException {
        Class<?> referencedInlineType = resultsClassLoader.loadClass("com.example.Inline");
        Class<?> inlineType = resultsClassLoader.loadClass("com.example.Inline__1");

        assertThat(inlineType, is(not(equalTo(referencedInlineType))));

        assertThat(referencedInlineType.getDeclaredField("inlineProperty"), is(notNullValue()));
        assertThat(referencedInlineType.getDeclaredField("inlineProperty").getType(), is(equalTo(Boolean.class)));

        assertThat(inlineType.getDeclaredField("inlineProperty"), is(notNullValue()));
        assertThat(inlineType.getDeclaredField("inlineProperty").getType(), is(equalTo(String.class)));

        Class<?> selfReferenceType = resultsClassLoader.loadClass("com.example.SelfReference");
        assertThat(selfReferenceType.getDeclaredField("selfRefProperty"), is(notNullValue()));
        assertThat(selfReferenceType.getDeclaredField("selfRefProperty").getType(), is(equalTo(inlineType)));
    }

}
