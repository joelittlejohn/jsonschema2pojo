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

import static java.util.Arrays.*;

import java.util.Collection;

import org.jsonschema2pojo.GenerationConfig;

public class LanguageFeatures {

    private static final Collection<String> LESS_THAN_8 = asList("1.1", "1.2", "1.3", "1.4", "1.5", "5", "1.6", "6", "1.7", "7");
    private static final Collection<String> LESS_THAN_7 = asList("1.1", "1.2", "1.3", "1.4", "1.5", "5", "1.6", "6");

    public static boolean canUseJava7(GenerationConfig config) {
        return !LESS_THAN_7.contains(config.getTargetVersion());
    }

    public static boolean canUseJava8(GenerationConfig config) {
        return !LESS_THAN_8.contains(config.getTargetVersion());
    }
}
