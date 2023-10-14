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

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jsonschema2pojo.integration.util.ClassLoaderMatcher.canLoad;
import static org.jsonschema2pojo.integration.util.ClassModifiersMatcher.hasModifiers;
import static org.jsonschema2pojo.integration.util.ClassPropertyMatcher.hasProperty;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;

import java.lang.reflect.Modifier;

public class NestedClassesIT {
    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void canGenerateSingleLevelNestedClass() throws Exception {
        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/nested/singleLevel.json",
                "com.example", config("useNestedClasses", true));
        assertSingleLevelEntityWasGeneratedCorrectly(classLoader);
    }

    @Test
    public void canGenerateMultipleLevelNestedClass() throws Exception {
        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/nested/multipleLevel.json",
                "com.example", config("useNestedClasses", true));
        assertMultipleLevelEntityWasGeneratedCorrectly(classLoader);
    }

    @Test
    public void canGenerateViaExternalRef() throws Exception {
        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/nested/externalRef.json",
                "com.example", config("useNestedClasses", true));
        assertSingleLevelEntityWasGeneratedCorrectly(classLoader);
        assertExternalRefEntityWasGeneratedCorrectly(classLoader);
    }

    @Test
    public void canGenerateAnArrayWhereItemTypeIsDefinedExternally() throws Exception {
        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/nested/arrayItemWithDefinition.json",
                "com.example", config("useNestedClasses", true));
        assertSingleLevelEntityWasGeneratedCorrectly(classLoader);
        assertArrayRefWithDefinitionEntityWasGeneratedCorrectly(classLoader);
    }

    @Test
    public void canGenerateTheWholeDirectory() throws Exception {
        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/nested",
                "com.example",
                config("useNestedClasses", Boolean.TRUE));

        assertSingleLevelEntityWasGeneratedCorrectly(classLoader);
        assertMultipleLevelEntityWasGeneratedCorrectly(classLoader);
        assertArrayRefWithDefinitionEntityWasGeneratedCorrectly(classLoader);
        assertExternalRefEntityWasGeneratedCorrectly(classLoader);
    }

    private static void assertSingleLevelEntityWasGeneratedCorrectly(ClassLoader classLoader) throws Exception {
        final Class<?> nestedOne = assertCanLoadNestedClass(classLoader, "com.example.SingleLevel$NestedOne");
        assertThat(nestedOne, hasProperty("bar", Boolean.class));

        final Class<?> nestedTwo = assertCanLoadNestedClass(classLoader, "com.example.SingleLevel$NestedTwo");
        assertThat(nestedTwo, hasProperty("foo", Integer.class));
    }

    private static void assertMultipleLevelEntityWasGeneratedCorrectly(ClassLoader classLoader) throws Exception {
        final Class<?> nestedOne = assertCanLoadNestedClass(classLoader, "com.example.MultipleLevel$NestedOne");
        assertThat(nestedOne, hasProperty("foo", String.class));

        final Class<?> nestedTwo = assertCanLoadNestedClass(classLoader, "com.example.MultipleLevel$NestedTwo");
        assertThat(nestedTwo, hasProperty("bar", "com.example.MultipleLevel$NestedTwo$Bar"));
    }

    private static void assertArrayRefWithDefinitionEntityWasGeneratedCorrectly(ClassLoader classLoader)
            throws Exception {
        assertThat(classLoader, canLoad("com.example.ArrayItemWithDefinition"));
        assertCanLoadNestedClass(classLoader, "com.example.ArrayItemWithDefinition$Parent");

        final Class<?> child = assertCanLoadNestedClass(classLoader,
                "com.example.ArrayItemWithDefinition$Parent$Child");
        assertThat(child, hasProperty("external", "com.example.SingleLevel"));
    }

    private static void assertExternalRefEntityWasGeneratedCorrectly(ClassLoader classLoader) throws Exception {
        assertThat(classLoader, canLoad("com.example.ExternalRef"));
        assertThat(classLoader.loadClass("com.example.ExternalRef"),
                hasProperty("subschema", "com.example.SingleLevel"));
    }

    private static Class<?> assertCanLoadNestedClass(ClassLoader classLoader, String name) throws Exception {
        assertThat(classLoader, canLoad(name));
        final Class<?> klazz = classLoader.loadClass(name);
        assertThat(klazz, hasModifiers(Modifier.STATIC));

        return klazz;
    }
}
