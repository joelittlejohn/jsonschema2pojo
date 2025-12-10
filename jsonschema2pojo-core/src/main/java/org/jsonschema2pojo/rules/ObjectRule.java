/**
 * Copyright © 2010-2020 Nokia
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

import static org.apache.commons.lang3.StringUtils.*;
import static org.jsonschema2pojo.rules.PrimitiveTypes.*;
import static org.jsonschema2pojo.util.TypeUtil.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jsonschema2pojo.AnnotationStyle;
import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.exception.ClassAlreadyExistsException;
import org.jsonschema2pojo.exception.GenerationException;
import org.jsonschema2pojo.util.AnnotationHelper;
import org.jsonschema2pojo.util.ParcelableHelper;
import org.jsonschema2pojo.util.ReflectionHelper;
import org.jsonschema2pojo.util.SerializableHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

/**
 * Applies the generation steps required for schemas of type "object".
 *
 * @see <a href=
 *      "http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.1">http:/
 *      /tools.ietf.org/html/draft-zyp-json-schema-03#section-5.1</a>
 */
public class ObjectRule implements Rule<JPackage, JType> {

    private final RuleFactory ruleFactory;
    private final ReflectionHelper reflectionHelper;
    private final ParcelableHelper parcelableHelper;

    protected ObjectRule(RuleFactory ruleFactory, ParcelableHelper parcelableHelper, ReflectionHelper reflectionHelper) {
        this.ruleFactory = ruleFactory;
        this.parcelableHelper = parcelableHelper;
        this.reflectionHelper = reflectionHelper;
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * When this rule is applied for schemas of type object, the properties of
     * the schema are used to generate a new Java class and determine its
     * characteristics. See other implementers of {@link Rule} for details.
     */
    @Override
    public JType apply(String nodeName, JsonNode node, JsonNode parent, JPackage _package, Schema schema) {

        JType superType = reflectionHelper.getSuperType(nodeName, node, _package, schema);
        if (superType.isPrimitive() || reflectionHelper.isFinal(superType)) {
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

        if (node.has("title")) {
            ruleFactory.getTitleRule().apply(nodeName, node.get("title"), node, jclass, schema);
        }

        if (node.has("description")) {
            ruleFactory.getDescriptionRule().apply(nodeName, node.get("description"), node, jclass, schema);
        }

        if (node.has("$comment")) {
            ruleFactory.getCommentRule().apply(nodeName, node.get("$comment"), node, jclass, schema);
        }

        // Creates the class definition for the builder
        if(ruleFactory.getGenerationConfig().isGenerateBuilders() && ruleFactory.getGenerationConfig().isUseInnerClassBuilders()){
            ruleFactory.getBuilderRule().apply(nodeName, node, parent, jclass, schema);
        }

        ruleFactory.getPropertiesRule().apply(nodeName, node.get("properties"), node, jclass, schema);

        if (node.has("javaInterfaces")) {
            addInterfaces(jclass, node.get("javaInterfaces"));
        }

        ruleFactory.getAdditionalPropertiesRule().apply(nodeName, node.get("additionalProperties"), node, jclass, schema);

        ruleFactory.getDynamicPropertiesRule().apply(nodeName, node.get("properties"), node, jclass, schema);

        if (node.has("required")) {
            ruleFactory.getRequiredArrayRule().apply(nodeName, node.get("required"), node, jclass, schema);
        }
       
        if (ruleFactory.getGenerationConfig().isIncludeGeneratedAnnotation()) {
            AnnotationHelper.addGeneratedAnnotation(ruleFactory.getGenerationConfig(), jclass);
        }
        if (ruleFactory.getGenerationConfig().isIncludeToString()) {
            addToString(jclass);
        }

        if (ruleFactory.getGenerationConfig().isIncludeHashcodeAndEquals()) {
            addHashCode(jclass, node);
            addEquals(jclass, node);
        }

        if (ruleFactory.getGenerationConfig().isParcelable()) {
            addParcelSupport(jclass);
        }

        if (ruleFactory.getGenerationConfig().isIncludeConstructors()) {
            ruleFactory.getConstructorRule().apply(nodeName, node, parent, jclass, schema);

        }

        if (ruleFactory.getGenerationConfig().isSerializable()) {
            SerializableHelper.addSerializableSupport(jclass);
        }

        return jclass;

    }

    private void addParcelSupport(JDefinedClass jclass) {
        jclass._implements(jclass.owner().directClass("android.os.Parcelable"));

        parcelableHelper.addWriteToParcel(jclass);
        parcelableHelper.addDescribeContents(jclass);
        parcelableHelper.addCreator(jclass);
        parcelableHelper.addConstructorFromParcel(jclass);
        // #742 : includeConstructors will include the default constructor
        if (!ruleFactory.getGenerationConfig().isIncludeConstructors()) {
            // Add empty constructor
            jclass.constructor(JMod.PUBLIC);
        }
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

        Annotator annotator = ruleFactory.getAnnotator();

        try {
            if (node.has("existingJavaType")) {
                String fqn = substringBefore(node.get("existingJavaType").asText(), "<");

                if (isPrimitive(fqn, _package.owner())) {
                    throw new ClassAlreadyExistsException(primitiveType(fqn, _package.owner()));
                }

                JClass existingClass = resolveType(_package, fqn + (node.get("existingJavaType").asText().contains("<") ? "<" + substringAfter(node.get("existingJavaType").asText(), "<") : ""));
                throw new ClassAlreadyExistsException(existingClass);
            }

            boolean usePolymorphicDeserialization = annotator.isPolymorphicDeserializationSupported(node);

            if (node.has("javaType")) {
                String fqn = node.path("javaType").asText();

                if (isPrimitive(fqn, _package.owner())) {
                    throw new GenerationException("javaType cannot refer to a primitive type (" + fqn + "), did you mean to use existingJavaType?");
                }

                if (fqn.contains("<")) {
                    throw new GenerationException("javaType does not support generic args (" + fqn + "), did you mean to use existingJavaType?");
                }

                int index = fqn.lastIndexOf(".") + 1;
                if (index == 0) { //Actually not a fully qualified name
                    fqn = _package.name() + "." + fqn;
                    index = fqn.lastIndexOf(".") + 1;
                }

                if (index >= 0 && index < fqn.length()) {
                    fqn = fqn.substring(0, index) + ruleFactory.getGenerationConfig().getClassNamePrefix() + fqn.substring(index) + ruleFactory.getGenerationConfig().getClassNameSuffix();
                }

                if (usePolymorphicDeserialization) {
                    newType = _package.owner()._class(JMod.PUBLIC, fqn, ClassType.CLASS);
                } else {
                    newType = _package.owner()._class(fqn);
                }
                ruleFactory.getLogger().debug("Adding " + newType.fullName());
            } else {
                final String className = ruleFactory.getNameHelper().getUniqueClassName(nodeName, node, _package);
                if (usePolymorphicDeserialization) {
                    newType = _package._class(JMod.PUBLIC, className, ClassType.CLASS);
                } else {
                    newType = _package._class(className);
                }
                ruleFactory.getLogger().debug("Adding " + newType.fullName());
            }
        } catch (JClassAlreadyExistsException e) {
            throw new ClassAlreadyExistsException(e.getExistingClass());
        }

        annotator.typeInfo(newType, node);
        annotator.propertyInclusion(newType, node);

        return newType;

    }

    private void addToString(JDefinedClass jclass) {
        Map<String, JFieldVar> fields = jclass.fields();
        JMethod toString = jclass.method(JMod.PUBLIC, String.class, "toString");
        Set<String> excludes = new HashSet<>(Arrays.asList(ruleFactory.getGenerationConfig().getToStringExcludes()));

        JBlock body = toString.body();

        // The following toString implementation roughly matches the commons ToStringBuilder for
        // backward compatibility
        JClass stringBuilderClass = jclass.owner().ref(StringBuilder.class);
        JVar sb = body.decl(stringBuilderClass, "sb", JExpr._new(stringBuilderClass));

        // Write the header, e.g.: example.domain.MyClass@85e382a7[
        body.add(sb
                .invoke("append").arg(jclass.dotclass().invoke("getName"))
                .invoke("append").arg(JExpr.lit('@'))
                .invoke("append").arg(
                        jclass.owner().ref(Integer.class).staticInvoke("toHexString").arg(
                                jclass.owner().ref(System.class).staticInvoke("identityHashCode").arg(JExpr._this())))
                .invoke("append").arg(JExpr.lit('[')));

        // If this has a parent class, include its toString()
        if (!jclass._extends().fullName().equals(Object.class.getName())) {
            JVar baseLength = body.decl(jclass.owner().INT, "baseLength", sb.invoke("length"));
            JVar superString = body.decl(jclass.owner().ref(String.class), "superString", JExpr._super().invoke("toString"));

            JBlock superToStringBlock = body._if(superString.ne(JExpr._null()))._then();

            // If super.toString() is in the Clazz@2ee6529d[field=10] format, extract the fields
            // from the wrapper
            JVar contentStart = superToStringBlock.decl(jclass.owner().INT, "contentStart",
                    superString.invoke("indexOf").arg(JExpr.lit('[')));
            JVar contentEnd = superToStringBlock.decl(jclass.owner().INT, "contentEnd",
                    superString.invoke("lastIndexOf").arg(JExpr.lit(']')));

            JConditional superToStringInnerConditional = superToStringBlock._if(
                    contentStart.gte(JExpr.lit(0)).cand(contentEnd.gt(contentStart)));

            superToStringInnerConditional._then().add(
                    sb.invoke("append")
                    .arg(superString)
                    .arg(contentStart.plus(JExpr.lit(1)))
                    .arg(contentEnd));

            // Otherwise, just append super.toString()
            superToStringInnerConditional._else().add(sb.invoke("append").arg(superString));

            // Append a comma if needed
            body._if(sb.invoke("length").gt(baseLength))
            ._then().add(sb.invoke("append").arg(JExpr.lit(',')));
        }

        // For each included instance field, add to the StringBuilder in the field=value format
        for (JFieldVar fieldVar : fields.values()) {
            if (excludes.contains(fieldVar.name()) || (fieldVar.mods().getValue() & JMod.STATIC) == JMod.STATIC) {
                continue;
            }

            body.add(sb.invoke("append").arg(fieldVar.name()));
            body.add(sb.invoke("append").arg(JExpr.lit('=')));

            if (fieldVar.type().isPrimitive()) {
                body.add(sb.invoke("append").arg(JExpr.refthis(fieldVar.name())));
            } else if (fieldVar.type().isArray()) {
                // Only primitive arrays are supported
                if (!fieldVar.type().elementType().isPrimitive()) {
                    throw new UnsupportedOperationException("Only primitive arrays are supported");
                }

                // Leverage Arrays.toString()
                body.add(sb.invoke("append")
                        .arg(JOp.cond(
                                JExpr.refthis(fieldVar.name()).eq(JExpr._null()),
                                JExpr.lit("<null>"),
                                jclass.owner().ref(Arrays.class).staticInvoke("toString")
                                .arg(JExpr.refthis(fieldVar.name()))
                                .invoke("replace").arg(JExpr.lit('[')).arg(JExpr.lit('{'))
                                .invoke("replace").arg(JExpr.lit(']')).arg(JExpr.lit('}'))
                                .invoke("replace").arg(JExpr.lit(", ")).arg(JExpr.lit(",")))));
            } else {
                body.add(sb.invoke("append")
                        .arg(JOp.cond(
                                JExpr.refthis(fieldVar.name()).eq(JExpr._null()),
                                JExpr.lit("<null>"),
                                JExpr.refthis(fieldVar.name()))));
            }

            body.add(sb.invoke("append").arg(JExpr.lit(',')));
        }

        // Add the trailer
        JConditional trailerConditional = body._if(
                sb.invoke("charAt").arg(sb.invoke("length").minus(JExpr.lit(1)))
                .eq(JExpr.lit(',')));

        trailerConditional._then().add(
                sb.invoke("setCharAt")
                .arg(sb.invoke("length").minus(JExpr.lit(1)))
                .arg(JExpr.lit(']')));

        trailerConditional._else().add(
                sb.invoke("append").arg(JExpr.lit(']')));


        body._return(sb.invoke("toString"));

        toString.annotate(Override.class);
    }

    private void addHashCode(JDefinedClass jclass, JsonNode node) {
        Map<String, JFieldVar> fields = removeFieldsExcludedFromEqualsAndHashCode(jclass.fields(), node);

        JMethod hashCode = jclass.method(JMod.PUBLIC, int.class, "hashCode");
        JBlock body = hashCode.body();
        JVar result = body.decl(jclass.owner().INT, "result", JExpr.lit(1));

        // Incorporate each non-excluded field in the hashCode calculation
        for (JFieldVar fieldVar : fields.values()) {
            if ((fieldVar.mods().getValue() & JMod.STATIC) == JMod.STATIC) {
                continue;
            }

            JFieldRef fieldRef = JExpr.refthis(fieldVar.name());

            JExpression fieldHash;
            if (fieldVar.type().isPrimitive()) {
                if ("long".equals(fieldVar.type().name())) {
                    fieldHash = JExpr.cast(jclass.owner().INT, fieldRef.xor(fieldRef.shrz(JExpr.lit(32))));
                } else if ("boolean".equals(fieldVar.type().name())) {
                    fieldHash = JOp.cond(fieldRef, JExpr.lit(1), JExpr.lit(0));
                } else if ("int".equals(fieldVar.type().name())) {
                    fieldHash = fieldRef;
                } else if ("double".equals(fieldVar.type().name())) {
                    JClass doubleClass = jclass.owner().ref(Double.class);
                    JExpression longField = doubleClass.staticInvoke("doubleToLongBits").arg(fieldRef);
                    fieldHash = JExpr.cast(jclass.owner().INT,
                            longField.xor(longField.shrz(JExpr.lit(32))));
                } else if ("float".equals(fieldVar.type().name())) {
                    fieldHash = jclass.owner().ref(Float.class).staticInvoke("floatToIntBits").arg(fieldRef);
                } else {
                    fieldHash = JExpr.cast(jclass.owner().INT, fieldRef);
                }
            } else if (fieldVar.type().isArray()) {
                if (!fieldVar.type().elementType().isPrimitive()) {
                    throw new UnsupportedOperationException("Only primitive arrays are supported");
                }

                fieldHash = jclass.owner().ref(Arrays.class).staticInvoke("hashCode").arg(fieldRef);
            } else {
                fieldHash = JOp.cond(fieldRef.eq(JExpr._null()), JExpr.lit(0), fieldRef.invoke("hashCode"));
            }

            body.assign(result, result.mul(JExpr.lit(31)).plus(fieldHash));
        }

        // Add super.hashCode()
        if (!jclass._extends().fullName().equals(Object.class.getName())) {
            body.assign(result, result.mul(JExpr.lit(31)).plus(JExpr._super().invoke("hashCode")));
        }

        body._return(result);
        hashCode.annotate(Override.class);
    }

    private Map<String, JFieldVar> removeFieldsExcludedFromEqualsAndHashCode(Map<String, JFieldVar> fields, JsonNode node) {
        Map<String, JFieldVar> filteredFields = new HashMap<>(fields);

        JsonNode properties = node.get("properties");

        if (properties != null) {
            if (node.has("excludedFromEqualsAndHashCode")) {
                JsonNode excludedArray = node.get("excludedFromEqualsAndHashCode");

                for (Iterator<JsonNode> iterator = excludedArray.elements(); iterator.hasNext(); ) {
                    String excludedPropertyName = iterator.next().asText();
                    JsonNode excludedPropertyNode = properties.get(excludedPropertyName);
                    filteredFields.remove(ruleFactory.getNameHelper().getPropertyName(excludedPropertyName, excludedPropertyNode));
                }
            }

            for (Map.Entry<String, JsonNode> entry : properties.properties()) {
                String propertyName = entry.getKey();
                JsonNode propertyNode = entry.getValue();

                if (propertyNode.has("excludedFromEqualsAndHashCode") &&
                        propertyNode.get("excludedFromEqualsAndHashCode").asBoolean()) {
                    filteredFields.remove(ruleFactory.getNameHelper().getPropertyName(propertyName, propertyNode));
                }
            }
        }

        return filteredFields;
    }

    private void addEquals(JDefinedClass jclass, JsonNode node) {
        Map<String, JFieldVar> fields = removeFieldsExcludedFromEqualsAndHashCode(jclass.fields(), node);

        JMethod equals = jclass.method(JMod.PUBLIC, boolean.class, "equals");
        JVar otherObject = equals.param(Object.class, "other");

        JBlock body = equals.body();

        body._if(otherObject.eq(JExpr._this()))._then()._return(JExpr.TRUE);
        body._if(otherObject._instanceof(jclass).eq(JExpr.FALSE))._then()._return(JExpr.FALSE);

        JVar rhsVar = body.decl(jclass, "rhs").init(JExpr.cast(jclass, otherObject));

        JExpression result = JExpr.lit(true);

        // First, check super.equals(other)
        if (!jclass._extends().fullName().equals(Object.class.getName())) {
            result = result.cand(JExpr._super().invoke("equals").arg(rhsVar));
        }

        // Chain the results of checking all other fields
        for (JFieldVar fieldVar : fields.values()) {
            if ((fieldVar.mods().getValue() & JMod.STATIC) == JMod.STATIC) {
                continue;
            }

            JFieldRef thisFieldRef = JExpr.refthis(fieldVar.name());
            JFieldRef otherFieldRef = JExpr.ref(rhsVar, fieldVar.name());
            JExpression fieldEquals;

            if (fieldVar.type().isPrimitive()) {
                if ("double".equals(fieldVar.type().name())) {
                    JClass doubleClass = jclass.owner().ref(Double.class);
                    fieldEquals = doubleClass.staticInvoke("doubleToLongBits").arg(thisFieldRef).eq(
                            doubleClass.staticInvoke("doubleToLongBits").arg(otherFieldRef));
                } else if ("float".equals(fieldVar.type().name())) {
                    JClass floatClass = jclass.owner().ref(Float.class);
                    fieldEquals = floatClass.staticInvoke("floatToIntBits").arg(thisFieldRef).eq(
                            floatClass.staticInvoke("floatToIntBits").arg(otherFieldRef));
                } else {
                    fieldEquals = thisFieldRef.eq(otherFieldRef);
                }
            } else if (fieldVar.type().isArray()) {
                if (!fieldVar.type().elementType().isPrimitive()) {
                    throw new UnsupportedOperationException("Only primitive arrays are supported");
                }

                fieldEquals = jclass.owner().ref(Arrays.class).staticInvoke("equals").arg(thisFieldRef).arg(otherFieldRef);
            } else {
                fieldEquals = thisFieldRef.eq(otherFieldRef).cor(
                        thisFieldRef.ne(JExpr._null())
                        .cand(thisFieldRef.invoke("equals").arg(otherFieldRef)));
            }

            // Chain the equality of this field with the previous comparisons
            result = result.cand(fieldEquals);
        }

        body._return(result);

        equals.annotate(Override.class);
    }

    private void addInterfaces(JDefinedClass jclass, JsonNode javaInterfaces) {
        for (JsonNode i : javaInterfaces) {
            jclass._implements(resolveType(jclass._package(), i.asText()));
        }
    }

    private boolean usesPolymorphicDeserialization(JsonNode node) {

        AnnotationStyle annotationStyle = ruleFactory.getGenerationConfig().getAnnotationStyle();

        if (annotationStyle == AnnotationStyle.JACKSON
                || annotationStyle == AnnotationStyle.JACKSON2
                || annotationStyle == AnnotationStyle.JACKSON3) {
            return ruleFactory.getGenerationConfig().isIncludeTypeInfo() || node.has("deserializationClassProperty");
        }

        return false;
    }

}
