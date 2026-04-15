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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.SchemaStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

public class AllOfRuleTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private GenerationConfig config;
    private RuleFactory ruleFactory;
    private AllOfRule rule;

    @BeforeEach
    public void setUp() {
        config = mock(GenerationConfig.class);
        when(config.getRefFragmentPathDelimiters()).thenReturn("#/.");
        when(config.isIncludeGetters()).thenReturn(true);
        when(config.isIncludeSetters()).thenReturn(true);
        when(config.isGenerateBuilders()).thenReturn(false);
        when(config.isIncludeConstructors()).thenReturn(false);
        when(config.isIncludeAdditionalProperties()).thenReturn(false);
        when(config.isIncludeGeneratedAnnotation()).thenReturn(false);

        ruleFactory = new RuleFactory(config, new NoopAnnotator(), new SchemaStore());
        rule = new AllOfRule(ruleFactory);
    }

    @Test
    public void applyReturnsClassUnchangedWhenAllOfIsNull() throws JClassAlreadyExistsException {
        JDefinedClass jclass = new JCodeModel()._class("org.jsonschema2pojo.rules.TestBase");
        Schema schema = mock(Schema.class);

        JDefinedClass result = rule.apply("test", null, null, jclass, schema);

        assertThat(result, sameInstance(jclass));
        assertThat(jclass.fields().isEmpty(), is(true));
    }

    @Test
    public void applyReturnsClassUnchangedWhenAllOfIsEmpty() throws JClassAlreadyExistsException {
        JDefinedClass jclass = new JCodeModel()._class("org.jsonschema2pojo.rules.TestBase2");
        Schema schema = mock(Schema.class);
        ArrayNode emptyAllOf = MAPPER.createArrayNode();

        JDefinedClass result = rule.apply("test", emptyAllOf, null, jclass, schema);

        assertThat(result, sameInstance(jclass));
        assertThat(jclass.fields().isEmpty(), is(true));
    }

    @Test
    public void applyMergesPropertiesFromInlineSubSchemaViaSchemaStore() throws Exception {
        // Build a real schema URI so PropertyRule can resolve sub-paths correctly
        java.net.URI schemaUri = getClass().getClassLoader()
                .getResource("schema/polymorphism/allof-merge.json").toURI();

        SchemaStore schemaStore = new SchemaStore();
        Schema schema = schemaStore.create(schemaUri, "#/.");

        JCodeModel codeModel = new JCodeModel();
        JDefinedClass jclass = codeModel._class("org.jsonschema2pojo.rules.TestMerge");

        // Use the allOf node from the real schema on disk
        JsonNode allOfNode = schema.getContent().get("allOf");
        assertThat("allOf should exist in allof-merge.json", allOfNode, notNullValue());

        RuleFactory realFactory = new RuleFactory(config, new NoopAnnotator(), schemaStore);
        AllOfRule realRule = new AllOfRule(realFactory);

        JDefinedClass result = realRule.apply("EnrichedPerson", allOfNode, null, jclass, schema);

        assertThat(result, sameInstance(jclass));
        assertThat("'email' should be merged from allOf", jclass.fields(), hasKey("email"));
        assertThat("'age' should be merged from allOf", jclass.fields(), hasKey("age"));
    }

    @Test
    public void applySkipsIfThenEntries() throws JClassAlreadyExistsException {
        JDefinedClass jclass = new JCodeModel()._class("org.jsonschema2pojo.rules.TestSkipIfThen");
        Schema schema = mock(Schema.class);

        // allOf: [ { if: {...}, then: {...} } ]
        ObjectNode ifThenEntry = MAPPER.createObjectNode();
        ifThenEntry.set("if", MAPPER.createObjectNode());
        ifThenEntry.set("then", MAPPER.createObjectNode());

        ArrayNode allOfNode = MAPPER.createArrayNode();
        allOfNode.add(ifThenEntry);

        JDefinedClass result = rule.apply("test", allOfNode, null, jclass, schema);

        assertThat(result, sameInstance(jclass));
        assertThat(jclass.fields().isEmpty(), is(true));
    }

    @Test
    public void applyDoesNotDuplicateExistingFields() throws Exception {
        java.net.URI schemaUri = getClass().getClassLoader()
                .getResource("schema/polymorphism/allof-merge.json").toURI();

        SchemaStore schemaStore = new SchemaStore();
        Schema schema = schemaStore.create(schemaUri, "#/.");

        JCodeModel codeModel = new JCodeModel();
        JDefinedClass jclass = codeModel._class("org.jsonschema2pojo.rules.TestNoDupe");

        // Pre-add the "email" field so it already exists
        jclass.field(com.sun.codemodel.JMod.PRIVATE, codeModel.ref(String.class), "email");

        JsonNode allOfNode = schema.getContent().get("allOf");
        RuleFactory realFactory = new RuleFactory(config, new NoopAnnotator(), schemaStore);
        AllOfRule realRule = new AllOfRule(realFactory);

        realRule.apply("EnrichedPerson", allOfNode, null, jclass, schema);

        // Should still have exactly one "email" field
        assertThat(jclass.fields().values().stream()
                .filter(f -> f.name().equals("email")).count(), is(1L));
    }

    @Test
    public void applyHandlesNonArrayNode() throws JClassAlreadyExistsException {
        JDefinedClass jclass = new JCodeModel()._class("org.jsonschema2pojo.rules.TestNonArray");
        Schema schema = mock(Schema.class);
        JsonNode notAnArray = MAPPER.createObjectNode();

        JDefinedClass result = rule.apply("test", notAnArray, null, jclass, schema);

        assertThat(result, sameInstance(jclass));
    }
}
