/**
 * Copyright © 2010-2014 Nokia
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
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.generateAndCompile;

import org.junit.Test;

/**
 * Tests the filtering of files in the source directory.
 * 
 * @author Christian Trimble
 */
public class FilteringIT {
    @Test
    public void shouldFilterFiles() throws ClassNotFoundException {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/filtering", "com.example",
                config("includes", new String[] { "**/*.json" }, "excludes", new String[] { "excluded.json" }));

        resultsClassLoader.loadClass("com.example.Included");
    }
}
