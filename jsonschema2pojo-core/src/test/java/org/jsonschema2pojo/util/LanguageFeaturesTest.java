/**
 * Copyright ¬© 2010-2014 Nokia
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

package org.jsonschema2pojo.util;

import java.util.Arrays;
import java.util.Collection;

import org.jsonschema2pojo.GenerationConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.jsonschema2pojo.util.LanguageFeaturesTest.VersionEnum.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

@RunWith(Parameterized.class)
public class LanguageFeaturesTest {

    public static enum VersionEnum {

        BEFORE_6(false, false, false),
        MAX_6(true, false, false),
        MAX_7(true, true, false),
        MAX_8(true, true, true),
        AFTER_8(true, true, true);

        public final boolean canUse6;
        public final boolean canUse7;
        public final boolean canUse8;

        VersionEnum(boolean canUse6, boolean canUse7, boolean canUse8) {
            this.canUse6 = canUse6;
            this.canUse7 = canUse7;
            this.canUse8 = canUse8;
        }
    }

    @Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
            { "1.5", BEFORE_6 },
            { "5", BEFORE_6 },
            { "1.6", MAX_6 },
            { "6", MAX_6 },
            { "1.7", MAX_7 },
            { "7", MAX_7 },
            { "1.8", MAX_8 },
            { "8", MAX_8 },
            { "1.9", AFTER_8 },
            { "9", AFTER_8 }
        });
    }

    private String version;
    private VersionEnum versionSpec;

    public LanguageFeaturesTest(String version, VersionEnum versionSpec) {
        this.version = version;
        this.versionSpec = versionSpec;
    }

    @Test
    public void correctTestForJava7() {
        assertThat(LanguageFeatures.canUseJava7(mockConfig(version)), equalTo(versionSpec.canUse7));
    }

    @Test
    public void correctTestForJava8() {
        assertThat(LanguageFeatures.canUseJava8(mockConfig(version)), equalTo(versionSpec.canUse8));
    }

    public static GenerationConfig mockConfig(String version) {
        GenerationConfig config = mock(GenerationConfig.class);
        when(config.getTargetVersion()).thenReturn(version);
        return config;
    }
}
