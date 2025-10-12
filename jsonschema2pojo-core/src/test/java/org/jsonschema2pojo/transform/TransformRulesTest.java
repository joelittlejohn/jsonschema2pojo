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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static com.spotify.hamcrest.jackson.JsonMatchers.jsonObject;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonText;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonArray;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonMissing;

public class TransformRulesTest {
  static ObjectMapper mapper = new ObjectMapper();
  static TransformRules rules = new TransformRules();
  @Test
  public void allOfNoOverlap() throws URISyntaxException, IOException {
      Pair<URI, ObjectNode> schema = loadSchema("/transformed/all-of-no-overlap.json");

      ObjectNode result = schema.getRight().deepCopy();

      rules.mergeSchema().accept(schema.getRight(), result);

    assertThat(result, is(jsonObject()
      .where("properties", is(jsonObject()
        .where("first", is(jsonObject()
          .where("type", is(jsonText("string")))
        ))
        .where("second", is(jsonObject()
          .where("type", is(jsonText("string")))
        ))
        .where("third", is(jsonObject()
          .where("type", is(jsonText("string")))
        ))
      ))
    ));
  }

  @Test
  public void allOfMultipleTypes() throws URISyntaxException, IOException {
    Pair<URI, ObjectNode> schema = loadSchema("/transformed/all-of-multiple-types.json");

    ObjectNode result = schema.getRight().deepCopy();

    rules.mergeSchema().accept(schema.getRight(), result);

    assertThat(result, is(jsonObject()
      .where("properties", is(jsonObject()
        .where("first", is(jsonObject()
          .where("type", is(jsonArray(contains(jsonText("number"), jsonText("string")))))
        ))
      ))
    ));
  }

  @Test
  public void allOfValidatorsNotCopied() throws URISyntaxException, IOException {
    Pair<URI, ObjectNode> schema = loadSchema("/transformed/all-of-multiple-types.json");

    ObjectNode result = schema.getRight().deepCopy();

    rules.mergeSchema().accept(schema.getRight(), result);

    assertThat(result, is(jsonObject()
      .where("properties", is(jsonObject()
        .where("first", is(jsonObject()
          .where("type", is(jsonArray(contains(jsonText("number"), jsonText("string")))))
          .where("maximum", is(jsonMissing()))
        ))
      ))
    ));
  }

  public static Pair<URI, ObjectNode> loadSchema(String path) throws IOException, URISyntaxException {
    URL url = TransformRulesTest.class.getResource(path);
    String content = IOUtils.toString(url, StandardCharsets.UTF_8);
    return Pair.of(
      url.toURI(),
      (ObjectNode)mapper.readTree(content)
    );
  }
}
