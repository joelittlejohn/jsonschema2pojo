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

package org.jsonschema2pojo;

import java.net.URL;

import com.sun.codemodel.JCodeModel;

import org.jsonschema2pojo.rules.RuleFactory;

public interface PostGenerateClassHook {

    /**
    * Mutating method for JCodeModel
    */
    default void apply(JCodeModel codeModel, GenerationConfig config, RuleFactory ruleFactory, Annotator annotator, URL source) {
        return;
    }
}