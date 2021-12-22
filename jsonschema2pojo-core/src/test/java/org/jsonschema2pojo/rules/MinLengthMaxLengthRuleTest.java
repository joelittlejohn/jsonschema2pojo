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
import jakarta.validation.constraints.Size;
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
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

/**
 * Tests {@link MinLengthMaxLengthRuleTest}
 */
public class MinLengthMaxLengthRuleTest extends AnnotationTestBase{

    private MinLengthMaxLengthRule rule;
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

    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(true, String.class, true),
                Arguments.of(true, String.class, false),
                Arguments.of(true, Collection.class, true),
                Arguments.of(true, Collection.class, false),
                Arguments.of(true, Map.class, true),
                Arguments.of(true, Map.class, false),
                Arguments.of(true, Array.class, true),
                Arguments.of(true, Array.class, false),
                Arguments.of(false, Byte.class, true),
                Arguments.of(false, Byte.class, false),
                Arguments.of(false, Short.class, true),
                Arguments.of(false, Short.class, false),
                Arguments.of(false, Integer.class, true),
                Arguments.of(false, Integer.class, false),
                Arguments.of(false, Long.class, true),
                Arguments.of(false, Long.class, false),
                Arguments.of(false, Float.class, true),
                Arguments.of(false, Float.class, false),
                Arguments.of(false, Double.class, true),
                Arguments.of(false, Double.class, false)
        );
    }

    @BeforeEach
    public void setUp() {
        rule = new MinLengthMaxLengthRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testMinLength(boolean isApplicable, Class<?> fieldClass, boolean useJakartaValidation) {
        Class<? extends Annotation> sizeClass = getSizeClass(useJakartaValidation);
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        final int minValue = new Random().nextInt();
        when(subNode.asInt()).thenReturn(minValue);
        when(node.get("minLength")).thenReturn(subNode);
        when(fieldVar.annotate(sizeClass)).thenReturn(annotation);
        when(node.has("minLength")).thenReturn(true);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(sizeClass);
        verify(annotation, times(isApplicable ? 1 : 0)).param("min", minValue);
        verify(annotation, never()).param(eq("max"), anyString());
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testMaxLength(boolean isApplicable, Class<?> fieldClass, boolean useJakartaValidation) {
        Class<? extends Annotation> sizeClass = getSizeClass(useJakartaValidation);
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);

        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        final int maxValue = new Random().nextInt();
        when(subNode.asInt()).thenReturn(maxValue);
        when(node.get("maxLength")).thenReturn(subNode);
        when(fieldVar.annotate(sizeClass)).thenReturn(annotation);
        when(node.has("maxLength")).thenReturn(true);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(sizeClass);
        verify(annotation, times(isApplicable ? 1 : 0)).param("max", maxValue);
        verify(annotation, never()).param(eq("min"), anyInt());
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testMaxAndMinLength(boolean isApplicable, Class<?> fieldClass, boolean useJakartaValidation) {
        Class<? extends Annotation> sizeClass = getSizeClass(useJakartaValidation);
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        final int minValue = new Random().nextInt();
        final int maxValue = new Random().nextInt();
        JsonNode maxSubNode = Mockito.mock(JsonNode.class);
        when(subNode.asInt()).thenReturn(minValue);
        when(maxSubNode.asInt()).thenReturn(maxValue);
        when(node.get("minLength")).thenReturn(subNode);
        when(node.get("maxLength")).thenReturn(maxSubNode);
        when(fieldVar.annotate(sizeClass)).thenReturn(annotation);
        when(node.has("minLength")).thenReturn(true);
        when(node.has("maxLength")).thenReturn(true);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(sizeClass);
        verify(annotation, times(isApplicable ? 1 : 0)).param("min", minValue);
        verify(annotation, times(isApplicable ? 1 : 0)).param("max", maxValue);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testMaxAndMinLengthGenericsOnType(boolean isApplicable, Class<?> fieldClass, boolean useJakartaValidation) {
        Class<? extends Annotation> sizeClass = getSizeClass(useJakartaValidation);
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        final int minValue = new Random().nextInt();
        final int maxValue = new Random().nextInt();
        JsonNode maxSubNode = Mockito.mock(JsonNode.class);
        when(subNode.asInt()).thenReturn(minValue);
        when(maxSubNode.asInt()).thenReturn(maxValue);
        when(node.get("minLength")).thenReturn(subNode);
        when(node.get("maxLength")).thenReturn(maxSubNode);
        when(fieldVar.annotate(sizeClass)).thenReturn(annotation);
        when(node.has("minLength")).thenReturn(true);
        when(node.has("maxLength")).thenReturn(true);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName() + "<String>");

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(sizeClass);
        verify(annotation, times(isApplicable ? 1 : 0)).param("min", minValue);
        verify(annotation, times(isApplicable ? 1 : 0)).param("max", maxValue);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testNotUsed(@SuppressWarnings("unused") boolean isApplicable, Class<?> fieldClass, boolean useJakartaValidation) {
        Class<? extends Annotation> sizeClass = getSizeClass(useJakartaValidation);
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        when(node.has("minLength")).thenReturn(false);
        when(node.has("maxLength")).thenReturn(false);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, never()).annotate(sizeClass);
        verify(annotation, never()).param(anyString(), anyInt());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void jsrDisable(boolean useJakartaValidation) {
        Class<? extends Annotation> sizeClass = getSizeClass(useJakartaValidation);
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
        when(config.isIncludeJsr303Annotations()).thenReturn(false);
        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, never()).annotate(sizeClass);
        verify(annotation, never()).param(anyString(), anyInt());
    }

}
