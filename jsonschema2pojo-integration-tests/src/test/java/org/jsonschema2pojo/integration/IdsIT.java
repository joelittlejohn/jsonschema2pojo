/**
 * Copyright © 2021 Andrew Lindesay
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

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class IdsIT {

    @Rule
    public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void jsonSchemaWithIdIsParsed() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/ids/ids.json", "com.example");

        Class<?> resultClass = resultsClassLoader.loadClass("com.example.Ids");

        Field field = resultClass.getDeclaredField("SCHEMA_ID");

        MatcherAssert.assertThat(field.getModifiers(), Matchers.equalTo(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL));
        MatcherAssert.assertThat(field.getName(), Matchers.equalTo("SCHEMA_ID"));
        MatcherAssert.assertThat(field.get(resultClass), Matchers.equalTo("id://foobar\""));
    }

    @Test(expected = NoSuchFieldException.class)
    public void jsonSchemaWithEmptyIdIsParsed() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/ids/idsEmpty.json", "com.example");

        Class<?> resultClass = resultsClassLoader.loadClass("com.example.IdsEmpty");

        // throws exception because the field is not present.
        resultClass.getDeclaredField("SCHEMA_ID");
    }

}
