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
import static org.jsonschema2pojo.integration.util.ClassPropertyMatcher.hasProperty;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;

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
        assertThat(classLoader, canLoad("com.example.SingleLevel$NestedOne"));
        assertThat(classLoader.loadClass("com.example.SingleLevel$NestedOne"),
                hasProperty("bar", Boolean.class));

        assertThat(classLoader, canLoad("com.example.SingleLevel$NestedTwo"));
        assertThat(classLoader.loadClass("com.example.SingleLevel$NestedTwo"),
                hasProperty("foo", Integer.class));
    }

    private static void assertMultipleLevelEntityWasGeneratedCorrectly(ClassLoader classLoader) throws Exception {
        assertThat(classLoader, canLoad("com.example.MultipleLevel$NestedOne"));
        assertThat(classLoader.loadClass("com.example.MultipleLevel$NestedOne"),
                hasProperty("foo", String.class));

        assertThat(classLoader, canLoad("com.example.MultipleLevel$NestedTwo"));
        assertThat(classLoader.loadClass("com.example.MultipleLevel$NestedTwo"),
                hasProperty("bar", "com.example.MultipleLevel$NestedTwo$Bar"));
    }

    private static void assertArrayRefWithDefinitionEntityWasGeneratedCorrectly(ClassLoader classLoader)
            throws Exception {
        assertThat(classLoader, canLoad("com.example.ArrayItemWithDefinition"));
        assertThat(classLoader, canLoad("com.example.ArrayItemWithDefinition$Parent"));
        assertThat(classLoader, canLoad("com.example.ArrayItemWithDefinition$Parent$Child"));
        assertThat(classLoader.loadClass("com.example.ArrayItemWithDefinition$Parent$Child"),
                hasProperty("external", "com.example.SingleLevel"));
    }

    private static void assertExternalRefEntityWasGeneratedCorrectly(ClassLoader classLoader) throws Exception {
        assertThat(classLoader, canLoad("com.example.ExternalRef"));
        assertThat(classLoader.loadClass("com.example.ExternalRef"),
                hasProperty("subschema", "com.example.SingleLevel"));
    }
}
