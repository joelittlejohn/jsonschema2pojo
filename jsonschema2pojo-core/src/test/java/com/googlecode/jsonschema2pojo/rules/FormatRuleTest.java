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
                {"uri", "java.lang.String"}});
    }

    public FormatRuleTest(String formatValue, String expectedTypeName) {
        this.formatValue = formatValue;
        this.expectedTypeName = expectedTypeName;
    }

    @Test
    public void applyGeneratesTypeFromFormatValue() {
        TextNode formatNode = TextNode.valueOf(formatValue);

        JType result = rule.apply("fooBar", formatNode, new JCodeModel()._package("com.example"));

        assertThat(result.fullName(), equalTo(expectedTypeName));
    }

}
