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
import org.mockito.MockitoAnnotations;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Random;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link DigitsRuleTest}
 */
@RunWith(Parameterized.class)
public class DigitsRuleTest {

    private final boolean isApplicable;
    private DigitsRule rule;
    private Class<?> fieldClass;
    @Mock
    private GenerationConfig config;
    @Mock
    private JsonNode node;
    @Mock
    private JsonNode subNodeInteger;
    @Mock
    private JsonNode subNodeFractional;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JFieldVar fieldVar;
    @Mock
    private JAnnotationUse annotation;

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


    public DigitsRuleTest(boolean isApplicable, Class<?> fieldClass) {
        this.isApplicable = isApplicable;
        this.fieldClass = fieldClass;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        rule = new DigitsRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));
    }

    @Test
    public void testHasIntegerAndFractionalDigits() {
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        final int intValue = new Random().nextInt();
        final int fractionalValue = new Random().nextInt();

        when(subNodeInteger.asInt()).thenReturn(intValue);
        when(subNodeFractional.asInt()).thenReturn(fractionalValue);
        when(node.get("integerDigits")).thenReturn(subNodeInteger);
        when(node.get("fractionalDigits")).thenReturn(subNodeFractional);
        when(fieldVar.annotate(Digits.class)).thenReturn(annotation);
        when(node.has("integerDigits")).thenReturn(true);
        when(node.has("fractionalDigits")).thenReturn(true);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(Digits.class);
        verify(annotation, times(isApplicable ? 1 : 0)).param("integer", intValue);
        verify(annotation, times(isApplicable ? 1 : 0)).param("fraction", fractionalValue);
    }

    @Test
    public void testNotUsed() {
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        when(node.has("integerDigits")).thenReturn(false);
        when(node.has("fractionalDigits")).thenReturn(false);
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

        verify(fieldVar, never()).annotate(DecimalMin.class);
        verify(annotation, never()).param(anyString(), anyInt());
    }

}