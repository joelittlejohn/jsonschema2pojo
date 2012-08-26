/**
 * Copyright © 2010-2011 Nokia
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

package com.googlecode.jsonschema2pojo;

import java.util.Iterator;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;

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
    public void propertyInclusion(JDefinedClass clazz) {
        clazz.annotate(JsonInclude.class).param("value", JsonInclude.Include.NON_NULL);
    }

    @Override
    public void propertyField(JFieldVar field, String propertyName) {
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

}
