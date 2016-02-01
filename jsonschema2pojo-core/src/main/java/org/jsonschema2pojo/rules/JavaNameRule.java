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

package org.jsonschema2pojo.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JDocCommentable;

import org.jsonschema2pojo.Schema;

public class JavaNameRule implements Rule<JDocCommentable, JDocComment> {

    public final String CORRESPONDING_PROPERTY_TEXT = "\nCorresponds to the \"%s\" property.";

    @Override
    public JDocComment apply(String nodeName, JsonNode node, JDocCommentable generatableType, Schema currentSchema) {
        JDocComment javaDoc = generatableType.javadoc();

        javaDoc.append(String.format(CORRESPONDING_PROPERTY_TEXT, nodeName));

        return javaDoc;
    }

}
