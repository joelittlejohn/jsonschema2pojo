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

import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;

/**
 * Applies the "ignore" for field and  "ignoreSetter" or "ignoreGetter" for methods in a schema rule.
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.7">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.7</a>
 */
public class IgnoreRule implements Rule<JDocCommentable, JDocComment> {

	private final RuleFactory ruleFactory;

	protected IgnoreRule(RuleFactory ruleFactory) {
		this.ruleFactory = ruleFactory;
	}

	/**
	 * Applies this schema rule to take the ignore code generation steps.
	 * <p>
	 * The ignore rule simply adds a note to the JavaDoc comment to mark a
	 * property as required.
	 * 
	 * @param nodeName
	 *            the name of the schema node for which this "required" rule has
	 *            been added
	 * @param node
	 *            the "required" node, having a value <code>true</code> or
	 *            <code>false</code>
	 * @param generatableType
	 *            the class or method which may be marked as "required"
	 * @return the JavaDoc comment attached to the generatableType, which
	 *         <em>may</em> have an added not to mark this construct as
	 *         required.
	 */
	@Override
	public JDocComment apply(String nodeName, JsonNode node, JDocCommentable generatableType, Schema schema) {
		JDocComment javadoc = generatableType.javadoc();

		if (node.asBoolean()) {
			if (generatableType instanceof JMethod) {
				/* Method ignored */
				((JMethod) generatableType).annotate(JsonIgnore.class);
			} else if (generatableType instanceof JFieldVar) {
				/* Attr ignored */
				((JFieldVar) generatableType).annotate(JsonIgnore.class);
			}
		}

		return javadoc;
	}
}
