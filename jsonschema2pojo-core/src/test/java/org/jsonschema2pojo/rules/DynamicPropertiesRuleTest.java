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

import static com.sun.codemodel.JMod.PUBLIC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Before;
import org.junit.Test;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JMethod;

public class DynamicPropertiesRuleTest {

    JCodeModel codeModel = new JCodeModel();
    RuleFactory factory;
    DynamicPropertiesRule rule;

    JDefinedClass type;
    JMethod numberGetter;
    JMethod numberSetter;

    JDefinedClass type2;

    @Before
    public void setUp() throws JClassAlreadyExistsException {
        type = codeModel._class("org.jsonschema2pojo.rules.ExampleClass");
        numberGetter = type.method(PUBLIC, codeModel._ref(Integer.class), "getNumber");
        numberSetter = type.method(PUBLIC, codeModel._ref(Integer.class), "setNumber");
        numberSetter.param(codeModel._ref(Integer.class), "value");

        type2 = codeModel._class("org.jsonschema2pojo.rules.ExampleParentClass");

        factory = new RuleFactory();
        rule = new DynamicPropertiesRule(factory);

    }

    @Test
    public void shouldAddNotFoundField() {
        JFieldRef var = rule.getOrAddNotFoundVar(type);
        assertThat(var, notNullValue());
    }

}
