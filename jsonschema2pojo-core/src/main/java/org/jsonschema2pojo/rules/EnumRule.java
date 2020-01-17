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

import static java.util.Arrays.*;
import static org.apache.commons.lang3.StringUtils.*;
import static org.jsonschema2pojo.rules.PrimitiveTypes.*;
import static org.jsonschema2pojo.util.TypeUtil.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.exception.ClassAlreadyExistsException;
import org.jsonschema2pojo.exception.GenerationException;
import org.jsonschema2pojo.model.EnumDefinition;
import org.jsonschema2pojo.model.EnumDefinitionExtensionType;
import org.jsonschema2pojo.model.EnumValueDefinition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

/**
 * Applies the "enum" schema rule.
 *
 * @see <a href=
 *      "http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.19">http:
 *      //tools.ietf.org/html/draft-zyp-json-schema-03#section-5.19</a>
 */
public class EnumRule implements Rule<JClassContainer, JType> {

    private static final String VALUE_FIELD_NAME = "value";

    private final RuleFactory ruleFactory;

    protected EnumRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
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
     * @param parent
     *            the parent node
     * @param container
     *            the class container (class or package) to which this enum
     *            should be added
     * @return the newly generated Java type that was created to represent the
     *         given enum
     */
    @Override
    public JType apply(String nodeName, JsonNode node, JsonNode parent, JClassContainer container, Schema schema) {

        JDefinedClass _enum;
        try {
            _enum = createEnum(node, nodeName, container);
        } catch (ClassAlreadyExistsException e) {
            return e.getExistingClass();
        }

        schema.setJavaTypeIfEmpty(_enum);

        // Add JavaDocs
        if (node.has("title")) {
            ruleFactory.getTitleRule().apply(nodeName, node.get("title"), node, _enum, schema);
        }

        if (node.has("description")) {
            ruleFactory.getDescriptionRule().apply(nodeName, node.get("description"), node, _enum, schema);
        }

        if (node.has("javaInterfaces")) {
            addInterfaces(_enum, node.get("javaInterfaces"));
        }

        // copy our node; remove the javaType as it will throw off the TypeRule for our case
        ObjectNode typeNode = (ObjectNode)node.deepCopy();
        typeNode.remove("javaType");

        // If type is specified on the enum, get a type rule for it.  Otherwise, we're a string.
        // (This is different from the default of Object, which is why we don't do this for every case.)
        JType backingType = node.has("type") ?
                ruleFactory.getTypeRule().apply(nodeName, typeNode, parent, container, schema) :
                    container.owner().ref(String.class);

        EnumDefinition enumDefinition = buildEnumDefinition(nodeName, node, backingType);

        JFieldVar valueField = addConstructorAndFields(enumDefinition, _enum);

        // override toString only if we have a sensible string to return
        if(isString(backingType)){
            addToString(_enum, valueField);
        }

        addFieldAccessors(_enum, valueField);
        addEnumConstants(enumDefinition, _enum, schema);
        addFactoryMethod(enumDefinition, _enum);

        applyCustomizations(enumDefinition, _enum);

        return _enum;
    }

    protected void addEnumConstants(EnumDefinition enumDefinition, JDefinedClass _enum, Schema schema) {

        JType type = enumDefinition.getBackingType();

        String nodeName = enumDefinition.getNodeName();
        JsonNode parentNode = enumDefinition.getEnumNode();

        for(EnumValueDefinition enumValueDefinition : enumDefinition.values()) {

            JEnumConstant constant = _enum.enumConstant(enumValueDefinition.getName());
            String value = enumValueDefinition.getValue();
            constant.arg(DefaultRule.getDefaultValue(type, value));

            Annotator annotator = ruleFactory.getAnnotator();
            annotator.enumConstant(_enum, constant, value);

            String enumNodeName = nodeName + "#" + value;

            if(enumValueDefinition.hasTitle()) {
                JsonNode titleNode = enumValueDefinition.getTitleNode();
                ruleFactory.getTitleRule().apply(enumNodeName, titleNode, parentNode, constant, schema);
            }

            if(enumValueDefinition.hasDescription()) {
                JsonNode descriptionNode = enumValueDefinition.getDescriptionNode();
                ruleFactory.getDescriptionRule().apply(enumNodeName, descriptionNode, parentNode, constant, schema);
            }
        }
    }

