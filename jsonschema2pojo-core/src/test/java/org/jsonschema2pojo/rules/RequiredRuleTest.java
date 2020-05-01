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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.sun.codemodel.*;
import org.hamcrest.Matchers;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;
import org.jsonschema2pojo.SchemaStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.*;

public class RequiredRuleTest {
    private static final String TARGET_CLASS_NAME = RequiredRuleTest.class.getName() + ".DummyClass";
    private DefaultGenerationConfig withJsr303andJsr305 = new DefaultGenerationConfig() {
        @Override
        public boolean isIncludeJsr303Annotations() {
            return true;
        }

        @Override
        public boolean isIncludeJsr305Annotations() {
            return true;
        }
    };

    private RequiredRule rule;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JFieldVar fieldVar;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        rule = new RequiredRule(new RuleFactory(withJsr303andJsr305, new Jackson2Annotator(withJsr303andJsr305), new SchemaStore()));
        mapper = new ObjectMapper();
    }

    @Test
    public void applyAddsTextAndAddCanNotBeNullAnnotationsWhenRequiredIsTrue() throws JClassAlreadyExistsException {
        final JDocComment javadoc = javadoc();
        when(fieldVar.javadoc()).thenReturn(javadoc);

        JDocCommentable result = rule.apply("nodeName", booleanNodeOfValue(true), null, fieldVar, null);

        assertThat(result.javadoc(), sameInstance(javadoc));
        assertThat(javadoc, Matchers.hasItems("\n(Required)"));
        verify(fieldVar).annotate(Nonnull.class);
        verify(fieldVar).annotate(NotNull.class);
    }

    @Test
    public void applySkipsTextAndAddCanBeNullAnnotationsWhenRequiredIsFalse() throws JClassAlreadyExistsException {
        final JDocComment javadoc = javadoc();
        when(fieldVar.javadoc()).thenReturn(javadoc);

        JDocCommentable result = rule.apply("nodeName", booleanNodeOfValue(false), null, fieldVar, null);

        assertThat(result.javadoc(), sameInstance(javadoc));
        assertThat(javadoc, empty());
        verify(fieldVar).annotate(Nullable.class);
    }

    private BooleanNode booleanNodeOfValue(boolean value) {
        return mapper.createObjectNode()
                .booleanNode(value);
    }

    private JDocComment javadoc() throws JClassAlreadyExistsException {
        return new JCodeModel()._class(TARGET_CLASS_NAME)
                .javadoc();
    }

    @Test
    public void shallBeNoopWhenRequiredIsNotBoolean() {
        final ArrayNode node = mapper.createObjectNode()
                .arrayNode(2);

        JDocCommentable result = rule.apply("nodeName", node, null, fieldVar, null);

        verifyNoInteractions(fieldVar);
    }

}
