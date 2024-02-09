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

import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.JsonPointerUtils;
import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import org.apache.commons.lang.StringUtils;

/**
 * Applies the schema rules that represent a property definition.
 *
 * @see <a href=
 * "http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.2">http:/
 * /tools.ietf.org/html/draft-zyp-json-schema-03#section-5.2</a>
 */
public class PropertyRule implements Rule<JDefinedClass, JDefinedClass> {

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
     * (see {@link GenerationConfig#isGenerateBuilders()} ),
     * then a builder method of the form <code>withFoo(Foo foo);</code> is also
     * added.
     *
     * @param nodeName the name of the property to be applied
     * @param node     the node describing the characteristics of this property
     * @param parent   the parent node
     * @param jclass   the Java class which should have this property added
     * @return the given jclass
     */
    @Override
    public JDefinedClass apply(String nodeName, JsonNode node, JsonNode parent, JDefinedClass jclass, Schema schema) {
        String propertyName;
        if (StringUtils.isEmpty(nodeName)) {
            propertyName = "__EMPTY__";
        } else {
            propertyName = ruleFactory.getNameHelper().getPropertyName(nodeName, node);
        }

        String pathToProperty;
        if (schema.getId() == null || schema.getId().getFragment() == null) {
            pathToProperty = "#/properties/" + JsonPointerUtils.encodeReferenceToken(nodeName);
        } else {
            pathToProperty = "#" + schema.getId().getFragment() + "/properties/" + JsonPointerUtils.encodeReferenceToken(nodeName);
        }

        Schema propertySchema = ruleFactory.getSchemaStore().create(schema, pathToProperty, ruleFactory.getGenerationConfig().getRefFragmentPathDelimiters());
        JType propertyType = ruleFactory.getSchemaRule().apply(nodeName, node, parent, jclass, propertySchema);
        propertySchema.setJavaTypeIfEmpty(propertyType);

        boolean isIncludeGetters = ruleFactory.getGenerationConfig().isIncludeGetters();
        boolean isIncludeSetters = ruleFactory.getGenerationConfig().isIncludeSetters();

        node = resolveRefs(node, schema);

        int accessModifier = isIncludeGetters || isIncludeSetters ? JMod.PRIVATE : JMod.PUBLIC;
        JFieldVar field = jclass.field(accessModifier, propertyType, propertyName);

        propertyAnnotations(nodeName, node, schema, field);

        formatAnnotation(field, jclass, node);

        ruleFactory.getAnnotator().propertyField(field, jclass, nodeName, node);

        if (isIncludeGetters) {
            JMethod getter = addGetter(jclass, field, nodeName, node, isRequired(nodeName, node, schema), useOptional(nodeName, node, schema));
            ruleFactory.getAnnotator().propertyGetter(getter, jclass, nodeName);
            propertyAnnotations(nodeName, node, schema, getter);
        }

        if (isIncludeSetters) {
            JMethod setter = addSetter(jclass, field, nodeName, node);
            ruleFactory.getAnnotator().propertySetter(setter, jclass, nodeName);
            propertyAnnotations(nodeName, node, schema, setter);
        }

        if (ruleFactory.getGenerationConfig().isGenerateBuilders()) {
            addBuilderMethod(jclass, field, nodeName, node);
        }

        if (node.has("pattern")) {
            ruleFactory.getPatternRule().apply(nodeName, node.get("pattern"), node, field, schema);
        }

        ruleFactory.getDefaultRule().apply(nodeName, node.get("default"), node, field, schema);

        ruleFactory.getMinimumMaximumRule().apply(nodeName, node, parent, field, schema);

        ruleFactory.getMinItemsMaxItemsRule().apply(nodeName, node, parent, field, schema);

        ruleFactory.getMinLengthMaxLengthRule().apply(nodeName, node, parent, field, schema);

        ruleFactory.getDigitsRule().apply(nodeName, node, parent, field, schema);

        if (isObject(node) || isArray(node)) {
            ruleFactory.getValidRule().apply(nodeName, node, parent, field, schema);
        }

        return jclass;
    }

    private boolean hasEnumerated(Schema schema, String arrayFieldName, String nodeName) {
        JsonNode array = schema.getContent().get(arrayFieldName);
        if (array != null) {
            for (JsonNode requiredNode : array) {
                if (nodeName.equals(requiredNode.asText()))
                    return true;
            }
        }

        return false;
    }

    private boolean hasFlag(JsonNode node, String fieldName) {
        if (node.has(fieldName)) {
            final JsonNode requiredNode = node.get(fieldName);
            return requiredNode.asBoolean();
        }

        return false;
    }

    private boolean isDeclaredAs(String type, String nodeName, JsonNode node, Schema schema) {
        return hasEnumerated(schema, type, nodeName) || hasFlag(node, type);
    }

    private boolean isRequired(String nodeName, JsonNode node, Schema schema) {
        return isDeclaredAs("required", nodeName, node, schema);
    }

    private boolean useOptional(String nodeName, JsonNode node, Schema schema) {
        return isDeclaredAs("javaOptional", nodeName, node, schema);
    }

    private void propertyAnnotations(String nodeName, JsonNode node, Schema schema, JDocCommentable generatedJavaConstruct) {
        if (node.has("title")) {
            ruleFactory.getTitleRule().apply(nodeName, node.get("title"), node, generatedJavaConstruct, schema);
        }

        if (node.has("javaName")) {
            ruleFactory.getJavaNameRule().apply(nodeName, node.get("javaName"), node, generatedJavaConstruct, schema);
        }

        if (node.has("description")) {
            ruleFactory.getDescriptionRule().apply(nodeName, node.get("description"), node, generatedJavaConstruct, schema);
        }

        if (node.has("$comment")) {
            ruleFactory.getCommentRule().apply(nodeName, node.get("$comment"), node, generatedJavaConstruct, schema);
        }

        if (node.has("required")) {
            ruleFactory.getRequiredRule().apply(nodeName, node.get("required"), node, generatedJavaConstruct, schema);
        } else {
            ruleFactory.getNotRequiredRule().apply(nodeName, node.get("required"), node, generatedJavaConstruct, schema);
        }
    }

    private void formatAnnotation(JFieldVar field, JDefinedClass clazz, JsonNode node) {
        String format = node.path("format").asText();
        if ("date-time".equalsIgnoreCase(format)) {
            ruleFactory.getAnnotator().dateTimeField(field, clazz, node);
        } else if ("date".equalsIgnoreCase(format)) {
            ruleFactory.getAnnotator().dateField(field, clazz, node);
        } else if ("time".equalsIgnoreCase(format)) {
            ruleFactory.getAnnotator().timeField(field, clazz, node);
        } else if ("email".equalsIgnoreCase(format) && ruleFactory.getGenerationConfig().isIncludeJsr303Annotations()) {
            if (ruleFactory.getGenerationConfig().isUseJakartaValidation()) {
                field.annotate(jakarta.validation.constraints.Email.class);
            } else {
                field.annotate(javax.validation.constraints.Email.class);
            }
        }
    }

    private JsonNode resolveRefs(JsonNode node, Schema parent) {
        if (node.has("$ref")) {
            Schema refSchema = ruleFactory.getSchemaStore().create(parent, node.get("$ref").asText(), ruleFactory.getGenerationConfig().getRefFragmentPathDelimiters());
            JsonNode refNode = refSchema.getContent();
            return resolveRefs(refNode, refSchema);
        } else {
            return node;
        }
    }

    private boolean isObject(JsonNode node) {
        return node.path("type").asText().equals("object");
    }

    private boolean isArray(JsonNode node) {
        return node.path("type").asText().equals("array");
    }

    private JType getReturnType(final JDefinedClass c, final JFieldVar field, final boolean required, final boolean usesOptional) {
        JType returnType = field.type();
        if (ruleFactory.getGenerationConfig().isUseOptionalForGetters() || usesOptional) {
            if (!required && field.type().isReference()) {
                returnType = c.owner().ref("java.util.Optional").narrow(field.type());
            }
        }

        return returnType;
    }

    private JMethod addGetter(JDefinedClass c, JFieldVar field, String jsonPropertyName, JsonNode node, boolean isRequired, boolean usesOptional) {

        JType type = getReturnType(c, field, isRequired, usesOptional);

        JMethod getter = c.method(JMod.PUBLIC, type, getGetterName(jsonPropertyName, field.type(), node));

        JBlock body = getter.body();
        if ((ruleFactory.getGenerationConfig().isUseOptionalForGetters() || usesOptional) && !isRequired
                && field.type().isReference()) {
            body._return(c.owner().ref("java.util.Optional").staticInvoke("ofNullable").arg(field));
        } else {
            body._return(field);
        }

        return getter;
    }

    private JMethod addSetter(JDefinedClass c, JFieldVar field, String jsonPropertyName, JsonNode node) {
        JMethod setter = c.method(JMod.PUBLIC, void.class, getSetterName(jsonPropertyName, node));

        JVar param = setter.param(field.type(), field.name());
        JBlock body = setter.body();
        body.assign(JExpr._this().ref(field), param);

        return setter;
    }

    private JMethod addBuilderMethod(JDefinedClass c, JFieldVar field, String jsonPropertyName, JsonNode node) {
        JMethod result = null;
        if(ruleFactory.getGenerationConfig().isUseInnerClassBuilders()) {
            result = addInnerBuilderMethod(c, field, jsonPropertyName, node);
        } else {
            result = addLegacyBuilder(c, field, jsonPropertyName, node);
        }
        return result;
    }

    private JMethod addLegacyBuilder(JDefinedClass c, JFieldVar field, String jsonPropertyName, JsonNode node) {
        JMethod builder = c.method(JMod.PUBLIC, c, getBuilderName(jsonPropertyName, node));

        JVar param = builder.param(field.type(), field.name());
        JBlock body = builder.body();
        body.assign(JExpr._this().ref(field), param);
        body._return(JExpr._this());

        return builder;
    }

    private JMethod addInnerBuilderMethod(JDefinedClass c, JFieldVar field, String jsonPropertyName, JsonNode node)    {
        JDefinedClass builderClass = ruleFactory.getReflectionHelper().getBaseBuilderClass(c);

        JMethod builderMethod = builderClass.method(JMod.PUBLIC, builderClass.narrow(builderClass.typeParams()), getBuilderName(jsonPropertyName, node));

        JVar param = builderMethod.param(field.type(), field.name());
        JBlock body = builderMethod.body();
        body.assign(JExpr.ref(JExpr.cast(c, JExpr._this().ref("instance")), field), param);
        body._return(JExpr._this());

        return builderMethod;
    }

    private String getBuilderName(String propertyName, JsonNode node) {
        return ruleFactory.getNameHelper().getBuilderName(propertyName, node);
    }

    private String getSetterName(String propertyName, JsonNode node) {
        return ruleFactory.getNameHelper().getSetterName(propertyName, node);
    }

    private String getGetterName(String propertyName, JType type, JsonNode node) {
        return ruleFactory.getNameHelper().getGetterName(propertyName, type, node);
    }

}
