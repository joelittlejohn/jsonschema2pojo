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

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonValue;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;

/**
 * Annotates generated Java types using the Jackson 1.x mapping annotations.
 * 
 * @see <a href="http://jackson.codehaus.org/">http://jackson.codehaus.org/</a>
 */
public class Jackson1Annotator implements Annotator {

    @Override
    public void propertyOrder(JDefinedClass clazz, JsonNode propertiesNode) {
        JAnnotationArrayMember annotationValue = clazz.annotate(JsonPropertyOrder.class).paramArray("value");

        for (Iterator<String> properties = propertiesNode.fieldNames(); properties.hasNext();) {
            annotationValue.param(properties.next());
        }
    }

    @Override
    public void propertyInclusion(JDefinedClass clazz) {
        clazz.annotate(JsonSerialize.class).param("include", JsonSerialize.Inclusion.NON_NULL);
    }

    @Override
    public void propertyField(JFieldVar field, String propertyName) {
        JAnnotationUse jsonPropertyAnnotation = field.annotate(JsonProperty.class);
        jsonPropertyAnnotation.param("value", propertyName);
    }

    @Override
    public void propertyGetter(JMethod getter, String propertyName) {
        JAnnotationUse jsonPropertyAnnotation = getter.annotate(JsonProperty.class);
        jsonPropertyAnnotation.param("value", propertyName);
    }

    @Override
    public void propertySetter(JMethod setter, String propertyName) {
        JAnnotationUse jsonPropertyAnnotation = setter.annotate(JsonProperty.class);
        jsonPropertyAnnotation.param("value", propertyName);
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
