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

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;

/**
 * A default implementation of the Annotator interface that makes it easier to
 * plug in different Annotator implementations.
 * <p>
 * Annotators that need the generation configuration should add a constructor
 * with {@link GenerationConfig} arg. Annotators that don't need the
 * configuration need only add a default constructor.
 */
public abstract class AbstractAnnotator implements Annotator {

    private GenerationConfig generationConfig;

    public AbstractAnnotator() {
    }

    public AbstractAnnotator(GenerationConfig generationConfig) {
        this.generationConfig = generationConfig;
    }

    @Override
    public void typeInfo(JDefinedClass clazz, JsonNode schema) {

    }

    @Override
    public void propertyOrder(JDefinedClass clazz, JsonNode propertiesNode) {
    }

    @Override
    public void propertyInclusion(JDefinedClass clazz, JsonNode schema) {
    }

    @Override
    public void propertyField(JFieldVar field, JDefinedClass clazz,
            String propertyName, JsonNode propertyNode) {
    }

    @Override
    public void propertyGetter(JMethod getter, JDefinedClass clazz, String propertyName) {
    }

    @Override
    public void propertySetter(JMethod setter, JDefinedClass clazz, String propertyName) {
    }

    @Override
    public void anyGetter(JMethod getter, JDefinedClass clazz) {
    }

    @Override
    public void anySetter(JMethod setter, JDefinedClass clazz) {
    }

    @Override
    public void enumCreatorMethod(JDefinedClass _enum, JMethod creatorMethod) {
    }

    @Override
    public void enumValueMethod(JDefinedClass _enum, JMethod valueMethod) {
    }

    @Override
    public void enumConstant(JDefinedClass _enum, JEnumConstant constant, String value) {
    }

    @Override
    public boolean isAdditionalPropertiesSupported() {
        return true;
    }

    @Override
    public void additionalPropertiesField(JFieldVar field, JDefinedClass clazz, String propertyName) {
    }

    @Override
    public boolean isPolymorphicDeserializationSupported(JsonNode node) {
        return false;
    }

    public GenerationConfig getGenerationConfig() {
        return generationConfig;
    }

    @Override
    public void dateField(JFieldVar field, JDefinedClass clazz, JsonNode node) {
    }

    @Override
    public void timeField(JFieldVar field, JDefinedClass clazz, JsonNode node) {
    }

    @Override
    public void dateTimeField(JFieldVar field, JDefinedClass clazz, JsonNode node) {
    }
}
