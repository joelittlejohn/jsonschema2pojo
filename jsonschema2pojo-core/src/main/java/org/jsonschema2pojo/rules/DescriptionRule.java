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

import org.apache.commons.lang3.StringUtils;
import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JDocCommentable;

/**
 * Applies the "description" schema property.
 *
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.22">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.22</a>
 */
public class DescriptionRule extends AbstractRuleFactoryRule<JDocCommentable, JDocComment> {

    /**
     * @deprecated Please switch to {@link DescriptionRule(RuleFactory)}
     */
    @Deprecated
    protected DescriptionRule() {
        super(null);
    }

    protected DescriptionRule(RuleFactory ruleFactory) {
        super(ruleFactory);
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * When a description node is found and applied with this rule, the value of
     * the description is added as a class level JavaDoc comment.
     *
     * @param nodeName
     *            the name of the object to which this description applies
     * @param node
     *            the "description" schema node
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
