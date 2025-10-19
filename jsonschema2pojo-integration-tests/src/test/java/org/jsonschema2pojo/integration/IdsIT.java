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

package org.jsonschema2pojo.integration;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class IdsIT {

    @RegisterExtension
    public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void jsonSchemaWithIdIsParsed() throws Exception {
        Class<?> classWithId = classSchemaRule.generateAndCompile("/schema/ids/ids.json", "com.example").loadClass("com.example.Ids");
        Field field = classWithId.getDeclaredField("SCHEMA_ID");

        MatcherAssert.assertThat(field.getModifiers(), Matchers.equalTo(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL));
        MatcherAssert.assertThat(field.getName(), Matchers.equalTo("SCHEMA_ID"));
        MatcherAssert.assertThat(field.get(classWithId), Matchers.equalTo("id://foobar\""));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void jsonSchemaWithEmptyIdIsParsed() throws Exception {
        Class<?> classWithoutId = classSchemaRule.generateAndCompile("/schema/ids/idsEmpty.json", "com.example").loadClass("com.example.IdsEmpty");
        Assertions.assertThrows(NoSuchFieldException.class, () -> {
            // throws exception because the field is not present.
            classWithoutId.getDeclaredField("SCHEMA_ID");
        });
    }

}
