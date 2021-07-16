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

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.lang.annotation.Annotation;
import java.util.Collection;

import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Schema;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JMod;

import jakarta.validation.constraints.NotNull;

@RunWith(Parameterized.class)
public class RequiredArrayRuleTest {

    private static final String TARGET_CLASS_NAME = RequiredArrayRuleTest.class.getName() + ".DummyClass";

    private RequiredArrayRule rule = new RequiredArrayRule(new RuleFactory());

    private final boolean useJakartaValidation;
    private final Class<? extends Annotation> notNullClass;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
                { false, javax.validation.constraints.NotNull.class },
                { true, NotNull.class }
        });
    }

    public RequiredArrayRuleTest(boolean useJakartaValidation, Class<? extends Annotation> notNullClass) {
        this.useJakartaValidation = useJakartaValidation;
        this.notNullClass = notNullClass;
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

    @Test
    public void shouldUpdateAnnotations() throws JClassAlreadyExistsException {
        setupRuleFactoryToIncludeJsr303();

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

    private void setupRuleFactoryToIncludeJsr303() {
        GenerationConfig config = mock(GenerationConfig.class);
        when(config.getPropertyWordDelimiters()).thenReturn(new char[] { '-', ' ', '_' });
        RuleFactory ruleFactory = new RuleFactory();
        ruleFactory.setGenerationConfig(config);
        rule = new RequiredArrayRule(ruleFactory);
        when(config.isIncludeJsr303Annotations()).thenReturn(true);
        when(config.isUseJakartaValidation()).thenReturn(useJakartaValidation);
    }
}
