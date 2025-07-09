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

import static java.util.Arrays.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.Collections;

import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.SchemaStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.node.TextNode;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;

@ParameterizedClass
@MethodSource("data")
public class FormatRuleArraysTest {

    private final GenerationConfig config = mock(GenerationConfig.class);
    private final FormatRule rule;

    private final Class<?> expectedType;

    public static Collection<Object[]> data() {
        return asList(new Object[][] {
                { "byte[]", byte[].class },
                { "java.lang.String[]", String[].class }});
    }

    public FormatRuleArraysTest(String formatValue, Class<?> expectedType) {
        this.expectedType = expectedType;

        when(config.getFormatTypeMapping()).thenReturn(Collections.singletonMap("test", formatValue));
        rule = new FormatRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));
    }

    @Test
    public void useArraysWithCustomTypeMapping() {
        JType result = rule.apply("fooBar", TextNode.valueOf("test"), null, new JCodeModel().ref(Object.class), null);

        assertThat(result.isArray(), is(true));

        JType expectedJType = new JCodeModel().ref(expectedType);

        assertThat(result.fullName(), equalTo(expectedJType.fullName()));
    }

}
