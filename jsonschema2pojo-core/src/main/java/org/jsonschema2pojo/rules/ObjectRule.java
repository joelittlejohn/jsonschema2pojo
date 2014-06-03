/**
 * Copyright © 2010-2013 Nokia
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

package org.jsonschema2pojo.rules;

import static org.jsonschema2pojo.rules.PrimitiveTypes.*;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.ArrayUtils.*;

import javax.annotation.Generated;

import com.fasterxml.jackson.databind.JsonNode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.sun.codemodel.JExpression;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.exception.ClassAlreadyExistsException;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

/**
 * Applies the generation steps required for schemas of type "object".
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.1">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.1</a>
 */
public class ObjectRule implements Rule<JPackage, JType> {

    private final RuleFactory ruleFactory;

    protected ObjectRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * When this rule is applied for schemas of type object, the properties of
     * the schema are used to generate a new Java class and determine its
     * characteristics. See other implementers of {@link Rule} for details.
     * <p>
     * A new Java type will be created when this rule is applied, it is
     * annotated as {@link Generated}, it is given <code>equals</code>,
     * <code>hashCode</code> and <code>toString</code> methods and implements
     * {@link Serializable}.
     */
    @Override
    public JType apply(String nodeName, JsonNode node, JPackage _package, Schema schema) {
        GenerationConfig config = ruleFactory.getGenerationConfig();

        JType superType = getSuperType(nodeName, node, _package, schema);

        if (superType.isPrimitive() || isFinal(superType)) {
            return superType;
        }

        JDefinedClass jclass;
        try {
            jclass = createClass(nodeName, node, _package);
        } catch (ClassAlreadyExistsException e) {
            return e.getExistingClass();
        }

        jclass._extends((JClass) superType);

        schema.setJavaTypeIfEmpty(jclass);
        addGeneratedAnnotation(jclass);

        if (node.has("title")) {
            ruleFactory.getTitleRule().apply(nodeName, node.get("title"), jclass, schema);
        }

        if (node.has("description")) {
            ruleFactory.getDescriptionRule().apply(nodeName, node.get("description"), jclass, schema);
        }

        JDefinedClass builderClass = null;
        if (config.isGenerateBuilderClasses()) {
            try {
                builderClass = generateExternalBuilder(jclass);
            } catch (JClassAlreadyExistsException e) {
                builderClass = e.getExistingClass();
            }
            ruleFactory.getAnnotator().objectBuilder(jclass, builderClass);
        }

        if (node.has("properties")) {
            ruleFactory.getPropertiesRule().apply(nodeName, node.get("properties"), jclass, schema);
        }

        if (config.isIncludeToString()) {
            addToString(jclass);
        }

        if (config.isIncludeHashcodeAndEquals()) {
            addHashCode(jclass);
            addEquals(jclass);
        }

        if (node.has("javaInterfaces")) {
            addInterfaces(jclass, node.get("javaInterfaces"));
        }

        ruleFactory.getAdditionalPropertiesRule().apply(nodeName, node.get("additionalProperties"), jclass, schema);

        if (config.isGenerateBuilderClasses()) {
            addConstructor(jclass, builderClass);
        }

        return jclass;

    }

    /**
     * Creates a constructor for the given generated class.
     *
     * The constructor will have one argument for every property in the class.  Defensive immutable copies will be made
     * of any {@link java.util.Collection} arguments.
     */
    private void addConstructor(JDefinedClass jclass, JDefinedClass builderClass) {
        JMethod constructor = jclass.constructor(JMod.PRIVATE);
        JVar builder = constructor.param(builderClass, "builder");
        JBlock body = constructor.body();
        for (Map.Entry<String, JFieldVar> e : jclass.fields().entrySet()) {
            JExpression src = JExpr.ref(builder, e.getKey());
            if (ruleFactory.getGenerationConfig().isImmutable()) {
                body.assign(JExpr._this().ref(e.getKey()), immutableCopy(src, e.getValue().type()));
            } else {
                body.assign(JExpr._this().ref(e.getKey()), src);
            }
        }
    }

    private static JExpression immutableCopy(JExpression expr, JType type) {
        JClass immutableClass;
        String typeName = type.erasure().fullName();
        if (typeName.equals("java.util.List")) {
            immutableClass = type.owner().ref(ImmutableList.class);
        } else if (typeName.equals("java.util.Set")) {
            immutableClass = type.owner().ref(ImmutableSet.class);
        } else if (typeName.equals("java.util.Map")) {
            immutableClass = type.owner().ref(ImmutableMap.class);
        } else {
            return expr;
        }
        return immutableClass.staticInvoke("copyOf").arg(expr);
    }

    /**
     * Creates a static inner Builder class for the given generated class.
     */
    private JDefinedClass generateExternalBuilder(JDefinedClass jclass) throws JClassAlreadyExistsException {
        JDefinedClass builderClass = jclass._class(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, "Builder");

        builderClass.constructor(JMod.PRIVATE);

        JMethod buildMethod = builderClass.method(JMod.PUBLIC, jclass, "build");
        buildMethod.body()._return(JExpr._new(jclass).arg(JExpr._this()));

        JMethod newBuilderMethod = jclass.method(JMod.PUBLIC | JMod.STATIC, builderClass, "newBuilder");
        newBuilderMethod.body()._return(JExpr._new(builderClass));

        return builderClass;
    }

