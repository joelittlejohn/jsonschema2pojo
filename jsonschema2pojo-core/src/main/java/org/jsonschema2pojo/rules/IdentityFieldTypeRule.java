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

package org.jsonschema2pojo.rules;
import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JType;

/**
 * This implementation instructs the generator to use the schema's type
 * as the type for the field definition.
 */
public class IdentityFieldTypeRule implements Rule<JType, JType> {
  /**
   * This implementation returns the schemaType parameter.
   *
   * @param nodeName the name of the field
   * @param node the field's node
   * @param parent the object contianing the field
   * @param schemaType the type derived from the field's schema
   * @param schema the schema for the property.
   * @return the type that should be used for the field.
   */
  @Override
  public JType apply(String nodeName, JsonNode node, JsonNode parent, JType schemaType, Schema schema) {
    return schemaType;
  }
}
