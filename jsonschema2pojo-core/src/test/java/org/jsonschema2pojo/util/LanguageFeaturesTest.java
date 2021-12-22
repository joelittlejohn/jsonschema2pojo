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

package org.jsonschema2pojo.util;

import org.jsonschema2pojo.GenerationConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.jsonschema2pojo.util.LanguageFeaturesTest.VersionEnum.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LanguageFeaturesTest {

    public enum VersionEnum {

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


    public static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of("1.5", BEFORE_6),
                Arguments.of("5", BEFORE_6),
                Arguments.of("1.6", MAX_6),
                Arguments.of("6", MAX_6),
                Arguments.of("1.7", MAX_7),
                Arguments.of("7", MAX_7),
                Arguments.of("1.8", MAX_8),
                Arguments.of("8", MAX_8),
                Arguments.of("1.9", AFTER_8),
                Arguments.of("9", AFTER_8)
        );
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void correctTestForJava7(String version, VersionEnum versionSpec) {
        assertThat(LanguageFeatures.canUseJava7(mockConfig(version)), equalTo(versionSpec.canUse7));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void correctTestForJava8(String version, VersionEnum versionSpec) {
        assertThat(LanguageFeatures.canUseJava8(mockConfig(version)), equalTo(versionSpec.canUse8));
    }

    public static GenerationConfig mockConfig(String version) {
        GenerationConfig config = mock(GenerationConfig.class);
        when(config.getTargetVersion()).thenReturn(version);
        return config;
    }
}
