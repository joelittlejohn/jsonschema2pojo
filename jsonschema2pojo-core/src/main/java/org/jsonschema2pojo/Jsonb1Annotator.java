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

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;

import org.jsonschema2pojo.rules.FormatRule;

import javax.json.bind.annotation.JsonbDateFormat;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.json.bind.annotation.JsonbTransient;

import java.util.Iterator;

/**
 * Annotates generated Java types using the JSON-B 1 mapping annotations. Implementation inspired by
 * Jackson2Annotator.
 */
public class Jsonb1Annotator extends AbstractAnnotator {

    public Jsonb1Annotator(GenerationConfig generationConfig) {
        super(generationConfig);
    }

    @Override
    public void propertyOrder(JDefinedClass clazz, JsonNode propertiesNode) {
        JAnnotationArrayMember annotationValue = clazz.annotate(JsonbPropertyOrder.class).paramArray("value");

        for (Iterator<String> properties = propertiesNode.fieldNames(); properties.hasNext();) {
            annotationValue.param(properties.next());
        }
    }

    @Override
    public void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
        field.annotate(JsonbProperty.class).param("value", propertyName);
    }

    @Override
    public void propertyGetter(JMethod getter, JDefinedClass clazz, String propertyName) {
        getter.annotate(JsonbProperty.class).param("value", propertyName);
    }

    @Override
    public void propertySetter(JMethod setter, JDefinedClass clazz, String propertyName) {
        setter.annotate(JsonbProperty.class).param("value", propertyName);
    }

    @Override
    public boolean isAdditionalPropertiesSupported() {
        return true;
    }

    @Override
    public void additionalPropertiesField(JFieldVar field, JDefinedClass clazz, String propertyName) {
        field.annotate(JsonbTransient.class);
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

        if (!field.type().fullName().equals("java.lang.String")) {
            pattern = pattern != null? pattern : FormatRule.ISO_8601_DATE_FORMAT;
            field.annotate(JsonbDateFormat.class).param("value", pattern);
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

        if (!field.type().fullName().equals("java.lang.String")) {
            pattern = pattern != null? pattern : FormatRule.ISO_8601_TIME_FORMAT;
            field.annotate(JsonbDateFormat.class).param("value", pattern);
        }
    }

    @Override
    public void dateTimeField(JFieldVar field, JDefinedClass clazz, JsonNode node) {
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

        if (!field.type().fullName().equals("java.lang.String")) {
            pattern = pattern != null? pattern : FormatRule.ISO_8601_DATETIME_FORMAT;
            field.annotate(JsonbDateFormat.class).param("value", pattern);
        }
    }
}
