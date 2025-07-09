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
import static org.jsonschema2pojo.integration.util.FileSearchMatcher.*;

import java.io.File;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class CommonsLangRemovalIT {

    @RegisterExtension public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void hashCodeAndEqualsDontUseCommonsLang() throws SecurityException {

        File generatedOutputDirectory = schemaRule.generate("/schema/properties/primitiveProperties.json", "com.example");

        assertThat(generatedOutputDirectory, not(containsText("org.apache.commons.lang3.")));
        assertThat(generatedOutputDirectory, not(containsText("org.apache.commons.lang.")));

    }

}
