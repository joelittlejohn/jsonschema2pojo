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

package org.jsonschema2pojo;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.sun.codemodel.JAnnotationUse;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonValue;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonView;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;

/**
 * Annotates generated Java types using the Jackson 1.x mapping annotations.
 *
 * @see <a href="http://jackson.codehaus.org/">http://jackson.codehaus.org/</a>
 */
public class Jackson1Annotator extends AbstractTypeInfoAwareAnnotator {

    private final JsonSerialize.Inclusion inclusionLevel;

    public Jackson1Annotator(GenerationConfig generationConfig) {
        super(generationConfig);
        switch (generationConfig.getInclusionLevel()) {
            case ALWAYS:
                inclusionLevel = JsonSerialize.Inclusion.ALWAYS;
                break;
            case NON_ABSENT:
                inclusionLevel = JsonSerialize.Inclusion.NON_NULL;
                break;
            case NON_DEFAULT:
                inclusionLevel = JsonSerialize.Inclusion.NON_DEFAULT;
                break;
            case NON_EMPTY:
                inclusionLevel = JsonSerialize.Inclusion.NON_EMPTY;
                break;
            case NON_NULL:
                inclusionLevel = JsonSerialize.Inclusion.NON_NULL;
                break;
            case USE_DEFAULTS:
                inclusionLevel = JsonSerialize.Inclusion.NON_NULL;
                break;
            default:
                inclusionLevel = JsonSerialize.Inclusion.NON_NULL;
                break;
        }
    }

    @Override
    public void propertyOrder(JDefinedClass clazz, JsonNode propertiesNode) {
        JAnnotationArrayMember annotationValue = clazz.annotate(JsonPropertyOrder.class).paramArray("value");

        for (Iterator<String> properties = propertiesNode.fieldNames(); properties.hasNext();) {
            annotationValue.param(properties.next());
        }
    }

    @Override
    public void propertyInclusion(JDefinedClass clazz, JsonNode schema) {
        clazz.annotate(JsonSerialize.class).param("include", inclusionLevel);
    }

    @Override
    public void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
        field.annotate(JsonProperty.class).param("value", propertyName);
        if (field.type().erasure().equals(field.type().owner().ref(Set.class))) {
            field.annotate(JsonDeserialize.class).param("as", LinkedHashSet.class);
        }

        if (propertyNode.has("javaJsonView")) {
            field.annotate(JsonView.class).param(
                    "value", field.type().owner().ref(propertyNode.get("javaJsonView").asText()));
        }
    }

    @Override
    public void propertyGetter(JMethod getter, JDefinedClass clazz, String propertyName) {
        getter.annotate(JsonProperty.class).param("value", propertyName);
    }

    @Override
    public void propertySetter(JMethod setter, JDefinedClass clazz, String propertyName) {
        setter.annotate(JsonProperty.class).param("value", propertyName);
    }

    @Override
    public void anyGetter(JMethod getter, JDefinedClass clazz) {
        getter.annotate(JsonAnyGetter.class);
    }

    @Override
    public void anySetter(JMethod setter, JDefinedClass clazz) {
        setter.annotate(JsonAnySetter.class);
    }

    @Override
    public void enumCreatorMethod(JDefinedClass _enum, JMethod creatorMethod) {
        creatorMethod.annotate(JsonCreator.class);
    }

    @Override
    public void enumValueMethod(JDefinedClass _enum, JMethod valueMethod) {
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

    protected void addJsonTypeInfoAnnotation(JDefinedClass jclass, String propertyName) {
        JAnnotationUse jsonTypeInfo = jclass.annotate(JsonTypeInfo.class);
        jsonTypeInfo.param("use", JsonTypeInfo.Id.CLASS);
        jsonTypeInfo.param("include", JsonTypeInfo.As.PROPERTY);

        // When not provided it will use default provided by "use" attribute
        if(StringUtils.isNotBlank(propertyName)) {
            jsonTypeInfo.param("property", propertyName);
        }
    }
}
