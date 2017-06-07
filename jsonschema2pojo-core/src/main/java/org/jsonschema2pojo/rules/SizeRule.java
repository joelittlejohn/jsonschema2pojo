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
import com.sun.codemodel.JFieldVar;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.util.AnnotationHelper;

import javax.validation.constraints.Size;

public class SizeRule implements Rule<JFieldVar, JFieldVar> {

	private final RuleFactory ruleFactory;

	protected SizeRule(RuleFactory ruleFactory) {
		this.ruleFactory = ruleFactory;
	}

	@Override
	public JFieldVar apply(String nodeName, JsonNode node, JFieldVar field, Schema currentSchema) {

		if (ruleFactory.getGenerationConfig().isIncludeJsr303Annotations()) {
			AnnotationHelper.annotateField(Size.class, node, field, null);
		}

		return field;
	}
}
