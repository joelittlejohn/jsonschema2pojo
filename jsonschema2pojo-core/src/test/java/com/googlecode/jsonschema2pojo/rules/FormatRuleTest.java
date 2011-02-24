/**
 * Copyright © 2010-2011 Nokia
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

package com.googlecode.jsonschema2pojo.rules;

import static java.util.Arrays.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Collection;

import org.codehaus.jackson.node.TextNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;

@RunWith(Parameterized.class)
public class FormatRuleTest {

    private FormatRule rule = new FormatRule();

    private final String formatValue;
    private final String expectedTypeName;

    @Parameters
    public static Collection<String[]> data() {
        return asList(new String[][] {
                {"date-time", "java.util.Date"},
                {"date", "java.lang.String"},
                {"time", "java.lang.String"},
                {"utc-millisec", "long"},
                {"regex", "java.lang.String"},
                {"color", "java.lang.String"},
                {"style", "java.lang.String"},
                {"phone", "java.lang.String"},
                {"phone", "java.lang.String"},
                {"uri", "java.lang.String"},
                {"email", "java.lang.String"},
                {"ip-address", "java.lang.String"},
                {"ipv6", "java.lang.String"},
                {"host-name", "java.lang.String"}});
    }

    public FormatRuleTest(String formatValue, String expectedTypeName) {
        this.formatValue = formatValue;
        this.expectedTypeName = expectedTypeName;
    }

    @Test
    public void applyGeneratesTypeFromFormatValue() {
        TextNode formatNode = TextNode.valueOf(formatValue);

        JType result = rule.apply("fooBar", formatNode, new JCodeModel().ref(String.class), null);

        assertThat(result.fullName(), equalTo(expectedTypeName));
    }

    @Test
    public void applyDefaultsToBaseType() {
        TextNode formatNode = TextNode.valueOf("unknown-format");

        JType baseType = new JCodeModel().ref(Long.class);

        JType result = rule.apply("fooBar", formatNode, baseType, null);

        assertThat(result, equalTo(baseType));
    }

}
