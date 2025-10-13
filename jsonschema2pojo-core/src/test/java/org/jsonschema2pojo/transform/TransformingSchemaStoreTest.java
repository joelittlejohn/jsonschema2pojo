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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonObject;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonText;


import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.tuple.Pair;
import org.jsonschema2pojo.*;

public class TransformingSchemaStoreTest {
  
  ContentResolver resolver = new ContentResolver();
  RuleLogger logger = new NoopRuleLogger();
  SchemaTransformer transformer;
  BiFunction<SchemaTransformer.Context, Pair<URI, JsonNode>, JsonNode> transform;
  BiPredicate<Integer, Duration> until;
  SchemaStore store;

  @BeforeEach
  public void setUp() {
    until = (steps, elapsedTime)->steps > 100;
    transform = createTransform();
    transformer = new SchemaTransformer(transform, until);
    store = new TransformingSchemaStore(transformer, resolver, logger);
  }

  @AfterEach
  public void tearDown() {
    store.clearCache();
  }

  @Test
  public void shouldTransformOnLoad() throws URISyntaxException {
    URI schemaUri = getClass().getResource("/transformed/referenced.json").toURI();

    Schema schema = store.create(schemaUri, "#/.");

    assertThat(schema, is(notNullValue()));
    assertThat(schema.getId(), is(equalTo(schemaUri)));
    assertThat(schema.getContent(), is(jsonObject()
      .where("properties", is(jsonObject()
        .where("textField", is(jsonObject()
          .where("type", is(jsonText("string")))
        ))
      ))
    ));
  }

  @Test
  @Disabled("This test requires $ref, which is not supported.")
  public void shouldIncludeReferencedProperties() throws URISyntaxException {
    URI schemaUri = getClass().getResource("/transformed/reference.json").toURI();

    Schema schema = store.create(schemaUri, "#/.");

    assertThat(schema, is(notNullValue()));
    assertThat(schema.getId(), is(equalTo(schemaUri)));
    assertThat(schema.getContent(), is(jsonObject()
      .where("properties", is(jsonObject()
        .where("textField", is(jsonObject()
          .where("type", is(jsonText("string")))
        ))
      ))
    ));
  }

  public BiFunction<SchemaTransformer.Context, Pair<URI, JsonNode>, JsonNode> createTransform() {
    TransformRules rules = new TransformRules();
    return (context, schema)->{
      ObjectNode schemaNode = (ObjectNode)schema.getRight();
      ObjectNode resultNode = schemaNode.deepCopy();

      rules.mergeSchema().accept(schemaNode, resultNode);

      return resultNode;
    };
  }
}
