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

import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.exception.ClassAlreadyExistsException;
import org.jsonschema2pojo.exception.GenerationException;

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
     * @param container
     *            the class container (class or package) to which this enum
     *            should be added
     * @return the newly generated Java type that was created to represent the
     *         given enum
     */
    @Override
    public JType apply(String nodeName, JsonNode node, JClassContainer container, Schema schema) {

        JDefinedClass _enum;
        try {
            _enum = createEnum(node, nodeName, container);
        } catch (ClassAlreadyExistsException e) {
            return e.getExistingClass();
        }

        schema.setJavaTypeIfEmpty(_enum);

        if (node.has("javaInterfaces")) {
            addInterfaces(_enum, node.get("javaInterfaces"));
        }
        
        // copy our node; remove the javaType as it will throw off the TypeRule for our case
        ObjectNode typeNode = (ObjectNode)node.deepCopy();
        typeNode.remove("javaType");

        // If type is specified on the enum, get a type rule for it.  Otherwise, we're a string.
        // (This is different from the default of Object, which is why we don't do this for every case.)
        JType backingType = node.has("type") ? 
                ruleFactory.getTypeRule().apply(nodeName, typeNode, container, schema) :
                container.owner().ref(String.class);
        
        JFieldVar valueField = addValueField(_enum, backingType);
        
        // override toString only if we have a sensible string to return
        if(isString(backingType)){
            addToString(_enum, valueField);
        }
        
        addValueMethod(_enum, valueField);
        
        addEnumConstants(node.path("enum"), _enum, node.path("javaEnumNames"), backingType);
        addFactoryMethod(_enum, backingType);

        return _enum;
    }

    private JDefinedClass createEnum(JsonNode node, String nodeName, JClassContainer container) throws ClassAlreadyExistsException {

        int modifiers = container.isPackage() ? JMod.PUBLIC : JMod.PUBLIC;

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
                    return container._class(modifiers, getEnumName(nodeName, node, container), ClassType.ENUM);
                } catch (JClassAlreadyExistsException e) {
                    throw new GenerationException(e);
                }
            }
        } catch (JClassAlreadyExistsException e) {
            throw new ClassAlreadyExistsException(e.getExistingClass());
        }
    }

    private void addFactoryMethod(JDefinedClass _enum, JType backingType) {
        JFieldVar quickLookupMap = addQuickLookupMap(_enum, backingType);

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

        ruleFactory.getAnnotator().enumCreatorMethod(fromValue);
    }

    private JFieldVar addQuickLookupMap(JDefinedClass _enum, JType backingType) {

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

    private JFieldVar addValueField(JDefinedClass _enum, JType type) {
        JFieldVar valueField = _enum.field(JMod.PRIVATE | JMod.FINAL, type, VALUE_FIELD_NAME);

        JMethod constructor = _enum.constructor(JMod.PRIVATE);
        JVar valueParam = constructor.param(type, VALUE_FIELD_NAME);
        JBlock body = constructor.body();
        body.assign(JExpr._this().ref(valueField), valueParam);

        return valueField;
    }

    private void addToString(JDefinedClass _enum, JFieldVar valueField) {
        JMethod toString = _enum.method(JMod.PUBLIC, String.class, "toString");
        JBlock body = toString.body();

        JExpression toReturn = JExpr._this().ref(valueField);
        if(!isString(valueField.type())){
            toReturn = toReturn.plus(JExpr.lit(""));
        }
        
        body._return(toReturn);

        toString.annotate(Override.class);
    }
    
    private void addValueMethod(JDefinedClass _enum, JFieldVar valueField) {
        JMethod fromValue = _enum.method(JMod.PUBLIC, valueField.type(), "value");

        JBlock body = fromValue.body();
        body._return(JExpr._this().ref(valueField));
        
        ruleFactory.getAnnotator().enumValueMethod(fromValue);
    }
    
    private boolean isString(JType type){
        return type.fullName().equals(String.class.getName());
    }

    private void addEnumConstants(JsonNode node, JDefinedClass _enum, JsonNode customNames, JType type) {
        Collection<String> existingConstantNames = new ArrayList<String>();
        for (int i = 0; i < node.size(); i++) {
            JsonNode value = node.path(i);

            if (!value.isNull()) {
                String constantName = getConstantName(value.asText(), customNames.path(i).asText());
                constantName = makeUnique(constantName, existingConstantNames);
                existingConstantNames.add(constantName);

                JEnumConstant constant = _enum.enumConstant(constantName);
                
                String typeName = type.unboxify().fullName(); 
                if(typeName.equals("int")){ // integer
                    constant.arg(JExpr.lit(value.intValue()));   
                } else if(typeName.equals("long")){ // integer-as-long
                    constant.arg(JExpr.lit(value.longValue()));
                } else if(typeName.equals("double")){ // number
                    constant.arg(JExpr.lit(value.doubleValue()));
                } else if(typeName.equals("boolean")){ // boolean
                    constant.arg(JExpr.lit(value.booleanValue()));    
                } else { // string, null, array, object?  
                    // only string should really be valid here... TODO throw error?
                    constant.arg(JExpr.lit(value.asText()));
                }
                ruleFactory.getAnnotator().enumConstant(constant, value.asText());
            }
        }
    }

    private String getEnumName(String nodeName, JsonNode node, JClassContainer container) {
        String fieldName = ruleFactory.getNameHelper().getFieldName(nodeName, node);
        String className = ruleFactory.getNameHelper().replaceIllegalCharacters(capitalize(fieldName));
        String normalizedName = ruleFactory.getNameHelper().normalizeName(className);

        Collection<String> existingClassNames = new ArrayList<String>();
        for (Iterator<JDefinedClass> classes = container.classes(); classes.hasNext();) {
            existingClassNames.add(classes.next().name());
        }
        return makeUnique(normalizedName, existingClassNames);
    }

    private String makeUnique(String name, Collection<String> existingNames) {
        boolean found = false;
        for (String existingName : existingNames) {
            if (name.equalsIgnoreCase(existingName)) {
                found = true;
                break;
            }
        }
        if (found) {
            name = makeUnique(name + "_", existingNames);
        }
        return name;
    }

    protected String getConstantName(String nodeName, String customName) {
        if (isNotBlank(customName)) {
            return customName;
        }

        List<String> enumNameGroups = new ArrayList<String>(asList(splitByCharacterTypeCamelCase(nodeName)));

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

    private void addInterfaces(JDefinedClass jclass, JsonNode javaInterfaces) {
        for (JsonNode i : javaInterfaces) {
            jclass._implements(resolveType(jclass._package(), i.asText()));
        }
    }

}
