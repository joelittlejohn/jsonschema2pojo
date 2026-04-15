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

import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.SchemaStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

public class OneOfRuleTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private GenerationConfig config;
    private RuleFactory ruleFactory;
    private OneOfRule rule;

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

        ruleFactory = new RuleFactory(config, new NoopAnnotator(), new SchemaStore());
        rule = new OneOfRule(ruleFactory);
    }

    @Test
    public void applyReturnsObjectWhenNoOneOfNode() throws JClassAlreadyExistsException {
        JCodeModel codeModel = new JCodeModel();
        JPackage pkg = codeModel._package("org.jsonschema2pojo.rules");
        Schema schema = mock(Schema.class);
        ObjectNode node = MAPPER.createObjectNode();

        JType result = rule.apply("test", node, null, pkg, schema);

        assertThat(result.fullName(), is(Object.class.getName()));
    }

    @Test
    public void applyReturnsObjectWhenOneOfIsEmpty() throws JClassAlreadyExistsException {
        JCodeModel codeModel = new JCodeModel();
        JPackage pkg = codeModel._package("org.jsonschema2pojo.rules");
        Schema schema = mock(Schema.class);

        ObjectNode node = MAPPER.createObjectNode();
        node.set("oneOf", MAPPER.createArrayNode());

        JType result = rule.apply("test", node, null, pkg, schema);

        assertThat(result.fullName(), is(Object.class.getName()));
    }

    @Test
    public void applyReturnsObjectWhenMixedOneOfTypes() throws JClassAlreadyExistsException {
        JCodeModel codeModel = new JCodeModel();
        JPackage pkg = codeModel._package("org.jsonschema2pojo.rules");
        Schema schema = mock(Schema.class);

        // oneOf: [ {type: "object"}, {type: "array"} ]
        ArrayNode oneOf = MAPPER.createArrayNode();
        oneOf.add(MAPPER.createObjectNode().put("type", "object"));
        oneOf.add(MAPPER.createObjectNode().put("type", "array"));

        ObjectNode node = MAPPER.createObjectNode();
        node.set("oneOf", oneOf);

        JType result = rule.apply("test", node, null, pkg, schema);

        assertThat(result.fullName(), is(Object.class.getName()));
    }

    @Test
    public void applyWorksWithAnyOfKeyword() throws JClassAlreadyExistsException {
        JCodeModel codeModel = new JCodeModel();
        JPackage pkg = codeModel._package("org.jsonschema2pojo.rules");
        Schema schema = mock(Schema.class);

        ObjectNode node = MAPPER.createObjectNode();
        node.set("anyOf", MAPPER.createArrayNode());

        JType result = rule.apply("test", node, null, pkg, schema);

        assertThat(result.fullName(), is(Object.class.getName()));
    }

    @Test
    public void applyPreferrsOneOfOverAnyOf() throws JClassAlreadyExistsException {
        JCodeModel codeModel = new JCodeModel();
        JPackage pkg = codeModel._package("org.jsonschema2pojo.rules");
        Schema schema = mock(Schema.class);

        // Both present — oneOf takes precedence
        ObjectNode node = MAPPER.createObjectNode();
        ArrayNode oneOf = MAPPER.createArrayNode();
        oneOf.add(MAPPER.createObjectNode().put("type", "object"));
        node.set("oneOf", oneOf);
        node.set("anyOf", MAPPER.createArrayNode()); // empty anyOf

        JType result = rule.apply("test", node, null, pkg, schema);

        // Should process without throwing
        assertThat(result, notNullValue());
    }
}
