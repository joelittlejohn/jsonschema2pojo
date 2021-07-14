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

import static java.util.Arrays.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JFieldVar;

import jakarta.validation.constraints.Pattern;

/**
 * Tests {@link PatternRuleTest}
 */
@RunWith(Parameterized.class)
public class PatternRuleTest {

    private final boolean isApplicable;
    private PatternRule rule;
    private Class<?> fieldClass;
    private final boolean useJakartaValidation;
    private final Class<? extends Annotation> patternClass;
    @Mock
    private GenerationConfig config;
    @Mock
    private JsonNode node;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JFieldVar fieldVar;
    @Mock
    private JAnnotationUse annotation;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
                { true, String.class },
                { false, UUID.class },
                { false, Collection.class },
                { false, Map.class },
                { false, Array.class },
                { false, Byte.class },
                { false, Short.class },
                { false, Integer.class },
                { false, Long.class },
                { false, Float.class },
                { false, Double.class },
        }).stream()
                .flatMap(o -> Stream.of(true, false).map(b -> Stream.concat(stream(o), Stream.of(b)).toArray()))
                .collect(Collectors.toList());
    }

    public PatternRuleTest(boolean isApplicable, Class<?> fieldClass, boolean useJakartaValidation) {
        this.isApplicable = isApplicable;
        this.fieldClass = fieldClass;
        this.useJakartaValidation = useJakartaValidation;
        this.patternClass = useJakartaValidation ? Pattern.class : javax.validation.constraints.Pattern.class;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        rule = new PatternRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
    }

    @Test
    public void testRegex() {
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

    @Test
    public void jsrDisable() {
        when(config.isIncludeJsr303Annotations()).thenReturn(false);
        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, never()).annotate(patternClass);
        verify(annotation, never()).param(anyString(), anyString());
    }

}