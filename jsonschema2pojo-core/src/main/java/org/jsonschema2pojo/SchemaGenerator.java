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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import org.jsonschema2pojo.exception.GenerationException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SchemaGenerator {

    private final ObjectMapper objectMapper;

    public SchemaGenerator() {
        this(null);
    }

    public SchemaGenerator(JsonFactory jsonFactory) {
        this.objectMapper = new ObjectMapper(jsonFactory)
                .enable(JsonParser.Feature.ALLOW_COMMENTS)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    }

    public ObjectNode schemaFromExample(URL example) {

        try (InputStream in = example.openStream()) {
            JsonNode content = this.objectMapper.readTree(in);
            return schemaFromExample(content);
        } catch (IOException e) {
            throw new GenerationException("Could not process JSON in source file", e);
        }

    }

    public ObjectNode schemaFromExample(JsonNode example) {

        if (example.isObject()) {
            return objectSchema(example);
        } else if (example.isArray()) {
            return arraySchema(example);
        } else {
            return simpleTypeSchema(example);
        }

    }

    private ObjectNode objectSchema(JsonNode exampleObject) {

        ObjectNode schema = this.objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = this.objectMapper.createObjectNode();
        for (Iterator<String> iter = exampleObject.fieldNames(); iter.hasNext(); ) {
            String property = iter.next();
            properties.set(property, schemaFromExample(exampleObject.get(property)));
        }
        schema.set("properties", properties);

        return schema;
    }

    private ObjectNode arraySchema(JsonNode exampleArray) {
        ObjectNode schema = this.objectMapper.createObjectNode();

        schema.put("type", "array");

        if (exampleArray.size() > 0) {

            JsonNode exampleItem = exampleArray.get(0).isObject() ? mergeArrayItems(exampleArray) : exampleArray.get(0);

            schema.set("items", schemaFromExample(exampleItem));
        }

        return schema;
    }

    private JsonNode mergeArrayItems(JsonNode exampleArray) {

        ObjectNode mergedItems = this.objectMapper.createObjectNode();

        for (JsonNode item : exampleArray) {
            if (item.isObject()) {
                mergeObjectNodes(mergedItems, (ObjectNode) item);
            }
        }

        return mergedItems;
    }

    private ObjectNode mergeObjectNodes(ObjectNode targetNode, ObjectNode updateNode) {
        Iterator<String> fieldNames = updateNode.fieldNames();
        while (fieldNames.hasNext()) {

            String fieldName = fieldNames.next();
            JsonNode targetValue = targetNode.get(fieldName);
            JsonNode updateValue = updateNode.get(fieldName);

            if (targetValue == null) {
                // Target node doesn't have this field from update node: just add it
                targetNode.set(fieldName, updateValue);

            } else {
                // Both nodes have the same field: merge the values
                if (targetValue.isObject() && updateValue.isObject()) {
                    // Both values are objects: recurse
                    targetNode.set(fieldName, mergeObjectNodes((ObjectNode) targetValue, (ObjectNode) updateValue));
                } else if (targetValue.isArray() && updateValue.isArray()) {
                    // Both values are arrays: concatenate them to be merged later
                    ((ArrayNode) targetValue).addAll((ArrayNode) updateValue);
                } else if (!updateValue.isNull()) {
                    // Values have different types and new value is not null: use the one from the update node
                    targetNode.set(fieldName, updateValue);
                }
            }
        }

        return targetNode;
    }

    private ObjectNode simpleTypeSchema(JsonNode exampleValue) {
        if (exampleValue.isNull()) {
            return objectMapper.createObjectNode().putNull("type");
        } else if (exampleValue.isLong()) {
            // longs are 'integers' in schema terms
            return objectMapper.createObjectNode().put("type", "integer").put("minimum", Long.MAX_VALUE);
        } else if (exampleValue.isInt()) {
            return objectMapper.createObjectNode().put("type", "integer");
        } else if (exampleValue.isTextual()) {
            return objectMapper.createObjectNode().put("type", "string");
        } else if (exampleValue.isNumber()) {
            return objectMapper.createObjectNode().put("type", "number");
        } else if (exampleValue.isBoolean()) {
            return objectMapper.createObjectNode().put("type", "boolean");
        } else {
            final var message = "Unsupported node of type='%s', class='%s' having value='%s'"
                    .formatted(exampleValue.getNodeType(), exampleValue.getClass().getSimpleName(), exampleValue);
            throw new UnsupportedOperationException(message);
        }
    }

}
