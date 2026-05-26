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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WireMockTest
public class NetworkSchemaIT {

    @RegisterExtension
    public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void networkSchema(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {

        URL schemaUrl = this.getClass().getResource("/schema/network/schema.json");
        String schemaBody = IOUtils.toString(schemaUrl, StandardCharsets.UTF_8);

        stubFor(get("/schema.json").willReturn(ok(schemaBody)));

        URL baseHttpUrl = new URL(wmRuntimeInfo.getHttpBaseUrl());
        URL schemaHttpUrl = new URL(baseHttpUrl, "/schema.json");
        
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(schemaHttpUrl, "com.example");

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.Schema");

        Method method = generatedType.getMethod("getField");

        assertThat(method, is(notNullValue()));
    }

}