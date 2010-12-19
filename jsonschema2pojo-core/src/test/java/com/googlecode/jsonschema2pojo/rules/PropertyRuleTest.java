/**
 * Copyright © 2010 Nokia
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

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

import com.googlecode.jsonschema2pojo.SchemaMapperImpl;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFormatter;

public class PropertyRuleTest {

    private static final String TARGET_CLASS_NAME = ArrayRuleTest.class.getName() + ".DummyClass";

    private static final String EXPECTED_NUMBER_RESULT =
            "public class DummyClass {\n\n" +
                    "    /**\n" +
                    "     * some bean property\n" +
                    "     * \n" +
                    "     */\n" +
                    "    private double fooBar;\n\n" +
                    "    /**\n" +
                    "     * some bean property\n" +
                    "     * (Optional)\n" +
                    "     * \n     */\n" +
                    "    public double getFooBar() {\n" +
                    "        return fooBar;\n" +
                    "    }\n\n" +
                    "    public void setFooBar(double fooBar) {\n" +
                    "        this.fooBar = fooBar;\n" +
                    "    }\n\n" +
                    "    public " + TARGET_CLASS_NAME + " withFooBar(double fooBar) {\n" +
                    "        this.fooBar = fooBar;\n" +
                    "        return this;\n" +
                    "    }\n\n" +
                    "}\n";

    private static final String EXPECTED_BOOLEAN_RESULT =
            "public class DummyClass {\n\n" +
                    "    /**\n" +
                    "     * some bean property\n" +
                    "     * \n" +
                    "     */\n" +
                    "    private boolean fooBar;\n\n" +
                    "    /**\n" +
                    "     * some bean property\n" +
                    "     * (Optional)\n" +
                    "     * \n     */\n" +
                    "    public boolean isFooBar() {\n" +
                    "        return fooBar;\n" +
                    "    }\n\n" +
                    "    public void setFooBar(boolean fooBar) {\n" +
                    "        this.fooBar = fooBar;\n" +
                    "    }\n\n" +
                    "    public " + TARGET_CLASS_NAME + " withFooBar(boolean fooBar) {\n" +
                    "        this.fooBar = fooBar;\n" +
                    "        return this;\n" +
                    "    }\n\n" +
                    "}\n";

    private final PropertyRule rule = new PropertyRule(new SchemaMapperImpl());

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

        assertThat(output.toString(), equalTo(EXPECTED_NUMBER_RESULT));

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

        assertThat(output.toString(), equalTo(EXPECTED_BOOLEAN_RESULT));

    }

}
