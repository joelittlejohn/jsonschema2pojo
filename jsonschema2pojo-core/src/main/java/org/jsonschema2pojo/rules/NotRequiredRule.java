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

import java.util.Iterator;

import javax.annotation.Nullable;

import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JFieldVar;

/**
 * Applies the "required" schema rule.
 *
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.7">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.7</a>
 */
public class NotRequiredRule extends AbstractRuleFactoryRule<JDocCommentable, JDocCommentable> {

    /**
     * Text added to JavaDoc to indicate that a field is not required
     */
    public static final String NOT_REQUIRED_COMMENT_TEXT = "\n(Can be null)";

    protected NotRequiredRule(RuleFactory ruleFactory) {
        super(ruleFactory);
    }

    /**
     * Applies this schema rule to take the not required code generation steps.
     * <p>
     * The not required rule adds a Nullable annotation if JSR-305 annotations are desired.
     *
     * @param nodeName
     *            the name of the schema node for which this "required" rule has
     *            been added
     * @param node
     *            the "not required" node, having a value <code>false</code> or
     *            <code>no value</code>
     * @param parent
     *            the parent node
     * @param generatableType
     *            the class or method which may be marked as "not required"
     * @return the JavaDoc comment attached to the generatableType, which
     *         <em>may</em> have an added not to mark this construct as
     *         not required.
     */
    @Override
    public JDocCommentable apply(String nodeName, JsonNode node, JsonNode parent, JDocCommentable generatableType, Schema schema) {

        // Since NotRequiredRule is executed for all fields that do not have "required" present,
        // we need to recognize whether the field is part of the RequiredArrayRule.
        JsonNode requiredArray = schema.getContent().get("required");

        if (requiredArray != null) {
            for (Iterator<JsonNode> iterator = requiredArray.elements(); iterator.hasNext(); ) {
                String requiredArrayItem = iterator.next().asText();
                if (nodeName.equals(requiredArrayItem)) {
                    return generatableType;
                }
            }
        }

        if (ruleFactory.getGenerationConfig().isIncludeJsr305Annotations()
                && generatableType instanceof JFieldVar) {
            generatableType.javadoc().append(NOT_REQUIRED_COMMENT_TEXT);
            ((JFieldVar) generatableType).annotate(Nullable.class);
        }

        return generatableType;
    }
}
