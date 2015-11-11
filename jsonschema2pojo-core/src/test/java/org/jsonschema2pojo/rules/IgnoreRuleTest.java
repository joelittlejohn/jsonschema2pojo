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

import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;

public class IgnoreRuleTest {

	private static final String TARGET_CLASS_NAME = IgnoreRuleTest.class.getName() + ".DummyClass";

	private IgnoreRule rule = new IgnoreRule(new RuleFactory());

	@Test
	public void applyAddsTextWhenRequired() throws JClassAlreadyExistsException {

		JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);
		String myPropertyTest = "myProperty";
		JFieldVar fieldVar = jclass.field(JMod.PRIVATE, String.class, myPropertyTest);

		ObjectMapper mapper = new ObjectMapper();
		BooleanNode ignoreNode = mapper.createObjectNode().booleanNode(true);

		/* Apply visitor pattern */
		JDocComment result = rule.apply(myPropertyTest, ignoreNode, fieldVar, null);

		/* Comprobate if annotation exist on class */
		JClass annotationClass = jclass.fields().get(myPropertyTest).annotations().iterator().next().getAnnotationClass();
		assertThat("ignore field", annotationClass != null);

	}



}
