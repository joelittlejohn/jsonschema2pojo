/**
 * Copyright © 2010 Nokia
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

package com.googlecode.jsonschema2pojo;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.googlecode.jsonschema2pojo.rules.AdditionalPropertiesRule;
import com.googlecode.jsonschema2pojo.rules.ArrayRule;
import com.googlecode.jsonschema2pojo.rules.DescriptionRule;
import com.googlecode.jsonschema2pojo.rules.EnumRule;
import com.googlecode.jsonschema2pojo.rules.FormatRule;
import com.googlecode.jsonschema2pojo.rules.ObjectRule;
import com.googlecode.jsonschema2pojo.rules.OptionalRule;
import com.googlecode.jsonschema2pojo.rules.PropertiesRule;
import com.googlecode.jsonschema2pojo.rules.PropertyRule;
import com.googlecode.jsonschema2pojo.rules.SchemaRule;
import com.googlecode.jsonschema2pojo.rules.TypeRule;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

public class SchemaMapperImpl implements SchemaMapper {

    private final Map<String, String> behaviourProperties;

    /**
     * Constructor.
     * 
     * @param behaviourProperties
     *            A map defining the behavioural properties of this mapper.
     */
    public SchemaMapperImpl(Map<String, String> behaviourProperties) {
        if (behaviourProperties == null) {
            this.behaviourProperties = new HashMap<String, String>();
        } else {
            this.behaviourProperties = behaviourProperties;
        }
    }

    @Override
    public void generate(JCodeModel codeModel, String className, String packageName, InputStream schemaContent) throws IOException {
        JsonNode schemaNode = readSchema(schemaContent);

        if (schemaNode.get("id") != null) {
            className = schemaNode.get("id").getTextValue();
        }

        JPackage jpackage = codeModel._package(packageName);

        if (schemaNode.get("enum") != null) {
            this.getEnumRule().apply(className, schemaNode.get("enum"), jpackage);
        } else {
            this.getTypeRule().apply(className, schemaNode, jpackage);
        }

    }

    private JsonNode readSchema(InputStream input) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readTree(input);
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
    public SchemaRule<JPackage, JType> getFormatRule() {
        return new FormatRule();
    }

    @Override
    public SchemaRule<JPackage, JDefinedClass> getObjectRule() {
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
    public SchemaRule<JPackage, JType> getTypeRule() {
        return new TypeRule(this);
    }

    @Override
    public SchemaRule<JDefinedClass, JDefinedClass> getAdditionalPropertiesRule() {
        return new AdditionalPropertiesRule(this);
    }

    @Override
    public String getBehaviourProperty(String key) {
        return behaviourProperties.get(key);
    }

}
