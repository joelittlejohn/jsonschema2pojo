/**
 * Copyright © 2010-2014 Nokia
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

import android.os.Parcelable;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.*;
import org.jsonschema2pojo.AnnotationStyle;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.exception.ClassAlreadyExistsException;
import org.jsonschema2pojo.util.NameHelper;
import org.jsonschema2pojo.util.ParcelableHelper;
import org.jsonschema2pojo.util.TypeUtil;

import javax.annotation.Generated;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.*;
import static org.jsonschema2pojo.rules.PrimitiveTypes.isPrimitive;
import static org.jsonschema2pojo.rules.PrimitiveTypes.primitiveType;
import static org.jsonschema2pojo.util.TypeUtil.resolveType;

/**
 * Applies the generation steps required for schemas of type "object".
 *
 * @see <a href=
 *      "http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.1">http:/
 *      /tools.ietf.org/html/draft-zyp-json-schema-03#section-5.1</a>
 */
public class ObjectRule implements Rule<JPackage, JType> {

    private final RuleFactory ruleFactory;
    private final ParcelableHelper parcelableHelper;

    protected ObjectRule(RuleFactory ruleFactory, ParcelableHelper parcelableHelper) {
        this.ruleFactory = ruleFactory;
        this.parcelableHelper = parcelableHelper;
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
        if (node.has("deserializationClassProperty")) {
            addJsonTypeInfoAnnotation(jclass, node);
        }

        if (node.has("title")) {
            ruleFactory.getTitleRule().apply(nodeName, node.get("title"), jclass, schema);
        }

        if (node.has("description")) {
            ruleFactory.getDescriptionRule().apply(nodeName, node.get("description"), jclass, schema);
        }

        if (node.has("properties")) {
            ruleFactory.getPropertiesRule().apply(nodeName, node.get("properties"), jclass, schema);
        }

        if (ruleFactory.getGenerationConfig().isIncludeToString()) {
            addToString(jclass);
        }

        if (node.has("javaInterfaces")) {
            addInterfaces(jclass, node.get("javaInterfaces"));
        }

        ruleFactory.getAdditionalPropertiesRule().apply(nodeName, node.get("additionalProperties"), jclass, schema);

        if (node.has("required")) {
            ruleFactory.getRequiredArrayRule().apply(nodeName, node.get("required"), jclass, schema);
        }

        if (ruleFactory.getGenerationConfig().isIncludeHashcodeAndEquals()) {
            addHashCode(jclass);
            addEquals(jclass);
        }

        if (ruleFactory.getGenerationConfig().isParcelable()) {
            addParcelSupport(jclass);
        }

        if (ruleFactory.getGenerationConfig().isIncludeConstructors()) {
            addConstructors(jclass, getConstructorProperties(node, ruleFactory.getGenerationConfig().isConstructorsRequiredPropertiesOnly()));
        }

        return jclass;

    }

    private void addParcelSupport(JDefinedClass jclass) {
        jclass._implements(Parcelable.class);

        parcelableHelper.addWriteToParcel(jclass);
        parcelableHelper.addDescribeContents(jclass);
        parcelableHelper.addCreator(jclass);
    }

