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

package com.googlecode.jsonschema2pojo.rules;

import java.util.Date;

import org.codehaus.jackson.JsonNode;

import com.googlecode.jsonschema2pojo.SchemaMapper;
import com.googlecode.jsonschema2pojo.exception.GenerationException;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

/**
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-02#section-5.1">http://tools.ietf.org/html/draft-zyp-json-schema-02#section-5.1</a>
 */
public class TypeRule implements SchemaRule<JPackage, JType> {

    private final SchemaMapper mapper;

    public TypeRule(SchemaMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public JType apply(String nodeName, JsonNode node, JPackage generatableType) {
        String propertyTypeName = node.get("type").getTextValue();

        if (propertyTypeName.equals("string")) {

            if (node.get("format") != null && node.get("format").getTextValue().equals("date-time")) {
                return generatableType.owner().ref(Date.class);
            }

            return generatableType.owner().ref(String.class);
        } else if (propertyTypeName.equals("number")) {

            return generatableType.owner().DOUBLE;
        } else if (propertyTypeName.equals("integer")) {

            return generatableType.owner().INT;
        } else if (propertyTypeName.equals("boolean")) {

            return generatableType.owner().BOOLEAN;
        } else if (propertyTypeName.equals("object")) {

            return mapper.getObjectRule().apply(nodeName, node, generatableType.getPackage());
        } else if (propertyTypeName.equals("array")) {

            return mapper.getArrayRule().apply(nodeName, node, generatableType);
        } else if (propertyTypeName.equals("null")) {

            return generatableType.owner().ref(Object.class);
        } else if (propertyTypeName.equals("any")) {

            return generatableType.owner().ref(Object.class);
        } else {

            throw new GenerationException("Unrecognised property type: " + propertyTypeName);
        }
    }

}
