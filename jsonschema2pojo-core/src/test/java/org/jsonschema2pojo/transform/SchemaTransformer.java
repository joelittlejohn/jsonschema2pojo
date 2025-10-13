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
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.jsonschema2pojo.ContentResolver;
import org.jsonschema2pojo.FragmentResolver;
import org.jsonschema2pojo.util.URIUtil;

import com.fasterxml.jackson.databind.JsonNode;

import net.karneim.pojobuilder.GeneratePojoBuilder;

@GeneratePojoBuilder
public class SchemaTransformer {
  protected final FragmentResolver fragmentResolver = new FragmentResolver();
  BiFunction<Context, Pair<URI, JsonNode>, JsonNode> transform;
  BiPredicate<Integer, Duration> processUntil;

  public SchemaTransformer(
    BiFunction<Context, Pair<URI, JsonNode>, JsonNode> transform,
    BiPredicate<Integer, Duration> until
  ) {
    Objects.requireNonNull(transform);
    Objects.requireNonNull(until);
    this.transform = transform;
    this.processUntil = until;
  }

  /**
   * Transforms all schemas in the schema map, updating them
   * if they change during the transformation process.
   */
  public void transform(
    Map<URI, JsonNode> schemaMap, 
    ContentResolver resolver,
    String refFragmentPathDelimiters
  )
  {
    Instant startTime = Instant.now();

    Context context = new ContextBuilder()
      .withInputSchemas(schemaMap)
      .withFragmentResolver(fragmentResolver)
      .build();
    for( int i = 0; !processUntil.test(i, Duration.between(startTime, Instant.now())); i++ ) {
      TransformStepResult result = transformStep(context);

      if ( result.getErrors().size() > 0 ) {
        Pair<Pair<URI, JsonNode>, Throwable> toThrow = result.getErrors().get(0);

        throw new RuntimeException("Error: "+toThrow.getLeft().getLeft(), toThrow.getRight());
      }

      if ( result.getUpdates() == 0 ) {
        return;
      }
    }

    throw new RuntimeException("Could not complete transforms in the time or steps allotted.");
  }

  protected TransformStepResult transformStep(Context context) {
    // go over the pairs.
    List<Triple<Pair<URI, JsonNode>, Boolean, Optional<Throwable>>> results = context.getInputSchemas().entrySet().parallelStream()
      .map(entry->Pair.<URI, JsonNode>of(entry.getKey(), entry.getValue()))
      .map(pair->applyTransform(context, pair))
      .collect(Collectors.toList());
    
    Integer updates = (int)results.stream()
      .filter(e->e.getMiddle())
      .count();
    
    List<Pair<Pair<URI, JsonNode>, Throwable>> errors = results.stream()
      .filter(t->t.getRight().isPresent())
      .map(e->Pair.of(e.getLeft(), e.getRight().get()))
      .collect(Collectors.toList());

    // scatter the results
    results.parallelStream()
      .filter(e->e.getMiddle())
      .map(Triple::getLeft)
      .forEach(result->
        context.getInputSchemas().put(result.getLeft(), result.getRight())
      );

    return new TransformStepResultBuilder()
      .withUpdates(updates)
      .withErrors(errors)
      .build();
  }

  public Triple<Pair<URI, JsonNode>, Boolean, Optional<Throwable>> applyTransform(Context context, Pair<URI, JsonNode> value) {
    Optional<JsonNode> result = Optional.empty();
    Optional<Throwable> exception = Optional.empty();
    try {
      result = Optional.of(transform.apply(context, value));
    } catch ( Throwable t ) {
      exception = Optional.of(t);
    }

    Boolean changed = result
      .map(resultValue->!resultValue.equals(value.getRight()))
      .orElse(false);

    return Triple.of(Pair.of(value.getLeft(), result.orElse(value.getRight())), changed, exception);
  }

  @GeneratePojoBuilder(excludeProperties = "needsContent")
  public static class Context {
    Map<URI, JsonNode> inputSchemas;
    Set<URI> needsContent = Collections.synchronizedSet(new HashSet<>());
    FragmentResolver fragmentResolver;
    String refFragmentPathDelimiters;

    public Optional<JsonNode> get(URI id) {
      URI normalizedId = id.normalize();
      boolean isFragment = normalizedId.toString().contains("#");
      URI baseId = isFragment
        ? URIUtil.removeFragment(id).normalize()
        : normalizedId;
      
      if ( !inputSchemas.containsKey(baseId) ) {
        needsContent.add(baseId);
        return Optional.empty();
      }

      JsonNode baseNode = inputSchemas.get(baseId);
      if ( !isFragment ) {
        return Optional.of(baseNode);
      }
      else {
        JsonNode childNode = fragmentResolver.resolve(baseNode, '#' + id.getFragment(), refFragmentPathDelimiters);
        return Optional.of(childNode);
      }
    }

      public Map<URI, JsonNode> getInputSchemas() {
          return inputSchemas;
      }

      public FragmentResolver getFragmentResolver() {
          return fragmentResolver;
      }

      public String getRefFragmentPathDelimiters() {
          return refFragmentPathDelimiters;
      }
  }

  @GeneratePojoBuilder
  public static class TransformStepResult {
    Integer updates;
    List<Pair<Pair<URI, JsonNode>, Throwable>> errors;
    public Integer getUpdates() {
      return updates;
    }
    public List<Pair<Pair<URI, JsonNode>, Throwable>> getErrors() {
      return errors;
    }
  }
}
