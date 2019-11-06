package org.jsonschema2pojo.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class EnumDefinition {
  private final JType type;
  private final ArrayList<EnumValueDefinition> enumValues;
  private final String nodeName;
  private final JsonNode enumNode;

  public EnumDefinition(String nodeName, JsonNode enumNode, JType type, ArrayList<EnumValueDefinition> enumValues) {
    this.nodeName = nodeName;
    this.enumNode = enumNode;
    this.type = type;
    this.enumValues = enumValues;
  }

  public JsonNode getEnumNode() {
    return enumNode;
  }

  public String getNodeName() {
    return nodeName;
  }

  public JType getType() {
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
