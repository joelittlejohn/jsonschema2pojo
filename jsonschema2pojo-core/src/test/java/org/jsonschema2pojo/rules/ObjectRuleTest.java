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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.util.ParcelableHelper;
import org.jsonschema2pojo.util.ReflectionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

import net.mfjassociates.tools.JaCoCoGenerated;

public class ObjectRuleTest {

    private final GenerationConfig spyGenerationConfig = spy(new DefaultGenerationConfig());
    private final RuleFactory spyRuleFactory = spy(new RuleFactory());

    private final ObjectRule rule = new ObjectRule(spyRuleFactory, new ParcelableHelper(), new ReflectionHelper(spyRuleFactory));

    @BeforeEach
    public void wireUpConfig() {
    	reset(spyGenerationConfig);
    	reset(spyRuleFactory);
        when(spyGenerationConfig.isIncludeGeneratedAnnotation()).thenReturn(false);
        when(spyGenerationConfig.isIncludeRuntimeGeneratedAnnotation()).thenReturn(true);
        when(spyRuleFactory.getGenerationConfig()).thenReturn(spyGenerationConfig);
    }
    @Test
    public void customRuntimeGenerated() {
    	JCodeModel codeModel=new JCodeModel();
        JPackage jpackage = codeModel._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        //objectNode.put("type", "string");
        Schema schema=mock(Schema.class);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, schema);
        assertThat(result).isNotNull();

		JDefinedClass targetClass = codeModel._getClass(jpackage.name()+".FooBar");
		Collection<JAnnotationUse> annotations = targetClass.annotations();
		assertThat(annotations)
			.as("target class not annotated with JaCoCo compatible generated annotation").anySatisfy(ja -> {
			assertThat(ja.getAnnotationClass().name()).isEqualTo(JaCoCoGenerated.JACOCO_GENERATED_CLASS_NAME);
			assertThrows(NullPointerException.class, () -> {
				assertThat(ja.getAnnotationMembers()).isNull();
			}, JaCoCoGenerated.JACOCO_GENERATED_CLASS_NAME+" annotation had annotation members and should not have any"); 
		});

    }
}
