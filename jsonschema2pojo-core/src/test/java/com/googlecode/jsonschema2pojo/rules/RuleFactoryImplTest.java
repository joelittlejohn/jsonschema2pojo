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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.googlecode.jsonschema2pojo.GenerationConfig;

public class RuleFactoryImplTest {

    @Test
    public void factoryMethodsCreateRules() {

        RuleFactory ruleFactory = new RuleFactoryImpl();

        assertThat(ruleFactory.getAdditionalPropertiesRule(), notNullValue());

        assertThat(ruleFactory.getArrayRule(), notNullValue());

        assertThat(ruleFactory.getDefaultRule(), notNullValue());

        assertThat(ruleFactory.getDescriptionRule(), notNullValue());

        assertThat(ruleFactory.getEnumRule(), notNullValue());

        assertThat(ruleFactory.getFormatRule(), notNullValue());

        assertThat(ruleFactory.getObjectRule(), notNullValue());

        assertThat(ruleFactory.getPropertiesRule(), notNullValue());

        assertThat(ruleFactory.getPropertyRule(), notNullValue());

        assertThat(ruleFactory.getSchemaRule(), notNullValue());

        assertThat(ruleFactory.getTitleRule(), notNullValue());

        assertThat(ruleFactory.getTypeRule(), notNullValue());

        assertThat(ruleFactory.getPropertiesRule(), notNullValue());

    }

    @Test
    public void generationConfigIsReturned() {

        GenerationConfig mockGenerationConfig = mock(GenerationConfig.class);

        RuleFactoryImpl ruleFactory = new RuleFactoryImpl(mockGenerationConfig);

        assertThat(ruleFactory.getGenerationConfig(), is(sameInstance(mockGenerationConfig)));

    }
}
