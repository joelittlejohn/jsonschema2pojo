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

import static org.easymock.EasyMock.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

import com.googlecode.jsonschema2pojo.Schema;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

public class PropertiesRuleTest {

    private static final String TARGET_CLASS_NAME = ArrayRuleTest.class.getName() + ".DummyClass";

    private RuleFactory mockRuleFactory = createMock(RuleFactory.class);
    private Schema mockSchema = createMock(Schema.class);
    private PropertiesRule rule = new PropertiesRule(mockRuleFactory);

    @Test
    public void applyIncludesMultipleProperties() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode property1 = objectMapper.createObjectNode();
        ObjectNode property2 = objectMapper.createObjectNode();
        ObjectNode property3 = objectMapper.createObjectNode();

        ObjectNode propertiesNode = objectMapper.createObjectNode();
        propertiesNode.put("property1", property1);
        propertiesNode.put("property2", property2);
        propertiesNode.put("property3", property3);

        PropertyRule mockPropertyRule = createMock(PropertyRule.class);
        expect(mockPropertyRule.apply("property1", property1, jclass, mockSchema)).andReturn(jclass);
        expect(mockPropertyRule.apply("property2", property2, jclass, mockSchema)).andReturn(jclass);
        expect(mockPropertyRule.apply("property3", property3, jclass, mockSchema)).andReturn(jclass);
        expect(mockRuleFactory.getPropertyRule()).andReturn(mockPropertyRule).anyTimes();

        replay(mockRuleFactory, mockPropertyRule);

        JDefinedClass result = rule.apply("fooBar", propertiesNode, jclass, mockSchema);

        assertThat(result, sameInstance(jclass));
        verify(mockPropertyRule);
    }

}
