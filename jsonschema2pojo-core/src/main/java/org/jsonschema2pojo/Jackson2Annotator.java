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

import static org.apache.commons.lang3.StringUtils.*;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sun.codemodel.JAnnotationUse;
import org.apache.commons.lang3.StringUtils;
import org.jsonschema2pojo.rules.FormatRule;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;

/**
 * Annotates generated Java types using the Jackson 2.x mapping annotations.
 *
 * @see <a
 *      href="https://github.com/FasterXML/jackson-annotations">https://github.com/FasterXML/jackson-annotations</a>
 */
public class Jackson2Annotator extends AbstractTypeInfoAwareAnnotator {

    private final JsonInclude.Include inclusionLevel;

    public Jackson2Annotator(GenerationConfig generationConfig) {
        super(generationConfig);
        switch (generationConfig.getInclusionLevel()) {
            case ALWAYS:
                inclusionLevel = JsonInclude.Include.ALWAYS;
                break;
            case NON_ABSENT:
                inclusionLevel = JsonInclude.Include.NON_ABSENT;
                break;
            case NON_DEFAULT:
                inclusionLevel = JsonInclude.Include.NON_DEFAULT;
                break;
            case NON_EMPTY:
                inclusionLevel = JsonInclude.Include.NON_EMPTY;
                break;
            case NON_NULL:
                inclusionLevel = JsonInclude.Include.NON_NULL;
                break;
            case USE_DEFAULTS:
                inclusionLevel = JsonInclude.Include.USE_DEFAULTS;
                break;
            default:
                inclusionLevel = JsonInclude.Include.NON_NULL;
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
        clazz.annotate(JsonInclude.class).param("value", inclusionLevel);
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

        if (propertyNode.has("description")) {
            field.annotate(JsonPropertyDescription.class).param("value", propertyNode.get("description").asText());
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
    public void enumConstant(JDefinedClass _enum, JEnumConstant constant, String value) {
    }

    @Override
    public boolean isAdditionalPropertiesSupported() {
        return true;
    }

    @Override
    public void additionalPropertiesField(JFieldVar field, JDefinedClass clazz, String propertyName) {
        field.annotate(JsonIgnore.class);
    }

    @Override
    public void dateField(JFieldVar field, JDefinedClass clazz, JsonNode node) {

        String pattern = null;
        if (node.has("customDatePattern")) {
            pattern = node.get("customDatePattern").asText();
        } else if (node.has("customPattern")) {
            pattern = node.get("customPattern").asText();
        } else if (isNotEmpty(getGenerationConfig().getCustomDatePattern())) {
            pattern = getGenerationConfig().getCustomDatePattern();
        } else if (getGenerationConfig().isFormatDates()) {
            pattern = FormatRule.ISO_8601_DATE_FORMAT;
        }

        if (pattern != null && !field.type().fullName().equals("java.lang.String")) {
            field.annotate(JsonFormat.class).param("shape", JsonFormat.Shape.STRING).param("pattern", pattern);
        }
    }

    @Override
    public void timeField(JFieldVar field, JDefinedClass clazz, JsonNode node) {

        String pattern = null;
        if (node.has("customTimePattern")) {
            pattern = node.get("customTimePattern").asText();
        } else if (node.has("customPattern")) {
            pattern = node.get("customPattern").asText();
        } else if (isNotEmpty(getGenerationConfig().getCustomTimePattern())) {
            pattern = getGenerationConfig().getCustomTimePattern();
        } else if (getGenerationConfig().isFormatDates()) {
            pattern = FormatRule.ISO_8601_TIME_FORMAT;
        }

        if (pattern != null && !field.type().fullName().equals("java.lang.String")) {
            field.annotate(JsonFormat.class).param("shape", JsonFormat.Shape.STRING).param("pattern", pattern);
        }
    }

    @Override
    public void dateTimeField(JFieldVar field, JDefinedClass clazz, JsonNode node) {
        String timezone = node.has("customTimezone") ? node.get("customTimezone").asText() : "UTC";

        String pattern = null;
        if (node.has("customDateTimePattern")) {
            pattern = node.get("customDateTimePattern").asText();
        } else if (node.has("customPattern")) {
            pattern = node.get("customPattern").asText();
        } else if (isNotEmpty(getGenerationConfig().getCustomDateTimePattern())) {
            pattern = getGenerationConfig().getCustomDateTimePattern();
        } else if (getGenerationConfig().isFormatDateTimes()) {
            pattern = FormatRule.ISO_8601_DATETIME_FORMAT;
        }

        if (pattern != null && !field.type().fullName().equals("java.lang.String")) {
            field.annotate(JsonFormat.class).param("shape", JsonFormat.Shape.STRING).param("pattern", pattern).param("timezone", timezone);
        }
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
