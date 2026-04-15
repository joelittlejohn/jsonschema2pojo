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

import java.util.ArrayList;
import java.util.List;

import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JType;

/**
 * Handles the JSON Schema "oneOf" keyword. Analyzes the sub-schemas to determine
 * the best Java type mapping:
 * <ul>
 *   <li>If all sub-schemas are $ref to object types, generates an interface or
 *       uses the first ref as the type with Jackson deduction</li>
 *   <li>If sub-schemas are a mix of types, maps to Object</li>
 *   <li>If all sub-schemas are primitive/simple types, maps to the nearest
 *       common Java supertype</li>
 * </ul>
 */
public class OneOfRule implements Rule<JClassContainer, JType> {

    private final RuleFactory ruleFactory;

    protected OneOfRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public JType apply(String nodeName, JsonNode node, JsonNode parent, JClassContainer generatableType, Schema schema) {
        JsonNode oneOfArray = node.get("oneOf");
        if (oneOfArray == null && node.has("anyOf")) {
            oneOfArray = node.get("anyOf");
        }

        if (oneOfArray == null || !oneOfArray.isArray() || oneOfArray.size() == 0) {
            return generatableType.owner().ref(Object.class);
        }

        List<JsonNode> objectSchemas = new ArrayList<>();
        List<JsonNode> otherSchemas = new ArrayList<>();
        boolean hasArray = false;

        for (JsonNode subSchema : oneOfArray) {
            JsonNode resolved = resolveRef(subSchema, schema);
            String type = resolved.has("type") ? resolved.get("type").asText() : "object";

            if ("object".equals(type) || resolved.has("properties") || resolved.has("$ref")) {
                objectSchemas.add(subSchema);
            } else if ("array".equals(type)) {
                hasArray = true;
                otherSchemas.add(subSchema);
            } else {
                otherSchemas.add(subSchema);
            }
        }

        if (objectSchemas.size() == oneOfArray.size() && objectSchemas.size() == 1) {
            return ruleFactory.getSchemaRule().apply(nodeName, objectSchemas.get(0), parent, generatableType, schema);
        }

        if (objectSchemas.size() == oneOfArray.size() && objectSchemas.size() > 1) {
            return ruleFactory.getSchemaRule().apply(nodeName, objectSchemas.get(0), parent, generatableType, schema);
        }

        if (node.has("type")) {
            return ruleFactory.getTypeRule().apply(nodeName, node, parent, generatableType.getPackage(), schema);
        }

        return generatableType.owner().ref(Object.class);
    }

    private JsonNode resolveRef(JsonNode node, Schema schema) {
        if (node.has("$ref")) {
            Schema refSchema = ruleFactory.getSchemaStore().create(
                    schema, node.get("$ref").asText(),
                    ruleFactory.getGenerationConfig().getRefFragmentPathDelimiters());
            return refSchema.getContent();
        }
        return node;
    }
}
