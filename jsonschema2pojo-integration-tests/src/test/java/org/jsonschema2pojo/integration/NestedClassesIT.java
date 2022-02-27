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

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;

public class NestedClassesIT {
    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void inlineSubschema() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/nested/inlineSubschema.json",
                "com.example", config("useNestedClasses", true));
        resultsClassLoader.loadClass("com.example.InlineSubschema$Subschema1").getDeclaredField("foo");
        resultsClassLoader.loadClass("com.example.InlineSubschema$Subschema2").getDeclaredField("bar");
    }

    @Test
    public void externalRef() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/nested/externalRef.json",
                "com.example", config("useNestedClasses", true));
        resultsClassLoader.loadClass("com.example.ExternalRef");
        resultsClassLoader.loadClass("com.example.InlineSubschema");

        resultsClassLoader.loadClass("com.example.InlineSubschema$Subschema1").getDeclaredField("foo");
        resultsClassLoader.loadClass("com.example.InlineSubschema$Subschema2").getDeclaredField("bar");
    }

    @Test
    public void arrayItemWithDefinition() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/nested/arrayItemWithDefinition.json",
                "com.example", config("useNestedClasses", true));
        resultsClassLoader.loadClass("com.example.ArrayItemWithDefinition");
        resultsClassLoader.loadClass("com.example.ArrayItemWithDefinition$Parent");
        resultsClassLoader.loadClass("com.example.ArrayItemWithDefinition$Parent$Child");
        resultsClassLoader.loadClass("com.example.InlineSubschema");

        resultsClassLoader.loadClass("com.example.InlineSubschema$Subschema1").getDeclaredField("foo");
        resultsClassLoader.loadClass("com.example.InlineSubschema$Subschema2").getDeclaredField("bar");
    }
}
