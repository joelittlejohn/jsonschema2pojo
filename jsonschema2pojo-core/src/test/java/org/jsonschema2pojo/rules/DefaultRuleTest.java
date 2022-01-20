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

import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.SchemaStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JMod;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class DefaultRuleTest {

    private final GenerationConfig config = mock(GenerationConfig.class);
    private final DefaultRule rule = new DefaultRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));

    private final Class<?> fieldTypeClass;

    @Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
                { Set.class },
                { List.class }
        });
    }

    public DefaultRuleTest(Class<?> fieldTypeClass) {
        this.fieldTypeClass = fieldTypeClass;
    }

    @Test
    public void whenIsInitializeCollections_false_applyDoesNotInitializeField() throws JClassAlreadyExistsException {
        final String fieldName = "fieldName";
        when(config.isInitializeCollections()).thenReturn(false);

        JDefinedClass jclass = new JCodeModel()._class("org.jsonschema2pojo.rules.ExampleClass");
        JFieldVar field = jclass.field(JMod.NONE, jclass.owner().ref(fieldTypeClass).narrow(Object.class), fieldName);
        ArrayNode node = new ObjectMapper().createArrayNode();

        StringWriter sw = new StringWriter();
        rule.apply("fooBar", node, null, field, null).bind(new JFormatter(sw));
        assertThat(sw.toString(), is(String.format("%s<%s> %s", fieldTypeClass.getName(), Object.class.getName(), fieldName)));
    }

    @Test
    public void whenIsInitializeCollections_true_applyInitializesField() throws JClassAlreadyExistsException {
        when(config.isInitializeCollections()).thenReturn(true);

        JDefinedClass jclass = new JCodeModel()._class("org.jsonschema2pojo.rules.ExampleClass");
        JFieldVar field = jclass.field(JMod.NONE, jclass.owner().ref(fieldTypeClass).narrow(Object.class), "value");
        ArrayNode node = new ObjectMapper().createArrayNode().add(1);

        StringWriter sw = new StringWriter();
        rule.apply("fooBar", node, null, field, null).bind(new JFormatter(sw));
        assertThat(sw.toString(), startsWith(String.format("%s<%s> value = ", fieldTypeClass.getName(), Object.class.getName())));
    }
}
