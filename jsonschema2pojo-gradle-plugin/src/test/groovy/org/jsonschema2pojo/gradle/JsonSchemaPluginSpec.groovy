/**
 * Copyright © 2010-2014 Nokia
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
package org.jsonschema2pojo.gradle

import static org.hamcrest.MatcherAssert.*;

import java.lang.reflect.Field
import java.nio.charset.StandardCharsets

import org.apache.commons.io.FileUtils
import org.junit.Test

class JsonSchemaPluginSpec {

  @Test
  void documentationIncludesAllProperties() {
    String documentation = FileUtils.readFileToString(new File("README.md"), StandardCharsets.UTF_8);

    Set<String> ignoredProperties = new HashSet<String>() {{
        add("sourceFiles");
        add("\$staticClassInfo\$");
        add("\$staticClassInfo");
        add("__\$stMC");
        add("metaClass");
        add("\$callSiteArray");
    }}

    List<String> missingProperties = new ArrayList<String>()
    for (Field f : JsonSchemaExtension.class.getDeclaredFields()) {
      if (!ignoredProperties.contains(f.getName()) && !documentation.contains("  " + f.getName() + " ")) {
        missingProperties.add(f.getName());
      }
    }

    assertThat(missingProperties.toString(), missingProperties.isEmpty())
  }

}
