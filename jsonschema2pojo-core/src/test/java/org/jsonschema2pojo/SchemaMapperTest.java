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

package org.jsonschema2pojo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import org.apache.commons.io.IOUtils;
import org.jsonschema2pojo.rules.RuleFactory;
import org.jsonschema2pojo.rules.SchemaRule;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;

public class SchemaMapperTest {

    @Test
    public void generateReadsSchemaAsObject() {

        final SchemaRule mockSchemaRule = mock(SchemaRule.class);

        final RuleFactory mockRuleFactory = mock(RuleFactory.class);
        when(mockRuleFactory.getSchemaRule()).thenReturn(mockSchemaRule);
        when(mockRuleFactory.getGenerationConfig()).thenReturn(new DefaultGenerationConfig());

        URL schemaContent = this.getClass().getResource("/schema/address.json");

        new SchemaMapper(mockRuleFactory, new SchemaGenerator()).generate(new JCodeModel(), "Address", "com.example.package", schemaContent);

        ArgumentCaptor<JPackage> capturePackage = ArgumentCaptor.forClass(JPackage.class);
        ArgumentCaptor<JsonNode> captureNode = ArgumentCaptor.forClass(JsonNode.class);

        verify(mockSchemaRule).apply(eq("Address"), captureNode.capture(), eq(null), capturePackage.capture(), Mockito.isA(Schema.class));

        assertThat(capturePackage.getValue().name(), is("com.example.package"));
        assertThat(captureNode.getValue(), is(notNullValue()));

    }

    @Test
    public void generateCreatesSchemaFromExampleJsonWhenInJsonMode() {

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

        verify(mockSchemaRule).apply(eq("Address"), eq(schemaNode), eq(null), capturePackage.capture(), Mockito.isA(Schema.class));

        assertThat(capturePackage.getValue().name(), is("com.example.package"));

    }

    @Test
    public void generateCreatesSchemaFromExampleJSONAsStringInput() throws IOException {

        String jsonContent = IOUtils.toString(
                Objects.requireNonNull(this.getClass().getResourceAsStream("/example-json/user.json")),
                StandardCharsets.UTF_8
        );

        ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();

        final SchemaRule mockSchemaRule = mock(SchemaRule.class);

        final GenerationConfig mockGenerationConfig = mock(GenerationConfig.class);
        when(mockGenerationConfig.getSourceType()).thenReturn(SourceType.JSON);

        final SchemaGenerator mockSchemaGenerator = mock(SchemaGenerator.class);
        when(mockSchemaGenerator.schemaFromExample(new ObjectMapper().readTree(jsonContent))).thenReturn(schemaNode);

        final RuleFactory mockRuleFactory = mock(RuleFactory.class);
        when(mockRuleFactory.getSchemaRule()).thenReturn(mockSchemaRule);
        when(mockRuleFactory.getGenerationConfig()).thenReturn(mockGenerationConfig);

        new SchemaMapper(mockRuleFactory, mockSchemaGenerator).generate(new JCodeModel(), "User", "com.example.package", jsonContent);

        ArgumentCaptor<JPackage> capturePackage = ArgumentCaptor.forClass(JPackage.class);

        verify(mockSchemaRule).apply(eq("User"), eq(schemaNode), eq(null), capturePackage.capture(), Mockito.isA(Schema.class));

        assertThat(capturePackage.getValue().name(), is("com.example.package"));
    }

    @Test
    public void generateCreatesSchemaFromSchemaAsStringInput() throws IOException {

        String schemaContent = IOUtils.toString(
                Objects.requireNonNull(this.getClass().getResourceAsStream("/schema/address.json")),
                StandardCharsets.UTF_8
        );

        final SchemaRule mockSchemaRule = mock(SchemaRule.class);

        final RuleFactory mockRuleFactory = mock(RuleFactory.class);
        when(mockRuleFactory.getSchemaRule()).thenReturn(mockSchemaRule);
        when(mockRuleFactory.getGenerationConfig()).thenReturn(new DefaultGenerationConfig());

        new SchemaMapper(mockRuleFactory, new SchemaGenerator()).generate(new JCodeModel(), "Address", "com.example.package", schemaContent);

        ArgumentCaptor<JPackage> capturePackage = ArgumentCaptor.forClass(JPackage.class);
        ArgumentCaptor<JsonNode> captureNode = ArgumentCaptor.forClass(JsonNode.class);

        verify(mockSchemaRule).apply(eq("Address"), captureNode.capture(), eq(null), capturePackage.capture(), Mockito.isA(Schema.class));

        assertThat(capturePackage.getValue().name(), is("com.example.package"));
        assertThat(captureNode.getValue(), is(notNullValue()));

    }
}
