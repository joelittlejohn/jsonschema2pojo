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
