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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

/**
 * Tests {@link MinimumMaximumRuleTest}
 */
public class MinimumMaximumRuleTest extends AnnotationTestBase {

    private MinimumMaximumRule rule;
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

    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(true, BigDecimal.class, true),
                Arguments.of(true, BigDecimal.class, false),
                Arguments.of(true, BigInteger.class, true),
                Arguments.of(true, BigInteger.class, false),
                Arguments.of(true, String.class, true),
                Arguments.of(true, String.class, false),
                Arguments.of(true, Byte.class, true),
                Arguments.of(true, Byte.class, false),
                Arguments.of(true, Short.class, true),
                Arguments.of(true, Short.class, false),
                Arguments.of(true, Integer.class, true),
                Arguments.of(true, Integer.class, false),
                Arguments.of(true, Long.class, true),
                Arguments.of(true, Long.class, false),
                Arguments.of(false, Float.class, true),
                Arguments.of(false, Float.class, false),
                Arguments.of(false, Double.class, true),
                Arguments.of(false, Double.class, false)
        );
    }

    @BeforeEach
    public void setUp() {
        rule = new MinimumMaximumRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testMinimum(boolean isApplicable, Class<?> fieldClass, boolean useJakartaValidation) {
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
        Class<? extends Annotation> decimalMinClass = getDecimalMinClass(useJakartaValidation);
        Class<? extends Annotation> decimalMaxClass = getDecimalMaxClass(useJakartaValidation);

        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        final String minValue = Integer.toString(new Random().nextInt());
        when(subNode.asText()).thenReturn(minValue);
        when(node.get("minimum")).thenReturn(subNode);
        when(fieldVar.annotate(decimalMinClass)).thenReturn(annotationMin);
        when(node.has("minimum")).thenReturn(true);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(decimalMinClass);
        verify(annotationMin, times(isApplicable ? 1 : 0)).param("value", minValue);
        verify(fieldVar, never()).annotate(decimalMaxClass);
        verify(annotationMax, never()).param(eq("value"), anyString());
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testMaximum(boolean isApplicable, Class<?> fieldClass, boolean useJakartaValidation) {
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
        Class<? extends Annotation> decimalMinClass = getDecimalMinClass(useJakartaValidation);
        Class<? extends Annotation> decimalMaxClass = getDecimalMaxClass(useJakartaValidation);

        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        final String maxValue = Integer.toString(new Random().nextInt());
        when(subNode.asText()).thenReturn(maxValue);
        when(node.get("maximum")).thenReturn(subNode);
        when(fieldVar.annotate(decimalMaxClass)).thenReturn(annotationMax);
        when(node.has("maximum")).thenReturn(true);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(decimalMaxClass);
        verify(annotationMax, times(isApplicable ? 1 : 0)).param("value", maxValue);
        verify(fieldVar, never()).annotate(decimalMinClass);
        verify(annotationMin, never()).param(eq("value"), anyString());
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testMaximumAndMinimum(boolean isApplicable, Class<?> fieldClass, boolean useJakartaValidation) {
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
        Class<? extends Annotation> decimalMinClass = getDecimalMinClass(useJakartaValidation);
        Class<? extends Annotation> decimalMaxClass = getDecimalMaxClass(useJakartaValidation);
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        final String minValue = Integer.toString(new Random().nextInt());
        final String maxValue = Integer.toString(new Random().nextInt());
        JsonNode maxSubNode = Mockito.mock(JsonNode.class);
        when(subNode.asText()).thenReturn(minValue);
        when(maxSubNode.asText()).thenReturn(maxValue);
        when(node.get("minimum")).thenReturn(subNode);
        when(node.get("maximum")).thenReturn(maxSubNode);
        when(fieldVar.annotate(decimalMinClass)).thenReturn(annotationMin);
        when(fieldVar.annotate(decimalMaxClass)).thenReturn(annotationMax);
        when(node.has("minimum")).thenReturn(true);
        when(node.has("maximum")).thenReturn(true);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(decimalMinClass);
        verify(annotationMin, times(isApplicable ? 1 : 0)).param("value", minValue);
        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(decimalMaxClass);
        verify(annotationMax, times(isApplicable ? 1 : 0)).param("value", maxValue);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testNotUsed(@SuppressWarnings("unused") boolean isApplicable, Class<?> fieldClass, boolean useJakartaValidation) {
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
        Class<? extends Annotation> sizeClass = getSizeClass(useJakartaValidation);

        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        when(node.has("minimum")).thenReturn(false);
        when(node.has("maximum")).thenReturn(false);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, never()).annotate(sizeClass);
        verify(annotationMin, never()).param(anyString(), anyString());
        verify(annotationMax, never()).param(anyString(), anyString());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void jsrDisable(boolean useJakartaValidation) {
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
        Class<? extends Annotation> decimalMinClass = getDecimalMinClass(useJakartaValidation);
        Class<? extends Annotation> decimalMaxClass = getDecimalMaxClass(useJakartaValidation);

        when(config.isIncludeJsr303Annotations()).thenReturn(false);
        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, never()).annotate(decimalMinClass);
        verify(annotationMin, never()).param(anyString(), anyString());
        verify(fieldVar, never()).annotate(decimalMaxClass);
        verify(annotationMax, never()).param(anyString(), anyString());
    }
}
