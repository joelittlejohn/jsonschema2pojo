/**
 * Copyright Â© 2010-2014 Nokia
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.*;
import com.sun.codemodel.writer.SingleStreamCodeWriter;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.SchemaStore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author {@link "https://github.com/s13o" "s13o"}
 */
public class DefaultRuleTest {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRuleTest.class);

    private final GenerationConfig config = mock(GenerationConfig.class);
    private final Schema schema = mock(Schema.class);
    private final DefaultRule rule = new DefaultRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));
    private final EnumRule enumRule = new EnumRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));
    private final ObjectMapper mapper = new ObjectMapper();


    @Test
    public void testStringEnumDefault() throws Exception{
        final JsonNode json = mapper.readTree("{\n" +
                "   \"title\" : \"'Tile' | 'Toast' | 'Badge' | 'Raw'\"," +
                "   \"type\" : \"string\",\n" +
                "   \"default\" : \"Badge\",\n" +
                "   \"enum\" : [ \"Tile\", \"Toast\", \"Badge\", \"Raw\" ]\n" +
                "}");

        final JCodeModel jModel = new JCodeModel();
        JDefinedClass jClass = jModel._class("Dummy");

        JType enumType = enumRule.apply("foo", json, jModel._class("StringEnumTest", ClassType.ENUM), schema);


        JFieldVar jField = jClass.field(JMod.PRIVATE, enumType, "foo");

        final ObjectNode defaultNode = mapper.createObjectNode();
        defaultNode.put("default", "Badge");

        JFieldVar field = rule.apply("bar", defaultNode.get("default"), jField, schema);

        assertThat(field, notNullValue());

        String context = generate(jModel);

        assertThat(context, notNullValue());
        assertThat(context, containsString("private StringEnumTest.Foo foo = StringEnumTest.Foo.fromValue(\"Badge\")"));
    }

    @Test
    public void testStringEnumNullDefault() throws Exception{
        final JsonNode json = mapper.readTree("{\n" +
                "   \"title\" : \"null, 'Tile' | 'Toast' | 'Badge' | 'Raw'\"," +
                "   \"type\" : \"string\",\n" +
                "   \"default\" : null,\n" +
                "   \"enum\" : [ \"Tile\", \"Toast\", \"Badge\", \"Raw\" ],\n" +
                "   \"javaEnumNames\" : [ \"Tile\", \"Toast\", \"Badge\", \"Raw\" ]\n" +
                "}");

        final JCodeModel jModel = new JCodeModel();
        JDefinedClass jClass = jModel._class("Dummy");

        JType enumType = enumRule.apply("foo", json, jModel._class("StringEnumTest", ClassType.ENUM), schema);


        JFieldVar jField = jClass.field(JMod.PRIVATE, enumType, "foo");

        final ObjectNode defaultNode = mapper.createObjectNode();
        defaultNode.set("default", null);

        JFieldVar field = rule.apply("bar", defaultNode.get("default"), jField, schema);

        assertThat(field, notNullValue());

        String context = generate(jModel);

        assertThat(context, notNullValue());
        assertThat(context, containsString("private StringEnumTest.Foo foo = null"));
    }

    @Test
    public void testIntEnumDefault() throws Exception{
        final JsonNode json = mapper.readTree("{\n" +
                "   \"title\" : \"'Tile' | 'Toast' | 'Badge' | 'Raw'\"," +
                "   \"type\" : \"integer\",\n" +
                "   \"default\" : 1,\n" +
                "   \"enum\" : [ 1, 2, 3, 4 ],\n" +
                "   \"javaEnumNames\" : [ \"Tile\", \"Toast\", \"Badge\", \"Raw\" ]\n" +
                "}");

        final JCodeModel jModel = new JCodeModel();
        JDefinedClass jClass = jModel._class("Dummy");

        JType enumType = enumRule.apply("foo", json, jModel._class("IntEnumTest", ClassType.ENUM), schema);


        JFieldVar jField = jClass.field(JMod.PRIVATE, enumType, "foo");

        final ObjectNode defaultNode = mapper.createObjectNode();
        defaultNode.put("default", 3);

        JFieldVar field = rule.apply("bar", defaultNode.get("default"), jField, schema);

        assertThat(field, notNullValue());

        String context = generate(jModel);

        assertThat(context, notNullValue());
        assertThat(context, containsString("private IntEnumTest.Foo foo = IntEnumTest.Foo.fromValue(3)"));
    }

    @Test
    public void testBooleanDefault() throws Exception{
        /*
        { "type" : "boolean", "default" : true }
        */
        final JCodeModel jModel = new JCodeModel();
        JDefinedClass jClass = jModel._class("Dummy");
        JFieldVar jField = jClass.field(JMod.PRIVATE, jModel.BOOLEAN, "foo");

        final ObjectNode defaultNode = mapper.createObjectNode();
        defaultNode.put("default", true);

        JFieldVar field = rule.apply("bar", defaultNode.get("default"), jField, schema);

        assertThat(field, notNullValue());

        String context = generate(jModel);

        assertThat(context, notNullValue());
        assertThat(context, containsString("private boolean foo = true;"));
    }

    @Test
    public void testIntDefault() throws Exception{
        /*
        { "type" : "integer", "default" : 1024 }
        */
        final JCodeModel jModel = new JCodeModel();
        JDefinedClass jClass = jModel._class("Dummy");
        JFieldVar jField = jClass.field(JMod.PRIVATE, jModel.INT, "foo");

        final ObjectNode defaultNode = mapper.createObjectNode();
        defaultNode.put("default", 1024);

        JFieldVar field = rule.apply("bar", defaultNode.get("default"), jField, schema);

        assertThat(field, notNullValue());

        String context = generate(jModel);

        assertThat(context, notNullValue());
        assertThat(context, containsString("private int foo = 1024;"));
    }

    @Test
    public void testStringDefault() throws Exception{
        /*
        { "type" : "string", "default" : "default" }
        */
        final JCodeModel jModel = new JCodeModel();
        JDefinedClass jClass = jModel._class("Dummy");
        JFieldVar jField = jClass.field(JMod.PRIVATE, jModel.ref(String.class.getName()), "foo");

        final ObjectNode defaultNode = mapper.createObjectNode();
        defaultNode.put("default", "default");

        JFieldVar field = rule.apply("bar", defaultNode.get("default"), jField, schema);

        assertThat(field, notNullValue());

        String context = generate(jModel);

        assertThat(context, notNullValue());
        assertThat(context, containsString("private String foo = \"default\";"));
    }

    @Test
    public void testStringNoDefault() throws Exception{
        /*
        { "type" : "string"}
        */
        final JCodeModel jModel = new JCodeModel();
        JDefinedClass jClass = jModel._class("Dummy");
        JFieldVar jField = jClass.field(JMod.PRIVATE, jModel.ref(String.class.getName()), "foo");

        final JsonNode defaultNode = mapper.createObjectNode();
//        defaultNode.put("default", "default");

        JFieldVar field = rule.apply("bar", defaultNode.get("default"), jField, schema);

        assertThat(field, notNullValue());

        String context = generate(jModel);

        assertThat(context, notNullValue());
        assertThat(context, containsString("private String foo;"));
    }

    @Test
    public void testStringNullDefault() throws Exception{
        /*
        { "type" : "string", "default" : null }
        */
        final JCodeModel jModel = new JCodeModel();
        JDefinedClass jClass = jModel._class("Dummy");
        JFieldVar jField = jClass.field(JMod.PRIVATE, jModel.ref(String.class.getName()), "foo");

        final JsonNode defaultNode = mapper.createObjectNode().set("default", null);

        JFieldVar field = rule.apply("bar", defaultNode.get("default"), jField, schema);

        assertThat(field, notNullValue());

        String context = generate(jModel);

        assertThat(context, notNullValue());
        assertThat(context, containsString("private String foo = null;"));
    }

    @Test
    public void testListOfStringsNullDefault() throws Exception{
        /*
        { "type" : "array", "default" : null}
        */
        final JCodeModel jModel = new JCodeModel();
        JDefinedClass jClass = jModel._class("Dummy");
        JFieldVar jField = jClass.field(JMod.PRIVATE, jModel.ref(List.class.getName()).narrow(String.class), "foo");

        final JsonNode defaultNode = mapper.createObjectNode().set("default", null);

        JFieldVar field = rule.apply("bar", defaultNode.get("default"), jField, schema);

        assertThat(field, notNullValue());

        String context = generate(jModel);

        assertThat(context, notNullValue());
        assertThat(context, containsString("private List<String> foo = null;"));
    }

    @Test
    public void testListOfStringsNoDefaultInitCollections() throws Exception{
        /*
        { "type" : "array", "default" : null}
        */
        final JCodeModel jModel = new JCodeModel();
        JDefinedClass jClass = jModel._class("Dummy");
        JFieldVar jField = jClass.field(JMod.PRIVATE, jModel.ref(List.class.getName()).narrow(String.class), "foo");

        final JsonNode defaultNode = mapper.createObjectNode();

        when(config.isInitializeCollections()).thenReturn(true);
        JFieldVar field = rule.apply("bar", defaultNode.get("default"), jField, schema);

        assertThat(field, notNullValue());

        String context = generate(jModel);

        assertThat(context, notNullValue());
        assertThat(context, containsString("private List<String> foo = new ArrayList<String>();"));
    }

    @Test
    public void testListOfStringsNoDefaultDontInitCollections() throws Exception{
        /*
        { "type" : "array", "default" : null}
        */
        final JCodeModel jModel = new JCodeModel();
        JDefinedClass jClass = jModel._class("Dummy");
        JFieldVar jField = jClass.field(JMod.PRIVATE, jModel.ref(List.class.getName()).narrow(String.class), "foo");

        final JsonNode defaultNode = mapper.createObjectNode();

        when(config.isInitializeCollections()).thenReturn(false);
        JFieldVar field = rule.apply("bar", defaultNode.get("default"), jField, schema);

        assertThat(field, notNullValue());

        String context = generate(jModel);

        assertThat(context, notNullValue());
        assertThat(context, containsString("private List<String> foo = null"));
    }

    @Test
    public void testSetOfIntDefault() throws Exception{
        /*
        { "type" : "array", "default" : [1, 2, 3]}
        */
        final JCodeModel jModel = new JCodeModel();
        JDefinedClass jClass = jModel._class("Dummy");
        JFieldVar jField = jClass.field(JMod.PRIVATE, jModel.ref(Set.class.getName()).narrow(Integer.class), "foo");

        final ObjectNode defaultNode = mapper.createObjectNode();
        ArrayNode array = defaultNode.putArray("default");
        array.add(1);
        array.add(2);
        array.add(3);

        JFieldVar field = rule.apply("bar", defaultNode.get("default"), jField, schema);

        assertThat(field, notNullValue());

        String context = generate(jModel);

        assertThat(context, notNullValue());
        assertThat(context, containsString("private Set<Integer> foo = new LinkedHashSet<Integer>(Arrays.asList(1, 2, 3));"));
    }

    @Test
    public void testListOfIntDefault() throws Exception{
        /*
        { "type" : "array", "default" : [1, 2, 3]}
        */
        final JCodeModel jModel = new JCodeModel();
        JDefinedClass jClass = jModel._class("Dummy");
        JFieldVar jField = jClass.field(JMod.PRIVATE, jModel.ref(List.class.getName()).narrow(Integer.class), "foo");

        final ObjectNode defaultNode = mapper.createObjectNode();
        ArrayNode array = defaultNode.putArray("default");
        array.add(1);
        array.add(2);
        array.add(3);

        JFieldVar field = rule.apply("bar", defaultNode.get("default"), jField, schema);

        assertThat(field, notNullValue());

        String context = generate(jModel);

        assertThat(context, notNullValue());
        assertThat(context, containsString("private List<Integer> foo = new ArrayList<Integer>(Arrays.asList(1, 2, 3));"));
    }

    @Test
    public void testListOfStringsDefault() throws Exception{
        /*
        { "type" : "array", "default" : ["1", "2", "3"]}
        */
        final JCodeModel jModel = new JCodeModel();
        JDefinedClass jClass = jModel._class("Dummy");
        JFieldVar jField = jClass.field(JMod.PRIVATE, jModel.ref(List.class.getName()).narrow(String.class), "foo");

        final ObjectNode defaultNode = mapper.createObjectNode();
        ArrayNode array = defaultNode.putArray("default");
        array.add("1");
        array.add("2");
        array.add("3");

        JFieldVar field = rule.apply("bar", defaultNode.get("default"), jField, schema);

        assertThat(field, notNullValue());

        String context = generate(jModel);

        assertThat(context, notNullValue());
        assertThat(context, containsString("private List<String> foo = new ArrayList<String>(Arrays.asList(\"1\", \"2\", \"3\"));"));
    }

    private String generate(JCodeModel jModel) throws IOException {
        OutputStream out = new StringOutputStream();
        jModel.build(new SingleStreamCodeWriter(out));
        logger.debug(out.toString());
        return out.toString();
    }

    private class StringOutputStream extends OutputStream{
        private StringBuilder string = new StringBuilder();

        @Override
        public void write(int b) throws IOException {
            this.string.append((char) b );
        }
        public String toString(){
            return this.string.toString();
        }

    }

}
