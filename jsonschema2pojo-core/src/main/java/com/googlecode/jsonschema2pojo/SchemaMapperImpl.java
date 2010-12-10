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

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.googlecode.jsonschema2pojo.rules.ArrayRule;
import com.googlecode.jsonschema2pojo.rules.DescriptionRule;
import com.googlecode.jsonschema2pojo.rules.EnumRule;
import com.googlecode.jsonschema2pojo.rules.ObjectRule;
import com.googlecode.jsonschema2pojo.rules.OptionalRule;
import com.googlecode.jsonschema2pojo.rules.PropertiesRule;
import com.googlecode.jsonschema2pojo.rules.PropertyRule;
import com.googlecode.jsonschema2pojo.rules.TypeRule;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;

public class SchemaMapperImpl implements SchemaMapper {

    @Override
    public void generate(JCodeModel codeModel, String className, String packageName, InputStream schemaContent) throws IOException {
        JsonNode schemaNode = readSchema(schemaContent);

        if (schemaNode.get("id") != null) {
            className = schemaNode.get("id").getTextValue();
        }

        JPackage jpackage = codeModel._package(packageName);

        this.getObjectRule().apply(className, schemaNode, jpackage);
    }

    private JsonNode readSchema(InputStream input) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readTree(input);
    }

    @Override
    public ArrayRule getArrayRule() {
        return new ArrayRule(this);
    }

    @Override
    public DescriptionRule getDescriptionRule() {
        return new DescriptionRule();
    }

    @Override
    public EnumRule getEnumRule() {
        return new EnumRule();
    }

    @Override
    public ObjectRule getObjectRule() {
        return new ObjectRule(this);
    }

    @Override
    public OptionalRule getOptionalRule() {
        return new OptionalRule();
    }

    @Override
    public PropertiesRule getPropertiesRule() {
        return new PropertiesRule(this);
    }

    @Override
    public PropertyRule getPropertyRule() {
        return new PropertyRule(this);
    }

    @Override
    public TypeRule getTypeRule() {
        return new TypeRule(this);
    }

}
