package org.jsonschema2pojo.rules;

import java.net.URI;

import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.SchemaStore2;
import org.jsonschema2pojo.rules.RuleFactory;
import org.jsonschema2pojo.rules.SchemaRule;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;

/**
 * URN Support
 * @author Labi0@github.com
 *
 */
public class SchemaRule2 extends SchemaRule {
  private final RuleFactory ruleFactory;

  public SchemaRule2(RuleFactory ruleFactory) {
    super(ruleFactory);
    this.ruleFactory = ruleFactory;
  }

  /**
   * @see org.jsonschema2pojo.rules.SchemaRule.apply
   */
  public JType apply(String nodeName, JsonNode schemaNode, JClassContainer generatableType, Schema schema) {
    if (nodeName.equals("id") && schemaNode.has("id")) { //LABI HACK :)
      SchemaStore2 schemaStore = (SchemaStore2) ruleFactory.getSchemaStore();
      URI schemaId = URI.create(schemaNode.get("id").asText());
      Schema newSchema = new Schema(schemaId, schemaNode);
      schemaStore.registerId(schemaId, newSchema);
      JType returnedType = (JDefinedClass) generatableType; 
      newSchema.setJavaType(returnedType);
      return returnedType;
    }
    JType javaType = super.apply(nodeName, schemaNode, generatableType, schema);
    


    return javaType;
  }
}
