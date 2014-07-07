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

package org.jsonschema2pojo.integration.config;

import static org.hamcrest.Matchers.not;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.jsonschema2pojo.integration.util.FileSearchMatcher.*;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;

public class CommonsLang3IT {

    @Test
    public void hashCodeAndEqualsUseCommonsLang2ByDefault() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        File generatedOutputDirectory = generate("/schema/properties/primitiveProperties.json", "com.example");

        assertThat(generatedOutputDirectory, not(containsText("org.apache.commons.lang3.")));
        assertThat(generatedOutputDirectory, containsText("org.apache.commons.lang."));

    }

    @Test
    public void hashCodeAndEqualsUseCommonsLang3() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        File generatedOutputDirectory = generate("/schema/properties/primitiveProperties.json", "com.example",
                config("useCommonsLang3", true));

        assertThat(generatedOutputDirectory, not(containsText("org.apache.commons.lang.")));
        assertThat(generatedOutputDirectory, containsText("org.apache.commons.lang3."));

    }

}
