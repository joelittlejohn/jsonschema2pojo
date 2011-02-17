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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class RuleFactoryImplTest {

    @Test
    public void factoryMethodsCreateRules() {

        RuleFactory ruleFactory = new RuleFactoryImpl(null);

        assertThat(ruleFactory.getArrayRule(), notNullValue());

        assertThat(ruleFactory.getDescriptionRule(), notNullValue());

        assertThat(ruleFactory.getEnumRule(), notNullValue());

        assertThat(ruleFactory.getFormatRule(), notNullValue());

        assertThat(ruleFactory.getObjectRule(), notNullValue());

        assertThat(ruleFactory.getOptionalRule(), notNullValue());

        assertThat(ruleFactory.getPropertiesRule(), notNullValue());

        assertThat(ruleFactory.getPropertyRule(), notNullValue());

        assertThat(ruleFactory.getTitleRule(), notNullValue());

        assertThat(ruleFactory.getTypeRule(), notNullValue());

        assertThat(ruleFactory.getPropertiesRule(), notNullValue());

    }

    @Test
    public void nullPropertiesAvoidsNullPointer() {
        RuleFactory ruleFactory = new RuleFactoryImpl(null);
        assertThat(ruleFactory.getBehaviourProperty("anything"), is(nullValue()));
    }

    @Test
    public void putAndGetProperties() {

        String key = "KEY";
        String value = "VALUE";

        Map<String, String> properties = new HashMap<String, String>();
        properties.put(key, value);

        RuleFactory ruleFactory = new RuleFactoryImpl(properties);

        assertThat(ruleFactory.getBehaviourProperty(key), is(value));

    }

}
