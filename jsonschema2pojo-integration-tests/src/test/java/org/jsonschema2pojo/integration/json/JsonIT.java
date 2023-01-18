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

package org.jsonschema2pojo.integration.json;

import java.io.IOException;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;

public class JsonIT {

    @Rule
    public final Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void multipleJsonFilesHavingPropertyWithSameNameAreMappedToCorrectJavaTypes() throws ReflectiveOperationException, IOException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/json/examplesWithSameName",
                "com.example",
                config("sourceType", "JSON"));

        final Class<?> aResponseType = resultsClassLoader.loadClass("com.example.AResponse");
        final Object aResponseValue = objectMapper.readValue(this.getClass().getResourceAsStream("/json/examplesWithSameName/AResponse.json"), aResponseType);
        assertThat(aResponseType.getMethod("getFieldA").invoke(aResponseValue), is("aValue"));
        final Class<?> aResponseCommonType = aResponseType.getMethod("getCommon").getReturnType();
        final Object aResponseCommonValue = aResponseType.getMethod("getCommon").invoke(aResponseValue);
        assertThat(aResponseCommonType.getMethod("getDiffA").invoke(aResponseCommonValue), is(1));

        final Class<?> bResponseType = resultsClassLoader.loadClass("com.example.BResponse");
        final Object bResponseValue = objectMapper.readValue(this.getClass().getResourceAsStream("/json/examplesWithSameName/BResponse.json"), bResponseType);
        assertThat(bResponseType.getMethod("getFieldB").invoke(bResponseValue), is("bValue"));
        final Class<?> bResponseCommonType = bResponseType.getMethod("getCommon").getReturnType();
        final Object bResponseCommonValue = bResponseType.getMethod("getCommon").invoke(bResponseValue);
        assertThat(bResponseCommonType.getMethod("getDiffB").invoke(bResponseCommonValue), is(true));
    }
}
