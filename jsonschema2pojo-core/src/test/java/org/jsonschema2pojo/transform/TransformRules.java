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

package org.jsonschema2pojo.transform;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * An example set of transform rules.  This set of rules will work
 * with inlined schemas, but will require updates to support the
 * $ref keyword.  Specifically, the URI being transformed will need to
 * be provided, so that the lookup process will work.
 */
public class TransformRules {
  public static String ALL_OF = "allOf";
  public static String ANY_OF = "anyOf";
  public static String ONE_OF = "oneOf";
  public static String NOT    = "not";
  public static String PROPERTIES = "properties";
  public static String ADDITIONAL_PROPERTIES = "additionalProperties";
  public static String TYPE = "type";

  public BiConsumer<ObjectNode, ObjectNode> mergeSchema() {
    return allOfRule()
      .andThen(anyOfRule())
      .andThen(oneOfRule())
      .andThen(notRule())
      .andThen(typeRule())
      .andThen(propertiesRule())
      .andThen(additionalPropertiesRule());
  }

  public BiConsumer<ObjectNode, ObjectNode> arrayMergeRule(String keyword) {
    return (source, target)->{
      if ( source.has(keyword) ) {
        BiConsumer<ObjectNode, ObjectNode> merge = mergeSchema();
        ArrayNode schemaArray = (ArrayNode)source.get(keyword);
        schemaArray.valueStream().forEach(schema->merge.accept((ObjectNode)schema, target));
      }
    };
  }

  public BiConsumer<ObjectNode, ObjectNode> allOfRule() {
    return arrayMergeRule(ALL_OF);
  }

  public BiConsumer<ObjectNode, ObjectNode> anyOfRule() {
    return arrayMergeRule(ANY_OF);
  }

  public BiConsumer<ObjectNode, ObjectNode> oneOfRule() {
    return arrayMergeRule(ONE_OF);
  }

  public BiConsumer<ObjectNode, ObjectNode> notRule() {
    return (source, target)->{
      if ( source.has(NOT) ) {
        mergeSchema().accept((ObjectNode)source.get(NOT), target);
      }
    };
  }

  public BiConsumer<ObjectNode, ObjectNode> typeRule() {
    return (source, target)->{
      JsonNode sourceType = source.get(TYPE);
      JsonNode targetType = target.get(TYPE);

      if( !( sourceType == null || sourceType.isTextual() || sourceType.isArray() ) ) {
        throw new IllegalArgumentException();
      }
      if( !( targetType == null || targetType.isTextual() || targetType.isArray() ) ) {
        throw new IllegalArgumentException();
      }

      List<TextNode> types = Stream.of(Optional.ofNullable(sourceType), Optional.ofNullable(targetType))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .flatMap(node->{
          if ( node.isTextual() ) {
            return Stream.of(node.asText());
          } else {
            return node.valueStream().map(JsonNode::asText);
          }
        })
        .sorted()
        .distinct()
        .map(type->target.textNode(type))
        .collect(Collectors.toList());
      
      if ( types.size() == 0 ) {
        target.remove(TYPE);
        return;
      }
      
      if ( types.size() == 1 ) {
        target.set(TYPE, types.get(0));
        return;
      }

      target.set(TYPE, target.arrayNode().addAll(types));
    };
  }

  public BiConsumer<ObjectNode, ObjectNode> propertiesRule() {
    return (source, target)->{
      ObjectNode sourceNode = (ObjectNode)source.get(PROPERTIES);
      ObjectNode targetNode = (ObjectNode)target.get(PROPERTIES);

      if ( sourceNode == null ) {
        return;
      }

      if( targetNode == null ) {
        targetNode = target.objectNode();
        target.set(PROPERTIES, targetNode);
      }

      BiConsumer<ObjectNode, ObjectNode> merge = mergeSchema();
      sourceNode.propertyStream().forEach(property->{
        if( target.get(PROPERTIES).has(property.getKey())) {
          merge.accept((ObjectNode)property.getValue(), (ObjectNode)target.get(PROPERTIES).get(property.getKey()));
        } else {
          ((ObjectNode)target.get(PROPERTIES)).set(property.getKey(), property.getValue().deepCopy());
        }
      });
    };
  }

  public BiConsumer<ObjectNode, ObjectNode> additionalPropertiesRule() {
    return (source, target)->{
      JsonNode sourceNode = source.get(ADDITIONAL_PROPERTIES);
      JsonNode targetNode = target.get(ADDITIONAL_PROPERTIES);

      if ( !(sourceNode == null || sourceNode.isObject() || sourceNode.isBoolean()) ) {
        throw new IllegalStateException();
      }
      if ( !(targetNode == null || targetNode.isObject() || targetNode.isBoolean()) ) {
        throw new IllegalStateException();
      }

      if ( sourceNode == null ) {
        return;
      }

      if ( targetNode == null && sourceNode.isBoolean() ) {
        targetNode = target.booleanNode(sourceNode.asBoolean());
        target.set(ADDITIONAL_PROPERTIES, targetNode);
        return;
      }

      if ( targetNode == null ) {
        targetNode = target.objectNode();
        target.set(ADDITIONAL_PROPERTIES, targetNode);
      }

      if ( sourceNode.isBoolean() && targetNode.isBoolean() ) {
        target.put(ADDITIONAL_PROPERTIES, sourceNode.asBoolean() || targetNode.asBoolean());
        return;
      }
        
      if ( sourceNode.isBoolean() && targetNode.isObject() ) {
        if ( sourceNode.asBoolean() == true ) {
          target.put(ADDITIONAL_PROPERTIES, true);
          return;
        } else {
          return;
        }
      }
      
      if ( sourceNode.isObject() && targetNode.isBoolean() ) {
        if( targetNode.asBoolean() == true ) {
          return;
        } else {
          target.set(ADDITIONAL_PROPERTIES, sourceNode.deepCopy());
          return;
        }
      }
      
      if ( sourceNode.isObject() && targetNode.isObject() ) {
        if ( sourceNode.equals(targetNode) ) {
          return;
        }
        mergeSchema().accept((ObjectNode)sourceNode, (ObjectNode)targetNode);
      }
      
      throw new IllegalStateException();
    };
  }
}
