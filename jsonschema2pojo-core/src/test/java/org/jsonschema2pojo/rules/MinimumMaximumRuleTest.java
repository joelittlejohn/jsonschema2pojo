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

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Random;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link MinimumMaximumRuleTest}
 */
@RunWith(Parameterized.class)
public class MinimumMaximumRuleTest {

    private final boolean isApplicable;
    private MinimumMaximumRule rule;
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
    private JAnnotationUse annotationMax;
    @Mock
    private JAnnotationUse annotationMin;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
                { true, BigDecimal.class },
                { true, BigInteger.class },
                { true, String.class },
                { true, Byte.class },
                { true, Short.class },
                { true, Integer.class },
                { true, Long.class },
                { false, Float.class },
                { false, Double.class },
        });
    }


    public MinimumMaximumRuleTest(boolean isApplicable, Class<?> fieldClass) {
        this.isApplicable = isApplicable;
        this.fieldClass = fieldClass;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        rule = new MinimumMaximumRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));
    }

    @Test
    public void testMinimum() {
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        final String minValue = Integer.toString(new Random().nextInt());
        when(subNode.asText()).thenReturn(minValue);
        when(node.get("minimum")).thenReturn(subNode);
        when(fieldVar.annotate(DecimalMin.class)).thenReturn(annotationMin);
        when(node.has("minimum")).thenReturn(true);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(DecimalMin.class);
        verify(annotationMin, times(isApplicable ? 1 : 0)).param("value", minValue);
        verify(fieldVar, never()).annotate(DecimalMax.class);
        verify(annotationMax, never()).param(eq("value"), anyString());
    }

    @Test
    public void testMaximum() {
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        final String maxValue = Integer.toString(new Random().nextInt());
        when(subNode.asText()).thenReturn(maxValue);
        when(node.get("maximum")).thenReturn(subNode);
        when(fieldVar.annotate(DecimalMax.class)).thenReturn(annotationMax);
        when(node.has("maximum")).thenReturn(true);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(DecimalMax.class);
        verify(annotationMax, times(isApplicable ? 1 : 0)).param("value", maxValue);
        verify(fieldVar, never()).annotate(DecimalMin.class);
        verify(annotationMin, never()).param(eq("value"), anyString());
    }

    @Test
    public void testMaximumAndMinimum() {
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        final String minValue = Integer.toString(new Random().nextInt());
        final String maxValue = Integer.toString(new Random().nextInt());
        JsonNode maxSubNode = Mockito.mock(JsonNode.class);
        when(subNode.asText()).thenReturn(minValue);
        when(maxSubNode.asText()).thenReturn(maxValue);
        when(node.get("minimum")).thenReturn(subNode);
        when(node.get("maximum")).thenReturn(maxSubNode);
        when(fieldVar.annotate(DecimalMin.class)).thenReturn(annotationMin);
        when(fieldVar.annotate(DecimalMax.class)).thenReturn(annotationMax);
        when(node.has("minimum")).thenReturn(true);
        when(node.has("maximum")).thenReturn(true);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(DecimalMin.class);
        verify(annotationMin, times(isApplicable ? 1 : 0)).param("value", minValue);
        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(DecimalMax.class);
        verify(annotationMax, times(isApplicable ? 1 : 0)).param("value", maxValue);
    }

    @Test
    public void testNotUsed() {
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        when(node.has("minimum")).thenReturn(false);
        when(node.has("maximum")).thenReturn(false);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, never()).annotate(Size.class);
        verify(annotationMin, never()).param(anyString(), anyString());
        verify(annotationMax, never()).param(anyString(), anyString());
    }

    @Test
    public void jsrDisable() {
        when(config.isIncludeJsr303Annotations()).thenReturn(false);
        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, never()).annotate(DecimalMin.class);
        verify(annotationMin, never()).param(anyString(), anyString());
        verify(fieldVar, never()).annotate(DecimalMax.class);
        verify(annotationMax, never()).param(anyString(), anyString());
    }
}