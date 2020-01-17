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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.databind.node.TextNode;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.SchemaStore;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

@RunWith(Parameterized.class)
public class FormatRuleJodaTest {

    private GenerationConfig config = mock(GenerationConfig.class);
    private FormatRule rule;

    private final String formatValue;
    private final Class<?> expectedType;

    @Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
                { "date-time", DateTime.class },
                { "date", LocalDate.class },
                { "time", LocalTime.class }});
    }

    public FormatRuleJodaTest(String formatValue, Class<?> expectedType) {
        this.formatValue = formatValue;
        this.expectedType = expectedType;
    }

    @Before
    public void setupConfig() {
        when(config.isUseJodaLocalTimes()).thenReturn(true);
        when(config.isUseJodaLocalDates()).thenReturn(true);
        when(config.isUseJodaDates()).thenReturn(true);
        rule = new FormatRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));
    }

    @Test
    public void applyGeneratesTypeFromFormatValue() {
        TextNode formatNode = TextNode.valueOf(formatValue);

        JType result = rule.apply("fooBar", formatNode, null, new JCodeModel().ref(String.class), null);

        assertThat(result.fullName(), equalTo(expectedType.getName()));
    }

}
