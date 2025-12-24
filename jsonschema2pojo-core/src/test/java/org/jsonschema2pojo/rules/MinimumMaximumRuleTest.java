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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JFieldVar;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

/**
 * Tests {@link MinimumMaximumRuleTest}
 */
@ExtendWith(MockitoExtension.class)
@ParameterizedClass(name = "useJakartaValidation={0}")
@ValueSource(booleans = {true, false})
class MinimumMaximumRuleTest {

    private final ObjectNode node = JsonNodeFactory.instance.objectNode();
    private Class<? extends Annotation> decimalMaxClass;
    private Class<? extends Annotation> decimalMinClass;
    private MinimumMaximumRule rule;
    @Parameter
    private boolean useJakartaValidation;
    @Mock
    private GenerationConfig config;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JFieldVar fieldVar;
    @Mock
    private JAnnotationUse annotationMax;
    @Mock
    private JAnnotationUse annotationMin;

    @BeforeEach
    void setUp() {
        rule = new MinimumMaximumRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));
        if (useJakartaValidation) {
            decimalMaxClass = DecimalMax.class;
            decimalMinClass = DecimalMin.class;
        } else {
            decimalMaxClass = javax.validation.constraints.DecimalMax.class;
            decimalMinClass = javax.validation.constraints.DecimalMin.class;
        }
    }

    @Nested
    @ParameterizedClass
    @ValueSource(classes = { Float.class, Double.class })
    class NonApplicableTypeTests {

        @Parameter
        Class<?> fieldClass;

        @BeforeEach
        void setUp() {
            when(config.isIncludeJsr303Annotations()).thenReturn(true);
            when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());
        }

        @Test
        void testMinimum() {
            final var minValue = new Random().nextInt();
            node.put("minimum", minValue);

            JFieldVar result = rule.apply("node", node, null, fieldVar, null);

            assertSame(fieldVar, result);
            verifyNoInteractions(annotationMin, annotationMax);
        }

        @Test
        void testMaximum() {
            node.put("maximum", new Random().nextInt());

            JFieldVar result = rule.apply("node", node, null, fieldVar, null);

            assertSame(fieldVar, result);
            verifyNoInteractions(annotationMin, annotationMax);
        }

    }

    @Nested
    @ParameterizedClass
    @ValueSource(classes = { BigDecimal.class, BigInteger.class, String.class, Byte.class, Short.class, Integer.class, Long.class })
    class ApplicableTypesTests {

        private final int minValue = new Random().nextInt();
        private final int maxValue = new Random().nextInt();
        @Parameter
        private Class<? extends Annotation> fieldClass;

        @BeforeEach
        void setUp() {
            when(config.isIncludeJsr303Annotations()).thenReturn(true);
            when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());
        }

        @Test
        void testMinimum() {
            node.put("minimum", minValue);
            when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
            when(fieldVar.annotate(decimalMinClass)).thenReturn(annotationMin);

            JFieldVar result = rule.apply("node", node, null, fieldVar, null);

            assertSame(fieldVar, result);
            verify(annotationMin).param("value", String.valueOf(minValue));
            verifyNoInteractions(annotationMax);
        }

        @Test
        void testMaximum() {
            node.put("maximum", maxValue);
            when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
            when(fieldVar.annotate(decimalMaxClass)).thenReturn(annotationMax);

            JFieldVar result = rule.apply("node", node, null, fieldVar, null);

            assertSame(fieldVar, result);
            verify(annotationMax).param("value", String.valueOf(maxValue));
            verifyNoInteractions(annotationMin);
        }

        @Test
        void testMaximumAndMinimum() {
            node.put("minimum", minValue);
            node.put("maximum", maxValue);
            when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
            when(fieldVar.annotate(decimalMinClass)).thenReturn(annotationMin);
            when(fieldVar.annotate(decimalMaxClass)).thenReturn(annotationMax);

            JFieldVar result = rule.apply("node", node, null, fieldVar, null);

            assertSame(fieldVar, result);
            verify(annotationMin).param("value", String.valueOf(minValue));
            verify(annotationMax).param("value", String.valueOf(maxValue));
        }

        @Test
        void testNotUsed() {
            when(config.isIncludeJsr303Annotations()).thenReturn(true);
            when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

            JFieldVar result = rule.apply("node", node, null, fieldVar, null);

            assertSame(fieldVar, result);
            verifyNoInteractions(annotationMin, annotationMax);
        }

    }

    @Test
    void jsrDisable() {
        when(config.isIncludeJsr303Annotations()).thenReturn(false);

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);

        assertSame(fieldVar, result);
        verifyNoInteractions(fieldVar, annotationMin, annotationMax);
    }

}
