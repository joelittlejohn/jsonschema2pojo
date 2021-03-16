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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.RuleLogger;
import org.jsonschema2pojo.SchemaStore;
import org.junit.Test;

public class RuleFactoryImplTest {

    @Test
    public void factoryMethodsCreateRules() {

        RuleFactory ruleFactory = new RuleFactory();

        assertThat(ruleFactory.getAdditionalPropertiesRule(), notNullValue());

        assertThat(ruleFactory.getArrayRule(), notNullValue());

        assertThat(ruleFactory.getDefaultRule(), notNullValue());

        assertThat(ruleFactory.getCommentRule(), notNullValue());

        assertThat(ruleFactory.getDescriptionRule(), notNullValue());

        assertThat(ruleFactory.getEnumRule(), notNullValue());

        assertThat(ruleFactory.getFormatRule(), notNullValue());

        assertThat(ruleFactory.getObjectRule(), notNullValue());

        assertThat(ruleFactory.getPropertiesRule(), notNullValue());

        assertThat(ruleFactory.getPropertyRule(), notNullValue());

        assertThat(ruleFactory.getSchemaRule(), notNullValue());

        assertThat(ruleFactory.getTitleRule(), notNullValue());

        assertThat(ruleFactory.getTypeRule(), notNullValue());

        assertThat(ruleFactory.getMinimumMaximumRule(), notNullValue());

        assertThat(ruleFactory.getMinItemsMaxItemsRule(), notNullValue());

        assertThat(ruleFactory.getPatternRule(), notNullValue());
        
        assertThat(ruleFactory.getMinLengthMaxLengthRule(), notNullValue());
        
        assertThat(ruleFactory.getValidRule(), notNullValue());

        assertThat(ruleFactory.getDigitsRule(), notNullValue());

    }

    @Test
    public void generationConfigIsReturned() {

        GenerationConfig mockGenerationConfig = mock(GenerationConfig.class);
        RuleLogger mockRuleLogger = mock(RuleLogger.class);

        RuleFactory ruleFactory = new RuleFactory(mockGenerationConfig, new NoopAnnotator(), new SchemaStore());
        ruleFactory.setLogger(mockRuleLogger);

        assertThat(ruleFactory.getGenerationConfig(), is(sameInstance(mockGenerationConfig)));
        assertThat(ruleFactory.getLogger(), is(sameInstance(mockRuleLogger)));
    }

    @Test
    public void generationRuleLoggerIsReturned() {

        GenerationConfig mockGenerationConfig = mock(GenerationConfig.class);
        RuleLogger mockRuleLogger = mock(RuleLogger.class);

        RuleFactory ruleFactory = new RuleFactory(new DefaultGenerationConfig(), new NoopAnnotator(), new SchemaStore());
        ruleFactory.setLogger(mockRuleLogger);

        assertThat(ruleFactory.getLogger(), is(sameInstance(mockRuleLogger)));
    }

    @Test
    public void schemaStoreIsReturned() {

        SchemaStore mockSchemaStore = mock(SchemaStore.class);

        RuleFactory ruleFactory = new RuleFactory(new DefaultGenerationConfig(), new NoopAnnotator(), mockSchemaStore);

        assertThat(ruleFactory.getSchemaStore(), is(sameInstance(mockSchemaStore)));
    }
}
