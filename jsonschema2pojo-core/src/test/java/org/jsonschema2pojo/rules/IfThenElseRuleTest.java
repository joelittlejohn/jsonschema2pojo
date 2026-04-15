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
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.Collection;

import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.SchemaStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

public class IfThenElseRuleTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private GenerationConfig config;
    private RuleFactory ruleFactory;
    private IfThenElseRule rule;

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
        when(config.getClassNamePrefix()).thenReturn("");
        when(config.getClassNameSuffix()).thenReturn("");
        when(config.getPropertyWordDelimiters()).thenReturn(new char[]{'-', '_'});

        ruleFactory = new RuleFactory(config, new NoopAnnotator(), new SchemaStore());
        rule = new IfThenElseRule(ruleFactory);
    }

    @Test
    public void applyReturnsBaseClassWhenNoIfNode() throws JClassAlreadyExistsException {
        JDefinedClass jclass = new JCodeModel()._class("org.jsonschema2pojo.rules.NoIfBase");
        Schema schema = mock(Schema.class);
        ObjectNode node = MAPPER.createObjectNode();
        node.set("then", MAPPER.createObjectNode());

        JDefinedClass result = rule.apply("test", node, null, jclass, schema);

        assertThat(result, sameInstance(jclass));
    }

    @Test
    public void applyReturnsBaseClassWhenNoThenNode() throws JClassAlreadyExistsException {
        JDefinedClass jclass = new JCodeModel()._class("org.jsonschema2pojo.rules.NoThenBase");
        Schema schema = mock(Schema.class);
        ObjectNode node = MAPPER.createObjectNode();
        node.set("if", MAPPER.createObjectNode());

        JDefinedClass result = rule.apply("test", node, null, jclass, schema);

        assertThat(result, sameInstance(jclass));
    }

    @Test
    public void applyReturnsBaseClassWhenIfHasNoDiscriminator() throws JClassAlreadyExistsException {
        JDefinedClass jclass = new JCodeModel()._class("org.jsonschema2pojo.rules.NoDiscrimBase");
        Schema schema = mock(Schema.class);

        // if: { properties: { type: { description: "no const" } } }
        ObjectNode propSchema = MAPPER.createObjectNode();
        propSchema.put("description", "no const value");

        ObjectNode ifProps = MAPPER.createObjectNode();
        ifProps.set("type", propSchema);

        ObjectNode ifNode = MAPPER.createObjectNode();
        ifNode.set("properties", ifProps);

        ObjectNode node = MAPPER.createObjectNode();
        node.set("if", ifNode);
        node.set("then", MAPPER.createObjectNode());

        JDefinedClass result = rule.apply("test", node, null, jclass, schema);

        assertThat(result, sameInstance(jclass));
    }

    @Test
    public void applyAllCreatesSubclassForEachIfThenEntry() throws Exception {
        java.net.URI schemaUri = getClass().getClassLoader()
                .getResource("schema/polymorphism/discriminated-union.json").toURI();
        SchemaStore schemaStore = new SchemaStore();
        Schema schema = schemaStore.create(schemaUri, "#/.");

        JCodeModel codeModel = new JCodeModel();
        JDefinedClass baseClass = codeModel._class("org.jsonschema2pojo.rules.Event");
        RuleFactory realFactory = new RuleFactory(config, new NoopAnnotator(), schemaStore);
        IfThenElseRule realRule = new IfThenElseRule(realFactory);

        JsonNode allOfNode = schema.getContent().get("allOf");
        realRule.applyAll("Event", allOfNode, null, baseClass, schema);

        JDefinedClass clickClass = codeModel._getClass("org.jsonschema2pojo.rules.ClickEvent");
        JDefinedClass viewClass  = codeModel._getClass("org.jsonschema2pojo.rules.ViewEvent");
        JDefinedClass scrollClass = codeModel._getClass("org.jsonschema2pojo.rules.ScrollEvent");

        assertThat("ClickEvent should be generated", clickClass, notNullValue());
        assertThat("ViewEvent should be generated",  viewClass,  notNullValue());
        assertThat("ScrollEvent should be generated", scrollClass, notNullValue());

        assertThat(clickClass._extends(), equalTo(baseClass));
        assertThat(viewClass._extends(),  equalTo(baseClass));
        assertThat(scrollClass._extends(), equalTo(baseClass));
    }

    @Test
    public void applyAllAddsFieldsToSubclass() throws Exception {
        java.net.URI schemaUri = getClass().getClassLoader()
                .getResource("schema/polymorphism/discriminated-union.json").toURI();
        SchemaStore schemaStore = new SchemaStore();
        Schema schema = schemaStore.create(schemaUri, "#/.");

        JCodeModel codeModel = new JCodeModel();
        JDefinedClass baseClass = codeModel._class("org.jsonschema2pojo.rules.EventFields");
        RuleFactory realFactory = new RuleFactory(config, new NoopAnnotator(), schemaStore);
        IfThenElseRule realRule = new IfThenElseRule(realFactory);

        JsonNode allOfNode = schema.getContent().get("allOf");
        realRule.applyAll("EventFields", allOfNode, null, baseClass, schema);

        JDefinedClass clickClass = codeModel._getClass("org.jsonschema2pojo.rules.ClickEvent");
        assertThat(clickClass, notNullValue());
        assertThat("ClickEvent should have 'x'",      clickClass.fields(), hasKey("x"));
        assertThat("ClickEvent should have 'y'",      clickClass.fields(), hasKey("y"));
        assertThat("ClickEvent should have 'button'", clickClass.fields(), hasKey("button"));
    }

    @Test
    public void applyAllAddsJsonTypeInfoAnnotationToBaseClass() throws Exception {
        java.net.URI schemaUri = getClass().getClassLoader()
                .getResource("schema/polymorphism/discriminated-union.json").toURI();
        SchemaStore schemaStore = new SchemaStore();
        Schema schema = schemaStore.create(schemaUri, "#/.");

        JCodeModel codeModel = new JCodeModel();
        JDefinedClass baseClass = codeModel._class("org.jsonschema2pojo.rules.EventAnnotations");
        RuleFactory realFactory = new RuleFactory(config, new NoopAnnotator(), schemaStore);
        IfThenElseRule realRule = new IfThenElseRule(realFactory);

        JsonNode allOfNode = schema.getContent().get("allOf");
        realRule.applyAll("EventAnnotations", allOfNode, null, baseClass, schema);

        Collection<JAnnotationUse> annotations = baseClass.annotations();
        boolean hasTypeInfo = annotations.stream()
                .anyMatch(a -> a.getAnnotationClass().name().equals("JsonTypeInfo"));
        boolean hasSubTypes = annotations.stream()
                .anyMatch(a -> a.getAnnotationClass().name().equals("JsonSubTypes"));

        assertThat("@JsonTypeInfo should be present on base class", hasTypeInfo, is(true));
        assertThat("@JsonSubTypes should be present on base class", hasSubTypes, is(true));
    }

    @Test
    public void applyAllSkipsEntriesWithoutIfThen() throws JClassAlreadyExistsException {
        JCodeModel codeModel = new JCodeModel();
        JDefinedClass baseClass = codeModel._class("org.jsonschema2pojo.rules.Product");
        Schema schema = buildSchemaWithContent(MAPPER.createObjectNode());

        ArrayNode allOfNode = MAPPER.createArrayNode();
        // Entry without if/then — just properties
        ObjectNode plainEntry = MAPPER.createObjectNode();
        ObjectNode props = MAPPER.createObjectNode();
        props.set("sku", MAPPER.createObjectNode().put("type", "string"));
        plainEntry.set("properties", props);
        allOfNode.add(plainEntry);

        rule.applyAll("Product", allOfNode, null, baseClass, schema);

        assertThat("No @JsonTypeInfo should be added when no if/then entries exist",
                baseClass.annotations().isEmpty(), is(true));
    }

    @Test
    public void applyAllHandlesNullAllOfNode() throws JClassAlreadyExistsException {
        JDefinedClass jclass = new JCodeModel()._class("org.jsonschema2pojo.rules.NullAllOfBase");
        Schema schema = mock(Schema.class);

        JDefinedClass result = rule.applyAll("test", null, null, jclass, schema);

        assertThat(result, sameInstance(jclass));
    }

    // --- helpers ---

    private ObjectNode buildIfThenEntry(String discriminatorProp, String constValue,
                                        String subclassTitle,
                                        String prop1, String prop2) {
        ObjectNode constNode = MAPPER.createObjectNode();
        constNode.put("const", constValue);

        ObjectNode ifPropNode = MAPPER.createObjectNode();
        ifPropNode.set(discriminatorProp, constNode);

        ObjectNode ifNode = MAPPER.createObjectNode();
        ifNode.set("properties", ifPropNode);

        ObjectNode thenProps = MAPPER.createObjectNode();
        if (prop1 != null) {
            thenProps.set(prop1, MAPPER.createObjectNode().put("type", "string"));
        }
        if (prop2 != null) {
            thenProps.set(prop2, MAPPER.createObjectNode().put("type", "string"));
        }

        ObjectNode thenNode = MAPPER.createObjectNode();
        thenNode.put("title", subclassTitle);
        thenNode.set("properties", thenProps);

        ObjectNode entry = MAPPER.createObjectNode();
        entry.set("if", ifNode);
        entry.set("then", thenNode);
        return entry;
    }

    private Schema buildSchemaWithContent(ObjectNode content) {
        Schema schema = mock(Schema.class);
        when(schema.getId()).thenReturn(URI.create("urn:test:schema"));
        when(schema.getContent()).thenReturn(content);
        when(schema.getGrandParent()).thenReturn(schema);
        when(schema.getParent()).thenReturn(schema);
        return schema;
    }
}
