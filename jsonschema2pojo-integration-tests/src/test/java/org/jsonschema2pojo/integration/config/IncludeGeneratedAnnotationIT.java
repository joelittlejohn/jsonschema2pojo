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
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

public class IncludeGeneratedAnnotationIT {

    private static final String PROP_KEY = "includeGeneratedAnnotation";
    private static final String SCHEMA_PATH = "/schema/" + PROP_KEY + "/" + PROP_KEY + ".json";
    private static final String TEST_PACKAGE = "com.example";

    @Rule
    public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void defaultConfigHasGeneratedAnnotationsOn() throws ClassNotFoundException {
        File source = schemaRule.generate(SCHEMA_PATH, TEST_PACKAGE);
        assertThat(source, FileSearchMatcher.containsText("@Generated"));

        File sourceJava8 = schemaRule.generate(SCHEMA_PATH, TEST_PACKAGE, config("targetVersion", "8"));
        assertThat(sourceJava8, FileSearchMatcher.containsText("javax.annotation.Generated"));

        File sourceJava9 = schemaRule.generate(SCHEMA_PATH, TEST_PACKAGE, config("targetVersion", "9"));
        assertThat(sourceJava9, FileSearchMatcher.containsText("javax.annotation.processing.Generated"));

        File sourceJava11 = schemaRule.generate(SCHEMA_PATH, TEST_PACKAGE, config("targetVersion", "11"));
        assertThat(sourceJava11, FileSearchMatcher.containsText("javax.annotation.processing.Generated"));
    }

    @Test
    public void disabled() throws ClassNotFoundException {
        File source = schemaRule.generate(SCHEMA_PATH, TEST_PACKAGE, config(PROP_KEY, false));

        assertThat(source, Matchers.not(FileSearchMatcher.containsText("@Generated")));
    }

    @Test
    public void enabled() throws ClassNotFoundException {
        File source = schemaRule.generate(SCHEMA_PATH, TEST_PACKAGE, config(PROP_KEY, true));

        assertThat(source, FileSearchMatcher.containsText("javax.annotation.Generated"));
    }

}