    /**
     * Allows a custom {@link EnumRule} implementation to extend {@link EnumRule} and do some custom behaviors.
     * <p>
     * This method is specifically added so that custom enum rule developers do not need to override the apply method.
     *
     * @param enumDefinition the enum definition.
     * @param _enum          the generated class model
     */
    protected void applyCustomizations(EnumDefinition enumDefinition, JDefinedClass _enum) {
        // Default Implementation does not have any customizations, this is for custom enum rule implementations.
    }

    /**
     * Builds the effective definition of an enumeration is based on what schema elements are provided.
     * <p/>
     * This function determines which method it should delegate creating of the definition to:
     *
     * For "enum" handled by {@link #buildEnumDefinitionWithNoExtensions(String, JsonNode, JsonNode, JType)}
     * For "enum" and "javaEnums" handled by {@link #buildEnumDefinitionWithJavaEnumsExtension(String, JsonNode, JsonNode, JsonNode, JType)}
     * For "enum" and "javaEnumNames" handled by {@link #buildEnumDefinitionWithJavaEnumNamesExtension(String, JsonNode, JsonNode, JsonNode, JType)}
     *
     * @param nodeName
     *            the name of the property which is an "enum"
     * @param node
     *            the enum node
     * @param backingType
     *            the object backing the value of enum, most commonly this is a string
     *
     * @return the effective definition for enumeration
     */
    protected EnumDefinition buildEnumDefinition(String nodeName, JsonNode node, JType backingType) {

      JsonNode enums = node.path("enum");
      JsonNode javaEnumNames = node.path("javaEnumNames");
      JsonNode javaEnums = node.path("javaEnums");

      if(!javaEnums.isMissingNode() && !javaEnumNames.isMissingNode()) {
        System.err.println("Both javaEnums and javaEnumNames provided; the property javaEnumNames will be ignored when both javaEnums and javaEnumNames are provided.");
      }

      if(!javaEnumNames.isMissingNode()) {
        System.err.println("javaEnumNames is deprecated; please migrate to javaEnums.");
      }

      EnumDefinition enumDefinition;

      if(!javaEnums.isMissingNode()) {
          enumDefinition = buildEnumDefinitionWithJavaEnumsExtension(nodeName, node, enums, javaEnums, backingType);
      } else if(!javaEnumNames.isMissingNode()) {
          enumDefinition = buildEnumDefinitionWithJavaEnumNamesExtension(nodeName, node, enums, javaEnumNames, backingType);
      } else {
          enumDefinition = buildEnumDefinitionWithNoExtensions(nodeName, node, enums, backingType);
      }

      return enumDefinition;
    }

    protected EnumDefinition buildEnumDefinitionWithNoExtensions(String nodeName, JsonNode parentNode, JsonNode enums, JType backingType) {
        ArrayList<EnumValueDefinition> enumValues = new ArrayList<>();

        Collection<String> existingConstantNames = new ArrayList<>();

        for (int i = 0; i < enums.size(); i++) {
            JsonNode value = enums.path(i);

            if (!value.isNull()) {
                String constantName = getConstantName(value.asText(), null);
                constantName = makeUnique(constantName, existingConstantNames);
                existingConstantNames.add(constantName);

                enumValues.add(new EnumValueDefinition(constantName, value.asText()));
            }
        }

        return new EnumDefinition(nodeName, parentNode, backingType, enumValues, EnumDefinitionExtensionType.NONE);
    }

