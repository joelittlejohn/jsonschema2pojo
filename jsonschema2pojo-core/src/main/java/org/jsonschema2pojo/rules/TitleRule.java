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
import org.jsonschema2pojo.Schema;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JDocCommentable;

/**
 * Applies the "title" property property.
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.21">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.21</a>
 */
public class TitleRule implements Rule<JDocCommentable, JDocComment> {

    protected TitleRule() {
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * When a title node is found and applied with this rule, the value of the
     * title is added as a JavaDoc comment. This rule is typically applied to
     * the generated field, generated getter and generated setter for the
     * property.
     * <p>
     * Note that the title is always inserted at the top of the JavaDoc comment.
     * 
     * @param nodeName
     *            the name of the property to which this title applies
     * @param node
     *            the "title" schema node
     * @param generatableType
     *            comment-able code generation construct, usually a field or
     *            method, which should have this title applied
     * @return the JavaDoc comment created to contain the title
     */
    @Override
    public JDocComment apply(String nodeName, JsonNode node, JDocCommentable generatableType, Schema schema) {
        JDocComment javadoc = generatableType.javadoc();

        javadoc.add(0, node.asText() + "\n<p>\n");

        return javadoc;
    }

}
