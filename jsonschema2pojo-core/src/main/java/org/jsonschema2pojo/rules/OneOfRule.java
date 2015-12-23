package org.jsonschema2pojo.rules;

import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JType;

public class OneOfRule implements Rule<JClassContainer, JType> {

    @Override
    public JType apply(String nodeName, JsonNode node, JClassContainer generatableType, Schema currentSchema) {
      // get the schemas of all the children.
        
      // find the most common super type, that is the field type.
        
      // find interfaces common to all types, add those to the field type as an intersection.
        
      // create a field with that type.
        
      // In Jackson annotator:
      // generate a deserializer that will...
        // for each type in declaration order...
          // if primitive, use static test method (or inline)
        
          // if not primitive, call test method attached to type.
        
          // if true, serialize to that type and return.
      
      // if nothing matched, fail 
      
      // attach deserializer to field.

      // add any support methods required.
    }

}
