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

package org.jsonschema2pojo.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Holds the an enum types effective definition.
 *
 * The definition of the enum can be decided by:
 *    "enum" (JSON-Schema)
 *    "enum" and "javaEnums" (JSON-Schema + jsonschema2pojo extension)
 *    "enum" and "javaEnumNames" (JSON-Schema + jsonschema2pojo extension)
 */
public class EnumDefinition {
  private final JType backingType;
  private final ArrayList<EnumValueDefinition> enumValues;
  private final String nodeName;
  private final JsonNode enumNode;
  private final EnumDefinitionExtensionType type;

  public EnumDefinition(String nodeName,
                        JsonNode enumNode,
                        JType backingType,
                        ArrayList<EnumValueDefinition> enumValues,
                        EnumDefinitionExtensionType type) {
    this.nodeName = nodeName;
    this.enumNode = enumNode;
    this.backingType = backingType;
    this.enumValues = enumValues;
    this.type = type;
  }

  public JType getBackingType() {
    return backingType;
  }

  public JsonNode getEnumNode() {
    return enumNode;
  }

  public String getNodeName() {
    return nodeName;
  }

  public EnumDefinitionExtensionType getType() {
    return type;
  }

  public int size() {
    if (enumValues != null) {
      return enumValues.size();
    } else {
      return 0;
    }
  }

  public Collection<EnumValueDefinition> values() {
    if (enumValues != null) {
      return Collections.unmodifiableCollection(enumValues);
    } else {
      return Collections.emptyList();
    }
  }
}
