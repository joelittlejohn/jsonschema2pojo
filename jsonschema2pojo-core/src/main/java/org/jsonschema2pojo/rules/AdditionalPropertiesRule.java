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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

/**
 * Applies the "additionalProperties" JSON schema rule.
 *
 * @see <a href=
 *      "http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.6">http:/
 *      /tools.ietf.org/html/draft-zyp-json-schema-03#section-5.6</a>
 */
public class AdditionalPropertiesRule implements Rule<JDefinedClass, JDefinedClass> {

    private final RuleFactory ruleFactory;

    protected AdditionalPropertiesRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * If additionalProperties is specified and set to the boolean value
     * <code>false</code>, this rule does not make any change to the generated
     * Java type (the type does not allow additional properties).
     * <p>
     * If the additionalProperties node is <code>null</code> (not specified in
     * the schema) or empty, then a new bean property named
     * "additionalProperties", of type {@link Map}{@literal <String,Object>} is
     * added to the generated type (with appropriate accessors). The accessors
     * are annotated to allow unrecognised (additional) properties found in JSON
     * data to be marshalled/unmarshalled from/to this map.
     * <p>
     * If the additionalProperties node is present and specifies a schema, then
     * an "additionalProperties" map is added to the generated type. This time
     * the map values will be restricted and must be instances of a newly
     * generated Java type that will be created based on the
     * additionalProperties schema provided. If the schema does not specify the
     * javaType property, the name of the newly generated type will be derived
     * from the nodeName and the suffix 'Property'.
     *
     * @param nodeName
     *            the name of the schema node for which the additionalProperties
     *            node applies
     * @param node
     *            the additionalProperties node itself, found in the schema (may
     *            be null if not specified in the schema)
     * @param jclass
     *            the Java type that is being generated to represent this schema
     * @return the given Java type jclass
     */
    @Override
    public JDefinedClass apply(String nodeName, JsonNode node, JsonNode parent, JDefinedClass jclass, Schema schema) {

        if (node != null && node.isBoolean() && node.asBoolean() == false) {
            // no additional properties allowed
            return jclass;
        }

        if (!this.ruleFactory.getGenerationConfig().isIncludeAdditionalProperties()) {
            // no additional properties allowed
            return jclass;
        }

        if (!ruleFactory.getAnnotator().isAdditionalPropertiesSupported()) {
            // schema allows additional properties, but serializer library can't support them
            return jclass;
        }

        JType propertyType;
        if (node != null && node.size() != 0) {
            String pathToAdditionalProperties;
            if (schema.getId() == null || schema.getId().getFragment() == null) {
                pathToAdditionalProperties = "#/additionalProperties";
            } else {
                pathToAdditionalProperties = "#" + schema.getId().getFragment() + "/additionalProperties";
            }
            Schema additionalPropertiesSchema = ruleFactory.getSchemaStore().create(schema, pathToAdditionalProperties, ruleFactory.getGenerationConfig().getRefFragmentPathDelimiters());
            propertyType = ruleFactory.getSchemaRule().apply(nodeName + "Property", node, parent, jclass, additionalPropertiesSchema);
            additionalPropertiesSchema.setJavaTypeIfEmpty(propertyType);
        } else {
            propertyType = jclass.owner().ref(Object.class);
            propertyType = ruleFactory.getValidRule().apply(nodeName, node, parent, propertyType, schema);
        }

        JFieldVar field = addAdditionalPropertiesField(jclass, propertyType);

        addGetter(jclass, field);

        addSetter(jclass, propertyType, field);

        if (ruleFactory.getGenerationConfig().isGenerateBuilders()) {
            addBuilder(jclass, propertyType, field);
        }

        return jclass;
    }

    private JFieldVar addAdditionalPropertiesField(JDefinedClass jclass, JType propertyType) {
        JClass propertiesMapType = jclass.owner().ref(Map.class);
        propertiesMapType = propertiesMapType.narrow(jclass.owner().ref(String.class), propertyType.boxify());

        JClass propertiesMapImplType = jclass.owner().ref(LinkedHashMap.class);
        propertiesMapImplType = propertiesMapImplType.narrow(jclass.owner().ref(String.class), propertyType.boxify());

        JFieldVar field = jclass.field(JMod.PRIVATE, propertiesMapType, "additionalProperties");

        ruleFactory.getAnnotator().additionalPropertiesField(field, jclass, "additionalProperties");

        field.init(JExpr._new(propertiesMapImplType));

        return field;
    }

    private void addSetter(JDefinedClass jclass, JType propertyType, JFieldVar field) {
        JMethod setter = jclass.method(JMod.PUBLIC, void.class, "setAdditionalProperty");

        ruleFactory.getAnnotator().anySetter(setter, jclass);

        JVar nameParam = setter.param(String.class, "name");
        JVar valueParam = setter.param(propertyType, "value");

        JInvocation mapInvocation = setter.body().invoke(JExpr._this().ref(field), "put");
        mapInvocation.arg(nameParam);
        mapInvocation.arg(valueParam);
    }

    private JMethod addGetter(JDefinedClass jclass, JFieldVar field) {
        JMethod getter = jclass.method(JMod.PUBLIC, field.type(), "getAdditionalProperties");

        ruleFactory.getAnnotator().anyGetter(getter, jclass);

        getter.body()._return(JExpr._this().ref(field));
        return getter;
    }

    private JMethod addBuilder(JDefinedClass jclass, JType propertyType, JFieldVar field) {

        JMethod result = null;
        if(ruleFactory.getGenerationConfig().isUseInnerClassBuilders()) {
            result = addInnerBuilder(jclass, propertyType, field);
        } else {
            result = addLegacyBuilder(jclass, propertyType, field);
        }

        return result;
    }

    private JMethod addLegacyBuilder(JDefinedClass jclass, JType propertyType, JFieldVar field) {
        JMethod builder = jclass.method(JMod.PUBLIC, jclass, "withAdditionalProperty");

        JVar nameParam = builder.param(String.class, "name");
        JVar valueParam = builder.param(propertyType, "value");

        JBlock body = builder.body();
        JInvocation mapInvocation = body.invoke(JExpr._this().ref(field), "put");
        mapInvocation.arg(nameParam);
        mapInvocation.arg(valueParam);
        body._return(JExpr._this());

        return builder;
    }

    private JMethod addInnerBuilder(JDefinedClass jclass, JType propertyType, JFieldVar field) {
        Optional<JDefinedClass> builderClass = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(jclass.classes(), Spliterator.ORDERED), false)
                .filter(definedClass -> definedClass.name().equals(getBuilderClassName(jclass)))
                .findFirst();

        JMethod builder = builderClass.get().method(JMod.PUBLIC, builderClass.get(), "withAdditionalProperty");

        JVar nameParam = builder.param(String.class, "name");
        JVar valueParam = builder.param(propertyType, "value");

        JBlock body = builder.body();
        JInvocation mapInvocation = body.invoke(JExpr.ref(JExpr.cast(jclass, JExpr._this().ref("instance")), field), "put");
        mapInvocation.arg(nameParam);
        mapInvocation.arg(valueParam);
        body._return(JExpr._this());

        return builder;
    }

    private String getBuilderClassName(JDefinedClass c) {
        return ruleFactory.getNameHelper().getBaseBuilderClassName(c);
    }

}
