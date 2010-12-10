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

import static org.apache.commons.lang.StringUtils.*;

import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonCreator;

import com.googlecode.jsonschema2pojo.exception.GenerationException;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

/**
 * {@link http://tools.ietf.org/html/draft-zyp-json-schema-02#section-5.17}
 */
public class EnumRule implements SchemaRule<JDefinedClass, JDefinedClass> {

    private static final String VALUE_FIELD_NAME = "value";

    private static final String ILLEGAL_CHARACTER_REGEX = "[^0-9a-zA-Z]";

    @Override
    public JDefinedClass apply(String nodeName, JsonNode node, JDefinedClass generatableType) {
        try {
            JDefinedClass _enum = generatableType._enum(getEnumName(nodeName));

            JFieldVar valueField = addValueField(_enum);
            addToString(_enum, valueField);
            addEnumConstants(node, _enum);

            return _enum;
        } catch (JClassAlreadyExistsException e) {
            throw new GenerationException(e);
        }
    }

    private JFieldVar addValueField(JDefinedClass _enum) {
        JFieldVar valueField = _enum.field(JMod.PRIVATE | JMod.FINAL, String.class, VALUE_FIELD_NAME);

        JMethod constructor = _enum.constructor(JMod.PRIVATE);
        JVar valueParam = constructor.param(String.class, VALUE_FIELD_NAME);
        JBlock body = constructor.body();
        body.assign(JExpr._this().ref(valueField), valueParam);

        return valueField;
    }

    private void addToString(JDefinedClass _enum, JFieldVar valueField) {
        JMethod toString = _enum.method(JMod.PUBLIC, String.class, "toString");
        JBlock body = toString.body();

        body._return(JExpr._this().ref(valueField));

        toString.annotate(JsonCreator.class);
        toString.annotate(Override.class);
    }

    private void addEnumConstants(JsonNode node, JDefinedClass _enum) {
        for (Iterator<JsonNode> values = node.getElements(); values.hasNext();) {
            JsonNode value = values.next();
            JEnumConstant constant = _enum.enumConstant(getConstantName(value.getValueAsText()));
            constant.arg(JExpr.lit(value.getValueAsText()));
        }
    }

    private String getEnumName(String nodeName) {
        return capitalize(nodeName).replaceAll(ILLEGAL_CHARACTER_REGEX, "_");
    }

    private String getConstantName(String nodeName) {
        String enumName = upperCase(join(splitByCharacterTypeCamelCase(nodeName), "_"));

        if (Character.isDigit(enumName.charAt(0))) {
            enumName = "_" + enumName;
        }

        return enumName;
    }

}
