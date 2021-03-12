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

package org.jsonschema2pojo.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JDocCommentable;
import org.apache.commons.lang3.StringUtils;
import org.jsonschema2pojo.Schema;

/**
 * Applies the "$comment" schema property from json-schema-07.
 *
 * @see <a
 *      href="https://tools.ietf.org/html/draft-handrews-json-schema-01#section-9">https://tools.ietf.org/html/draft-handrews-json-schema-01#section-9</a>
 */
public class CommentRule implements Rule<JDocCommentable, JDocComment> {

    protected CommentRule() {
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * When a $comment node is found and applied with this rule, the value of
     * the $comment is added as a method and field level JavaDoc comment.
     *
     * @param nodeName
     *            the name of the object to which this description applies
     * @param node
     *            the "$comment" schema node
     * @param parent
     *            the parent node
     * @param generatableType
     *            comment-able code generation construct, usually a java class,
     *            which should have this description applied
     * @return the JavaDoc comment created to contain the description
     */
    @Override
    public JDocComment apply(String nodeName, JsonNode node, JsonNode parent, JDocCommentable generatableType, Schema schema) {
        JDocComment javadoc = generatableType.javadoc();

        String descriptionText = node.asText();

        if(StringUtils.isNotBlank(descriptionText)) {

            String[] lines = node.asText().split("/\r?\n/");

            for(String line : lines) {
                javadoc.append(line);
            }
        }

        return javadoc;
    }

}
