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
import java.util.UUID;

import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.SchemaStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JFieldVar;

import jakarta.validation.constraints.Pattern;

/**
 * Tests {@link PatternRuleTest}
 */
@ExtendWith(MockitoExtension.class)
@ParameterizedClass(name = "useJakartaValidation={0}")
@ValueSource(booleans = {true, false})
class PatternRuleTest {

    private final String patternValue = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$";
    private final JsonNode node = JsonNodeFactory.instance.textNode(patternValue);
    private PatternRule rule;
    private Class<? extends Annotation> patternClass;
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
        rule = new PatternRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));
        patternClass = useJakartaValidation ? Pattern.class : javax.validation.constraints.Pattern.class;
    }

    @Test
    void fieldAnnotated_whenFieldTypeIsString() {
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
        when(fieldVar.annotate(patternClass)).thenReturn(annotation);
        when(fieldVar.type().boxify().fullName()).thenReturn(String.class.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);

        assertSame(fieldVar, result);
        verify(fieldVar).annotate(patternClass);
        verify(annotation).param("regexp", patternValue);
    }

    @ParameterizedTest
    @ValueSource(classes = { UUID.class, Collection.class, Map.class, Array.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class })
    void fieldNotAnnotated_whenFieldTypeIsNonApplicable(Class<?> fieldClass) {
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);

        assertSame(fieldVar, result);
        verifyNoInteractions(annotation);
    }

    @Test
    void jsrDisable() {
        when(config.isIncludeJsr303Annotations()).thenReturn(false);

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);

        assertSame(fieldVar, result);
        verify(fieldVar, never()).annotate(patternClass);
        verify(annotation, never()).param(anyString(), anyString());
    }

}
