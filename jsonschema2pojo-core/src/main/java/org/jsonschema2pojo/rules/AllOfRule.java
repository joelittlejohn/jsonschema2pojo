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

import java.util.Iterator;
import java.util.Map;

import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JDefinedClass;

/**
 * Handles the JSON Schema "allOf" keyword by merging properties from all
 * sub-schemas into the target class. When a sub-schema is a $ref, it is
 * resolved and its properties are merged. When if/then blocks are present,
 * they are delegated to {@link IfThenElseRule}.
 */
public class AllOfRule implements Rule<JDefinedClass, JDefinedClass> {

    private final RuleFactory ruleFactory;

    protected AllOfRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public JDefinedClass apply(String nodeName, JsonNode allOfNode, JsonNode parent, JDefinedClass jclass, Schema schema) {
        if (allOfNode == null || !allOfNode.isArray()) {
            return jclass;
        }

        for (JsonNode subSchema : allOfNode) {
            if (subSchema.has("if") && subSchema.has("then")) {
                continue;
            }

            ResolvedEntry resolved = resolveIfRef(subSchema, schema);

            if (resolved.content.has("properties")) {
                mergeProperties(nodeName, resolved.content, parent, jclass, resolved.schema);
            }
        }

        return jclass;
    }

    private void mergeProperties(String nodeName, JsonNode subSchema, JsonNode parent, JDefinedClass jclass, Schema schema) {
        JsonNode properties = subSchema.get("properties");
        if (properties != null) {
            for (Iterator<Map.Entry<String, JsonNode>> fields = properties.fields(); fields.hasNext(); ) {
                Map.Entry<String, JsonNode> field = fields.next();
                String propertyName = field.getKey();
                if (jclass.fields().containsKey(ruleFactory.getNameHelper().getPropertyName(propertyName, field.getValue()))) {
                    continue;
                }
                ruleFactory.getPropertyRule().apply(propertyName, field.getValue(), properties, jclass, schema);
            }
        }
    }

    private ResolvedEntry resolveIfRef(JsonNode node, Schema schema) {
        if (node.has("$ref")) {
            Schema refSchema = ruleFactory.getSchemaStore().create(
                    schema, node.get("$ref").asText(),
                    ruleFactory.getGenerationConfig().getRefFragmentPathDelimiters());
            return new ResolvedEntry(refSchema.getContent(), refSchema);
        }
        return new ResolvedEntry(node, schema);
    }

    private static class ResolvedEntry {
        final JsonNode content;
        final Schema schema;

        ResolvedEntry(JsonNode content, Schema schema) {
            this.content = content;
            this.schema = schema;
        }
    }
}
