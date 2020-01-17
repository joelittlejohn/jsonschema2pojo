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

public class EnumValueDefinition {
  private final String name;
  private final String value;
  private final JsonNode titleNode;
  private final JsonNode descriptionNode;
  private final JsonNode extensionNode;

  public EnumValueDefinition(String name, String value) {
    this(name, value, null, null, null);
  }

  public EnumValueDefinition(String name, String value, JsonNode extensionNode) {
    this(name, value, extensionNode,null, null);
  }

  public EnumValueDefinition(String name, String value, JsonNode extensionNode, JsonNode titleNode, JsonNode descriptionNode) {
    this.name = name;
    this.value = value;
    this.extensionNode = extensionNode;
    this.titleNode = titleNode;
    this.descriptionNode = descriptionNode;
  }

  public JsonNode getDescriptionNode() {
    return descriptionNode;
  }

  public JsonNode getExtensionNode() {
    return extensionNode;
  }

  public String getName() {
    return name;
  }

  public JsonNode getTitleNode() {
    return titleNode;
  }

  public String getValue() {
    return value;
  }

  public boolean hasDescription() {
    return descriptionNode != null && !descriptionNode.isMissingNode();
  }

  public boolean hasTitle() {
    return titleNode != null && !titleNode.isMissingNode();
  }
}
