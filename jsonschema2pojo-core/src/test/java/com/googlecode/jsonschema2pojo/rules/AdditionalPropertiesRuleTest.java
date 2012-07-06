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

import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.googlecode.jsonschema2pojo.Schema;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFormatter;

public class AdditionalPropertiesRuleTest {

    private static final String TARGET_CLASS_NAME = AdditionalPropertiesRuleTest.class.getName() + ".DummyClass";

    private AdditionalPropertiesRule rule = new AdditionalPropertiesRule(new RuleFactoryImpl());

    private static final String EXPECTED_RESULT_NO_ADDITIONAL_PROPS = "public class DummyClass {\n\n\n}\n";

    private static final String EXPECTED_RESULT_DEFAULT_ADDITIONAL_PROPS =
            "public class DummyClass {\n\n" +
                    "    private java.util.Map<java.lang.String, java.lang.Object> additionalProperties = new java.util.HashMap<java.lang.String, java.lang.Object>();\n\n" +
                    "    @com.fasterxml.jackson.annotation.JsonAnyGetter\n" +
                    "    public java.util.Map<java.lang.String, java.lang.Object> getAdditionalProperties() {\n" +
                    "        return this.additionalProperties;\n" +
                    "    }\n\n" +
                    "    @com.fasterxml.jackson.annotation.JsonAnySetter\n" +
                    "    public void setAdditionalProperties(java.lang.String name, java.lang.Object value) {\n" +
                    "        this.additionalProperties.put(name, value);\n" +
                    "    }\n\n" +
                    "}\n";

    private static final String EXPECTED_ADDITIONAL_STRING_PROPS =
            "public class DummyClass {\n\n" +
                    "    private java.util.Map<java.lang.String, java.lang.String> additionalProperties = new java.util.HashMap<java.lang.String, java.lang.String>();\n\n" +
                    "    @com.fasterxml.jackson.annotation.JsonAnyGetter\n" +
                    "    public java.util.Map<java.lang.String, java.lang.String> getAdditionalProperties() {\n" +
                    "        return this.additionalProperties;\n" +
                    "    }\n\n" +
                    "    @com.fasterxml.jackson.annotation.JsonAnySetter\n" +
                    "    public void setAdditionalProperties(java.lang.String name, java.lang.String value) {\n" +
                    "        this.additionalProperties.put(name, value);\n" +
                    "    }\n\n" +
                    "}\n";

    private static final String EXPECTED_ADDITIONAL_OBJECT_PROPS =
            "public class DummyClass {\n\n" +
                    "    private java.util.Map<java.lang.String, com.googlecode.jsonschema2pojo.rules.AdditionalPropertiesRuleTest.NodeProperty> additionalProperties" +
                    " = new java.util.HashMap<java.lang.String, com.googlecode.jsonschema2pojo.rules.AdditionalPropertiesRuleTest.NodeProperty>();\n\n" +
                    "    @com.fasterxml.jackson.annotation.JsonAnyGetter\n" +
                    "    public java.util.Map<java.lang.String, com.googlecode.jsonschema2pojo.rules.AdditionalPropertiesRuleTest.NodeProperty> getAdditionalProperties() {\n" +
                    "        return this.additionalProperties;\n" +
                    "    }\n\n" +
                    "    @com.fasterxml.jackson.annotation.JsonAnySetter\n" +
                    "    public void setAdditionalProperties(java.lang.String name, com.googlecode.jsonschema2pojo.rules.AdditionalPropertiesRuleTest.NodeProperty value) {\n" +
                    "        this.additionalProperties.put(name, value);\n" +
                    "    }\n\n" +
                    "}\n";

    @Before
    public void clearSchemaCache() {
        Schema.clearCache();
    }

    @Test
    public void applyWithNoAdditionalPropertiesAllowed() throws JClassAlreadyExistsException {
        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        BooleanNode node = new ObjectMapper().createObjectNode().booleanNode(false);

        JDefinedClass result = rule.apply("node", node, jclass, null);

        StringWriter output = new StringWriter();
        result.declare(new JFormatter(output));

        assertThat(output.toString(), equalTo(EXPECTED_RESULT_NO_ADDITIONAL_PROPS));
    }

    @Test
    public void applyWithDefaultAdditionalProperties() throws JClassAlreadyExistsException {
        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        JDefinedClass result = rule.apply("node", null, jclass, null);

        StringWriter output = new StringWriter();
        result.declare(new JFormatter(output));

        assertThat(output.toString(), equalTo(EXPECTED_RESULT_DEFAULT_ADDITIONAL_PROPS));
    }

    @Test
    public void applyWithAdditionalPropertiesStringSchema() throws JClassAlreadyExistsException {
        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectNode node = new ObjectMapper().createObjectNode();
        node.put("type", "string");

        JDefinedClass result = rule.apply("node", node, jclass, mock(Schema.class));

        StringWriter output = new StringWriter();
        result.declare(new JFormatter(output));

        assertThat(output.toString(), equalTo(EXPECTED_ADDITIONAL_STRING_PROPS));
    }

    @Test
    public void applyWithAdditionalPropertiesObjectSchema() throws JClassAlreadyExistsException {
        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectNode node = new ObjectMapper().createObjectNode();
        node.put("type", "object");

        JDefinedClass result = rule.apply("node", node, jclass, mock(Schema.class));

        StringWriter output = new StringWriter();
        result.declare(new JFormatter(output));

        assertThat(output.toString(), equalTo(EXPECTED_ADDITIONAL_OBJECT_PROPS));
    }

}
