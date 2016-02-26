/**
 * Copyright © 2010-2014 Nokia
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

package org.jsonschema2pojo;

import static com.sun.codemodel.JExpr.FALSE;
import static com.sun.codemodel.JExpr.TRUE;
import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr._null;
import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.invoke;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jsonschema2pojo.exception.GenerationException;
import org.jsonschema2pojo.rules.RuleFactory;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;
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
import com.fasterxml.jackson.databind.node.ValueNode;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

/**
 * Annotates generated Java types using the Jackson 2.x mapping annotations.
 * 
 * @see <a
 *      href="https://github.com/FasterXML/jackson-annotations">https://github.com/FasterXML/jackson-annotations</a>
 */
public class Jackson2Annotator extends AbstractAnnotator {

    @Override
    public void propertyOrder(JDefinedClass clazz, JsonNode propertiesNode) {
        JAnnotationArrayMember annotationValue = clazz.annotate(JsonPropertyOrder.class).paramArray("value");

        for (Iterator<String> properties = propertiesNode.fieldNames(); properties.hasNext();) {
            annotationValue.param(properties.next());
        }
    }

    @Override
    public void propertyInclusion(JDefinedClass clazz, JsonNode schema) {
        clazz.annotate(JsonInclude.class).param("value", JsonInclude.Include.NON_NULL);
    }

    @Override
    public void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
        field.annotate(JsonProperty.class).param("value", propertyName);
        if (field.type().erasure().equals(field.type().owner().ref(Set.class))) {
            field.annotate(JsonDeserialize.class).param("as", LinkedHashSet.class);
        }

