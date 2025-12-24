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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.SchemaStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JFieldVar;

import jakarta.validation.constraints.Digits;

/**
 * Tests {@link DigitsRuleTest}
 */
@ExtendWith(MockitoExtension.class)
@ParameterizedClass(name = "useJakartaValidation={0}")
@ValueSource(booleans = {true, false})
class DigitsRuleTest {

    private final int intValue = new Random().nextInt();
    private final int fractionalValue = new Random().nextInt();
    private final ObjectNode node = JsonNodeFactory.instance.objectNode().put("integerDigits", intValue).put("fractionalDigits", fractionalValue);
    private DigitsRule rule;
    private Class<? extends Annotation> digitsClass;
    @Parameter
    private boolean useJakartaValidation;
    @Mock
    private GenerationConfig config;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JFieldVar fieldVar;
    @Mock
    private JAnnotationUse annotation;

    @BeforeEach
    void setUp() {
        rule = new DigitsRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));
        digitsClass = useJakartaValidation ? Digits.class: javax.validation.constraints.Digits.class;
    }

    @ParameterizedTest
    @ValueSource(classes = {Float.class, Double.class})
    void fieldNotAnnotated_whenFieldTypeIsNonApplicable(Class<?> fieldClass) {
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        when(fieldVar.annotate(digitsClass)).thenReturn(annotation);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);

        assertSame(fieldVar, result);
        verifyNoInteractions(annotation);
    }

    @Nested
    @ParameterizedClass
    @ValueSource(classes = { BigDecimal.class, BigInteger.class, String.class, Byte.class, Short.class, Integer.class, Long.class })
    class ApplicableTypeTests {

        @Parameter
        private Class<?> fieldClass;

        @Test
        void testHasIntegerAndFractionalDigits() {
            when(config.isIncludeJsr303Annotations()).thenReturn(true);
            when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
            when(fieldVar.annotate(digitsClass)).thenReturn(annotation);
            when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

            JFieldVar result = rule.apply("node", node, null, fieldVar, null);

            assertSame(fieldVar, result);
            verify(fieldVar).annotate(digitsClass);
            verify(annotation).param("integer", intValue);
            verify(annotation).param("fraction", fractionalValue);
        }

        @Test
        void testNotUsed() {
            node.removeAll();
            when(config.isIncludeJsr303Annotations()).thenReturn(true);

            JFieldVar result = rule.apply("node", node, null, fieldVar, null);

            assertSame(fieldVar, result);
            verifyNoInteractions(annotation, fieldVar);
        }

    }

    @Test
     void jsrDisable() {
        when(config.isIncludeJsr303Annotations()).thenReturn(false);

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);

        assertSame(fieldVar, result);
        verifyNoInteractions(annotation, fieldVar);
    }

}
