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
import jakarta.validation.constraints.Pattern;
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
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

/**
 * Tests {@link PatternRuleTest}
 */
public class PatternRuleTest extends AnnotationTestBase{

    private PatternRule rule;
    @Mock
    private GenerationConfig config;
    @Mock
    private JsonNode node;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JFieldVar fieldVar;
    @Mock
    private JAnnotationUse annotation;

    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(true, String.class, true),
                Arguments.of(true, String.class, false),
                Arguments.of(false, UUID.class, true),
                Arguments.of(false, UUID.class, false),
                Arguments.of(false, Collection.class, true),
                Arguments.of(false, Collection.class, false),
                Arguments.of(false, Map.class, true),
                Arguments.of(false, Map.class, false),
                Arguments.of(false, Array.class, true),
                Arguments.of(false, Array.class, false),
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
        rule = new PatternRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testRegex(boolean isApplicable, Class<?> fieldClass, boolean useJakartaValidation) {
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
        Class<? extends Annotation> patternClass = getPatterClass(useJakartaValidation);

        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        final String patternValue = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$";

        when(node.asText()).thenReturn(patternValue);
        when(fieldVar.annotate(patternClass)).thenReturn(annotation);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(patternClass);
        verify(annotation, times(isApplicable ? 1 : 0)).param("regexp", patternValue);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void jsrDisable(boolean useJakartaValidation) {
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
        Class<? extends Annotation> patternClass = getPatterClass(useJakartaValidation);

        when(config.isIncludeJsr303Annotations()).thenReturn(false);
        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, never()).annotate(patternClass);
        verify(annotation, never()).param(anyString(), anyString());
    }
}
