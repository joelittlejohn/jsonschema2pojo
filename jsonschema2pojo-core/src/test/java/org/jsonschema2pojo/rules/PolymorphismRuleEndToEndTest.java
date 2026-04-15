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

import java.net.URI;
import java.util.Collection;

import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.junit.jupiter.api.Test;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;

/**
 * End-to-end tests for JSON Schema polymorphism support.
 * These tests drive the full generator pipeline against real JSON Schema files
 * and verify the generated class hierarchy.
 */
public class PolymorphismRuleEndToEndTest {

    private static final String TARGET_PACKAGE = "org.jsonschema2pojo.rules.generated";

    // -------------------------------------------------------------------------
    // discriminated-union.json  (allOf with if/then)
    // -------------------------------------------------------------------------

    @Test
    public void discriminatedUnionGeneratesBaseClassWithJsonTypeInfo() throws Exception {
        JCodeModel codeModel = generateFrom("schema/polymorphism/discriminated-union.json");

        JDefinedClass eventClass = findClass(codeModel, "Event");
        assertThat("Event base class should be generated", eventClass, notNullValue());

        boolean hasTypeInfo = eventClass.annotations().stream()
                .anyMatch(a -> a.getAnnotationClass().name().equals("JsonTypeInfo"));
        assertThat("Event base class should have @JsonTypeInfo", hasTypeInfo, is(true));
    }

    @Test
    public void discriminatedUnionGeneratesBaseClassWithJsonSubTypes() throws Exception {
        JCodeModel codeModel = generateFrom("schema/polymorphism/discriminated-union.json");

        JDefinedClass eventClass = findClass(codeModel, "Event");
        assertThat(eventClass, notNullValue());

        boolean hasSubTypes = eventClass.annotations().stream()
                .anyMatch(a -> a.getAnnotationClass().name().equals("JsonSubTypes"));
        assertThat("Event base class should have @JsonSubTypes", hasSubTypes, is(true));
    }

    @Test
    public void discriminatedUnionGeneratesClickEventSubclass() throws Exception {
        JCodeModel codeModel = generateFrom("schema/polymorphism/discriminated-union.json");

        JDefinedClass clickClass = findClass(codeModel, "ClickEvent");
        assertThat("ClickEvent subclass should be generated", clickClass, notNullValue());

        JDefinedClass eventClass = findClass(codeModel, "Event");
        assertThat("ClickEvent should extend Event", clickClass._extends(), equalTo(eventClass));
    }

    @Test
    public void discriminatedUnionClickEventHasOwnFields() throws Exception {
        JCodeModel codeModel = generateFrom("schema/polymorphism/discriminated-union.json");

        JDefinedClass clickClass = findClass(codeModel, "ClickEvent");
        assertThat(clickClass, notNullValue());
        assertThat("ClickEvent should have 'x' field", clickClass.fields(), hasKey("x"));
        assertThat("ClickEvent should have 'y' field", clickClass.fields(), hasKey("y"));
        assertThat("ClickEvent should have 'button' field", clickClass.fields(), hasKey("button"));
    }

    @Test
    public void discriminatedUnionGeneratesViewEventSubclass() throws Exception {
        JCodeModel codeModel = generateFrom("schema/polymorphism/discriminated-union.json");

        JDefinedClass viewClass = findClass(codeModel, "ViewEvent");
        assertThat("ViewEvent subclass should be generated", viewClass, notNullValue());
        assertThat("ViewEvent should have 'url' field", viewClass.fields(), hasKey("url"));
        assertThat("ViewEvent should have 'duration' field", viewClass.fields(), hasKey("duration"));
    }

    @Test
    public void discriminatedUnionGeneratesScrollEventSubclass() throws Exception {
        JCodeModel codeModel = generateFrom("schema/polymorphism/discriminated-union.json");

        JDefinedClass scrollClass = findClass(codeModel, "ScrollEvent");
        assertThat("ScrollEvent subclass should be generated", scrollClass, notNullValue());
        assertThat("ScrollEvent should have 'deltaY' field", scrollClass.fields(), hasKey("deltaY"));
    }

