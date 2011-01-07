/**
 * Copyright © 2010 Nokia
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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;

import com.googlecode.jsonschema2pojo.SchemaMapper;
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
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-02#section-5.5">http://tools.ietf.org/html/draft-zyp-json-schema-02#section-5.5</a>
 */
public class AdditionalPropertiesRule implements SchemaRule<JDefinedClass, JDefinedClass> {

    private final SchemaMapper mapper;

    public AdditionalPropertiesRule(SchemaMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public JDefinedClass apply(String nodeName, JsonNode node, JDefinedClass jclass) {

        if (node != null && node.isBoolean() && node.getBooleanValue() == false) {
            // no additional properties allowed
            return jclass;
        }

        JType propertyType;
        if (node != null && node.size() != 0) {
            propertyType = mapper.getTypeRule().apply(nodeName + "Property", node, jclass.getPackage());
        } else {
            propertyType = jclass.owner().ref(Object.class);
        }

        JFieldVar field = addAdditionalPropertiesField(jclass, propertyType);

        addGetter(jclass, field);

        addSetter(jclass, propertyType, field);

        return jclass;
    }

    private JFieldVar addAdditionalPropertiesField(JDefinedClass jclass, JType propertyType) {
        JClass propertiesMapType = jclass.owner().ref(Map.class);
        propertiesMapType = propertiesMapType.narrow(jclass.owner().ref(String.class), propertyType.boxify());

        JClass propertiesMapImplType = jclass.owner().ref(HashMap.class);
        propertiesMapImplType = propertiesMapImplType.narrow(jclass.owner().ref(String.class), propertyType.boxify());

        JFieldVar field = jclass.field(JMod.PRIVATE, propertiesMapType, "additionalProperties");
        field.init(JExpr._new(propertiesMapImplType));

        return field;
    }

    private void addSetter(JDefinedClass jclass, JType propertyType, JFieldVar field) {
        JMethod setter = jclass.method(JMod.PUBLIC, void.class, "setAdditionalProperties");
        setter.annotate(JsonAnySetter.class);

        JVar nameParam = setter.param(String.class, "name");
        JVar valueParam = setter.param(propertyType, "value");

        JInvocation mapInvocation = setter.body().invoke(JExpr._this().ref(field), "put");
        mapInvocation.arg(nameParam);
        mapInvocation.arg(valueParam);
    }

    private JMethod addGetter(JDefinedClass jclass, JFieldVar field) {
        JMethod getter = jclass.method(JMod.PUBLIC, field.type(), "getAdditionalProperties");
        getter.annotate(JsonAnyGetter.class);
        getter.body()._return(JExpr._this().ref(field));
        return getter;
    }

}