    /**
     * Retrieve the list of properties to go in the constructor from node. This
     * is all properties listed in node["properties"] if ! onlyRequired, and
     * only required properties if onlyRequired.
     * 
     * @param node
     * @return
     */
    private List<String> getConstructorProperties(JsonNode node, boolean onlyRequired) {

        if (!node.has("properties")) {
            return new ArrayList<String>();
        }

        List<String> rtn = new ArrayList<String>();

        NameHelper nameHelper = ruleFactory.getNameHelper();
        for (Iterator<Map.Entry<String, JsonNode>> properties = node.get("properties").fields(); properties.hasNext();) {
            Map.Entry<String, JsonNode> property = properties.next();

            JsonNode propertyObj = property.getValue();
            if (onlyRequired) {
                if (propertyObj.has("required") && propertyObj.get("required").asBoolean()) {
                    rtn.add(nameHelper.getPropertyName(property.getKey()));
                }
            } else {
                rtn.add((nameHelper.getPropertyName(property.getKey())));
            }
        }
        return rtn;
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
            boolean usePolymorphicDeserialization = usesPolymorphicDeserialization(node);
            if (node.has("javaType")) {
                String fqn = substringBefore(node.get("javaType").asText(), "<");

                if (isPrimitive(fqn, _package.owner())) {
                    throw new ClassAlreadyExistsException(primitiveType(fqn, _package.owner()));
                }

                int index = fqn.lastIndexOf(".") + 1;
                if (index >= 0 && index < fqn.length()) {
                    fqn = fqn.substring(0, index) + ruleFactory.getGenerationConfig().getClassNamePrefix() + fqn.substring(index) + ruleFactory.getGenerationConfig().getClassNameSuffix();
                }

                try {
                    _package.owner().ref(Thread.currentThread().getContextClassLoader().loadClass(fqn));
                    JClass existingClass = TypeUtil.resolveType(_package, fqn + (node.get("javaType").asText().contains("<") ? "<" + substringAfter(node.get("javaType").asText(), "<") : ""));

                    throw new ClassAlreadyExistsException(existingClass);
                } catch (ClassNotFoundException e) {
                    if (usePolymorphicDeserialization) {
                        newType = _package.owner()._class(JMod.PUBLIC, fqn, ClassType.CLASS);
                    } else {
                        newType = _package.owner()._class(fqn);
                    }
                }
            } else {
                if (usePolymorphicDeserialization) {
                    newType = _package._class(JMod.PUBLIC, getClassName(nodeName, _package), ClassType.CLASS);
                } else {
                    newType = _package._class(getClassName(nodeName, _package));
                }
            }
        } catch (JClassAlreadyExistsException e) {
            throw new ClassAlreadyExistsException(e.getExistingClass());
        }

        ruleFactory.getAnnotator().propertyInclusion(newType, node);

        return newType;

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

    private void addJsonTypeInfoAnnotation(JDefinedClass jclass, JsonNode node) {
        if (this.ruleFactory.getGenerationConfig().getAnnotationStyle() == AnnotationStyle.JACKSON2) {
            JsonNode nodeAnnotation = node.get("deserializationClassProperty");
            if (!nodeAnnotation.has("externalProperty") || !nodeAnnotation.get("externalProperty").booleanValue()) {
                JAnnotationUse jsonTypeInfo = jclass.annotate(JsonTypeInfo.class);
                jsonTypeInfo.param("use", JsonTypeInfo.Id.NAME);
                jsonTypeInfo.param("include", JsonTypeInfo.As.PROPERTY);
                jsonTypeInfo.param("property", nodeAnnotation.get("propertyName").textValue());
                jsonTypeInfo.param("visible", true);
                JAnnotationUse jsonSubTypes = jclass.annotate(JsonSubTypes.class);
                JAnnotationArrayMember jsonSubTypesValues = jsonSubTypes.paramArray("value");
                Iterator<JsonNode> subClasses = nodeAnnotation.get("children").iterator();

                while (subClasses.hasNext()) {
                    JsonNode childAnnotationData = subClasses.next();
                    String subClass = childAnnotationData.get("className").asText();
                    String value = childAnnotationData.get("value").asText();
                    addJsonSubtypeAnnotation(jclass, jsonSubTypesValues, subClass, value);
                }
            }
        }
    }

    private void addJsonSubtypeAnnotation(JDefinedClass jclass, JAnnotationArrayMember jsonSubTypesValues, String subClass, String value) {
        JAnnotationUse jsonSubType = jsonSubTypesValues.annotate(JsonSubTypes.Type.class);
        jsonSubType.param("value", jclass.owner().ref(subClass));
        jsonSubType.param("name", value);
    }

    private void addToString(JDefinedClass jclass) {
        JMethod toString = jclass.method(JMod.PUBLIC, String.class, "toString");

        Class<?> toStringBuilder = ruleFactory.getGenerationConfig().isUseCommonsLang3() ? org.apache.commons.lang3.builder.ToStringBuilder.class : org.apache.commons.lang.builder.ToStringBuilder.class;

        JBlock body = toString.body();
        JInvocation reflectionToString = jclass.owner().ref(toStringBuilder).staticInvoke("reflectionToString");
        reflectionToString.arg(JExpr._this());
        body._return(reflectionToString);

        toString.annotate(Override.class);
    }

