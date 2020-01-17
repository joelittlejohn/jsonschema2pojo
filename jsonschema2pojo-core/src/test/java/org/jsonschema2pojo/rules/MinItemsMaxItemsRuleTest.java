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

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JFieldVar;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.SchemaStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.validation.constraints.Size;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Random;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link MinItemsMaxItemsRuleTest}
 */
@RunWith(Parameterized.class)
public class MinItemsMaxItemsRuleTest {

    private final boolean isApplicable;
    private MinItemsMaxItemsRule rule;
    private Class<?> fieldClass;
    @Mock
    private GenerationConfig config;
    @Mock
    private JsonNode node;
    @Mock
    private JsonNode subNode;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JFieldVar fieldVar;
    @Mock
    private JAnnotationUse annotation;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
                { true, String.class },
                { true, Collection.class },
                { true, Map.class },
                { true, Array.class },
                { false, Byte.class },
                { false, Short.class },
                { false, Integer.class },
                { false, Long.class },
                { false, Float.class },
                { false, Double.class },
        });
    }


    public MinItemsMaxItemsRuleTest(boolean isApplicable, Class<?> fieldClass) {
        this.isApplicable = isApplicable;
        this.fieldClass = fieldClass;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        rule = new MinItemsMaxItemsRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));
    }

    @Test
    public void testMinLength() {
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        final int minValue = new Random().nextInt();
        when(subNode.asInt()).thenReturn(minValue);
        when(node.get("minItems")).thenReturn(subNode);
        when(fieldVar.annotate(Size.class)).thenReturn(annotation);
        when(node.has("minItems")).thenReturn(true);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(Size.class);
        verify(annotation, times(isApplicable ? 1 : 0)).param("min", minValue);
        verify(annotation, never()).param(eq("max"), anyString());
    }

    @Test
    public void testMaxLength() {
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        final int maxValue = new Random().nextInt();
        when(subNode.asInt()).thenReturn(maxValue);
        when(node.get("maxItems")).thenReturn(subNode);
        when(fieldVar.annotate(Size.class)).thenReturn(annotation);
        when(node.has("maxItems")).thenReturn(true);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(Size.class);
        verify(annotation, times(isApplicable ? 1 : 0)).param("max", maxValue);
        verify(annotation, never()).param(eq("min"), anyInt());
    }

    @Test
    public void testMaxAndMinLength() {
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        final int minValue = new Random().nextInt();
        final int maxValue = new Random().nextInt();
        JsonNode maxSubNode = Mockito.mock(JsonNode.class);
        when(subNode.asInt()).thenReturn(minValue);
        when(maxSubNode.asInt()).thenReturn(maxValue);
        when(node.get("minItems")).thenReturn(subNode);
        when(node.get("maxItems")).thenReturn(maxSubNode);
        when(fieldVar.annotate(Size.class)).thenReturn(annotation);
        when(node.has("minItems")).thenReturn(true);
        when(node.has("maxItems")).thenReturn(true);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(Size.class);
        verify(annotation, times(isApplicable ? 1 : 0)).param("min", minValue);
        verify(annotation, times(isApplicable ? 1 : 0)).param("max", maxValue);
    }

    @Test
    public void testMaxAndMinLengthGenericsOnType() {
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        final int minValue = new Random().nextInt();
        final int maxValue = new Random().nextInt();
        JsonNode maxSubNode = Mockito.mock(JsonNode.class);
        when(subNode.asInt()).thenReturn(minValue);
        when(maxSubNode.asInt()).thenReturn(maxValue);
        when(node.get("minItems")).thenReturn(subNode);
        when(node.get("maxItems")).thenReturn(maxSubNode);
        when(fieldVar.annotate(Size.class)).thenReturn(annotation);
        when(node.has("minItems")).thenReturn(true);
        when(node.has("maxItems")).thenReturn(true);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName() + "<String>");

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(Size.class);
        verify(annotation, times(isApplicable ? 1 : 0)).param("min", minValue);
        verify(annotation, times(isApplicable ? 1 : 0)).param("max", maxValue);
    }

    @Test
    public void testNotUsed() {
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        when(node.has("minItems")).thenReturn(false);
        when(node.has("maxItems")).thenReturn(false);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, never()).annotate(Size.class);
        verify(annotation, never()).param(anyString(), anyInt());
    }

    @Test
    public void jsrDisable() {
        when(config.isIncludeJsr303Annotations()).thenReturn(false);
        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, never()).annotate(Size.class);
        verify(annotation, never()).param(anyString(), anyInt());
    }
}