        if (propertyNode.has("javaJsonView")) {
            field.annotate(JsonView.class).param(
                "value", field.type().owner().ref(propertyNode.get("javaJsonView").asText()));
        }
    }
    
    @Override
    public void propertyDeserializer(RuleFactory ruleFactory, JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode, Schema currentSchema) {
      if (propertyNode.has("oneOf")) {
        JClass deserializer = addOneOfDeserializer(ruleFactory, field, clazz, propertyName, propertyNode, currentSchema);
        field.annotate(JsonDeserialize.class).param("using", deserializer);
      }
    }

    @Override
    public void propertyGetter(JMethod getter, String propertyName) {
        getter.annotate(JsonProperty.class).param("value", propertyName);
    }

    @Override
    public void propertySetter(JMethod setter, String propertyName) {
        setter.annotate(JsonProperty.class).param("value", propertyName);
    }

    @Override
    public void anyGetter(JMethod getter) {
        getter.annotate(JsonAnyGetter.class);
    }

    @Override
    public void anySetter(JMethod setter) {
        setter.annotate(JsonAnySetter.class);
    }

    @Override
    public void enumCreatorMethod(JMethod creatorMethod) {
        creatorMethod.annotate(JsonCreator.class);
    }

    @Override
    public void enumValueMethod(JMethod valueMethod) {
        valueMethod.annotate(JsonValue.class);
    }

    @Override
    public void enumConstant(JEnumConstant constant, String value) {
    }

    @Override
    public boolean isAdditionalPropertiesSupported() {
        return true;
    }

    @Override
    public void additionalPropertiesField(JFieldVar field, JDefinedClass clazz, String propertyName) {
        field.annotate(JsonIgnore.class);
    }
    
    JClass addOneOfDeserializer( RuleFactory ruleFactory, JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode, Schema currentSchema ) {
      JCodeModel model = clazz.owner();
      JsonNode oneOf = propertyNode.get("oneOf");
      String fieldName = field.name();
      if( !oneOf.isArray() ) throw new IllegalArgumentException("oneOf must contain an array");
      
      String deserContainerName = fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1);
      JDefinedClass deserContainer = innerClass(clazz, JMod.PUBLIC|JMod.STATIC, deserContainerName);

      JClass jsonDeserializer = model.ref(JsonDeserializer.class).narrow(field.type());
      try {
        
        JDefinedClass fieldDeser = deserContainer._class(JMod.PUBLIC | JMod.STATIC, "Jackson2Deserializer");
        fieldDeser._extends(jsonDeserializer);
        
        JMethod deserMethod = fieldDeser.method(JMod.PUBLIC, field.type(), "deserialize");
        deserMethod._throws(model.ref(IOException.class));
        deserMethod._throws(model.ref(JsonProcessingException.class));
        JVar parser = deserMethod.param(model.ref(JsonParser.class), "jp");
        deserMethod.param(model.ref(DeserializationContext.class), "ctxt");
        
        JBlock body = deserMethod.body();
        JVar codec = body.decl(model.ref(ObjectCodec.class), "codec", parser.invoke("getCodec"));
        JVar tree = body.decl(model.ref(TreeNode.class), "tree", parser.invoke("readValueAsTree"));
        
        for( int i = 0; i < oneOf.size(); i++ ) {
          JType optionType = ruleFactory.getSchemaRule().apply(fieldName+"Option"+i, oneOf.get(i), clazz.parentContainer(), currentSchema);
          // add a method to accept the option.
          JMethod acceptMethod = acceptMethod(fieldDeser, i, oneOf.get(i) );
          
          // add code to deserialize with the type if the option is successful.
          JConditional ifAccepted = body._if(fieldDeser.staticInvoke(acceptMethod).arg(tree));
          // in the if, try the parse, move on if it fails.
          JBlock ifAcceptedThen = ifAccepted._then();
          JClass typeRefClass = model.ref(TypeReference.class).narrow(optionType);
          JVar typeRef = ifAcceptedThen.decl(typeRefClass, "typeRef", _new(model.anonymousClass(typeRefClass)));
          ifAcceptedThen._return(
              codec.invoke("readValue")
                .arg(codec.invoke("treeAsTokens").arg(tree))
                .arg(typeRef));
        }
        deserMethod.body()._return(_null());
        
        return fieldDeser;
      } catch (JClassAlreadyExistsException e) {
        throw new GenerationException("could not add oneOf deserializer to "+fieldName, e);
      }
    }
    
    JMethod acceptMethod(JDefinedClass deserClass, int optionIndex, JsonNode optionNode) {
      final JCodeModel model = deserClass.owner();
      JMethod acceptMethod = deserClass.method(JMod.PRIVATE|JMod.STATIC, model.BOOLEAN, "isOption"+optionIndex);
      final JVar tree = acceptMethod.param(model.ref(TreeNode.class), "tree");
      
      JBlock body = acceptMethod.body();
      
      JsonNode typeNode = optionNode.path("type");
      String type = typeNode.isMissingNode() ? "object" : typeNode.asText();
      JVar token = body.decl(model.ref(JsonToken.class), "token", invoke(tree, "asToken"));
      
      if( "string".equals(type) ) {
        filterOtherTokens(model, body, token, "VALUE_STRING");

        new OneOfTemplates.StringFilterTemplate(optionNode, model, body) {
          @Override public JExpression valueExpr() {return valueNodeValue(model, tree, "asText");}        
        }.execute();
     
        body._return(TRUE);
      }
      else if( "boolean".equals(type)) {
        filterOtherTokens(model, body, token, "VALUE_TRUE", "VALUE_FALSE");
        
        body._return(TRUE);
      }
      else if( "integer".equals(type) ) {
        filterOtherTokens(model, body, token, "VALUE_NUMBER_INT");
        
        new OneOfTemplates.IntegerFilterTemplate(optionNode,optionIndex, model, deserClass, body) {
          @Override public JExpression valueExpr() { return valueNodeValue(model, tree, "bigIntegerValue");}
        }.execute();
              
        body._return(TRUE);
      }
      else if( "number".equals(type) ) {
        filterOtherTokens(model, body, token, "VALUE_NUMBER_FLOAT");     
        
        body._return(TRUE);
      }
      else if( "array".equals(type) ) {
        filterOtherTokens(model, body, token, "START_ARRAY");               
        
        body._return(TRUE);
      }
      
      return acceptMethod;
    }
    
    static void filterOtherTokens(JCodeModel model, JBlock block, JVar token, String firstType, String... types) {
      JExpression test = model.ref(JsonToken.class).staticRef(firstType).ne(token);
      for( String type : types ) {
        test = test.cand(model.ref(JsonToken.class).staticRef(type));
      }
      block._if(test)
      ._then()._return(FALSE);
    }
    
    static JExpression valueNodeValue(JCodeModel model, JVar tree, String accessor ) {
      return ((JExpression)cast(model.ref(ValueNode.class), tree)).invoke(accessor);
    }
    
    static JDefinedClass innerClass( JDefinedClass clazz, int mods, String name ) {
      try {
        return clazz._class(mods, name);
      } catch( JClassAlreadyExistsException cae ) {
        return cae.getExistingClass();
      }
    }

}
