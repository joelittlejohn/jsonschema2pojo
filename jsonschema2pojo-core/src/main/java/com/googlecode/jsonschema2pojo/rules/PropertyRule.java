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

import static java.lang.Character.*;
import static javax.lang.model.SourceVersion.*;
import static org.apache.commons.lang.StringUtils.*;

import org.apache.commons.lang.WordUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.googlecode.jsonschema2pojo.Schema;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

/**
 * Applies the schema rules that represent a property definition.
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.2">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.2</a>
 */
public class PropertyRule implements SchemaRule<JDefinedClass, JDefinedClass> {

    private static final String ILLEGAL_CHARACTER_REGEX = "[^0-9a-zA-Z_$]";

    private final RuleFactory ruleFactory;

    protected PropertyRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * This rule adds a property to a given Java class according to the Java
     * Bean spec. A private field is added to the class, along with accompanying
     * accessor methods.
     * <p>
     * If this rule's schema mapper is configured to include builder methods
     * (see
     * {@link com.googlecode.jsonschema2pojo.GenerationConfig#isGenerateBuilders()}
     * ), then a builder method of the form <code>withFoo(Foo foo);</code> is
     * also added.
     * 
     * @param nodeName
     *            the name of the property to be applied
     * @param node
     *            the node describing the characteristics of this property
     * @param jclass
     *            the Java class which should have this property added
     * @return the given jclass
     */
    @Override
    public JDefinedClass apply(String nodeName, JsonNode node, JDefinedClass jclass, Schema schema) {

        String propertyName = getPropertyName(nodeName);

        JType propertyType = ruleFactory.getSchemaRule().apply(nodeName, node, jclass, schema);

        JFieldVar field = jclass.field(JMod.PRIVATE, propertyType, propertyName);

        ruleFactory.getAnnotator().propertyField(field, nodeName);

        JMethod getter = addGetter(jclass, field, nodeName);
        JMethod setter = addSetter(jclass, field, nodeName);

        if (ruleFactory.getGenerationConfig().isGenerateBuilders()) {
            addBuilder(jclass, field);
        }

        if (node.has("title")) {
            ruleFactory.getTitleRule().apply(nodeName, node.get("title"), field, schema);
            ruleFactory.getTitleRule().apply(nodeName, node.get("title"), getter, schema);
            ruleFactory.getTitleRule().apply(nodeName, node.get("title"), setter, schema);
        }

        if (node.has("description")) {
            ruleFactory.getDescriptionRule().apply(nodeName, node.get("description"), field, schema);
            ruleFactory.getDescriptionRule().apply(nodeName, node.get("description"), getter, schema);
            ruleFactory.getDescriptionRule().apply(nodeName, node.get("description"), setter, schema);
        }

        if (node.has("required")) {
            ruleFactory.getRequiredRule().apply(nodeName, node.get("required"), field, schema);
            ruleFactory.getRequiredRule().apply(nodeName, node.get("required"), getter, schema);
            ruleFactory.getRequiredRule().apply(nodeName, node.get("required"), setter, schema);
        }

        if (node.has("pattern")) {
            ruleFactory.getPatternRule().apply(nodeName, node.get("pattern"), field, schema);
        }

        ruleFactory.getDefaultRule().apply(nodeName, node.get("default"), field, schema);

        ruleFactory.getMinimumMaximumRule().apply(nodeName, node, field, schema);

        ruleFactory.getMinItemsMaxItemsRule().apply(nodeName, node, field, schema);

        return jclass;
    }

    private JMethod addGetter(JDefinedClass c, JFieldVar field, String jsonPropertyName) {
        JMethod getter = c.method(JMod.PUBLIC, field.type(), getGetterName(field.name(), field.type()));

        JBlock body = getter.body();
        body._return(field);

        ruleFactory.getAnnotator().propertyGetter(getter, jsonPropertyName);

        return getter;
    }

    private JMethod addSetter(JDefinedClass c, JFieldVar field, String jsonPropertyName) {
        JMethod setter = c.method(JMod.PUBLIC, void.class, getSetterName(field.name()));

        JVar param = setter.param(field.type(), field.name());
        JBlock body = setter.body();
        body.assign(JExpr._this().ref(field), param);

        ruleFactory.getAnnotator().propertySetter(setter, jsonPropertyName);

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

        if (isKeyword(nodeName)) {
            nodeName += "_";
        }

        char[] wordDelimiters = ruleFactory.getGenerationConfig().getPropertyWordDelimiters();

        if (containsAny(nodeName, wordDelimiters)) {
            nodeName = capitalizeTrailingWords(nodeName, wordDelimiters);
        }

        if (isDigit(nodeName.charAt(0))) {
            nodeName = "_" + nodeName;
        }

        return nodeName.replaceAll(ILLEGAL_CHARACTER_REGEX, "_");
    }

    private String capitalizeTrailingWords(String nodeName, char[] wordDelimiters) {
        String capitalizedNodeName = WordUtils.capitalize(nodeName, wordDelimiters);
        capitalizedNodeName = nodeName.charAt(0) + capitalizedNodeName.substring(1);

        for (char c : wordDelimiters) {
            capitalizedNodeName = remove(capitalizedNodeName, c);
        }

        return capitalizedNodeName;
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
