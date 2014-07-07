/**
 * Copyright © 2010-2014 Nokia
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

package org.jsonschema2pojo;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jsonschema2pojo.rules.RuleFactory;
import org.jsonschema2pojo.rules.SchemaRule;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;

public class SchemaMapperTest {

    @Test
    public void generateReadsSchemaAsObject() throws IOException {

        final SchemaRule mockSchemaRule = mock(SchemaRule.class);

        final RuleFactory mockRuleFactory = mock(RuleFactory.class);
        when(mockRuleFactory.getSchemaRule()).thenReturn(mockSchemaRule);
        when(mockRuleFactory.getGenerationConfig()).thenReturn(new DefaultGenerationConfig());

        URL schemaContent = this.getClass().getResource("/schema/address.json");

        new SchemaMapper(mockRuleFactory, new SchemaGenerator()).generate(new JCodeModel(), "Address", "com.example.package", schemaContent);

        ArgumentCaptor<JPackage> capturePackage = ArgumentCaptor.forClass(JPackage.class);
        ArgumentCaptor<JsonNode> captureNode = ArgumentCaptor.forClass(JsonNode.class);

        verify(mockSchemaRule).apply(eq("Address"), captureNode.capture(), capturePackage.capture(), Mockito.isA(Schema.class));

        assertThat(capturePackage.getValue().name(), is("com.example.package"));
        assertThat(captureNode.getValue(), is(notNullValue()));

    }

    @Test
    public void generateCreatesSchemaFromExampleJsonWhenInJsonMode() throws IOException {

        URL schemaContent = this.getClass().getResource("/schema/address.json");

        ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();

        final SchemaRule mockSchemaRule = mock(SchemaRule.class);

        final GenerationConfig mockGenerationConfig = mock(GenerationConfig.class);
        when(mockGenerationConfig.getSourceType()).thenReturn(SourceType.JSON);

        final SchemaGenerator mockSchemaGenerator = mock(SchemaGenerator.class);
        when(mockSchemaGenerator.schemaFromExample(schemaContent)).thenReturn(schemaNode);

        final RuleFactory mockRuleFactory = mock(RuleFactory.class);
        when(mockRuleFactory.getSchemaRule()).thenReturn(mockSchemaRule);
        when(mockRuleFactory.getGenerationConfig()).thenReturn(mockGenerationConfig);

        new SchemaMapper(mockRuleFactory, mockSchemaGenerator).generate(new JCodeModel(), "Address", "com.example.package", schemaContent);

        ArgumentCaptor<JPackage> capturePackage = ArgumentCaptor.forClass(JPackage.class);

        verify(mockSchemaRule).apply(eq("Address"), eq(schemaNode), capturePackage.capture(), Mockito.isA(Schema.class));

        assertThat(capturePackage.getValue().name(), is("com.example.package"));

    }

}
