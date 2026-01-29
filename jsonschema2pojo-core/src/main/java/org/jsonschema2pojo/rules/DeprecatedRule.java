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

import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JFieldVar;

/**
 * Applies the "deprecated" schema rule.
 *
 * @see <a
 *      href="https://json-schema.org/understanding-json-schema/reference/generic.html#deprecated">https://json-schema.org/understanding-json-schema/reference/generic.html#deprecated</a>
 */
public class DeprecatedRule implements Rule<JDocCommentable, JDocCommentable> {

    private final RuleFactory ruleFactory;

    protected DeprecatedRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * The deprecated rule adds a @Deprecated annotation to fields that are
     * marked as deprecated in the JSON Schema and also adds a note to the
     * JavaDoc comment to mark a property as deprecated.
     *
     * @param nodeName
     *            the name of the schema node for which this "deprecated" rule has
     *            been added
     * @param node
     *            the "deprecated" node, having a value <code>true</code> or
     *            <code>false</code>
     * @param parent
     *            the parent node
     * @param generatableType
     *            the class or method which may be marked as "deprecated"
     * @return the JavaDoc comment attached to the generatableType, which
     *         <em>may</em> have an added note to mark this construct as
     *         deprecated.
     */
    @Override
    public JDocCommentable apply(String nodeName, JsonNode node, JsonNode parent, JDocCommentable generatableType, Schema schema) {

        if (node.asBoolean()) {
            generatableType.javadoc().append("\n@deprecated");

            if (ruleFactory.getGenerationConfig().isIncludeDeprecatedAnnotations()
                    && generatableType instanceof JFieldVar) {
                ((JFieldVar) generatableType).annotate(Deprecated.class);
            }
        }

        return generatableType;
    }
}
