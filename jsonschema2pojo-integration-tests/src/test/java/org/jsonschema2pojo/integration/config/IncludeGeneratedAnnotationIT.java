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

import static org.hamcrest.MatcherAssert.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;

import java.io.File;

import org.hamcrest.Matchers;
import org.jsonschema2pojo.integration.util.FileSearchMatcher;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoTestBase;
import org.junit.jupiter.api.Test;

public class IncludeGeneratedAnnotationIT extends Jsonschema2PojoTestBase {

    private static final String PROP_KEY = "includeGeneratedAnnotation";
    private static final String SCHEMA_PATH = "/schema/" + PROP_KEY + "/" + PROP_KEY + ".json";
    private static final String TEST_PACKAGE = "com.example";

    @Test
    public void defaultConfig() throws ClassNotFoundException {
        File source = generate(SCHEMA_PATH, TEST_PACKAGE);

        assertThat(source, FileSearchMatcher.containsText("javax.annotation.Generated"));
    }

    @Test
    public void disabled() throws ClassNotFoundException {
        File source = generate(SCHEMA_PATH, TEST_PACKAGE, config(PROP_KEY, false));

        assertThat(source, Matchers.not(FileSearchMatcher.containsText("javax.annotation.Generated")));
    }

    @Test
    public void enabled() throws ClassNotFoundException {
        File source = generate(SCHEMA_PATH, TEST_PACKAGE, config(PROP_KEY, true));

        assertThat(source, FileSearchMatcher.containsText("javax.annotation.Generated"));
    }

}
