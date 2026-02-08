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

import java.lang.annotation.Annotation;

import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.model.JAnnotatedClass;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JType;

import jakarta.validation.Valid;

/**
 * Applies the {@code @Valid} annotation to fields that require cascading validation.
 * <p>
 * For object fields, the annotation is placed on the field declaration.
 * For array/collection fields, the annotation is placed on the item type parameter
 * (e.g., {@code List<@Valid Item>}) to ensure proper cascading validation of elements.
 */
public class ValidRule implements Rule<JFieldVar, JFieldVar> {

    private final RuleFactory ruleFactory;

    public ValidRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    /**
     * Applies the {@code @Valid} annotation to the given field if JSR-303 annotations are enabled.
     *
     * @param nodeName the name of the JSON property
     * @param node the JSON schema node
     * @param parent the parent JSON schema node
     * @param field the field to annotate
     * @param currentSchema the current schema being processed
     * @return the field, potentially with {@code @Valid} annotation added
     */
    @Override
    public JFieldVar apply(String nodeName, JsonNode node, JsonNode parent, JFieldVar field, Schema currentSchema) {

        if (ruleFactory.getGenerationConfig().isIncludeJsr303Annotations()) {
            if (isArray(node)) {
                // For arrays, the @Valid annotation is placed on the item type (e.g., List<@Valid Item>)
                // We need to rebuild the type but the type param should become an annotated type param
                JClass existingFieldType = (JClass) field.type();
                JClass existingItemType = existingFieldType.getTypeParameters().get(0);
                JClass annotatedItemType = JAnnotatedClass.of(existingItemType).annotated(getValidClass());
                JClass newFieldType = existingFieldType.erasure().narrow(annotatedItemType);
                field.type(newFieldType);
            } else if (isMap(field.type())) {
                // For maps (including additionalProperties), the @Valid annotation is placed on the value type
                // (e.g. Map<String, @Valid Item>)
                // We need to rebuild the type but the value type param should become an annotated type param
                JClass existingFieldType = (JClass) field.type();
                JClass existingKeyType = existingFieldType.getTypeParameters().get(0);
                JClass existingValueType = existingFieldType.getTypeParameters().get(1);
                JClass annotatedValueType = JAnnotatedClass.of(existingValueType).annotated(getValidClass());
                JClass newFieldType = existingFieldType.erasure().narrow(existingKeyType).narrow(annotatedValueType);
                field.type(newFieldType);
            } else {
                field.annotate(getValidClass());
            }
        }

        return field;
    }

    private Class<? extends Annotation> getValidClass() {
        return ruleFactory.getGenerationConfig().isUseJakartaValidation()
                ? Valid.class
                : javax.validation.Valid.class;
    }

    private boolean isArray(JsonNode node) {
        return node != null && node.path("type").asText().equals("array");
    }

    private boolean isMap(JType jtype) {
        return (jtype instanceof JClass) && ((JClass) jtype).erasure().isAssignableFrom(jtype.owner().ref(java.util.Map.class));
    }

}
