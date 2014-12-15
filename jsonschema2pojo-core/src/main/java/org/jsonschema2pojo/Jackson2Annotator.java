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

package org.jsonschema2pojo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationValue;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JStringLiteral;
import com.sun.codemodel.JVar;

import static org.jsonschema2pojo.rules.ObjectRule.immutableCopy;

/**
 * Annotates generated Java types using the Jackson 2.x mapping annotations.
 *
 * @see <a
 *      href="https://github.com/FasterXML/jackson-annotations">https://github.com/FasterXML/jackson-annotations</a>
 */
public class Jackson2Annotator implements Annotator {

    @Override
    public void propertyOrder(JDefinedClass clazz, JsonNode propertiesNode) {
        JAnnotationArrayMember annotationValue = clazz.annotate(JsonPropertyOrder.class).paramArray("value");

        for (Iterator<String> properties = propertiesNode.fieldNames(); properties.hasNext();) {
            annotationValue.param(properties.next());
        }
    }

    @Override
    public void propertyInclusion(JDefinedClass clazz, JsonNode schema) {
        clazz.annotate(JsonInclude.class).param("value", JsonInclude.Include.NON_NULL);
    }

    @Override
    public void objectBuilder(JDefinedClass clazz, JDefinedClass builder) {
        JMethod constructor = clazz.constructor(JMod.PRIVATE);
        constructor.annotate(JsonCreator.class);
        JBlock body = constructor.body();

        for (JVar field : clazz.fields().values()) {
            for (JAnnotationUse annotation : field.annotations()) {
                if (annotation.getAnnotationClass().name().equals("JsonProperty")) {
                    JAnnotationValue value = annotation.getAnnotationMembers().get("value");
                    if (value != null) {
                        String str;
                        try {
                            Field valueField = value.getClass().getDeclaredField("value");
                            valueField.setAccessible(true);
                            str = ((JStringLiteral) valueField.get(value)).str;
                        } catch (Exception e) {
                            throw new IllegalStateException("Field " + field.name() + " " +
                                    "is not properly annotated");
                        }

                        // In order to support default values, we need to first find the default
                        // value from the RHS of the declaration of the field in the builder, if
                        // one exists, then add a ternary to the generated code that sets the field
                        // to the default if the supplied value is null.
                        //
                        // Unfortunately the initialization value of a field is private in CodeModel
                        // so we have to resort to reflection again to get the value.
                        JExpression defaultValue;
                        try {
                            JFieldVar builderField = builder.fields().get(field.name());
                            if (builderField == null) {
                                throw new IllegalStateException("Field " + field.name() +
                                    "not found in builder " + builder.fullName());
                            }
                            Field initField = builderField.getClass()
                                .getSuperclass().getDeclaredField("init");
                            initField.setAccessible(true);
                            defaultValue = (JExpression) initField.get(builderField);
                        } catch (Exception ex) {
                            throw new IllegalStateException("Failed to get init field -- this" +
                                "indicates an incompatible change in CodeModel", ex);
                        }

                        JExpression assignment;
                        if (defaultValue != null) {
                            // this means there's a default value and we must use it if the value is
                            // null
                            assignment = JOp.cond(
                                field.eq(JExpr._null()), defaultValue, JExpr.ref(field.name()));
                        } else {
                            assignment = JExpr.ref(field.name());
                        }
                        constructor.param(field.type(), field.name())
                                .annotate(annotation.getAnnotationClass())
                                .param("value", str);
                        body.assign(JExpr._this().ref(field.name()),
                                // TODO(micah): this should respect immutability config
                                immutableCopy(assignment, field.type()));
                    }
                }
            }

            if (field.name().equals("additionalProperties")) {
                JClass propertiesMapImplType = clazz.owner().ref(HashMap.class);
                propertiesMapImplType = propertiesMapImplType.narrow(clazz.owner().ref(String.class),
                        clazz.owner().ref(Object.class));

                body.assign(JExpr._this().ref(field.name()), JExpr._new(propertiesMapImplType));
            }
        }
    }

    @Override
    public void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
        field.annotate(JsonProperty.class).param("value", propertyName);
    }

    @Override
    public void propertyGetter(JMethod getter, String propertyName) {
        getter.annotate(JsonProperty.class).param("value", propertyName);
    }

    @Override
    public void propertySetter(JMethod setter, String propertyName) {
        setter.annotate(JsonProperty.class).param("value", propertyName);
    }

    @Override
    public void anyGetter(JMethod getter) {
        getter.annotate(JsonAnyGetter.class);
    }

    @Override
    public void anySetter(JMethod setter) {
        setter.annotate(JsonAnySetter.class);
    }

    @Override
    public void enumCreatorMethod(JMethod creatorMethod) {
        creatorMethod.annotate(JsonCreator.class);
    }

    @Override
    public void enumValueMethod(JMethod valueMethod) {
        valueMethod.annotate(JsonValue.class);
    }

    @Override
    public boolean isAdditionalPropertiesSupported() {
        return true;
    }

    @Override
    public void additionalPropertiesField(JFieldVar field, JDefinedClass clazz, String propertyName) {
        field.annotate(JsonIgnore.class);
    }
}
