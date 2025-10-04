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

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.jsonschema2pojo.ContentResolver;

import com.fasterxml.jackson.databind.node.ObjectNode;

import net.karneim.pojobuilder.GeneratePojoBuilder;

@GeneratePojoBuilder(excludeProperties = "inputSchemas")
public class SchemaTransformer {

  List<MutablePair<URI, ObjectNode>> inputSchemas = new ArrayList<>();
  BiFunction<Context, Pair<URI, ObjectNode>, ObjectNode> transform;
  ContentResolver resolver;

  public SchemaTransformer add(URI uri, ObjectNode node) {
    inputSchemas.add(MutablePair.of(uri, node));
    return this;
  }

  /**
   * Applies transform the the input Schemas until 
   */
  public List<Pair<Integer, ReductionResult>> applyUntil(
    BiPredicate<Integer, Duration> until
  )
  {
    Instant startTime = Instant.now();
    LinkedList<Pair<Integer, ReductionResult>> results = new LinkedList<>();
    for( int i = 0; !until.test(i, Duration.between(startTime, Instant.now())); i++ ) {
      ReductionResult result = gatherScatter();

      results.add(Pair.of(i, result));

      if ( result.getErrors().size() > 0 ) {
        Pair<Pair<URI, ObjectNode>, Throwable> toThrow = result.getErrors().get(0);

        throw new RuntimeException("Error: "+toThrow.getLeft().getLeft(), toThrow.getRight());
      }

      if ( result.getUpdates() == 0 ) {
        return results;
      }
    }

    return results;
  }

  public ReductionResult gatherScatter() {
    // create a context for the cycle

    Context context = new ContextBuilder()
      .withInputSchemas(inputSchemas)
      .withResolver(resolver)
      .build();

    // go over the pairs.
    List<Triple<Pair<URI, ObjectNode>, Boolean, Optional<Throwable>>> results = inputSchemas.stream()
      // apply the reducer
      .map(pair->applyTransform(transform, context, pair))
      .collect(Collectors.toList());
    
    Integer updates = (int)results.stream().filter(e->e.getMiddle()).count();
    
    List<Pair<Pair<URI, ObjectNode>, Throwable>> errors = results.stream()
      .filter(t->t.getRight().isPresent())
      .map(e->Pair.of(e.getLeft(), e.getRight().get()))
      .collect(Collectors.toList());

    // scatter the results
    results.stream()
      .filter(e->e.getMiddle())
      .forEach(result->{
        inputSchemas.stream()
          .filter(e->e.getLeft().equals(result.getLeft().getLeft()))
          .findFirst()
          .ifPresent(
            e->e.setRight(result.getLeft().getRight())
          );
      });

    return new ReductionResultBuilder()
      .withUpdates(updates)
      .withErrors(errors)
      .build();
  }

  public Triple<Pair<URI, ObjectNode>, Boolean, Optional<Throwable>> applyTransform(BiFunction<Context, Pair<URI, ObjectNode>, ObjectNode> reducer, Context context, Pair<URI, ObjectNode> value) {
    Optional<ObjectNode> result = Optional.empty();
    Optional<Throwable> exception = Optional.empty();
    try {
      result = Optional.of(reducer.apply(context, value));
    } catch ( Throwable t ) {
      exception = Optional.of(t);
    }

    Boolean changed = result
      .map(resultValue->!resultValue.equals(value.getRight()))
      .orElse(false);

    return Triple.of(Pair.of(value.getLeft(), result.orElse(value.getRight())), changed, exception);
  }

  @GeneratePojoBuilder
  public static class Context {
    List<MutablePair<URI, ObjectNode>> inputSchemas;
    ContentResolver resolver;
    public Optional<ObjectNode> findByURI(URI uri) throws URISyntaxException {
      URI documentUri = new URI(
        uri.getScheme(),
        uri.getUserInfo(),
        uri.getHost(),
        uri.getPort(),
        uri.getPath(),
        null,
        null);
      
      return inputSchemas.stream()
        .filter(p->p.getLeft().equals(documentUri))
        .findFirst()
        .map(Pair::getRight);
    }
    public List<MutablePair<URI, ObjectNode>> getInputSchemas() {
      return inputSchemas;
    }
  }

  @GeneratePojoBuilder
  public static class ReductionResult {
    Integer updates;
    List<Pair<Pair<URI, ObjectNode>, Throwable>> errors;
    public Integer getUpdates() {
      return updates;
    }
    public List<Pair<Pair<URI, ObjectNode>, Throwable>> getErrors() {
      return errors;
    }
  }
}
