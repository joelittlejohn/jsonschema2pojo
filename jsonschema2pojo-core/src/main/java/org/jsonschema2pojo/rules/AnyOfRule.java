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

import static org.apache.commons.lang3.StringUtils.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.jsonschema2pojo.Jsonschema2Pojo;
import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

/**
 * Applies the "anyOf" and "oneOf" schema rules.
 * <p>
 * Both keywords are handled identically: a marker interface is generated
 * and each child schema produces a class that implements it. When any
 * child schema is a non-object type, the rule falls back to
 * {@link Object java.lang.Object}.
 *
 * @see <a href="https://json-schema.org/understanding-json-schema/reference/combining#anyOf">anyOf</a>
 * @see <a href="https://json-schema.org/understanding-json-schema/reference/combining#oneOf">oneOf</a>
 */
public class AnyOfRule implements Rule<JClassContainer, JType> {

    private final RuleFactory ruleFactory;

    protected AnyOfRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public JType apply(String nodeName, JsonNode node, JsonNode parent, JClassContainer generatableType, Schema currentSchema) {

        String keyword = node.has("anyOf") ? "anyOf" : "oneOf";
        JsonNode subSchemas = node.get(keyword);

        // Empty array or non-array: fall back to Object
        if (!subSchemas.isArray() || subSchemas.size() == 0) {
            return generatableType.owner().ref(Object.class);
        }

        // Check if all children are object-like schemas
        for (JsonNode subSchema : subSchemas) {
            JsonNode resolved = resolveIfRef(subSchema, currentSchema);
            if (!isObjectSchema(resolved)) {
                return generatableType.owner().ref(Object.class);
            }
        }

        // Create marker interface
        JPackage _package = generatableType.getPackage();
        String interfaceName = ruleFactory.getNameHelper().getUniqueClassName(nodeName, node, _package);
        JDefinedClass markerInterface;
        try {
            markerInterface = _package._class(JMod.PUBLIC, interfaceName, ClassType.INTERFACE);
        } catch (JClassAlreadyExistsException e) {
            return e.getExistingClass();
        }

        // Generate a type for each child schema that implements the interface
        List<JDefinedClass> generatedChildTypes = new ArrayList<>();
        for (int i = 0; i < subSchemas.size(); i++) {
            JsonNode subSchema = subSchemas.get(i);
            String childName = deriveChildName(nodeName, subSchema, i);

            // For inline schemas (no $ref), create a self-referencing root
            // Schema so that PropertyRule can resolve #/properties/... within
            // the child's own content. For $ref schemas, SchemaRule's own
            // $ref resolution will replace the schema appropriately.
            Schema childSchema = subSchema.has("$ref") ? currentSchema
                    : new Schema(null, subSchema, null);

            JType childType = ruleFactory.getSchemaRule().apply(
                    childName, subSchema, node, generatableType, childSchema);

            if (childType instanceof JDefinedClass) {
                JDefinedClass childClass = (JDefinedClass) childType;
                childClass._implements(markerInterface);
                generatedChildTypes.add(childClass);
            }
        }

        // If a discriminator is specified, add polymorphic type annotations
        if (node.has("discriminator") && node.get("discriminator").has("propertyName")) {
            String discriminatorProperty = node.get("discriminator").get("propertyName").asText();
            ruleFactory.getAnnotator().subTypeInfo(
                    markerInterface, discriminatorProperty,
                    generatedChildTypes.toArray(new JDefinedClass[0]));
        }

        return markerInterface;
    }

    private JsonNode resolveIfRef(JsonNode subSchema, Schema currentSchema) {
        if (subSchema.has("$ref")) {
            String refPath = subSchema.get("$ref").asText();
            Schema resolved = ruleFactory.getSchemaStore().create(
                    currentSchema, refPath,
                    ruleFactory.getGenerationConfig().getRefFragmentPathDelimiters());
            return resolved.getContent();
        }
        return subSchema;
    }

    private boolean isObjectSchema(JsonNode node) {
        if (node.path("type").asText().equals("object")) {
            return true;
        }
        if (node.has("properties")) {
            return true;
        }
        // No type specified and no indication of a primitive type: treat as object
        if (!node.has("type")) {
            return true;
        }
        return false;
    }

    private String deriveChildName(String parentName, JsonNode subSchema, int index) {
        // Prefer $ref target name
        if (subSchema.has("$ref")) {
            String ref = subSchema.get("$ref").asText();
            if (!"#".equals(ref)) {
                String name;
                if (!contains(ref, "#")) {
                    name = Jsonschema2Pojo.getNodeName(ref, ruleFactory.getGenerationConfig());
                } else {
                    String[] parts = split(ref, "/\\#");
                    name = parts[parts.length - 1];
                }
                return URLDecoder.decode(name, StandardCharsets.UTF_8);
            }
        }

        // Use title if available
        if (subSchema.has("title")) {
            return subSchema.get("title").asText();
        }

        // Fallback to parent name + Option + index
        return parentName + "Option" + index;
    }

}
