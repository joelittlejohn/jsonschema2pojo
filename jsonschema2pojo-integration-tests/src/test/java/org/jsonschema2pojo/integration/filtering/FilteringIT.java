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

package org.jsonschema2pojo.integration.filtering;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests the filtering of files in the source directory.
 * 
 * @author Christian Trimble
 */
public class FilteringIT {
    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    URL filteredSchemaUrl;
    URL subSchemaUrl;
    
    @Before
    public void setUp() throws MalformedURLException {
        filteredSchemaUrl = new File("./src/test/resources/schema/filtering").toURI().toURL();
        subSchemaUrl = new File("./src/test/resources/schema/filtering/sub").toURI().toURL();
    }

    @Test
    public void shouldFilterFiles() throws ClassNotFoundException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(filteredSchemaUrl, "com.example",
                config("includes", new String[] { "**/*.json" }, "excludes", new String[] { "excluded.json" }));

        resultsClassLoader.loadClass("com.example.Included");
    }
    
    @Test(expected=ClassNotFoundException.class)
    public void shouldNotProcessExcludedFiles() throws ClassNotFoundException {
        ClassLoader resultsClassLoader =schemaRule. generateAndCompile(filteredSchemaUrl, "com.example",
                config("includes", new String[] { "**/*.json" }, "excludes", new String[] { "excluded.json" }));

        resultsClassLoader.loadClass("com.example.Excluded");
    }

    @Test
    public void shouldIncludeNestedFilesWithFiltering() throws ClassNotFoundException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(filteredSchemaUrl, "com.example",
                config("includes", new String[] { "**/*.json" }, "excludes", new String[] { "excluded.json" }));

        resultsClassLoader.loadClass("com.example.sub.Sub");
    }

    @Test
    public void shouldUseDefaultExcludesWithoutIncludesAndExcludes() throws ClassNotFoundException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(subSchemaUrl, "com.example.sub",
                config("includes", new String[] {}, "excludes", new String[] {}));

        resultsClassLoader.loadClass("com.example.sub.Sub");
        resultsClassLoader.loadClass("com.example.sub.sub2.Sub");
    }
}
