/*
 * Copyright © 2010-2017 Nokia
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

package org.jsonschema2pojo.integration;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Small test for checking that the "x-" and regular variants of non-spec keyword extensions cannot be used at the same time.
 */
public class KeywordExtensionsIT {

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();
    @Rule public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testKeywordExtensionCollision() {
        expectedEx.expect(IllegalStateException.class);
        expectedEx.expectMessage("Define only one of 'extends' or 'x-extends' (preferred)");

        schemaRule.generate("/schema/keywordExtensions/keywordExtensionsCollisions.json", "com.example");
    }

}
