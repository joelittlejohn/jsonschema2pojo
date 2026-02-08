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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JType;

/**
 * Applies the "allOf" schema rule.
 * <p>
 * When "allOf" is found in a schema, all sub-schemas are deep-merged into a
 * single schema and then processed as if it were a regular schema. Any sibling
 * keywords (properties, required, type, etc.) that appear alongside "allOf"
 * are also merged into the result.
 *
 * @see <a href="https://json-schema.org/understanding-json-schema/reference/combining#allOf">allOf</a>
 */
public class AllOfRule implements Rule<JClassContainer, JType> {

    private final RuleFactory ruleFactory;

    protected AllOfRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * The allOf sub-schemas are resolved (including any $ref), deep-merged
     * into a single schema node, and then re-entered through
     * {@link SchemaRule#apply} so that the merged result is processed
     * through the normal pipeline (including enum detection, type
     * determination, etc.).
     */
    @Override
    public JType apply(String nodeName, JsonNode node, JsonNode parent, JClassContainer generatableType, Schema currentSchema) {

        JsonNode allOfNode = node.get("allOf");
        ObjectNode mergedSchema = JsonNodeFactory.instance.objectNode();

        for (JsonNode subSchema : allOfNode) {
            JsonNode resolved = resolveSubSchema(subSchema, currentSchema);
            mergedSchema = deepMerge(mergedSchema, resolved);
        }

        // Merge sibling keywords from the parent schema (everything except "allOf" itself)
        ObjectNode siblingNode = ((ObjectNode) node.deepCopy());
        siblingNode.remove("allOf");
        mergedSchema = deepMerge(mergedSchema, siblingNode);

        // Create a new root Schema wrapping the merged content. Using a null
        // ID makes this a self-referencing root, so that downstream rules
        // (e.g. PropertyRule) that resolve "#/properties/..." via
        // SchemaStore.create() will navigate this merged content rather than
        // the original schema file content.
        Schema mergedSchemaWrapper = new Schema(null, mergedSchema, null);

        // Re-enter through SchemaRule so enum, $ref, etc. are handled correctly.
        // The merged node no longer has "allOf", so there is no infinite loop.
        return ruleFactory.getSchemaRule().apply(nodeName, mergedSchema, parent, generatableType, mergedSchemaWrapper);
    }

    private JsonNode resolveSubSchema(JsonNode subSchema, Schema currentSchema) {
        if (subSchema.has("$ref")) {
            String refPath = subSchema.get("$ref").asText();
            Schema resolved = ruleFactory.getSchemaStore().create(
                    currentSchema, refPath,
                    ruleFactory.getGenerationConfig().getRefFragmentPathDelimiters());
            return resolved.getContent();
        }
        // Recursively flatten nested allOf
        if (subSchema.has("allOf")) {
            ObjectNode flattened = JsonNodeFactory.instance.objectNode();
            for (JsonNode nested : subSchema.get("allOf")) {
                JsonNode resolved = resolveSubSchema(nested, currentSchema);
                flattened = deepMerge(flattened, resolved);
            }
            ObjectNode siblingNode = ((ObjectNode) subSchema.deepCopy());
            siblingNode.remove("allOf");
            flattened = deepMerge(flattened, siblingNode);
            return flattened;
        }
        return subSchema;
    }

    private ObjectNode deepMerge(ObjectNode base, JsonNode overrideNode) {
        if (!overrideNode.isObject()) {
            return base;
        }

        ObjectNode result = base.deepCopy();
        Iterator<String> fieldNames = overrideNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode overrideValue = overrideNode.get(fieldName);

            if (result.has(fieldName)) {
                JsonNode baseValue = result.get(fieldName);

                if (baseValue.isObject() && overrideValue.isObject()) {
                    // Recursive merge for nested objects (e.g. "properties")
                    result.set(fieldName, deepMerge((ObjectNode) baseValue.deepCopy(), overrideValue));
                } else if (baseValue.isArray() && overrideValue.isArray()) {
                    // Union for arrays (e.g. "required") with deduplication
                    ArrayNode merged = (ArrayNode) baseValue.deepCopy();
                    Set<String> existing = new HashSet<>();
                    for (JsonNode n : merged) {
                        existing.add(n.asText());
                    }
                    for (JsonNode n : overrideValue) {
                        if (!existing.contains(n.asText())) {
                            merged.add(n.deepCopy());
                        }
                    }
                    result.set(fieldName, merged);
                } else {
                    // Scalars: last value wins
                    result.set(fieldName, overrideValue.deepCopy());
                }
            } else {
                result.set(fieldName, overrideValue.deepCopy());
            }
        }
        return result;
    }

}