    @Test
    public void discriminatedUnionBaseClassPreservesOwnProperties() throws Exception {
        JCodeModel codeModel = generateFrom("schema/polymorphism/discriminated-union.json");

        JDefinedClass eventClass = findClass(codeModel, "Event");
        assertThat(eventClass, notNullValue());
        assertThat("Event base class should have 'id' field", eventClass.fields(), hasKey("id"));
        assertThat("Event base class should have 'kind' field", eventClass.fields(), hasKey("kind"));
    }

    // -------------------------------------------------------------------------
    // allof-merge.json  (allOf property merging)
    // -------------------------------------------------------------------------

    @Test
    public void allOfMergeGeneratesClassWithOwnProperties() throws Exception {
        JCodeModel codeModel = generateFrom("schema/polymorphism/allof-merge.json");

        JDefinedClass personClass = findClass(codeModel, "EnrichedPerson");
        assertThat("EnrichedPerson class should be generated", personClass, notNullValue());

        assertThat("Should have 'firstName' from own properties", personClass.fields(), hasKey("firstName"));
        assertThat("Should have 'lastName' from own properties",  personClass.fields(), hasKey("lastName"));
    }

    @Test
    public void allOfMergeMergesPropertiesFromSubSchema() throws Exception {
        // Drive the AllOfRule directly with a real schema URI so paths resolve correctly
        java.net.URI schemaUri = getClass().getClassLoader()
                .getResource("schema/polymorphism/allof-merge.json").toURI();

        SchemaStore schemaStore = new SchemaStore();
        Schema schema = schemaStore.create(schemaUri, "#/.");

        GenerationConfig config = new DefaultGenerationConfig() {
            @Override public boolean isIncludeGetters() { return true; }
            @Override public boolean isIncludeSetters() { return true; }
            @Override public boolean isIncludeConstructors() { return false; }
            @Override public boolean isIncludeAdditionalProperties() { return false; }
            @Override public boolean isIncludeGeneratedAnnotation() { return false; }
        };

        JCodeModel codeModel = new JCodeModel();
        JDefinedClass jclass = codeModel._class(TARGET_PACKAGE + ".EnrichedPersonMerged");
        RuleFactory factory = new RuleFactory(config, new NoopAnnotator(), schemaStore);
        AllOfRule allOfRule = new AllOfRule(factory);

        allOfRule.apply("EnrichedPerson", schema.getContent().get("allOf"), null, jclass, schema);

        assertThat("'email' should be merged from allOf", jclass.fields(), hasKey("email"));
        assertThat("'age' should be merged from allOf",   jclass.fields(), hasKey("age"));
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private JCodeModel generateFrom(String schemaClasspath) throws Exception {
        URI schemaUri = getClass().getClassLoader().getResource(schemaClasspath).toURI();

        JCodeModel codeModel = new JCodeModel();
        GenerationConfig config = new DefaultGenerationConfig() {
            @Override public boolean isIncludeGeneratedAnnotation() { return true; }
            @Override public boolean isIncludeGetters() { return true; }
            @Override public boolean isIncludeSetters() { return true; }
            @Override public boolean isIncludeConstructors() { return false; }
            @Override public boolean isIncludeAdditionalProperties() { return false; }
            @Override public boolean isGenerateBuilders() { return false; }
            @Override public boolean isUseTitleAsClassname() { return true; }
        };

        RuleFactory ruleFactory = new RuleFactory(config, new NoopAnnotator(), new SchemaStore());
        SchemaMapper schemaMapper = new SchemaMapper(ruleFactory, null);
        schemaMapper.generate(codeModel, "Event", TARGET_PACKAGE, schemaUri.toURL());

        return codeModel;
    }

    private JDefinedClass findClass(JCodeModel codeModel, String simpleName) {
        for (java.util.Iterator<JPackage> pkgs = codeModel.packages(); pkgs.hasNext(); ) {
            JPackage pkg = pkgs.next();
            for (java.util.Iterator<JDefinedClass> classes = pkg.classes(); classes.hasNext(); ) {
                JDefinedClass cls = classes.next();
                if (cls.name().equals(simpleName)) {
                    return cls;
                }
            }
        }
        return null;
    }
}
