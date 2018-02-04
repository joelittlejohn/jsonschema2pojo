/**
 * Copyright © 2010-2017 Nokia
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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import org.jsonschema2pojo.AnnotationStyle;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.exception.ClassAlreadyExistsException;
import org.jsonschema2pojo.util.MakeUniqueClassName;
import org.jsonschema2pojo.util.NameHelper;
import org.jsonschema2pojo.util.ParcelableHelper;
import org.jsonschema2pojo.util.SerializableHelper;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
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

        if (node.has("deserializationClassProperty")) {
            addJsonTypeInfoAnnotation(jclass, node);
        }

        if (node.has("title")) {
            ruleFactory.getTitleRule().apply(nodeName, node.get("title"), jclass, schema);
        }

        if (node.has("description")) {
            ruleFactory.getDescriptionRule().apply(nodeName, node.get("description"), jclass, schema);
        }

        ruleFactory.getPropertiesRule().apply(nodeName, node.get("properties"), jclass, schema);

        if (node.has("javaInterfaces")) {
            addInterfaces(jclass, node.get("javaInterfaces"));
        }

        ruleFactory.getAdditionalPropertiesRule().apply(nodeName, node.get("additionalProperties"), jclass, schema);

        ruleFactory.getDynamicPropertiesRule().apply(nodeName, node.get("properties"), jclass, schema);

        if (node.has("required")) {
            ruleFactory.getRequiredArrayRule().apply(nodeName, node.get("required"), jclass, schema);
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
            addConstructors(jclass, node, schema, ruleFactory.getGenerationConfig().isConstructorsRequiredPropertiesOnly());
        }

        if (ruleFactory.getGenerationConfig().isSerializable()) {
            SerializableHelper.addSerializableSupport(jclass);
        }

        return jclass;

    }

    private void addParcelSupport(JDefinedClass jclass) {
        jclass._implements(Parcelable.class);

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
     * Retrieve the list of properties to go in the constructor from node. This
     * is all properties listed in node["properties"] if ! onlyRequired, and
     * only required properties if onlyRequired.
     *
     * @param node
     * @return
     */
    private LinkedHashSet<String> getConstructorProperties(JsonNode node, boolean onlyRequired) {

        if (!node.has("properties")) {
            return new LinkedHashSet<String>();
        }

        LinkedHashSet<String> rtn = new LinkedHashSet<String>();
        Set<String> draft4RequiredProperties = new HashSet<String>();

        // setup the set of required properties for draft4 style "required"
        if (onlyRequired && node.has("required")) {
            JsonNode requiredArray =  node.get("required");
            if (requiredArray.isArray()) {
                for (JsonNode requiredEntry: requiredArray) {
                    if (requiredEntry.isTextual()) {
                        draft4RequiredProperties.add(requiredEntry.asText());
                    }
                }
            }
        }

        NameHelper nameHelper = ruleFactory.getNameHelper();
        for (Iterator<Map.Entry<String, JsonNode>> properties = node.get("properties").fields(); properties.hasNext();) {
            Map.Entry<String, JsonNode> property = properties.next();

            JsonNode propertyObj = property.getValue();
            if (onlyRequired) {
                // draft3 style
                if (propertyObj.has("required") && propertyObj.get("required").asBoolean()) {
                    rtn.add(nameHelper.getPropertyName(property.getKey(), property.getValue()));
                }

                // draft4 style
                if (draft4RequiredProperties.contains(property.getKey())) {
                    rtn.add(nameHelper.getPropertyName(property.getKey(), property.getValue()));
                }
            } else {
                rtn.add(nameHelper.getPropertyName(property.getKey(), property.getValue()));
            }
        }
        return rtn;
    }

    /**
     * Recursive, walks the schema tree and assembles a list of all properties of this schema's super schemas
     */
    private LinkedHashSet<String> getSuperTypeConstructorPropertiesRecursive(JsonNode node, Schema schema, boolean onlyRequired) {
        Schema superTypeSchema = getSuperSchema(node, schema, true);

        if (superTypeSchema == null) {
            return new LinkedHashSet<String>();
        }

        JsonNode superSchemaNode = superTypeSchema.getContent();

        LinkedHashSet<String> rtn = getConstructorProperties(superSchemaNode, onlyRequired);
        rtn.addAll(getSuperTypeConstructorPropertiesRecursive(superSchemaNode, superTypeSchema, onlyRequired));

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
                JClass existingClass;

                try {
                    _package.owner().ref(Thread.currentThread().getContextClassLoader().loadClass(fqn));
                    existingClass = resolveType(_package, fqn + (node.get("javaType").asText().contains("<") ? "<" + substringAfter(node.get("javaType").asText(), "<") : ""));

                    throw new ClassAlreadyExistsException(existingClass);
                } catch (ClassNotFoundException e) {

                }

                int index = fqn.lastIndexOf(".") + 1;
                if (index >= 0 && index < fqn.length()) {
                    fqn = fqn.substring(0, index) + ruleFactory.getGenerationConfig().getClassNamePrefix() + fqn.substring(index) + ruleFactory.getGenerationConfig().getClassNameSuffix();
                }

                try {

                    _package.owner().ref(Thread.currentThread().getContextClassLoader().loadClass(fqn));
                    existingClass = resolveType(_package, fqn + (node.get("javaType").asText().contains("<") ? "<" + substringAfter(node.get("javaType").asText(), "<") : ""));

                    throw new ClassAlreadyExistsException(existingClass);
                } catch (ClassNotFoundException e) {

                }
                if (usePolymorphicDeserialization) {
                    newType = _package.owner()._class(JMod.PUBLIC, fqn, ClassType.CLASS);
                } else {
                    newType = _package.owner()._class(fqn);
                }
            } else {
                if (usePolymorphicDeserialization) {
                    newType = _package._class(JMod.PUBLIC, getClassName(nodeName, node, _package), ClassType.CLASS);
                } else {
                    newType = _package._class(getClassName(nodeName, node, _package));
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

    private JType getSuperType(String nodeName, JsonNode node, JPackage jPackage, Schema schema) {
        if (node.has("extends") && node.has("extendsJavaClass")) {
            throw new IllegalStateException("'extends' and 'extendsJavaClass' defined simultaneously");
        }

        JType superType = jPackage.owner().ref(Object.class);
        Schema superTypeSchema = getSuperSchema(node, schema, false);
        if (superTypeSchema != null) {
            superType = ruleFactory.getSchemaRule().apply(nodeName + "Parent", node.get("extends"), jPackage, superTypeSchema);
        } else if (node.has("extendsJavaClass")) {
            superType = resolveType(jPackage, node.get("extendsJavaClass").asText());
        }

        return superType;
    }

    private Schema getSuperSchema(JsonNode node, Schema schema, boolean followRefs) {
        if (node.has("extends")) {
            String path;
            if (schema.getId().getFragment() == null) {
                path = "#extends";
            } else {
                path = "#" + schema.getId().getFragment() + "/extends";
            }

            Schema superSchema = ruleFactory.getSchemaStore().create(schema, path, ruleFactory.getGenerationConfig().getRefFragmentPathDelimiters());

            if (followRefs) {
                superSchema = resolveSchemaRefsRecursive(superSchema);
            }

            return superSchema;
        }
        return null;
    }

    private Schema resolveSchemaRefsRecursive(Schema schema) {
        JsonNode schemaNode = schema.getContent();
        if (schemaNode.has("$ref")) {
            schema = ruleFactory.getSchemaStore().create(schema, schemaNode.get("$ref").asText(), ruleFactory.getGenerationConfig().getRefFragmentPathDelimiters());
            return resolveSchemaRefsRecursive(schema);
        }
        return schema;
    }

    private void addJsonTypeInfoAnnotation(JDefinedClass jclass, JsonNode node) {
        if (ruleFactory.getGenerationConfig().getAnnotationStyle() == AnnotationStyle.JACKSON2) {
            String annotationName = node.get("deserializationClassProperty").asText();
            JAnnotationUse jsonTypeInfo = jclass.annotate(JsonTypeInfo.class);
            jsonTypeInfo.param("use", JsonTypeInfo.Id.CLASS);
            jsonTypeInfo.param("include", JsonTypeInfo.As.PROPERTY);
            jsonTypeInfo.param("property", annotationName);
        }
    }

    private void addToString(JDefinedClass jclass) {
        Map<String, JFieldVar> fields = jclass.fields();
        JMethod toString = jclass.method(JMod.PUBLIC, String.class, "toString");
        Set<String> excludes = new HashSet<String>(Arrays.asList(ruleFactory.getGenerationConfig().getToStringExcludes()));

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
        Map<String, JFieldVar> filteredFields = new HashMap<String, JFieldVar>(fields);

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

            for (Iterator<Map.Entry<String, JsonNode>> iterator = properties.fields(); iterator.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = iterator.next();
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

    private void addConstructors(JDefinedClass jclass, JsonNode node, Schema schema, boolean onlyRequired) {

        LinkedHashSet<String> classProperties = getConstructorProperties(node, onlyRequired);
        LinkedHashSet<String> combinedSuperProperties = getSuperTypeConstructorPropertiesRecursive(node, schema, onlyRequired);

        // no properties to put in the constructor => default constructor is good enough.
        if (classProperties.isEmpty() && combinedSuperProperties.isEmpty()) {
            return;
        }

        // add a no-args constructor for serialization purposes
        JMethod noargsConstructor = jclass.constructor(JMod.PUBLIC);
        noargsConstructor.javadoc().add("No args constructor for use in serialization");

        // add the public constructor with property parameters
        JMethod fieldsConstructor = jclass.constructor(JMod.PUBLIC);
        JBlock constructorBody = fieldsConstructor.body();
        JInvocation superInvocation = constructorBody.invoke("super");

        Map<String, JFieldVar> fields = jclass.fields();
        Map<String, JVar> classFieldParams = new HashMap<String, JVar>();

        for (String property : classProperties) {
            JFieldVar field = fields.get(property);

            if (field == null) {
                throw new IllegalStateException("Property " + property + " hasn't been added to JDefinedClass before calling addConstructors");
            }

            fieldsConstructor.javadoc().addParam(property);
            JVar param = fieldsConstructor.param(field.type(), field.name());
            constructorBody.assign(JExpr._this().ref(field), param);
            classFieldParams.put(property, param);
        }

        List<JVar> superConstructorParams = new ArrayList<JVar>();


        for (String property : combinedSuperProperties) {
            JFieldVar field = searchSuperClassesForField(property, jclass);

            if (field == null) {
                throw new IllegalStateException("Property " + property + " hasn't been added to JDefinedClass before calling addConstructors");
            }

            JVar param = classFieldParams.get(property);

            if (param == null) {
                param = fieldsConstructor.param(field.type(), field.name());
            }

            fieldsConstructor.javadoc().addParam(property);
            superConstructorParams.add(param);
        }

        for (JVar param : superConstructorParams) {
            superInvocation.arg(param);
        }
    }

    private static JDefinedClass definedClassOrNullFromType(JType type)
    {
        if (type == null || type.isPrimitive())
        {
            return null;
        }
        JClass fieldClass = type.boxify();
        JPackage jPackage = fieldClass._package();
        return jPackage._getClass(fieldClass.name());
    }

    /**
     * This is recursive with searchClassAndSuperClassesForField
     */
    private JFieldVar searchSuperClassesForField(String property, JDefinedClass jclass) {
        JClass superClass = jclass._extends();
        JDefinedClass definedSuperClass = definedClassOrNullFromType(superClass);
        if (definedSuperClass == null) {
            return null;
        }
        return searchClassAndSuperClassesForField(property, definedSuperClass);
    }

    private JFieldVar searchClassAndSuperClassesForField(String property, JDefinedClass jclass) {
        Map<String, JFieldVar> fields = jclass.fields();
        JFieldVar field = fields.get(property);
        if (field == null) {
            return searchSuperClassesForField(property, jclass);
        }
        return field;
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

    private String getClassName(String nodeName, JsonNode node, JPackage _package) {
        String prefix = ruleFactory.getGenerationConfig().getClassNamePrefix();
        String suffix = ruleFactory.getGenerationConfig().getClassNameSuffix();
        String fieldName = ruleFactory.getNameHelper().getFieldName(nodeName, node);
        String capitalizedFieldName = capitalize(fieldName);
        String fullFieldName = createFullFieldName(capitalizedFieldName, prefix, suffix);

        String className = ruleFactory.getNameHelper().replaceIllegalCharacters(fullFieldName);
        String normalizedName = ruleFactory.getNameHelper().normalizeName(className);
        return makeUnique(normalizedName, _package);
    }

    private String createFullFieldName(String nodeName, String prefix, String suffix) {
        String returnString = nodeName;
        if (prefix != null) {
            returnString = prefix + returnString;
        }

        if (suffix != null) {
            returnString = returnString + suffix;
        }

        return returnString;
    }

    private String makeUnique(String className, JPackage _package) {
        try {
            JDefinedClass _class = _package._class(className);
            _package.remove(_class);
            return className;
        } catch (JClassAlreadyExistsException e) {
            return makeUnique(MakeUniqueClassName.makeUnique(className), _package);
        }
    }

    private boolean usesPolymorphicDeserialization(JsonNode node) {
        if (ruleFactory.getGenerationConfig().getAnnotationStyle() == AnnotationStyle.JACKSON2) {
            return node.has("deserializationClassProperty");
        }
        return false;
    }

}
