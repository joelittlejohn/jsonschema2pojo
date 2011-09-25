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

import static org.easymock.EasyMock.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.StringWriter;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.jsonschema2pojo.Schema;
import com.googlecode.jsonschema2pojo.exception.GenerationException;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFormatter;

public class EnumRuleTest {

    private static final String TARGET_CLASS_NAME = ArrayRuleTest.class.getName() + ".DummyClass";

    private static final String EXPECTED_TEXT_RESULT =
            "public class DummyClass {\n\n\n" +
                    "    @javax.annotation.Generated(\"com.googlecode.jsonschema2pojo\")\n" +
                    "    public static enum NewEnum {\n\n" +
                    "        VALUE_ONE(\"valueOne\"),\n" +
                    "        VALUE_TWO(\"valueTwo\"),\n" +
                    "        VALUE_THREE(\"valueThree\");\n" +
                    "        private final java.lang.String value;\n\n" +
                    "        private NewEnum(java.lang.String value) {\n" +
                    "            this.value = value;\n" +
                    "        }\n\n" +
                    "        @org.codehaus.jackson.annotate.JsonValue\n" +
                    "        @java.lang.Override\n" +
                    "        public java.lang.String toString() {\n" +
                    "            return this.value;\n" +
                    "        }\n\n" +
                    "        @org.codehaus.jackson.annotate.JsonCreator\n" +
                    "        public static com.googlecode.jsonschema2pojo.rules.ArrayRuleTest.DummyClass.NewEnum fromValue(java.lang.String value) {\n" +
                    "            for (com.googlecode.jsonschema2pojo.rules.ArrayRuleTest.DummyClass.NewEnum c: com.googlecode.jsonschema2pojo.rules.ArrayRuleTest.DummyClass.NewEnum.values()) {\n" +
                    "                if (c.value.equals(value)) {\n" +
                    "                    return c;\n" +
                    "                }\n" +
                    "            }\n" +
                    "            throw new java.lang.IllegalArgumentException(value);\n" +
                    "        }\n\n" +
                    "    }\n\n" +
                    "}\n";

    private static final String EXPECTED_TEXT_WITH_SPACES_RESULT =
            "public class DummyClass {\n\n\n" +
                    "    @javax.annotation.Generated(\"com.googlecode.jsonschema2pojo\")\n" +
                    "    public static enum NewEnum {\n\n" +
                    "        VALUE_ONE(\"value one\"),\n" +
                    "        VALUE_TWO(\"value two\"),\n" +
                    "        VALUE_THREE(\"value three\");\n" +
                    "        private final java.lang.String value;\n\n" +
                    "        private NewEnum(java.lang.String value) {\n" +
                    "            this.value = value;\n" +
                    "        }\n\n" +
                    "        @org.codehaus.jackson.annotate.JsonValue\n" +
                    "        @java.lang.Override\n" +
                    "        public java.lang.String toString() {\n" +
                    "            return this.value;\n" +
                    "        }\n\n" +
                    "        @org.codehaus.jackson.annotate.JsonCreator\n" +
                    "        public static com.googlecode.jsonschema2pojo.rules.ArrayRuleTest.DummyClass.NewEnum fromValue(java.lang.String value) {\n" +
                    "            for (com.googlecode.jsonschema2pojo.rules.ArrayRuleTest.DummyClass.NewEnum c: com.googlecode.jsonschema2pojo.rules.ArrayRuleTest.DummyClass.NewEnum.values()) {\n" +
                    "                if (c.value.equals(value)) {\n" +
                    "                    return c;\n" +
                    "                }\n" +
                    "            }\n" +
                    "            throw new java.lang.IllegalArgumentException(value);\n" +
                    "        }\n\n" +
                    "    }\n\n" +
                    "}\n";

    private static final String EXPECTED_NUMBER_RESULT =
            "public class DummyClass {\n\n\n" +
                    "    @javax.annotation.Generated(\"com.googlecode.jsonschema2pojo\")\n" +
                    "    public static enum NewEnum {\n\n" +
                    "        _100(\"100\"),\n" +
                    "        _200(\"200\"),\n" +
                    "        _300(\"300\");\n" +
                    "        private final java.lang.String value;\n\n" +
                    "        private NewEnum(java.lang.String value) {\n" +
                    "            this.value = value;\n" +
                    "        }\n\n" +
                    "        @org.codehaus.jackson.annotate.JsonValue\n" +
                    "        @java.lang.Override\n" +
                    "        public java.lang.String toString() {\n" +
                    "            return this.value;\n" +
                    "        }\n\n" +
                    "        @org.codehaus.jackson.annotate.JsonCreator\n" +
                    "        public static com.googlecode.jsonschema2pojo.rules.ArrayRuleTest.DummyClass.NewEnum fromValue(java.lang.String value) {\n" +
                    "            for (com.googlecode.jsonschema2pojo.rules.ArrayRuleTest.DummyClass.NewEnum c: com.googlecode.jsonschema2pojo.rules.ArrayRuleTest.DummyClass.NewEnum.values()) {\n" +
                    "                if (c.value.equals(value)) {\n" +
                    "                    return c;\n" +
                    "                }\n" +
                    "            }\n" +
                    "            throw new java.lang.IllegalArgumentException(value);\n" +
                    "        }\n\n" +
                    "    }\n\n" +
                    "}\n";

    private EnumRule rule = new EnumRule();

    @Before
    public void clearSchemaCache() {
        Schema.clearCache();
    }
    
    @Test(expected = GenerationException.class)
    public void applyFailsWhenEnumAlreadyExists() throws JClassAlreadyExistsException {
        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        jclass._enum("ExistingEnum");

        rule.apply("existingEnum", new ObjectMapper().createObjectNode(), jclass, null);
    }

    @Test
    public void applyCreatesTextEnum() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode enumNode = mapper.createArrayNode();
        enumNode.add("valueOne");
        enumNode.add("valueTwo");
        enumNode.add("valueThree");

        rule.apply("newEnum", enumNode, jclass, createNiceMock(Schema.class));

        StringWriter output = new StringWriter();
        jclass.declare(new JFormatter(output));

        assertThat(output.toString(), equalTo(EXPECTED_TEXT_RESULT));

    }

    @Test
    public void applyCreatesTextEnumWithSpaces() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode enumNode = mapper.createArrayNode();
        enumNode.add("value one");
        enumNode.add("value two");
        enumNode.add("value three");

        rule.apply("newEnum", enumNode, jclass, createNiceMock(Schema.class));

        StringWriter output = new StringWriter();
        jclass.declare(new JFormatter(output));

        assertThat(output.toString(), equalTo(EXPECTED_TEXT_WITH_SPACES_RESULT));

    }

    @Test
    public void applyCreatesNumberEnum() throws JClassAlreadyExistsException {
        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode enumNode = mapper.createArrayNode();
        enumNode.add("100");
        enumNode.add("200");
        enumNode.add("300");

        rule.apply("newEnum", enumNode, jclass, createNiceMock(Schema.class));

        StringWriter output = new StringWriter();
        jclass.declare(new JFormatter(output));

        assertThat(output.toString(), equalTo(EXPECTED_NUMBER_RESULT));
    }

}
