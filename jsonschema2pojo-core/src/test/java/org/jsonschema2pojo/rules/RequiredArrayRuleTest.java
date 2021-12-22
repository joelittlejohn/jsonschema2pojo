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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sun.codemodel.*;
import jakarta.validation.constraints.NotNull;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Schema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequiredArrayRuleTest {

    private static final String TARGET_CLASS_NAME = RequiredArrayRuleTest.class.getName() + ".DummyClass";

    private RequiredArrayRule rule = new RequiredArrayRule(new RuleFactory());


    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(false, javax.validation.constraints.NotNull.class),
                Arguments.of(true, NotNull.class)
        );
    }

    @Test
    public void shouldUpdateJavaDoc() throws JClassAlreadyExistsException {
        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        jclass.field(JMod.PRIVATE, jclass.owner().ref(String.class), "fooBar");
        jclass.field(JMod.PRIVATE, jclass.owner().ref(String.class), "foo");

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode requiredNode = mapper.createArrayNode().add("fooBar");

        rule.apply("Class", requiredNode, null, jclass, new Schema(null, requiredNode, null));

        JDocComment fooBarJavaDoc = jclass.fields().get("fooBar").javadoc();
        JDocComment fooJavaDoc = jclass.fields().get("foo").javadoc();

        assertThat(fooBarJavaDoc.size(), is(1));
        assertThat((String) fooBarJavaDoc.get(0), is("\n(Required)"));

        assertThat(fooJavaDoc.size(), is(0));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldUpdateAnnotations(boolean useJakartaValidation, Class<? extends Annotation> notNullClass) throws JClassAlreadyExistsException {
        setupRuleFactoryToIncludeJsr303(useJakartaValidation);

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        jclass.field(JMod.PRIVATE, jclass.owner().ref(String.class), "fooBar");
        jclass.field(JMod.PRIVATE, jclass.owner().ref(String.class), "foo");

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode requiredNode = mapper.createArrayNode().add("foo_bar");

        rule.apply("Class", requiredNode, null, jclass, new Schema(null, requiredNode, null));

        Collection<JAnnotationUse> fooBarAnnotations = jclass.fields().get("fooBar").annotations();
        Collection<JAnnotationUse> fooAnnotations = jclass.fields().get("foo").annotations();

        assertThat(fooBarAnnotations.size(), is(1));
        assertThat(fooBarAnnotations.iterator().next().getAnnotationClass().name(), is(notNullClass.getSimpleName()));

        assertThat(fooAnnotations.size(), is(0));
    }

    private void setupRuleFactoryToIncludeJsr303(boolean useJakartaValidation) {
        GenerationConfig config = mock(GenerationConfig.class);
        when(config.getPropertyWordDelimiters()).thenReturn(new char[]{'-', ' ', '_'});
        RuleFactory ruleFactory = new RuleFactory();
        ruleFactory.setGenerationConfig(config);
        rule = new RequiredArrayRule(ruleFactory);
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
    }
}
