/**
 * Copyright ¬© 2010-2014 Nokia
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.exception.GenerationException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

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
    
    JClass addOneOfDeserializer( JType fieldType, String nodeName, JsonNode node, JClassContainer generatableType, Schema currentSchema ) {
      JCodeModel model = generatableType.owner();
      JsonNode oneOf = node.get("oneOf");
      String fieldName = ruleFactory.getNameHelper().getPropertyName(nodeName);
      if( !oneOf.isArray() ) throw new IllegalArgumentException("oneOf must contain an array");
      String deserializerName = fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1)+"$Jackson2Deserializer";
      JClass jsonDeserializer = model.ref(JsonDeserializer.class).narrow(fieldType);
      try {
        JDefinedClass fieldDeser = generatableType._class(JMod.PUBLIC | JMod.STATIC, deserializerName);
        fieldDeser._extends(jsonDeserializer);
        
        JMethod deserMethod = fieldDeser.method(JMod.PUBLIC, fieldType, "deserialize");
        deserMethod._throws(model.ref(IOException.class));
        deserMethod._throws(model.ref(JsonProcessingException.class));
        JVar parser = deserMethod.param(model.ref(JsonParser.class), "jp");
        deserMethod.param(model.ref(DeserializationContext.class), "ctxt");
        
        JBlock body = deserMethod.body();
        JVar codec = body.decl(model.ref(ObjectCodec.class), "codec", parser.invoke("getCodec"));
        JVar tree = body.decl(model.ref(TreeNode.class), "tree", parser.invoke("readValueAsTree"));
        
        for( int i = 0; i < oneOf.size(); i++ ) {
          JType optionType = ruleFactory.getSchemaRule().apply(fieldName+"Option"+i, oneOf.get(i), generatableType, currentSchema);
          // add a method to accept the option.
          JMethod acceptMethod = null;
          // TODO: create a method for accepting the schema on the deserializer.
          
          // add code to deserialize with the type if the option is successful.
          JConditional ifAccepted = body._if(fieldDeser.staticInvoke(acceptMethod).arg(tree));
          ifAccepted._then()._return(
              codec.invoke("readValue")
                .arg(codec.invoke("treeAsTokens").arg(tree))
                .arg(JExpr._new(model.ref(TypeReference.class).narrow(fieldType))));
        }
        deserMethod.body()._return(JExpr._null());
        
        return fieldDeser;
      } catch (JClassAlreadyExistsException e) {
        throw new GenerationException("could not add oneOf deserializer to "+fieldName, e);
      }
    }
    
    JMethod acceptMethod(JDefinedClass deserClass, int optionIndex, JsonNode optionNode, Schema optionSchema) {
      JCodeModel model = deserClass.owner();
      JMethod acceptMethod = deserClass.method(JMod.PRIVATE, model.BOOLEAN, "acceptOption"+optionIndex);
      JVar tree = acceptMethod.param(model.ref(TreeNode.class), "tree");
      
      JBlock body = acceptMethod.body();
      
      JsonNode typeNode = optionNode.path("type");
      String type = typeNode.isMissingNode() ? "object" : typeNode.asText();
      JVar token = body.decl(model.ref(JsonToken.class), "token", JExpr.invoke(tree, "asToken"));
      
      if( "string".equals(type) ) {
        body._return(model.ref(JsonToken.class).staticRef("VALUE_STRING").eq(token));
      }
      else if( "boolean".equals(type)) {
        body._return(model.ref(JsonToken.class).staticRef("VALUE_TRUE").eq(token).cor(model.ref(JsonToken.class).staticRef("VALUE_FALSE").eq(token)));                
      }
      else if( "integer".equals(type) ) {
        body._return(model.ref(JsonToken.class).staticRef("VALUE_NUMBER_INT").eq(token));        
      }
      else if( "number".equals(type) ) {
        body._return(model.ref(JsonToken.class).staticRef("VALUE_NUMBER_FLOAT").eq(token));        
      }
      else if( "array".equals(type) ) {
        body._return(model.ref(JsonToken.class).staticRef("START_ARRAY").eq(token));                
      }
      
      return acceptMethod;
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
