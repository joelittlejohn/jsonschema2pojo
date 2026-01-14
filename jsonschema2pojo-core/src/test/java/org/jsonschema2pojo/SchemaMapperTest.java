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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.jsonschema2pojo.rules.RuleFactory;
import org.jsonschema2pojo.rules.SchemaRule;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;

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

        String jsonContent = IOUtils.toString(this.getClass().getResourceAsStream("/example-json/user.json"), StandardCharsets.UTF_8);

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

        String schemaContent = IOUtils.toString(this.getClass().getResourceAsStream("/schema/address.json"), StandardCharsets.UTF_8);

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

    @Test
    void generateCreatesSchemaFromExampleJsonWithNullField() throws IOException {
        // https://github.com/joelittlejohn/jsonschema2pojo/pull/1746
        // null field values in array must not erase previous inner type lookup
        // previous behavior: when a field value is null, previous inner type lookup are forgotten
        // intended behavior: null value are just ignored; any previous lookup are kept
        
        // read a json and generate a schema
        String jsonExample = IOUtils.toString(this.getClass().getResourceAsStream("/schema/array-null-field.json"), StandardCharsets.UTF_8);
        final SchemaRule mockSchemaRule = mock(SchemaRule.class);
        final RuleFactory mockRuleFactory = mock(RuleFactory.class);
        when(mockRuleFactory.getSchemaRule()).thenReturn(mockSchemaRule);
        DefaultGenerationConfig generationConfig = spy(new DefaultGenerationConfig());
        when(generationConfig.getSourceType()).thenReturn(SourceType.JSON);
        when(mockRuleFactory.getGenerationConfig()).thenReturn(generationConfig);
        new SchemaMapper(mockRuleFactory, new SchemaGenerator()).generate(new JCodeModel(), "ArrayItem", "com.example.package", jsonExample);
        
        // check generated schema
        // schema : array -> myfield: object -> subfield1: string, subfield2: string
        ArgumentCaptor<JsonNode> captureNode = ArgumentCaptor.forClass(JsonNode.class);
        verify(mockSchemaRule).apply(eq("ArrayItem"), captureNode.capture(), eq(null), any(), Mockito.isA(Schema.class));
        assertThat(captureNode.getValue().get("items"), is(notNullValue()));
        // array item has a myfield field
        assertThat(captureNode.getValue().get("items").get("properties").get("myfield"), is(notNullValue()));
        assertThat(captureNode.getValue().get("items").get("properties").get("myfield").get("type").asText(), is("object"));
        // myfield is an object type, containing subfield1 and subfield2 that are string types
        JsonNode myfieldProperties = captureNode.getValue().get("items").get("properties").get("myfield").get("properties");
        assertThat(myfieldProperties.get("subfield1").get("type").asText(), is("string"));
        assertThat(myfieldProperties.get("subfield2").get("type").asText(), is("string"));
    }
}
