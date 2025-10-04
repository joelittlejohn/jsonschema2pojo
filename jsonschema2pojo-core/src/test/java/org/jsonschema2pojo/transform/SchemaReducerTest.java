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
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;

import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jsonschema2pojo.transform.SchemaReducer.Context;
import org.jsonschema2pojo.transform.SchemaReducer.ReductionResult;


public class SchemaReducerTest {
  ObjectMapper mapper = new ObjectMapper();

  @Test
  public void firstTest() throws JsonMappingException, JsonProcessingException {
    SchemaReducer reducer = new SchemaReducerBuilder()
      .withReducer(this::transform)
      .build()
      .add(
        URI.create("http://example.com/schema"),
        createObject(node->{
          ObjectNode properties = node
            .put("type", "object")
            .putObject("properties");
          
          properties.putObject("first")
            .put("type", "string");

          ArrayNode allOf = node.putArray("allOf");
          allOf.addObject()
            .put("type", "object")
            .putObject("properties")
            .putObject("second")
            .put("type", "string");
          
          allOf.addObject()
            .put("type", "object")
            .putObject("properties")
            .putObject("third")
            .put("type", "string");
        })
      );

    List<Pair<Integer, ReductionResult>> results = reducer
        .approach(this::transform, 5);

    assertThat(results.size(), equalTo(2));

    ObjectNode finalState = reducer.inputSchemas.get(0).getRight();
    ObjectNode finalStateProperties = (ObjectNode)finalState.get("properties");
    assertThat(finalStateProperties, hasProperty("first"));
    assertThat(finalStateProperties, hasProperty("second"));
    assertThat(finalStateProperties, hasProperty("third"));
  }

  public ObjectNode transform( Context context, Pair<URI, ObjectNode> node ) {
    ObjectNode output = (ObjectNode)node.getRight().deepCopy();

    Optional<JsonNode> type = output.get("type").asOptional();
    Optional<ArrayNode> possibleAllOf = output.get("allOf").asOptional().map(ArrayNode.class::cast);

    Function<ObjectNode, Stream<ObjectNode>> streamProperties = 
      schema->schema
        .get("properties")
        .asOptional()
        .map(Stream::of)
        .orElseGet(Stream::empty)
        .map(ObjectNode.class::cast);

    Predicate<JsonNode> isNotEmpty = n->!n.isEmpty();
    
    List<ObjectNode> allOfProperties = possibleAllOf
      .filter(JsonNode::isArray)
      .map(JsonNode::valueStream)
      .orElse(Stream.empty())
      .map(ObjectNode.class::cast)
      .flatMap(streamProperties)
      .filter(isNotEmpty)
      .collect(Collectors.toList());

    if( !allOfProperties.isEmpty() ) {
      ObjectNode outputProperties = output.has("properties") ? (ObjectNode)output.get("properties") : output.putObject("properties");
      
      allOfProperties.stream()
        .map(JsonNode::properties)
        .flatMap(Set::stream)
        .forEach(entry->{
          if( outputProperties.has(entry.getKey()) ) {
            JsonNode outputProperty = outputProperties.get(entry.getKey());
            // TODO: merge rule goes here.
          } else {
            outputProperties.set(entry.getKey(), entry.getValue().deepCopy());
          }
        });
    }
    return output;
  }

  public ObjectNode createObject(Consumer<ObjectNode> addProperties) {
    ObjectNode node = mapper.createObjectNode();
    addProperties.accept(node);
    return node;
  }
  public ArrayNode createArray(Consumer<ArrayNode> addItems) {
    ArrayNode node = mapper.createArrayNode();
    addItems.accept(node);
    return node;
  }

  public ObjectNode createStringSchema(Consumer<ObjectNode> addProperties) {
    return createObject(props->{
      props.put("type", "string");
      addProperties.accept(props);
    });
  }

  public <T> Consumer<T> noChanges() {
    return (t)->{};
  }

  public static Matcher<ObjectNode> hasProperty(String name) {
    return new TypeSafeMatcher<ObjectNode>() {

      @Override
      public void describeTo(Description description) {
        description.appendText("obejct did not have "+name);
      }

      @Override
      protected boolean matchesSafely(ObjectNode item) {
        return item.has(name);
      }

    };
  }
}