    private void addHashCode(JDefinedClass jclass) {
        Map<String, JFieldVar> fields = jclass.fields();
        if (fields.isEmpty()) {
            return;
        }

        JMethod hashCode = jclass.method(JMod.PUBLIC, int.class, "hashCode");

        Class<?> hashCodeBuilder = ruleFactory.getGenerationConfig().isUseCommonsLang3() ? org.apache.commons.lang3.builder.HashCodeBuilder.class : org.apache.commons.lang.builder.HashCodeBuilder.class;

        JBlock body = hashCode.body();
        JClass hashCodeBuilderClass = jclass.owner().ref(hashCodeBuilder);
        JInvocation hashCodeBuilderInvocation = JExpr._new(hashCodeBuilderClass);

        if (!jclass._extends().name().equals("Object")) {
            hashCodeBuilderInvocation = hashCodeBuilderInvocation.invoke("appendSuper").arg(JExpr._super().invoke("hashCode"));
        }

        for (JFieldVar fieldVar : fields.values()) {
            hashCodeBuilderInvocation = hashCodeBuilderInvocation.invoke("append").arg(fieldVar);
        }

        body._return(hashCodeBuilderInvocation.invoke("toHashCode"));

        hashCode.annotate(Override.class);
    }

    private void addConstructors(JDefinedClass jclass, List<String> properties) {

        // no properties to put in the constructor => default constructor is good enough.
        if (properties.isEmpty()) {
            return;
        }

        // add a no-args constructor for serialization purposes
        JMethod noargsConstructor = jclass.constructor(JMod.PUBLIC);
        noargsConstructor.javadoc().add("No args constructor for use in serialization");

        // add the public constructor with property parameters
        JMethod fieldsConstructor = jclass.constructor(JMod.PUBLIC);
        JBlock constructorBody = fieldsConstructor.body();

        Map<String, JFieldVar> fields = jclass.fields();

        for (String property : properties) {
            JFieldVar field = fields.get(property);

            if (field == null) {
                throw new IllegalStateException("Property " + property + " hasn't been added to JDefinedClass before calling addConstructors");
            }

            fieldsConstructor.javadoc().addParam(property);
            JVar param = fieldsConstructor.param(field.type(), field.name());
            constructorBody.assign(JExpr._this().ref(field), param);
        }
    }

    private void addEquals(JDefinedClass jclass) {
        Map<String, JFieldVar> fields = jclass.fields();
        if (fields.isEmpty()) {
            return;
        }

        JMethod equals = jclass.method(JMod.PUBLIC, boolean.class, "equals");
        JVar otherObject = equals.param(Object.class, "other");

        Class<?> equalsBuilder = ruleFactory.getGenerationConfig().isUseCommonsLang3() ? org.apache.commons.lang3.builder.EqualsBuilder.class : org.apache.commons.lang.builder.EqualsBuilder.class;

        JBlock body = equals.body();

        body._if(otherObject.eq(JExpr._this()))._then()._return(JExpr.TRUE);
        body._if(otherObject._instanceof(jclass).eq(JExpr.FALSE))._then()._return(JExpr.FALSE);

        JVar rhsVar = body.decl(jclass, "rhs").init(JExpr.cast(jclass, otherObject));
        JClass equalsBuilderClass = jclass.owner().ref(equalsBuilder);
        JInvocation equalsBuilderInvocation = JExpr._new(equalsBuilderClass);

        if (!jclass._extends().name().equals("Object")) {
            equalsBuilderInvocation = equalsBuilderInvocation.invoke("appendSuper").arg(JExpr._super().invoke("equals").arg(otherObject));
        }

        for (JFieldVar fieldVar : fields.values()) {
            equalsBuilderInvocation = equalsBuilderInvocation.invoke("append").arg(fieldVar).arg(rhsVar.ref(fieldVar.name()));
        }

        JInvocation reflectionEquals = jclass.owner().ref(equalsBuilder).staticInvoke("reflectionEquals");
        reflectionEquals.arg(JExpr._this());
        reflectionEquals.arg(otherObject);

        body._return(equalsBuilderInvocation.invoke("isEquals"));

        equals.annotate(Override.class);
    }

    private void addInterfaces(JDefinedClass jclass, JsonNode javaInterfaces) {
        for (JsonNode i : javaInterfaces) {
            jclass._implements(resolveType(jclass._package(), i.asText()));
        }
    }

    private String getClassName(String nodeName, JPackage _package) {
        String className = ruleFactory.getNameHelper().replaceIllegalCharacters(capitalize(nodeName));
        String normalizedName = ruleFactory.getNameHelper().normalizeName(className);
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

    private boolean usesPolymorphicDeserialization(JsonNode node) {
        if (ruleFactory.getGenerationConfig().getAnnotationStyle() == AnnotationStyle.JACKSON2) {
            return node.has("deserializationClassProperty");
        }
        return false;
    }

}
