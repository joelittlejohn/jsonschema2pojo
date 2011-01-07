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

import org.codehaus.jackson.JsonNode;

import com.googlecode.jsonschema2pojo.SchemaMapper;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

/**
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.2">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.2</a>
 */
public class PropertyRule implements SchemaRule<JDefinedClass, JDefinedClass> {

    private static final String ILLEGAL_CHARACTER_REGEX = "[^0-9a-zA-Z]";

    private final SchemaMapper mapper;

    public PropertyRule(SchemaMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public JDefinedClass apply(String nodeName, JsonNode node, JDefinedClass c) {

        String propertyName = getPropertyName(nodeName);

        JType propertyType;
        if (node.get("enum") != null) {
            propertyType = mapper.getEnumRule().apply(nodeName, node.get("enum"), c);
        } else {
            propertyType = mapper.getTypeRule().apply(nodeName, node, c.getPackage());
        }

        JFieldVar field = c.field(JMod.PRIVATE, propertyType, propertyName);

        JMethod getter = addGetter(c, field);
        addSetter(c, field);

        boolean shouldAddBuilders = Boolean.parseBoolean(
                mapper.getBehaviourProperty(SchemaMapper.GENERATE_BUILDERS_PROPERTY));

        if (shouldAddBuilders) {
            addBuilder(c, field);
        }

        if (node.get("description") != null) {
            mapper.getDescriptionRule().apply(nodeName, node.get("description"), field);
            mapper.getDescriptionRule().apply(nodeName, node.get("description"), getter);
        }

        if (node.get("optional") != null) {
            mapper.getOptionalRule().apply(nodeName, node.get("optional"), getter);
        }

        return c;
    }

    private JMethod addGetter(JDefinedClass c, JFieldVar field) {
        JMethod getter = c.method(JMod.PUBLIC, field.type(), getGetterName(field.name(), field.type()));

        JBlock body = getter.body();
        body._return(field);

        return getter;
    }

    private JMethod addSetter(JDefinedClass c, JFieldVar field) {
        JMethod setter = c.method(JMod.PUBLIC, void.class, getSetterName(field.name()));

        JVar param = setter.param(field.type(), field.name());
        JBlock body = setter.body();
        body.assign(JExpr._this().ref(field), param);

        return setter;
    }

    private JMethod addBuilder(JDefinedClass c, JFieldVar field) {
        JMethod builder = c.method(JMod.PUBLIC, c, getBuilderName(field.name()));

        JVar param = builder.param(field.type(), field.name());
        JBlock body = builder.body();
        body.assign(JExpr._this().ref(field), param);
        body._return(JExpr._this());

        return builder;
    }

    private String getPropertyName(String nodeName) {
        return nodeName.replaceAll(ILLEGAL_CHARACTER_REGEX, "_");
    }

    private String getSetterName(String propertyName) {
        return "set" + capitalize(propertyName);
    }

    private String getBuilderName(String propertyName) {
        return "with" + capitalize(propertyName);
    }

    private String getGetterName(String propertyName, JType type) {
        String prefix = (type.equals(type.owner()._ref(boolean.class))) ? "is" : "get";
        return prefix + capitalize(propertyName);
    }

}
