/**
 * Copyright © 2010-2011 Nokia
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

package com.googlecode.jsonschema2pojo.rules;

import java.util.HashMap;
import java.util.Map;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

public class RuleFactoryImpl implements RuleFactory {

    private final Map<String, String> behaviourProperties;

    /**
     * Create a new rule factory with the given behaviour properties
     * 
     * @param behaviourProperties
     *            A map defining the behavioural properties of this context.
     */
    public RuleFactoryImpl(Map<String, String> behaviourProperties) {
        if (behaviourProperties == null) {
            this.behaviourProperties = new HashMap<String, String>();
        } else {
            this.behaviourProperties = behaviourProperties;
        }
    }

    @Override
    public SchemaRule<JPackage, JClass> getArrayRule() {
        return new ArrayRule(this);
    }

    @Override
    public SchemaRule<JDocCommentable, JDocComment> getDescriptionRule() {
        return new DescriptionRule();
    }

    @Override
    public SchemaRule<JClassContainer, JDefinedClass> getEnumRule() {
        return new EnumRule();
    }

    @Override
    public SchemaRule<JType, JType> getFormatRule() {
        return new FormatRule();
    }

    @Override
    public SchemaRule<JPackage, JType> getObjectRule() {
        return new ObjectRule(this);
    }

    @Override
    public SchemaRule<JDocCommentable, JDocComment> getOptionalRule() {
        return new OptionalRule();
    }

    @Override
    public SchemaRule<JDefinedClass, JDefinedClass> getPropertiesRule() {
        return new PropertiesRule(this);
    }

    @Override
    public SchemaRule<JDefinedClass, JDefinedClass> getPropertyRule() {
        return new PropertyRule(this);
    }

    @Override
    public SchemaRule<JDocCommentable, JDocComment> getRequiredRule() {
        return new RequiredRule();
    }

    @Override
    public SchemaRule<JClassContainer, JType> getTypeRule() {
        return new TypeRule(this);
    }

    @Override
    public SchemaRule<JDefinedClass, JDefinedClass> getAdditionalPropertiesRule() {
        return new AdditionalPropertiesRule(this);
    }

    @Override
    public SchemaRule<JDocCommentable, JDocComment> getTitleRule() {
        return new TitleRule();
    }

    @Override
    public SchemaRule<JClassContainer, JType> getSchemaRule() {
        return new JsonSchemaRule(this);
    }

    @Override
    public SchemaRule<JFieldVar, JFieldVar> getDefaultRule() {
        return new DefaultRule();
    }

    @Override
    public String getBehaviourProperty(String key) {
        return behaviourProperties.get(key);
    }

}
