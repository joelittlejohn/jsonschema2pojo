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
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
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

import jakarta.validation.constraints.Size;

/**
 * Tests {@link MinLengthMaxLengthRuleTest}
 */
@ExtendWith(MockitoExtension.class)
@ParameterizedClass(name = "useJakartaValidation={0}")
@ValueSource(booleans = {true, false})
class MinLengthMaxLengthRuleTest {

    private final ObjectNode node = JsonNodeFactory.instance.objectNode();
    private MinLengthMaxLengthRule rule;
    private Class<? extends Annotation> sizeClass;
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
        rule = new MinLengthMaxLengthRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));
        sizeClass = useJakartaValidation ? Size.class : javax.validation.constraints.Size.class;
    }

    @Nested
    @ParameterizedClass
    @ValueSource(classes = { Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class })
    class NonApplicableTypeTests {

        @Parameter
        private Class<?> fieldClass;

        @Test
        void testMinLength() {
            node.put("minLength", new Random().nextInt());
            when(config.isIncludeJsr303Annotations()).thenReturn(true);
            when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

            JFieldVar result = rule.apply("node", node, null, fieldVar, null);

            assertSame(fieldVar, result);
            verifyNoInteractions(annotation);
        }

        @Test
        void testMaxLength() {
            node.put("maxLength", new Random().nextInt());
            when(config.isIncludeJsr303Annotations()).thenReturn(true);
            when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

            JFieldVar result = rule.apply("node", node, null, fieldVar, null);

            assertSame(fieldVar, result);
            verifyNoInteractions(annotation);
        }

    }

    @Nested
    @ParameterizedClass
    @ValueSource(classes = { String.class, Collection.class, Map.class, Array.class })
    class ApplicableTypeTests {

        private final int minValue = new Random().nextInt();
        private final int maxValue = new Random().nextInt();
        @Parameter
        private Class<?> fieldClass;

        @Test
        void testMinLength() {
            node.put("minLength", minValue);
            when(config.isIncludeJsr303Annotations()).thenReturn(true);
            when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
            when(fieldVar.annotate(sizeClass)).thenReturn(annotation);
            when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

            JFieldVar result = rule.apply("node", node, null, fieldVar, null);

            assertSame(fieldVar, result);
            verify(fieldVar).annotate(sizeClass);
            verify(annotation).param("min", minValue);
            verifyNoMoreInteractions(annotation);
        }

        @Test
        void testMaxLength() {
            node.put("maxLength", maxValue);
            when(config.isIncludeJsr303Annotations()).thenReturn(true);
            when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
            when(fieldVar.annotate(sizeClass)).thenReturn(annotation);
            when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

            JFieldVar result = rule.apply("node", node, null, fieldVar, null);

            assertSame(fieldVar, result);
            verify(fieldVar).annotate(sizeClass);
            verify(annotation).param("max", maxValue);
            verifyNoMoreInteractions(annotation);
        }

        @Test
        void testMaxAndMinLength() {
            node.put("minLength", minValue);
            node.put("maxLength", maxValue);
            when(config.isIncludeJsr303Annotations()).thenReturn(true);
            when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
            when(fieldVar.annotate(sizeClass)).thenReturn(annotation);
            when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

            JFieldVar result = rule.apply("node", node, null, fieldVar, null);

            assertSame(fieldVar, result);
            verify(fieldVar).annotate(sizeClass);
            verify(annotation).param("min", minValue);
            verify(annotation).param("max", maxValue);
        }

        @Test
        void testMaxAndMinLengthGenericsOnType() {
            node.put("minLength", minValue);
            node.put("maxLength", maxValue);
            when(config.isIncludeJsr303Annotations()).thenReturn(true);
            when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
            when(fieldVar.annotate(sizeClass)).thenReturn(annotation);
            when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName() + "<String>");

            JFieldVar result = rule.apply("node", node, null, fieldVar, null);

            assertSame(fieldVar, result);
            verify(fieldVar).annotate(sizeClass);
            verify(annotation).param("min", minValue);
            verify(annotation).param("max", maxValue);
        }

        @Test
        void testNotUsed() {
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
        verifyNoInteractions(fieldVar, annotation);
    }

}
