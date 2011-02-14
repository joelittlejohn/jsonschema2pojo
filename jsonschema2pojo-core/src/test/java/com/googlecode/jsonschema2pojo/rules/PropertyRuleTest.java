/**
 * Copyright © 2011 Nokia
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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

import com.googlecode.jsonschema2pojo.SchemaMapper;
import com.googlecode.jsonschema2pojo.SchemaMapperImpl;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFormatter;

public class PropertyRuleTest {

    private static final String TARGET_CLASS_NAME = PropertyRuleTest.class.getName() + ".DummyClass";

    private static final String EXPECTED_NUMBER_RESULT_WITH_BUILDER =
            "public class DummyClass {\n\n" +
                    "    /**\n" +
                    "     * some bean property\n" +
                    "     * (Optional)\n" +
                    "     * \n" +
                    "     */\n" +
                    "    private double fooBar;\n\n" +
                    "    /**\n" +
                    "     * some bean property\n" +
                    "     * (Optional)\n" +
                    "     * \n" +
                    "     */\n" +
                    "    public double getFooBar() {\n" +
                    "        return fooBar;\n" +
                    "    }\n\n" +
                    "    /**\n" +
                    "     * some bean property\n" +
                    "     * (Optional)\n" +
                    "     * \n" +
                    "     */\n" +
                    "    public void setFooBar(double fooBar) {\n" +
                    "        this.fooBar = fooBar;\n" +
                    "    }\n\n" +
                    "    public " + TARGET_CLASS_NAME + " withFooBar(double fooBar) {\n" +
                    "        this.fooBar = fooBar;\n" +
                    "        return this;\n" +
                    "    }\n\n" +
                    "}\n";

    private static final String EXPECTED_NUMBER_RESULT_WITHOUT_BUILDER =
            "public class DummyClass {\n\n" +
                    "    /**\n" +
                    "     * some bean property\n" +
                    "     * (Optional)\n" +
                    "     * \n" +
                    "     */\n" +
                    "    private double fooBar;\n\n" +
                    "    /**\n" +
                    "     * some bean property\n" +
                    "     * (Optional)\n" +
                    "     * \n" +
                    "     */\n" +
                    "    public double getFooBar() {\n" +
                    "        return fooBar;\n" +
                    "    }\n\n" +
                    "    /**\n" +
                    "     * some bean property\n" +
                    "     * (Optional)\n" +
                    "     * \n" +
                    "     */\n" +
                    "    public void setFooBar(double fooBar) {\n" +
                    "        this.fooBar = fooBar;\n" +
                    "    }\n\n" +
                    "}\n";

    private static final String EXPECTED_BOOLEAN_RESULT_WITHOUT_BUILDER =
            "public class DummyClass {\n\n" +
                    "    /**\n" +
                    "     * some bean property\n" +
                    "     * (Optional)\n" +
                    "     * \n" +
                    "     */\n" +
                    "    private boolean fooBar;\n\n" +
                    "    /**\n" +
                    "     * some bean property\n" +
                    "     * (Optional)\n" +
                    "     * \n" +
                    "     */\n" +
                    "    public boolean isFooBar() {\n" +
                    "        return fooBar;\n" +
                    "    }\n\n" +
                    "    /**\n" +
                    "     * some bean property\n" +
                    "     * (Optional)\n" +
                    "     * \n     */\n" +
                    "    public void setFooBar(boolean fooBar) {\n" +
                    "        this.fooBar = fooBar;\n" +
                    "    }\n\n" +
                    "}\n";

    private static final String EXPECTED_ENUM_RESULT =
            "public class DummyClass {\n\n" +
                    "    /**\n" +
                    "     * some bean property\n" +
                    "     * (Optional)\n" +
                    "     * \n" +
                    "     */\n" +
                    "    private com.googlecode.jsonschema2pojo.rules.PropertyRuleTest.DummyClass.FooBar fooBar;\n\n" +
                    "    /**\n" +
                    "     * some bean property\n" +
                    "     * (Optional)\n" +
                    "     * \n" +
                    "     */\n" +
                    "    public com.googlecode.jsonschema2pojo.rules.PropertyRuleTest.DummyClass.FooBar getFooBar() {\n" +
                    "        return fooBar;\n" +
                    "    }\n\n" +
                    "    /**\n" +
                    "     * some bean property\n" +
                    "     * (Optional)\n" +
                    "     * \n" +
                    "     */\n" +
                    "    public void setFooBar(com.googlecode.jsonschema2pojo.rules.PropertyRuleTest.DummyClass.FooBar fooBar) {\n" +
                    "        this.fooBar = fooBar;\n" +
                    "    }\n\n" +
                    "    @javax.annotation.Generated(\"com.googlecode.jsonschema2pojo\")\n" +
                    "    public static enum FooBar {\n\n" +
                    "        AB_C(\"ab c\");\n" +
                    "        private final java.lang.String value;\n\n" +
                    "        private FooBar(java.lang.String value) {\n" +
                    "            this.value = value;\n" +
                    "        }\n\n" +
                    "        @org.codehaus.jackson.annotate.JsonValue\n" +
                    "        @java.lang.Override\n" +
                    "        public java.lang.String toString() {\n" +
                    "            return this.value;\n" +
                    "        }\n\n" +
                    "        @org.codehaus.jackson.annotate.JsonCreator\n" +
                    "        public static com.googlecode.jsonschema2pojo.rules.PropertyRuleTest.DummyClass.FooBar fromValue(java.lang.String value) {\n" +
                    "            for (com.googlecode.jsonschema2pojo.rules.PropertyRuleTest.DummyClass.FooBar c: com.googlecode.jsonschema2pojo.rules.PropertyRuleTest.DummyClass.FooBar.values()) {\n" +
                    "                if (c.value.equals(value)) {\n" +
                    "                    return c;\n" +
                    "                }\n" +
                    "            }\n" +
                    "            throw new java.lang.IllegalArgumentException(value);\n" +
                    "        }\n\n" +
                    "    }\n\n" +
                    "}\n";

    private final PropertyRule rule = new PropertyRule(new SchemaMapperImpl(null));

    @Test
    public void applyAddsBeanProperty() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectNode propertyNode = new ObjectMapper().createObjectNode();
        propertyNode.put("type", "number");
        propertyNode.put("description", "some bean property");
        propertyNode.put("optional", true);

        JDefinedClass result = rule.apply("fooBar", propertyNode, jclass);

        StringWriter output = new StringWriter();
        result.declare(new JFormatter(output));

        assertThat(output.toString(), equalTo(EXPECTED_NUMBER_RESULT_WITHOUT_BUILDER));

    }

    @Test
    public void applyAddsBeanPropertyIncludingBuilder() throws JClassAlreadyExistsException {

        // TODO: refactor common code between these tests

        Map<String, String> properties = new HashMap<String, String>();
        properties.put(SchemaMapper.GENERATE_BUILDERS_PROPERTY, "true");
        PropertyRule ruleWithBuilder = new PropertyRule(new SchemaMapperImpl(properties));

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectNode propertyNode = new ObjectMapper().createObjectNode();
        propertyNode.put("type", "number");
        propertyNode.put("description", "some bean property");
        propertyNode.put("optional", true);

        JDefinedClass result = ruleWithBuilder.apply("fooBar", propertyNode, jclass);

        StringWriter output = new StringWriter();
        result.declare(new JFormatter(output));

        assertThat(output.toString(), equalTo(EXPECTED_NUMBER_RESULT_WITH_BUILDER));

    }

    @Test
    public void applyAddsBooleanBeanProperty() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectNode propertyNode = new ObjectMapper().createObjectNode();
        propertyNode.put("type", "boolean");
        propertyNode.put("description", "some bean property");
        propertyNode.put("optional", true);

        JDefinedClass result = rule.apply("fooBar", propertyNode, jclass);

        StringWriter output = new StringWriter();
        result.declare(new JFormatter(output));

        assertThat(output.toString(), equalTo(EXPECTED_BOOLEAN_RESULT_WITHOUT_BUILDER));

    }

    @Test
    public void applyAddsEnumProperty() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode enumNode = objectMapper.createArrayNode();
        enumNode.add("ab c");

        ObjectNode propertyNode = new ObjectMapper().createObjectNode();
        propertyNode.put("type", "string");
        propertyNode.put("enum", enumNode);
        propertyNode.put("description", "some bean property");
        propertyNode.put("optional", true);

        JDefinedClass result = rule.apply("fooBar", propertyNode, jclass);

        StringWriter output = new StringWriter();
        result.declare(new JFormatter(output));

        assertThat(output.toString(), equalTo(EXPECTED_ENUM_RESULT));

    }

}