    protected EnumDefinition buildEnumDefinitionWithJavaEnumNamesExtension(String nodeName, JsonNode parentNode, JsonNode enums, JsonNode javaEnumNames, JType backingType) {

        ArrayList<EnumValueDefinition> enumValues = new ArrayList<>();

        Collection<String> existingConstantNames = new ArrayList<>();

        for (int i = 0; i < enums.size(); i++) {
            JsonNode value = enums.path(i);

            if (!value.isNull()) {
                String constantName = getConstantName(value.asText(), javaEnumNames.path(i).asText());
                constantName = makeUnique(constantName, existingConstantNames);
                existingConstantNames.add(constantName);

                enumValues.add(new EnumValueDefinition(constantName, value.asText(), javaEnumNames));
            }
        }

        return new EnumDefinition(nodeName, parentNode, backingType, enumValues, EnumDefinitionExtensionType.JAVA_ENUM_NAMES);
    }

    protected EnumDefinition buildEnumDefinitionWithJavaEnumsExtension(String nodeName, JsonNode enumNode, JsonNode enums, JsonNode javaEnums, JType type) {
        ArrayList<EnumValueDefinition> enumValues = new ArrayList<>();

        Collection<String> existingConstantNames = new ArrayList<>();

        for (int i = 0; i < enums.size(); i++) {
            JsonNode value = enums.path(i);

            if (!value.isNull()) {
                JsonNode javaEnumNode = javaEnums.path(i);

                if(javaEnumNode.isMissingNode()) {
                    System.err.println("javaEnum entry for " + value.asText() + " was not found.");
                }

                String constantName = getConstantName(value.asText(), javaEnumNode.path("name").asText());
                constantName = makeUnique(constantName, existingConstantNames);
                existingConstantNames.add(constantName);

                JsonNode titleNode = javaEnumNode.path("title");
                JsonNode descriptionNode = javaEnumNode.path("description");

                enumValues.add(new EnumValueDefinition(constantName, value.asText(), javaEnumNode, titleNode, descriptionNode));
            }
        }

        return new EnumDefinition(nodeName, enumNode, type, enumValues, EnumDefinitionExtensionType.JAVA_ENUMS);
    }

    protected JDefinedClass createEnum(JsonNode node, String nodeName, JClassContainer container) throws ClassAlreadyExistsException {

        try {
            if (node.has("javaType")) {
                String fqn = node.get("javaType").asText();

                if (isPrimitive(fqn, container.owner())) {
                    throw new GenerationException("Primitive type '" + fqn + "' cannot be used as an enum.");
                }

                try {
                    Class<?> existingClass = Thread.currentThread().getContextClassLoader().loadClass(fqn);
                    throw new ClassAlreadyExistsException(container.owner().ref(existingClass));
                } catch (ClassNotFoundException e) {
                    return container.owner()._class(fqn, ClassType.ENUM);
                }
            } else {
                try {
                    return container._class(JMod.PUBLIC, getEnumName(nodeName, node, container), ClassType.ENUM);
                } catch (JClassAlreadyExistsException e) {
                    throw new GenerationException(e);
                }
            }
        } catch (JClassAlreadyExistsException e) {
            throw new ClassAlreadyExistsException(e.getExistingClass());
        }
    }

    protected JFieldVar addConstructorAndFields(EnumDefinition enumDefinition, JDefinedClass _enum) {

        JType backingType = enumDefinition.getBackingType();
        JFieldVar valueField = _enum.field(JMod.PRIVATE | JMod.FINAL, backingType, VALUE_FIELD_NAME);

        JMethod constructor = _enum.constructor(JMod.PRIVATE);
        JVar valueParam = constructor.param(backingType, VALUE_FIELD_NAME);
        JBlock body = constructor.body();
        body.assign(JExpr._this().ref(valueField), valueParam);

        return valueField;
    }

