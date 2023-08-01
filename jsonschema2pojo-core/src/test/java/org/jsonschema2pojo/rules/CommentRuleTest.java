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
import com.fasterxml.jackson.databind.node.TextNode;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;

public class CommentRuleTest {

    private static final String TARGET_CLASS_NAME = CommentRuleTest.class.getName() + ".DummyClass";

    private final CommentRule rule = new CommentRule(new RuleFactory());

    @Test
    public void applyAddsCommentToJavadoc() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectMapper mapper = new ObjectMapper();
        TextNode commentNode = mapper.createObjectNode().textNode("some comment");

        JDocComment result = rule.apply("fooBar", commentNode, null, jclass, null);

        assertThat(result, sameInstance(jclass.javadoc()));
        assertThat(result.size(), is(1));
        assertThat((String) result.get(0), is("some comment"));

    }
}
