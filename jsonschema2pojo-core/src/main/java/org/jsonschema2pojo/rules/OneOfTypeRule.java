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

public class OneOfTypeRule implements Rule<JClassContainer, JType> {

  private final RuleFactory ruleFactory;

  protected OneOfTypeRule(RuleFactory ruleFactory) {
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

      // TODO: find interfaces common to all types, add those to the field type as an intersection.

      return fieldType;
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
