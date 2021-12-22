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

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

/**
 * Tests {@link DigitsRuleTest}
 */
public class DigitsRuleTest extends AnnotationTestBase {

    private DigitsRule rule;
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
        rule = new DigitsRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));

    }

    @ParameterizedTest
    @MethodSource("data")
    public void testHasIntegerAndFractionalDigits(boolean isApplicable, Class<?> fieldClass, boolean useJakartaValidation) {
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
        Class<? extends Annotation> digitsClass = getDigitsClass(useJakartaValidation);

        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        final int intValue = new Random().nextInt();
        final int fractionalValue = new Random().nextInt();

        when(subNodeInteger.asInt()).thenReturn(intValue);
        when(subNodeFractional.asInt()).thenReturn(fractionalValue);
        when(node.get("integerDigits")).thenReturn(subNodeInteger);
        when(node.get("fractionalDigits")).thenReturn(subNodeFractional);
        when(fieldVar.annotate(digitsClass)).thenReturn(annotation);
        when(node.has("integerDigits")).thenReturn(true);
        when(node.has("fractionalDigits")).thenReturn(true);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(digitsClass);
        verify(annotation, times(isApplicable ? 1 : 0)).param("integer", intValue);
        verify(annotation, times(isApplicable ? 1 : 0)).param("fraction", fractionalValue);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testNotUsed(@SuppressWarnings("unused") boolean isApplicable, Class<?> fieldClass, boolean useJakartaValidation) {
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
        Class<? extends Annotation> sizeClass = getSizeClass(useJakartaValidation);

        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        when(node.has("integerDigits")).thenReturn(false);
        when(node.has("fractionalDigits")).thenReturn(false);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, never()).annotate(sizeClass);
        verify(annotation, never()).param(anyString(), anyInt());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void jsrDisable(boolean useJakartaValidation) {
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
        Class<? extends Annotation> decimalMinClass = getDigitsClass(useJakartaValidation);

        when(config.isIncludeJsr303Annotations()).thenReturn(false);
        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, never()).annotate(decimalMinClass);
        verify(annotation, never()).param(anyString(), anyInt());
    }

}
