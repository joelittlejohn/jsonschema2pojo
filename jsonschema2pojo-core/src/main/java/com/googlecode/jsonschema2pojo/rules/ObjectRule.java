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

import static org.apache.commons.lang.StringUtils.*;

import java.io.Serializable;

import javax.annotation.Generated;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.JsonNode;

import com.googlecode.jsonschema2pojo.Schema;
import com.googlecode.jsonschema2pojo.SchemaMapper;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JVar;

/**
 * Applies the generation steps required for schemas of type "object".
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-02#section-5.1">http://tools.ietf.org/html/draft-zyp-json-schema-02#section-5.1</a>
 */
public class ObjectRule implements SchemaRule<JPackage, JDefinedClass> {

    private static final String ILLEGAL_CHARACTER_REGEX = "[^0-9a-zA-Z]";

    private final RuleFactory ruleFactory;

    protected ObjectRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * When this rule is applied for schemas of type object, the properties of
     * the schema are used to generate a new Java class and determine its
     * characteristics. See other implementers of {@link SchemaRule} for
     * details.
     * <p>
     * A new Java type will be created when this rule is applied, it is
     * annotated as {@link Generated}, it is given <code>equals</code>,
     * <code>hashCode</code> and <code>toString</code> methods and implements
     * {@link Serializable}.
     */
    @Override
    public JDefinedClass apply(String nodeName, JsonNode node, JPackage _package, Schema schema) {

        JDefinedClass jclass;
        try {
            if (node.has("javaType")) {
                jclass = _package.owner()._class(node.get("javaType").getTextValue());
            } else {
                jclass = _package._class(getClassName(nodeName));
            }
        } catch (JClassAlreadyExistsException e) {
            return e.getExistingClass();
        }

        schema.setJavaTypeIfEmpty(jclass);
        addGeneratedAnnotation(jclass);
        addSerializable(jclass);

        if (node.has("title")) {
            ruleFactory.getTitleRule().apply(nodeName, node.get("title"), jclass, schema);
        }

        if (node.has("description")) {
            ruleFactory.getDescriptionRule().apply(nodeName, node.get("description"), jclass, schema);
        }

        if (node.has("properties")) {
            ruleFactory.getPropertiesRule().apply(nodeName, node.get("properties"), jclass, schema);
        }

        addToString(jclass);
        addHashCode(jclass);
        addEquals(jclass);

        ruleFactory.getAdditionalPropertiesRule().apply(nodeName, node.get("additionalProperties"), jclass, schema);

        return jclass;

    }

    private void addSerializable(JDefinedClass jclass) {
        jclass._implements(Serializable.class);
    }

    private void addGeneratedAnnotation(JDefinedClass jclass) {
        JAnnotationUse generated = jclass.annotate(Generated.class);
        generated.param("value", SchemaMapper.class.getPackage().getName());
    }

    private void addToString(JDefinedClass jclass) {
        JMethod toString = jclass.method(JMod.PUBLIC, String.class, "toString");

        JBlock body = toString.body();
        JInvocation reflectionToString = jclass.owner().ref(ToStringBuilder.class).staticInvoke("reflectionToString");
        reflectionToString.arg(JExpr._this());
        body._return(reflectionToString);

        toString.annotate(Override.class);
    }

    private void addHashCode(JDefinedClass jclass) {
        JMethod hashCode = jclass.method(JMod.PUBLIC, int.class, "hashCode");

        JBlock body = hashCode.body();
        JInvocation reflectionHashCode = jclass.owner().ref(HashCodeBuilder.class).staticInvoke("reflectionHashCode");
        reflectionHashCode.arg(JExpr._this());
        body._return(reflectionHashCode);

        hashCode.annotate(Override.class);
    }

    private void addEquals(JDefinedClass jclass) {
        JMethod equals = jclass.method(JMod.PUBLIC, boolean.class, "equals");
        JVar otherObject = equals.param(Object.class, "other");

        JBlock body = equals.body();
        JInvocation reflectionEquals = jclass.owner().ref(EqualsBuilder.class).staticInvoke("reflectionEquals");
        reflectionEquals.arg(JExpr._this());
        reflectionEquals.arg(otherObject);
        body._return(reflectionEquals);

        equals.annotate(Override.class);
    }

    private String getClassName(String nodeName) {
        return capitalize(nodeName).replaceAll(ILLEGAL_CHARACTER_REGEX, "_");
    }

}
