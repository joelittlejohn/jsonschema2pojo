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

import org.jsonschema2pojo.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class RuleFactoryImplTest {

    @Test
    public void factoryMethodsCreateRules() {

        RuleFactory ruleFactory = new RuleFactory();

        assertNotNull(ruleFactory.getAdditionalPropertiesRule());
        assertNotNull(ruleFactory.getArrayRule());
        assertNotNull(ruleFactory.getDefaultRule());
        assertNotNull(ruleFactory.getCommentRule());
        assertNotNull(ruleFactory.getDescriptionRule());
        assertNotNull(ruleFactory.getEnumRule());
        assertNotNull(ruleFactory.getFormatRule());
        assertNotNull(ruleFactory.getObjectRule());
        assertNotNull(ruleFactory.getPropertiesRule());
        assertNotNull(ruleFactory.getPropertyRule());
        assertNotNull(ruleFactory.getSchemaRule());
        assertNotNull(ruleFactory.getTitleRule());
        assertNotNull(ruleFactory.getTypeRule());
        assertNotNull(ruleFactory.getMinimumMaximumRule());
        assertNotNull(ruleFactory.getMinItemsMaxItemsRule());
        assertNotNull(ruleFactory.getMinLengthMaxLengthRule());
        assertNotNull(ruleFactory.getValidRule());
        assertNotNull(ruleFactory.getDigitsRule());
    }

    @Test
    public void generationConfigIsReturned() {

        GenerationConfig mockGenerationConfig = mock(GenerationConfig.class);
        RuleLogger mockRuleLogger = mock(RuleLogger.class);

        RuleFactory ruleFactory = new RuleFactory(mockGenerationConfig, new NoopAnnotator(), new SchemaStore());
        ruleFactory.setLogger(mockRuleLogger);

        assertSame(mockGenerationConfig, ruleFactory.getGenerationConfig());
        assertSame(mockRuleLogger, ruleFactory.getLogger());
    }

    @Test
    public void generationRuleLoggerIsReturned() {

        GenerationConfig mockGenerationConfig = mock(GenerationConfig.class);
        RuleLogger mockRuleLogger = mock(RuleLogger.class);

        RuleFactory ruleFactory = new RuleFactory(new DefaultGenerationConfig(), new NoopAnnotator(), new SchemaStore());
        ruleFactory.setLogger(mockRuleLogger);

        assertNotSame(mockGenerationConfig, ruleFactory.getGenerationConfig());
        assertSame(mockRuleLogger, ruleFactory.getLogger());
    }

    @Test
    public void schemaStoreIsReturned() {
        SchemaStore mockSchemaStore = mock(SchemaStore.class);
        RuleFactory ruleFactory = new RuleFactory(new DefaultGenerationConfig(), new NoopAnnotator(), mockSchemaStore);
        assertSame(mockSchemaStore, ruleFactory.getSchemaStore());
    }
}
