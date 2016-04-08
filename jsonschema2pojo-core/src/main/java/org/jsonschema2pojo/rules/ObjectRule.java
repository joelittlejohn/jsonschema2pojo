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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import org.jsonschema2pojo.AnnotationStyle;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.exception.ClassAlreadyExistsException;
import org.jsonschema2pojo.util.NameHelper;
import org.jsonschema2pojo.util.ParcelableHelper;
import org.jsonschema2pojo.util.TypeUtil;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.jsonschema2pojo.rules.PrimitiveTypes.isPrimitive;
import static org.jsonschema2pojo.rules.PrimitiveTypes.primitiveType;
import static org.jsonschema2pojo.util.TypeUtil.resolveType;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    private static final Comparator<JClass> INTERFACE_COMPARATOR =
    new Comparator<JClass>() {
      public int compare(JClass object1, JClass object2) {
        if (object1 == null && object2 == null) {
          return 0;
        }
        if (object1 == null) {
          return 1;
        }
        if (object2 == null) {
          return -1;
        }
        final String name1 = object1.getQualifiedName();
        final String name2 = object2.getQualifiedName();
        if (name1 == null && name2 == null) {
          return 0;
        }
        if (name1 == null) {
          return 1;
        }
        if (name2 == null) {
          return -1;
        }
        return name1.compareTo(name2);
      }
    };

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

        ruleFactory.getPropertiesRule().apply(nodeName, node.get("properties"), jclass, schema);

        if (ruleFactory.getGenerationConfig().isIncludeToString()) {
            addToString(jclass);
        }

        if (node.has("javaInterfaces")) {
            addInterfaces(jclass, node.get("javaInterfaces"));
        }

        ruleFactory.getAdditionalPropertiesRule().apply(nodeName, node.get("additionalProperties"), jclass, schema);

        ruleFactory.getDynamicPropertiesRule().apply(nodeName, node.get("properties"), jclass, schema);

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

        if (ruleFactory.getGenerationConfig().isSerializable()) {
            addSerializableSupport(jclass);
        }

        return jclass;

    }

    private void addParcelSupport(JDefinedClass jclass) {
        jclass._implements(Parcelable.class);

        parcelableHelper.addWriteToParcel(jclass);
        parcelableHelper.addDescribeContents(jclass);
        parcelableHelper.addCreator(jclass);
    }

    private static void processMethodCollectionForSerializableSupport(Collection<JMethod> methods, DataOutputStream data) {
        TreeMap<String, JClass> sortedMethods = new TreeMap<>();
        for (JMethod method : methods) {
            //Collect non-private methods
            if ((method.mods().getValue() & JMod.PRIVATE) != JMod.PRIVATE) {
                sortedMethods.put(method.name(), method);
            }
        }
        for (JMethod method : sortedMethods.values()) {
            dataOutputStream.writeUTF(method.name());
            dataOutputStream.writeInt(method.mods());
            dataOutputStream.writeUTF(method.type.fullName());
            for (JVar param : method.params()) {
                dataOutputStream.writeUTF(param.type().fullName());
            }
        }
    }

    private static void processDefinedClassForSerializableSupport(JDefinedClass jclass, DataOutputStream dataOutputStream) {
        dataOutputStream.writeUTF(jclass.fullName());
            dataOutputStream.writeInt(jclass.mods().getValue());

            for (JTypeVar typeParam : jclass.typeParams()) {
                dataOutputStream.writeUTF(typeParam.fullName())
            }

            //sorted
            TreeMap<String, JClass> sortedClasses = new TreeMap<>();
            for (JClass nestedClass : jclass.classes()) {
                sortedClasses.put(nestedClass.fullName(), nestedClass);
            }
            for (JClass nestedClass : sortedClasses.values()) {
                processDefinedClassForSerializableSupport(nestedClass, dataOutputStream);
            }

            //sorted
            TreeSet<String> fieldNames = new TreeSet(jclass.fields().keySet());
            for (String fieldName : fieldNames) {
                JFieldVar fieldVar = jclass.fields().get(fieldName);
                //non private members
                if (fieldVar.mods().getValue() & JMod.PRIVATE) != JMod.PRIVATE) {
                    processFieldVarForSerializableSupport(jclass.fields().get(fieldName), dataOutputStream);
                }
            }

            Iterator<JClass> interfaces = jclass._implements();
            List<JClass> interfacesList = new ArrayList<>();
            for (JClass interface : interfaces) {
                interfacesList.add(interface);
            }
            Collections.sort(interfacesList, INTERFACE_COMPARATOR);
            for (JClass interface : interfacesList) {
                dataOutputStream.writeUTF(interface.fullName());
            }

            //we should probably serialize the parent class too! (but what if it has serialversionUID on it? that would be a field and would affect the serialversionUID!)
            if (jclass._extends()) {
                dataOutputStream.writeUTF(jclass.extends().fullName());
            }

            processMethodCollectionForSerializableSupport(jclass.methods(), dataOutputStream);
            processMethodCollectionForSerializableSupport(jclass.constructors(), dataOutputStream);
    }


    private static void processFieldVarForSerializableSupport(JFieldVar fieldVar, DataOutputStream dataOutputStream) {
        dataOutputStream.writeUTF(fieldVar.name());
        dataOutputStream.writeInt(fieldVar.mods().getValue());
        JType type = fieldVar.type();
        dataOutputStream.writeUTF(type.fullName());
    }
    
    private void addSerializableSupport(JDefinedClass jclass) {
        jclass._implements(Serializable.class);

        final boolean isSerializable = psiClass.isInheritor(serializable, true);

        try {

            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final DataOutputStream      dataOutputStream      = new DataOutputStream(byteArrayOutputStream);

            processDefinedClassForSerializableSupport(jclass, dataOutputStream);

            dataOutputStream.flush();

            final MessageDigest digest           = MessageDigest.getInstance("SHA");
            final byte[]        digestBytes      = digest.digest(byteArrayOutputStream.toByteArray());
            long                serialVersionUID = 0L;

            for (int i = Math.min(digestBytes.length, 8) - 1; i >= 0; i--) {
                serialVersionUID = serialVersionUID << 8 | (long)(digestBytes[i] & 0xff);
            }

            JFieldVar  serialUIDField = jclass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, long.class, "serialVersionUID");
            serialUIDField.init(JExpr.lit(serialVersionUID));

        } catch (NoSuchAlgorithmException exception) {
            final RuntimeException securityException = new SecurityException(exception.getMessage());
            securityException.initCause(exception);
            throw securityException;
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
                    rtn.add(nameHelper.getPropertyName(property.getKey(), property.getValue()));
                }
            } else {
                rtn.add((nameHelper.getPropertyName(property.getKey(), property.getValue())));
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
        if (node.has("extends")) {
            String path;
            if (schema.getId().getFragment() == null) {
                path = "#extends";
            } else {
                path = "#" + schema.getId().getFragment() + "/extends";
            }
            Schema superTypeSchema = ruleFactory.getSchemaStore().create(schema, path);
            superType = ruleFactory.getSchemaRule().apply(nodeName + "Parent", node.get("extends"), jPackage, superTypeSchema);
        } else if (node.has("extendsJavaClass")) {
            superType = resolveType(jPackage, node.get("extendsJavaClass").asText());
        }

        return superType;
    }

    private void addGeneratedAnnotation(JDefinedClass jclass) {
        JAnnotationUse generated = jclass.annotate(Generated.class);
        generated.param("value", SchemaMapper.class.getPackage().getName());
    }

    private void addJsonTypeInfoAnnotation(JDefinedClass jclass, JsonNode node) {
        if (this.ruleFactory.getGenerationConfig().getAnnotationStyle() == AnnotationStyle.JACKSON2) {
            String annotationName = node.get("deserializationClassProperty").asText();
            JAnnotationUse jsonTypeInfo = jclass.annotate(JsonTypeInfo.class);
            jsonTypeInfo.param("use", JsonTypeInfo.Id.CLASS);
            jsonTypeInfo.param("include", JsonTypeInfo.As.PROPERTY);
            jsonTypeInfo.param("property", annotationName);
        }
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
            if( (fieldVar.mods().getValue() & JMod.STATIC) == JMod.STATIC) continue;
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
            if( (fieldVar.mods().getValue() & JMod.STATIC) == JMod.STATIC ) continue;
            equalsBuilderInvocation = equalsBuilderInvocation.invoke("append")
                    .arg(fieldVar)
                    .arg(rhsVar.ref(fieldVar.name()));
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
