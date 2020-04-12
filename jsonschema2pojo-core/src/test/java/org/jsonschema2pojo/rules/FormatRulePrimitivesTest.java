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

import com.fasterxml.jackson.databind.node.TextNode;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.SchemaStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class FormatRulePrimitivesTest {

    private final GenerationConfig config = mock(GenerationConfig.class);
    private final FormatRule rule;

    private final Class<?> primitive;
    private final Class<?> wrapper;

    @Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
                { boolean.class, Boolean.class },
                { byte.class, Byte.class },
                { char.class, Character.class },
                { double.class, Double.class },
                { float.class, Float.class },
                { int.class, Integer.class },
                { long.class, Long.class },
                { short.class, Short.class },
                { void.class, Void.class },
                { null, BigDecimal.class },
                { null, String.class }});
    }

    public FormatRulePrimitivesTest(Class<?> primitive, Class<?> wrapper) {
        this.primitive = primitive;
        this.wrapper = wrapper;

        when(config.isUsePrimitives()).thenReturn(true);
        when(config.getFormatTypeMapping()).thenReturn(Collections.singletonMap("test", wrapper.getName()));
        rule = new FormatRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));
    }

    @Test
    public void usePrimitivesWithCustomTypeMapping() {
        JType result = rule.apply("fooBar", TextNode.valueOf("test"), null, new JCodeModel().ref(Object.class), null);

        Class<?> expected = primitive != null ? primitive : wrapper;
        assertThat(result.fullName(), equalTo(expected.getName()));
        assertThat(result.isPrimitive(), equalTo(primitive != null));
    }

}
