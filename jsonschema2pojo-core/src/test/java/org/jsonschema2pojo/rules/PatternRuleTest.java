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

import javax.validation.constraints.Pattern;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link PatternRuleTest}
 */
@RunWith(Parameterized.class)
public class PatternRuleTest {

    private final boolean isApplicable;
    private PatternRule rule;
    private Class<?> fieldClass;
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
        });
    }


    public PatternRuleTest(boolean isApplicable, Class<?> fieldClass) {
        this.isApplicable = isApplicable;
        this.fieldClass = fieldClass;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        rule = new PatternRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));
    }

    @Test
    public void testRegex() {
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        final String patternValue = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$";

        when(node.asText()).thenReturn(patternValue);
        when(fieldVar.annotate(Pattern.class)).thenReturn(annotation);
        when(fieldVar.type().boxify().fullName()).thenReturn(fieldClass.getTypeName());

        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, times(isApplicable ? 1 : 0)).annotate(Pattern.class);
        verify(annotation, times(isApplicable ? 1 : 0)).param("regexp", patternValue);
    }

    @Test
    public void jsrDisable() {
        when(config.isIncludeJsr303Annotations()).thenReturn(false);
        JFieldVar result = rule.apply("node", node, null, fieldVar, null);
        assertSame(fieldVar, result);

        verify(fieldVar, never()).annotate(Pattern.class);
        verify(annotation, never()).param(anyString(), anyString());
    }

}