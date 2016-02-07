/**
 * Copyright ¬© 2010-2014 Nokia
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.Matcher;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.databind.ObjectMapper;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class OneOfIT {
  public static ObjectMapper mapper = new ObjectMapper();
  @Rule public Jsonschema2PojoRule rule = new Jsonschema2PojoRule();
  private String schema;
  private Supplier<Reader> input;
  private String typeName;
  private Matcher<Object> matcher;
  
  @Parameters(name="{0}")
  public static Collection<Object[]> parameters() {
    return Arrays.asList(new Object[][]{
      {
        "simpleOneOfString",
        "/schema/oneOf/oneOf.json",
        "com.example.OneOf",
        json("{\"stringOrInteger\": 1}"),
        hasProperty("stringOrInteger", equalTo(1))
      },{
        "simpleOneOfInteger",
        "/schema/oneOf/oneOf.json",
        "com.example.OneOf",
        json("{\"stringOrInteger\": \"1\"}"),
        hasProperty("stringOrInteger", equalTo("1"))
      },{
        "stringLengthBelowMin",
        "/schema/oneOf/oneOfStringLength.json",
        "com.example.OneOfStringLength",
        json("{\"minLength\": \"http://example.com/\"}"),
        hasProperty("minLength", equalTo(URI.create("http://example.com/")))
      },{
        "stringLengthAtMin",
        "/schema/oneOf/oneOfStringLength.json",
        "com.example.OneOfStringLength",
        json("{\"minLength\": \"http://example.com/a\"}"),
        hasProperty("minLength", equalTo("http://example.com/a"))
      },{
        "stringLengthAboveMin",
        "/schema/oneOf/oneOfStringLength.json",
        "com.example.OneOfStringLength",
        json("{\"minLength\": \"http://example.com/ab\"}"),
        hasProperty("minLength", equalTo("http://example.com/ab"))
      },{
        "stringLengthBelowMax",
        "/schema/oneOf/oneOfStringLength.json",
        "com.example.OneOfStringLength",
        json("{\"maxLength\": \"http://example.com/\"}"),
        hasProperty("maxLength", equalTo("http://example.com/"))
      },{
        "stringLengthAtMax",
        "/schema/oneOf/oneOfStringLength.json",
        "com.example.OneOfStringLength",
        json("{\"maxLength\": \"http://example.com/a\"}"),
        hasProperty("maxLength", equalTo("http://example.com/a"))
      },{
        "stringLengthAboveMax",
        "/schema/oneOf/oneOfStringLength.json",
        "com.example.OneOfStringLength",
        json("{\"maxLength\": \"http://example.com/ab\"}"),
        hasProperty("maxLength", equalTo(URI.create("http://example.com/ab")))
      }
    });
  }
  
  public OneOfIT( String label, String schema, String typeName, Supplier<Reader> input, Matcher<Object> matcher ) {
    this.schema = schema;
    this.input = input;
    this.typeName = typeName;
    this.matcher = matcher;
  }
  
  @Test
  public void generationValid() throws ClassNotFoundException, IOException {
    ClassLoader loader = rule.generateAndCompile(schema, "com.example");
    
    Class<?> type = loader.loadClass(typeName);
    
    try (Reader in = input.get()) {
      assertThat(mapper.readValue(in, type), matcher);
    }
  }
  
  public static interface Supplier<T> {
    public T get();
  }
  
  public static Supplier<Reader> resource(final String relativeResource ) {
    return new Supplier<Reader>() {
      @Override
      public Reader get() {
        return new InputStreamReader(OneOfIT.class.getResourceAsStream(relativeResource));
      }
    };
  }
  
  public static Supplier<Reader> json(final String json) {
    return new Supplier<Reader>() {
      @Override
      public Reader get() {
        return new StringReader(json);
      }
    };    
  }
}
