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

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hamcrest.Matcher;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaConstructor;
import com.thoughtworks.qdox.model.impl.DefaultJavaClass;

@RunWith(Enclosed.class)
public class ConstructorsJavadocIT {

    private static final DefaultJavaClass cString = new DefaultJavaClass(String.class.getName());
    private static final DefaultJavaClass cDouble = new DefaultJavaClass(Double.class.getName());

    @RunWith(Parameterized.class)
    public static class ConstructorWithRequiredPropertiesOnlyIT {

        @Parameters(name = "{0}")
        public static Collection<String> data() {
            return asList("constructorsRequiredPropertiesOnly", "includeRequiredPropertiesConstructor");
        }

        @Rule
        public final Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

        private final String configuration;

        public ConstructorWithRequiredPropertiesOnlyIT(String configuration) {
            this.configuration = configuration;
        }

        @Test
        public void requiredPropertiesConstructor_hasJavadocForDocumentedProperties() throws IOException {
            JavaClass javaClass = buildJavadocClass(
                    schemaRule,
                    config("includeConstructors", true, "includeAllPropertiesConstructor", false, configuration, true));

            assertThat(javaClass.getConstructors().size(), is(2));
            assertNoArgConstructor(javaClass.getConstructor(Collections.emptyList()));
            final List<String> paramTagValues = getParamTagValues(javaClass.getConstructor(asList(cString, cString, cString, cString, cDouble, cDouble)));
            // 3 documented properties in sub class and 2 documented from super class
            assertThat(paramTagValues.size(), is(5));
            assertSuperClassDocumentedProperties(paramTagValues);
            assertThat(paramTagValues, hasItemContaining("descriptionOnly", "A description_only description."));
            assertThat(paramTagValues, hasItemContaining("titleAndDescription", "A title_and_description title. A title_and_description description."));
            assertThat(paramTagValues, hasItemContaining("tileOnly", "A title_only title."));
        }
    }

    public static class ConstructorWithAllPropertiesIT {

        @Rule
        public final Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

        @Test
        public void allPropertiesConstructor_hasJavadocForDocumentedProperties() throws IOException {
            // 'constructorsRequiredPropertiesOnly' is false and 'includeAllPropertiesConstructor' is true by default
            JavaClass javaClass = buildJavadocClass(schemaRule, config("includeConstructors", true));

            assertThat(javaClass.getConstructors().size(), is(2));
            assertNoArgConstructor(javaClass.getConstructor(Collections.emptyList()));
            final List<String> paramTagValues = getParamTagValues(javaClass.getConstructor(
                            asList(cString, cString, cString, cString, cString, cString, cString, cString, cDouble, cDouble, cDouble)));
            // 6 documented properties in sub class and 2 documented from super class
            assertThat(paramTagValues.size(), is(8));
            assertThat(paramTagValues, hasItemContaining("descriptionOnly", "A description_only description."));
            assertThat(paramTagValues, hasItemContaining("nonRequiredTileOnly", "A non_required_tile_only title."));
            assertThat(paramTagValues, hasItemContaining("titleAndDescription", "A title_and_description title. A title_and_description description."));
            assertThat(paramTagValues, hasItemContaining("nonRequiredDescriptionOnly", "A non_required_description_only description."));
            assertThat(paramTagValues, hasItemContaining("tileOnly", "A title_only title."));
            assertThat(paramTagValues, hasItemContaining("nonRequiredTitleAndDescription", "A non_required_title_and_description title. A non_required_title_and_description description."));
            assertSuperClassDocumentedProperties(paramTagValues);
        }

    }

    public static class IncludeCopyConstructorIT {

        @Rule
        public final Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

        @Test
        public void copyConstructor_hasJavadocForDocumentedProperties() throws IOException {
            JavaClass javaClass = buildJavadocClass(schemaRule, config("includeConstructors", true, "includeCopyConstructor", true));

            assertNoArgConstructor(javaClass.getConstructor(Collections.emptyList()));

            final List<String> paramTagValues = getParamTagValues(javaClass.getConstructor(Collections.singletonList(javaClass)));
            assertThat(paramTagValues, hasSize(1));
            assertThat(paramTagValues, hasItemContaining("source", "the object being copied"));
        }

    }

    private static JavaClass buildJavadocClass(Jsonschema2PojoRule schemaRule, Map<String, Object> config) throws IOException {
        schemaRule.generateAndCompile("/schema/constructors/javadoc/javadocConstructor.json", "com.example", config);

        final JavaProjectBuilder javaDocBuilder = new JavaProjectBuilder();
        javaDocBuilder.addSource(schemaRule.generated("com/example/JavadocConstructor.java"));
        return javaDocBuilder.getClassByName("com.example.JavadocConstructor");
    }

    private static List<String> getParamTagValues(JavaConstructor javaConstructor) {
        return javaConstructor.getTagsByName("param").stream().map(DocletTag::getValue).collect(Collectors.toList());
    }

    private static void assertNoArgConstructor(JavaConstructor noArgConstructor) {
        assertThat(noArgConstructor.getComment(), equalTo("No args constructor for use in serialization"));
    }

    private static void assertSuperClassDocumentedProperties(List<String> paramTagValues) {
        assertThat(paramTagValues, hasItemContaining("scPropertyWTitle", "A sc_property_w_title title."));
        assertThat(paramTagValues, hasItemContaining("scPropertyWDescription", "A sc_property_w_description description."));
    }

    private static Matcher<Iterable<? super String>> hasItemContaining(String first, String second) {
        return hasItem(both(containsString(first)).and(containsString(second)));
    }

}