    /**
     * Creates a new Java class that will be generated.
     * 
     * @param nodeName
     *            the node name which may be used to dictate the new class name
     * @param node
     *            the node representing the schema that caused the need for a
     *            new class. This node may include a 'javaType' property which
     *            if present will override the fully qualified name of the newly
     *            generated class.
     * @param _package
     *            the package which may contain a new class after this method
     *            call
     * @return a reference to a newly created class
     * @throws ClassAlreadyExistsException
     *             if the given arguments cause an attempt to create a class
     *             that already exists, either on the classpath or in the
     *             current map of classes to be generated.
     */
    private JDefinedClass createClass(String nodeName, JsonNode node, JPackage _package) throws ClassAlreadyExistsException {

        JDefinedClass newType;

        try {
            if (node.has("javaType")) {
                String fqn = substringBefore(node.get("javaType").asText(), "<");
                String[] genericArguments = split(substringBetween(node.get("javaType").asText(), "<", ">"), ",");

                if (isPrimitive(fqn, _package.owner())) {
                    throw new ClassAlreadyExistsException(primitiveType(fqn, _package.owner()));
                }

                try {
                    JClass existingClass = _package.owner().ref(Thread.currentThread().getContextClassLoader().loadClass(fqn));
                    
                    if (isNotEmpty(genericArguments)) {
                        existingClass = addGenericArguments(_package, existingClass, genericArguments);
                    }
                            
                    throw new ClassAlreadyExistsException(existingClass);
                } catch (ClassNotFoundException e) {
                    newType = _package.owner()._class(fqn);
                }
            } else {
                newType = _package._class(getClassName(nodeName, _package));
            }
        } catch (JClassAlreadyExistsException e) {
            throw new ClassAlreadyExistsException(e.getExistingClass());
        }

        ruleFactory.getAnnotator().propertyInclusion(newType, node);

        return newType;

    }

    private JClass addGenericArguments(JPackage _package, JClass existingClass, String[] genericArgumentClassNames) {
        JClass[] genericArgumentClasses = new JClass[genericArgumentClassNames.length];
        for (int i=0; i<genericArgumentClasses.length; i++) {
            genericArgumentClasses[i] = _package.owner().ref(genericArgumentClassNames[i]);
        }
        
        return existingClass.narrow(genericArgumentClasses);
    }

    private boolean isFinal(JType superType) {
        try {
            Class<?> javaClass = Class.forName(superType.fullName());
            return Modifier.isFinal(javaClass.getModifiers());
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private JType getSuperType(String nodeName, JsonNode node, JClassContainer jClassContainer, Schema schema) {
        JType superType = jClassContainer.owner().ref(Object.class);
        if (node.has("extends")) {
            superType = ruleFactory.getSchemaRule().apply(nodeName + "Parent", node.get("extends"), jClassContainer, schema);
        }
        return superType;
    }

    private void addGeneratedAnnotation(JDefinedClass jclass) {
        JAnnotationUse generated = jclass.annotate(Generated.class);
        generated.param("value", SchemaMapper.class.getPackage().getName());
    }

    private void addToString(JDefinedClass jclass) {
        JMethod toString = jclass.method(JMod.PUBLIC, String.class, "toString");

        Class<?> toStringBuilder = ruleFactory.getGenerationConfig().isUseCommonsLang3() ?
                org.apache.commons.lang3.builder.ToStringBuilder.class : 
                    org.apache.commons.lang.builder.ToStringBuilder.class;
        
        JBlock body = toString.body();
        JInvocation reflectionToString = jclass.owner().ref(toStringBuilder).staticInvoke("reflectionToString");
        reflectionToString.arg(JExpr._this());
        body._return(reflectionToString);

        toString.annotate(Override.class);
    }

    private void addHashCode(JDefinedClass jclass) {
        JMethod hashCode = jclass.method(JMod.PUBLIC, int.class, "hashCode");

        Class<?> hashcodeBuiler = ruleFactory.getGenerationConfig().isUseCommonsLang3() ?
                org.apache.commons.lang3.builder.HashCodeBuilder.class : 
                    org.apache.commons.lang.builder.HashCodeBuilder.class;
        
        JBlock body = hashCode.body();
        JInvocation reflectionHashCode = jclass.owner().ref(hashcodeBuiler).staticInvoke("reflectionHashCode");
        reflectionHashCode.arg(JExpr._this());
        body._return(reflectionHashCode);

        hashCode.annotate(Override.class);
    }

    private void addEquals(JDefinedClass jclass) {
        JMethod equals = jclass.method(JMod.PUBLIC, boolean.class, "equals");
        JVar otherObject = equals.param(Object.class, "other");

        Class<?> equalsBuilder = ruleFactory.getGenerationConfig().isUseCommonsLang3() ?
                org.apache.commons.lang3.builder.EqualsBuilder.class : 
                    org.apache.commons.lang.builder.EqualsBuilder.class;

        JBlock body = equals.body();
        JInvocation reflectionEquals = jclass.owner().ref(equalsBuilder).staticInvoke("reflectionEquals");
        reflectionEquals.arg(JExpr._this());
        reflectionEquals.arg(otherObject);
        body._return(reflectionEquals);

        equals.annotate(Override.class);
    }

    private void addInterfaces(JDefinedClass jclass, JsonNode javaInterfaces) {
        for (JsonNode i : javaInterfaces) {
            jclass._implements(jclass.owner().ref(i.asText()));
        }
    }

    private String getClassName(String nodeName, JPackage _package) {
        String className = ruleFactory.getNameHelper().replaceIllegalCharacters(nodeName);
        String normalizedName = ruleFactory.getNameHelper().normalizeName(className, true);
        return makeUnique(normalizedName, _package);
    }

    private String makeUnique(String className, JPackage _package) {
        try {
            JDefinedClass _class = _package._class(className);
            _package.remove(_class);
            return className;
        } catch (JClassAlreadyExistsException e) {
            return makeUnique(className + "_", _package);
        }
    }

}
