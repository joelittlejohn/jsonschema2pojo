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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.output.NullWriter;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jsonschema2pojo.integration.util.Compiler;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

/**
 * <p>Tests looking for warning coming from generated output.</p>
 *
 * <p>Notes: The eclipse compiler used in these tests has an open issue with the SuppressWarnings annotation.  As a result, some warnings must be
 * accepted here that would not be present in practice.
 * <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=469725">Bug 469725 - ECJ compiler: @SuppressWarnings annotation is ignored when ecj is invoked via java compiler tool API</a>
 * </p>
 *
 * @author Christian Trimble
 *
 */
@RunWith(Parameterized.class)
public class CompilerWarningIT {
  @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule().captureDiagnostics();
  
  @Parameters(name="{0}")
  public static Collection<Object[]> parameters() {
    JavaCompiler systemJavaCompiler = Compiler.systemJavaCompiler();
    JavaCompiler eclipseCompiler = Compiler.eclipseCompiler();
    return Arrays.asList(new Object[][] {
      {
        "includeAccessorsWithSystemJavaCompiler",
        systemJavaCompiler,
        config("includeDynamicAccessors", true, "includeDynamicGetters", true, "includeDynamicSetters", true, "includeDynamicBuilders", true),
        "/schema/dynamic/parentType.json",
        Matchers.empty()
      },
      {
        "includeAccessorsWithEclipseCompiler",
        eclipseCompiler,
        config("includeDynamicAccessors", true, "includeDynamicGetters", true, "includeDynamicSetters", true, "includeDynamicBuilders", true),
        "/schema/dynamic/parentType.json",
        onlyCastExceptions()
      }
    });
  }

  private JavaCompiler compiler;
  private Map<String, Object> config;
  private String schema;
  private Matcher<List<Diagnostic<? extends JavaFileObject>>> matcher;

  public CompilerWarningIT(String label, JavaCompiler compiler, Map<String, Object> config, String schema, Matcher<List<Diagnostic<? extends JavaFileObject>>> matcher) {
    this.compiler = compiler;
    this.config = config;
    this.schema = schema;
    this.matcher = matcher;
  }

  @Test
  public void checkWarnings() {
    schemaRule.generate(schema, "com.example", config);
    schemaRule.compile(compiler, new NullWriter(), new ArrayList<>(), config);
    
    List<Diagnostic<? extends JavaFileObject>> warnings = warnings(schemaRule.getDiagnostics());
    
    assertThat(warnings, matcher);
  }

  public static List<Diagnostic<? extends JavaFileObject>> warnings(List<Diagnostic<? extends JavaFileObject>> all) {
    List<Diagnostic<? extends JavaFileObject>> warnings = new ArrayList<>();
    for( Diagnostic<? extends JavaFileObject> entry : all ) {
      if( entry.getKind() == Kind.WARNING ) {
        warnings.add(entry);
      }
    }
    return warnings;
  }

  public static Matcher<Iterable<Diagnostic<? extends JavaFileObject>>> onlyCastExceptions() {
    return Matchers.everyItem(hasMessage(containsString("Type safety: Unchecked cast from")));
  }

  public static Matcher<Diagnostic<? extends JavaFileObject>> hasMessage( Matcher<String> messageMatcher ) {
    return new FeatureMatcher<Diagnostic<? extends JavaFileObject>, String>(messageMatcher, "message", "message") {
      @Override
      protected String featureValueOf(Diagnostic<? extends JavaFileObject> actual) {
        return actual.getMessage(Locale.ENGLISH);
      }
    };
  }
}