    protected void addFactoryMethod(EnumDefinition enumDefinition, JDefinedClass _enum) {

        JType backingType = enumDefinition.getBackingType();
        JFieldVar quickLookupMap = addQuickLookupMap(enumDefinition, _enum);

        JMethod fromValue = _enum.method(JMod.PUBLIC | JMod.STATIC, _enum, "fromValue");
        JVar valueParam = fromValue.param(backingType, "value");

        JBlock body = fromValue.body();
        JVar constant = body.decl(_enum, "constant");
        constant.init(quickLookupMap.invoke("get").arg(valueParam));

        JConditional _if = body._if(constant.eq(JExpr._null()));

        JInvocation illegalArgumentException = JExpr._new(_enum.owner().ref(IllegalArgumentException.class));
        JExpression expr = valueParam;

        // if string no need to add ""
        if(!isString(backingType)){
            expr = expr.plus(JExpr.lit(""));
        }

        illegalArgumentException.arg(expr);
        _if._then()._throw(illegalArgumentException);
        _if._else()._return(constant);

        ruleFactory.getAnnotator().enumCreatorMethod(_enum, fromValue);
    }

    protected void addFieldAccessors(JDefinedClass _enum, JFieldVar valueField) {
        JMethod fromValue = _enum.method(JMod.PUBLIC, valueField.type(), "value");

        JBlock body = fromValue.body();
        body._return(JExpr._this().ref(valueField));

        ruleFactory.getAnnotator().enumValueMethod(_enum, fromValue);
    }

    protected JFieldVar addQuickLookupMap(EnumDefinition enumDefinition, JDefinedClass _enum) {

        JType backingType = enumDefinition.getBackingType();

        JClass lookupType = _enum.owner().ref(Map.class).narrow(backingType.boxify(), _enum);
        JFieldVar lookupMap = _enum.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, lookupType, "CONSTANTS");

        JClass lookupImplType = _enum.owner().ref(HashMap.class).narrow(backingType.boxify(), _enum);
        lookupMap.init(JExpr._new(lookupImplType));

        JForEach forEach = _enum.init().forEach(_enum, "c", JExpr.invoke("values"));
        JInvocation put = forEach.body().invoke(lookupMap, "put");
        put.arg(forEach.var().ref("value"));
        put.arg(forEach.var());

        return lookupMap;
    }

    protected void addToString(JDefinedClass _enum, JFieldVar valueField) {
        JMethod toString = _enum.method(JMod.PUBLIC, String.class, "toString");
        JBlock body = toString.body();

        JExpression toReturn = JExpr._this().ref(valueField);
        if(!isString(valueField.type())){
            toReturn = toReturn.plus(JExpr.lit(""));
        }

        body._return(toReturn);

        toString.annotate(Override.class);
    }

    protected boolean isString(JType type){
        return type.fullName().equals(String.class.getName());
    }

    protected String getEnumName(String nodeName, JsonNode node, JClassContainer container) {
        String fieldName = ruleFactory.getNameHelper().getClassName(nodeName, node);
        String className = ruleFactory.getNameHelper().replaceIllegalCharacters(capitalize(fieldName));
        String normalizedName = ruleFactory.getNameHelper().normalizeName(className);

        Collection<String> existingClassNames = new ArrayList<>();
        for (Iterator<JDefinedClass> classes = container.classes(); classes.hasNext();) {
            existingClassNames.add(classes.next().name());
        }
        return makeUnique(normalizedName, existingClassNames);
    }

    protected String makeUnique(final String name, Collection<String> existingNames) {
        boolean found = false;

        for (String existingName : existingNames) {
            if (name.equalsIgnoreCase(existingName)) {
                found = true;
                break;
            }
        }

        if (found) {
            String newName = makeUnique(name + "_", existingNames);
            System.err.println("Enum name " + name + " already used; trying to replace it with " + newName);
            return newName;
        }

        return name;
    }

    protected String getConstantName(String nodeName, String customName) {
        if (isNotBlank(customName)) {
            return customName;
        }

        List<String> enumNameGroups = new ArrayList<>(asList(splitByCharacterTypeCamelCase(nodeName)));

        String enumName = "";
        for (Iterator<String> iter = enumNameGroups.iterator(); iter.hasNext();) {
            if (containsOnly(ruleFactory.getNameHelper().replaceIllegalCharacters(iter.next()), "_")) {
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

    protected void addInterfaces(JDefinedClass jclass, JsonNode javaInterfaces) {
        for (JsonNode i : javaInterfaces) {
            jclass._implements(resolveType(jclass._package(), i.asText()));
        }
    }

}
