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

package org.jsonschema2pojo.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.maven.shared.utils.StringUtils;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.jsonschema2pojo.integration.util.LogEvent;
import org.jsonschema2pojo.integration.util.LogEvent.Level;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class OfIT {

    @RegisterExtension public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule().captureLoggingOutput();

    public static String ALL_OF = "allOf";
    public static String ANY_OF = "anyOf";
    public static String ONE_OF = "oneOf";
    public static String NOT    = "not";

    @ParameterizedTest(name="{0} at {1} warning count")
    @MethodSource("warningCountArgs")
    public void warningCounts(String operation, String location, String schema, int warningCount) {
      schemaRule.generate(schema, "com.example");

      assertThat(
        (int)findWarnings(m->m.contains(operation)).count(),
        equalTo(warningCount)
      );
    }

    public static Stream<Object[]> warningCountArgs() {
      return Arrays.stream(new Object[][] {
        { "allOf", "root,child" , 1},
        { "anyOf", "root,child" , 1},
        { "oneOf", "root,child" , 1},
        { "not"  , "root,child" , 1},
      })
      .flatMap(flatten(1))
      .map(derive(2, OfIT::filePath, 0, 1));
    }

    public static Function<Object[], Stream<Object[]>> flatten(int flattenIndex) {
      return row->{
        return Arrays.stream(row[flattenIndex].toString().split(","))
          .map(flattened->{
            Object[] result = row.clone();
            result[flattenIndex] = flattened;
            return result;
          });
      };
    }

    public static <T, U, R> Function<Object[], Object[]> derive(int insertIndex, BiFunction<T, U, R> create, int arg1, int arg2) {
      return row->{
        Object[] result = new Object[row.length+1];
        System.arraycopy(row, 0, result, 0, insertIndex);
        result[insertIndex] = create.apply((T)row[arg1], (U)row[arg2]);
        System.arraycopy(row, insertIndex, result, insertIndex+1, row.length-insertIndex);
        return result;
      };
    }

    @ParameterizedTest(name="{0} at {1} file count")
    @MethodSource("fileCountArgs")
    public void fileCounts(String operation, String location, String schema, int fileCount) {
      schemaRule.generate(schema, "com.example");

      assertThat(
        Stream.of(new File(schemaRule.getGenerateDir(), "./com/example"))
          .filter(File::exists)
          .map(File::listFiles)
          .flatMap(Arrays::stream)
          .count(),
        equalTo((long)fileCount)
      );
    }

    public static Stream<Object[]> fileCountArgs() {
      return Arrays.stream(new Object[][] {
        { "allOf", "root" , 0},
        { "allOf", "child", 2},
        { "anyOf", "root" , 0},
        { "anyOf", "child", 2},
        { "oneOf", "root" , 0},
        { "oneOf", "child", 2},
        { "not"  , "root" , 0},
        { "not"  , "child", 2},
      })
      .map(row->new Object[] {row[0], row[1], filePath((String)row[0], (String)row[1]), row[2]});
    }
    public static String filePath(String name, String location) {
      return "/schema/of/"+name+StringUtils.capitalise(location)+".json";
    }

    Stream<LogEvent> findWarnings(ComposablePredicate<String> messagePredicate) {
      return findEvents(
          schemaRule.getLogs().stream(),
          Level.WARN::equals,
          messagePredicate,
          any()
        );
    }

    static Stream<LogEvent> findEvents(
      Stream<LogEvent> events, 
      ComposablePredicate<Level> levelPredicate,
      ComposablePredicate<String> messagePredicate,
      ComposablePredicate<Throwable> errorPredicate
    ) {
        return events
          .filter(levelPredicate.compose(LogEvent::getLevel)::test)
          .filter(messagePredicate.compose(orElseNull(LogEvent::getMessage))::test)
          .filter(errorPredicate.compose(orElseNull(LogEvent::getError)));
    }

    static <T, R> Function<T, R> orElseNull(Function<T, Optional<R>> optionalReturn) {
      return optionalReturn.andThen(value->value.orElse(null));
    }

    static <T> ComposablePredicate<T> any() { return t->true; }

    static interface ComposablePredicate<T> extends Predicate<T> {
      default public <U> ComposablePredicate<U> compose(Function<U, T> translation) {
        return (value)->test(translation.apply(value));
      }
    }
}
