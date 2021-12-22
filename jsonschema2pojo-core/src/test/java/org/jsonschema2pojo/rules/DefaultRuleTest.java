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
import com.sun.codemodel.*;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.SchemaStore;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.StringWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultRuleTest {

    private final GenerationConfig config = mock(GenerationConfig.class);
    private final DefaultRule rule = new DefaultRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));

    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(Set.class),
                Arguments.of(List.class)
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    public void whenIsInitializeCollections_false_applyDoesNotInitializeField(Class<?> fieldTypeClass) throws JClassAlreadyExistsException {
        final String fieldName = "fieldName";
        when(config.isInitializeCollections()).thenReturn(false);

        JDefinedClass jclass = new JCodeModel()._class("org.jsonschema2pojo.rules.ExampleClass");
        JFieldVar field = jclass.field(JMod.NONE, jclass.owner().ref(fieldTypeClass).narrow(Object.class), fieldName);
        ArrayNode node = new ObjectMapper().createArrayNode();

        StringWriter sw = new StringWriter();
        rule.apply("fooBar", node, null, field, null).bind(new JFormatter(sw));
        assertThat(sw.toString(), is(String.format("%s<%s> %s", fieldTypeClass.getName(), Object.class.getName(), fieldName)));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void whenIsInitializeCollections_true_applyInitializesField(Class<?> fieldTypeClass) throws JClassAlreadyExistsException {
        when(config.isInitializeCollections()).thenReturn(true);

        JDefinedClass jclass = new JCodeModel()._class("org.jsonschema2pojo.rules.ExampleClass");
        JFieldVar field = jclass.field(JMod.NONE, jclass.owner().ref(fieldTypeClass).narrow(Object.class), "value");
        ArrayNode node = new ObjectMapper().createArrayNode().add(1);

        StringWriter sw = new StringWriter();
        rule.apply("fooBar", node, null, field, null).bind(new JFormatter(sw));
        assertThat(sw.toString(), startsWith(String.format("%s<%s> value = ", fieldTypeClass.getName(), Object.class.getName())));
    }
}
