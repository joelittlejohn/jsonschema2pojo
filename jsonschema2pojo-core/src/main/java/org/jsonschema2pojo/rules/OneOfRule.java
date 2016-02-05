package org.jsonschema2pojo.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JType;

public class OneOfRule implements Rule<JClassContainer, JType> {

  private final RuleFactory ruleFactory;

  protected OneOfRule(RuleFactory ruleFactory) {
      this.ruleFactory = ruleFactory;
  }
  
    @Override
    public JType apply(String nodeName, JsonNode node, JClassContainer generatableType, Schema currentSchema) {
      // get the schemas of all the children.
      
      JsonNode optionNodes = node.get("oneOf");
      List<JType> options = new ArrayList<JType>();
      for( int i = 0; i < optionNodes.size(); i++ ) {
        options.add(ruleFactory.getSchemaRule().apply(nodeName+"Option"+i, optionNodes.get(i), generatableType, currentSchema));
      }

      // find the most common super type, that is the field type.
      JType fieldType = greatestCommonAncestor(options);
      return fieldType;
        
      // find interfaces common to all types, add those to the field type as an intersection.
        
      // create a field with that type.  The parent is doing this.
        
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
    
    static JType greatestCommonAncestor( List<JType> types ) {
      JType first = types.get(0);
      List<JType> currentAncestors = ancestorTypes(first);
      for( int i = 1; i < types.size(); i++ ) {
        List<JType> nextAncestors = ancestorTypes(types.get(i));
        currentAncestors = commonAncestors(currentAncestors, nextAncestors);
      }
      return currentAncestors.get(currentAncestors.size()-1);
    }
    
    static List<JType> commonAncestors(List<JType> ancestors1, List<JType> ancestors2) {
      List<JType> common = new ArrayList<JType>(ancestors1);
      common.retainAll(ancestors2);
      return common;
    }
    
    static List<JType> ancestorTypes( JType type ) {
      return ancestors( addFirst(new LinkedList<JType>(), type) );
    }
    
    static List<JType> ancestors( LinkedList<JType> ancestors ) {
      JType objectType = ancestors.getFirst().owner()._ref(Object.class);
      if( ancestors.getFirst().equals(objectType) ) {
        return ancestors;
      }
      else if(ancestors.getFirst().isArray() || ancestors.getFirst().isPrimitive() ) {
        return addFirst(ancestors, objectType);
      } else if( ancestors.getFirst() instanceof JClass ) {
        JClass typeClass = (JClass)ancestors.getFirst();
        return ancestors(addFirst(ancestors, typeClass._extends()));
      } else {
        throw new IllegalArgumentException("unhandled type "+ancestors.getFirst());
      }
    }
    
    static <T> LinkedList<T> addFirst(LinkedList<T> list, T item) {
      list.addFirst(item);
      return list;
    }

}
