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

package com.googlecode.jsonschema2pojo;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.googlecode.jsonschema2pojo.rules.JsonSchemaRule;
import com.googlecode.jsonschema2pojo.rules.RuleFactory;
import com.googlecode.jsonschema2pojo.rules.RuleFactoryImpl;
import com.googlecode.jsonschema2pojo.rules.SchemaRule;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

public class SchemaMapperImplTest {

    @Test
    public void generateReadsSchemaAsObject() throws IOException {

        final JsonSchemaRule mockSchemaRule = mock(JsonSchemaRule.class);

        final RuleFactory ruleFactory = new RuleFactoryImpl() {
            @Override
            public SchemaRule<JClassContainer, JType> getSchemaRule() {
                return mockSchemaRule;
            }
        };

        URL schemaContent = this.getClass().getResource("/schema/address.json");

        new SchemaMapperImpl(ruleFactory).generate(new JCodeModel(), "Address", "com.example.package", schemaContent);

        ArgumentCaptor<JPackage> capturePackage = ArgumentCaptor.forClass(JPackage.class);
        ArgumentCaptor<JsonNode> captureNode = ArgumentCaptor.forClass(JsonNode.class);

        verify(mockSchemaRule).apply(eq("Address"), captureNode.capture(), capturePackage.capture(), isNull(Schema.class));

        assertThat(capturePackage.getValue().name(), is("com.example.package"));
        assertThat(captureNode.getValue(), is(notNullValue()));

    }

}
