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

import static java.util.Arrays.*;
import static org.apache.commons.lang.StringUtils.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.googlecode.jsonschema2pojo.Schema;
import com.googlecode.jsonschema2pojo.SchemaMapper;
import com.googlecode.jsonschema2pojo.exception.GenerationException;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

/**
 * Applies the "enum" schema rule.
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-02#section-5.17">http://tools.ietf.org/html/draft-zyp-json-schema-02#section-5.17</a>
 */
public class EnumRule implements SchemaRule<JClassContainer, JDefinedClass> {

    private static final String VALUE_FIELD_NAME = "value";

    private static final String ILLEGAL_CHARACTER_REGEX = "[^0-9a-zA-Z]";

    protected EnumRule() {
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * A Java {@link Enum} is created, with constants for each of the enum
     * values present in the schema. The enum name is derived from the nodeName,
     * and the enum type itself is created as an inner class of the owning type.
     * In the rare case that no owning type exists (the enum is the root of the
     * schema), then the enum becomes a public class in its own right.
     * <p>
     * The actual JSON value for each enum constant is held in a property called
     * "value" in the generated type. A static factory method
     * <code>fromValue(String)</code> is added to the generated enum, and the
     * methods are annotated to allow Jackson to marshal/unmarshal values
     * correctly.
     * 
     * @param nodeName
     *            the name of the property which is an "enum"
     * @param node
     *            the enum node
     * @param container
     *            the class container (class or package) to which this enum
     *            should be added
     * @return the newly generated Java type that was created to represent the
     *         given enum
     */
    @Override
    public JDefinedClass apply(String nodeName, JsonNode node, JClassContainer container, Schema schema) {

        JDefinedClass _enum = createEnum(nodeName, container);
        schema.setJavaTypeIfEmpty(_enum);

        addGeneratedAnnotation(_enum);

        JFieldVar valueField = addValueField(_enum);
        addToString(_enum, valueField);
        addEnumConstants(node, _enum);
        addFactoryMethod(node, _enum);

        return _enum;
    }

    private JDefinedClass createEnum(String nodeName, JClassContainer container) {

        int modifiers = container.isPackage() ? JMod.PUBLIC : JMod.PUBLIC | JMod.STATIC;

        String name = getEnumName(nodeName);

        try {
            return container._class(modifiers, name, ClassType.ENUM);
        } catch (JClassAlreadyExistsException e) {
            throw new GenerationException(e);
        }

    }

    private void addFactoryMethod(JsonNode node, JDefinedClass _enum) {
        JMethod fromValue = _enum.method(JMod.PUBLIC | JMod.STATIC, _enum, "fromValue");
        JVar valueParam = fromValue.param(String.class, "value");
        JBlock body = fromValue.body();

        JForEach forEach = body.forEach(_enum, "c", _enum.staticInvoke("values"));

        JInvocation invokeEquals = forEach.var().ref("value").invoke("equals");
        invokeEquals.arg(valueParam);

        forEach.body()._if(invokeEquals)._then()._return(forEach.var());

        JInvocation illegalArgumentException = JExpr._new(_enum.owner().ref(IllegalArgumentException.class));
        illegalArgumentException.arg(valueParam);
        body._throw(illegalArgumentException);

        fromValue.annotate(JsonCreator.class);
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

        toString.annotate(JsonValue.class);
        toString.annotate(Override.class);
    }

    private void addEnumConstants(JsonNode node, JDefinedClass _enum) {
        for (Iterator<JsonNode> values = node.elements(); values.hasNext();) {
            JsonNode value = values.next();

            if (!value.isNull()) {
                JEnumConstant constant = _enum.enumConstant(getConstantName(value.asText()));
                constant.arg(JExpr.lit(value.asText()));
            }
        }
    }

    private void addGeneratedAnnotation(JDefinedClass jclass) {
        JAnnotationUse generated = jclass.annotate(Generated.class);
        generated.param("value", SchemaMapper.class.getPackage().getName());
    }

    private String getEnumName(String nodeName) {
        return capitalize(nodeName).replaceAll(ILLEGAL_CHARACTER_REGEX, "_");
    }

    private String getConstantName(String nodeName) {
        List<String> enumNameGroups = new ArrayList<String>(asList(splitByCharacterTypeCamelCase(nodeName)));

        String enumName = "";
        for (Iterator<String> iter = enumNameGroups.iterator(); iter.hasNext();) {
            if (containsOnly(iter.next().replaceAll(ILLEGAL_CHARACTER_REGEX, "_"), "_")) {
                iter.remove();
            }
        }

        enumName = upperCase(join(enumNameGroups, "_"));

        if (isEmpty(enumName)) {
            enumName = "__EMPTY__";
        } else if (Character.isDigit(enumName.charAt(0))) {
            enumName = "_" + enumName;
        }

        return enumName;
    }

}
