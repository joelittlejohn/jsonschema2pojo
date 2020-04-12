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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocCommentable;

public class RequiredRuleTest {

    private static final String TARGET_CLASS_NAME = RequiredRuleTest.class.getName() + ".DummyClass";

    private RequiredRule rule = new RequiredRule(new RuleFactory());

    @Test
    public void applyAddsTextWhenRequired() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectMapper mapper = new ObjectMapper();
        BooleanNode descriptionNode = mapper.createObjectNode().booleanNode(true);

        JDocCommentable result = rule.apply("fooBar", descriptionNode, null, jclass, null);

        assertThat(result.javadoc(), sameInstance(jclass.javadoc()));
        assertThat(result.javadoc().size(), is(1));
        assertThat((String) result.javadoc().get(0), is("\n(Required)"));

    }

    @Test
    public void applySkipsTextWhenNotRequired() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectMapper mapper = new ObjectMapper();
        BooleanNode descriptionNode = mapper.createObjectNode().booleanNode(false);

        JDocCommentable result = rule.apply("fooBar", descriptionNode, null, jclass, null);

        assertThat(result.javadoc(), sameInstance(jclass.javadoc()));
        assertThat(result.javadoc().size(), is(0));
    }

}
