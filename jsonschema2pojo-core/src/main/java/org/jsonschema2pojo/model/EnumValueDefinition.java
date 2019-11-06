package org.jsonschema2pojo.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;

public class EnumValueDefinition {
  private final String name;
  private final String value;
  private final JsonNode titleNode;
  private final JsonNode descriptionNode;

  public EnumValueDefinition(String name, String value) {
    this(name, value, null, null);
  }

  public EnumValueDefinition(String name, String value, JsonNode titleNode, JsonNode descriptionNode) {
    this.name = name;
    this.value = value;
    this.titleNode = titleNode;
    this.descriptionNode = descriptionNode;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public JsonNode getTitleNode() {
    return titleNode;
  }

  public JsonNode getDescriptionNode() {
    return descriptionNode;
  }

  public boolean hasDescription() {
    return descriptionNode != null && !descriptionNode.isMissingNode();
  }

  public boolean hasTitle() {
    return titleNode != null && !titleNode.isMissingNode();
  }
}
