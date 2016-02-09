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

package org.jsonschema2pojo;

import static com.sun.codemodel.JExpr.FALSE;
import static com.sun.codemodel.JExpr.lit;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.jsonschema2pojo.exception.GenerationException;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

public class OneOfTemplates {

  public static abstract class IntegerFilterTemplate {
    private JsonNode optionNode;
    private JCodeModel model;
    private JBlock body;
    private int optionIndex;
    private JDefinedClass jclass;
  
    public IntegerFilterTemplate(JsonNode optionNode, int optionIndex, JCodeModel model, JDefinedClass jclass, JBlock body ) {
      this.optionNode = optionNode;
      this.optionIndex = optionIndex;
      this.model = model;
      this.jclass = jclass;
      this.body = body;
    }
    public void execute() {
      JsonNode minimum = optionNode.path("minimum");
      JsonNode maximum = optionNode.path("maximum");     
      if( !maximum.isMissingNode() || !minimum.isMissingNode() ) {
        JVar value = body.decl(model.ref(BigInteger.class), "value", valueExpr());
        if( !minimum.isMissingNode() ) {
          JVar minimumField = bigIntegerConstant(model, jclass, "OPTION_"+optionIndex+"_MINIMUM", minimum);
          body._if(minimumField.invoke("compareTo").arg(value).gt(lit(0)))._then()._return(FALSE);
        }
        if( !maximum.isMissingNode() ) {
          JVar maximumField = bigIntegerConstant(model, jclass, "OPTION_"+optionIndex+"_MAXIMUM", maximum);
          body._if(maximumField.invoke("compareTo").arg(value).lt(lit(0)))._then()._return(FALSE);
        }
      }
    }
    
    public abstract JExpression valueExpr();
  }
  
  static JVar bigIntegerConstant( JCodeModel model, JDefinedClass jclass, String name, JsonNode node ) {
    return jclass.field(JMod.PUBLIC|JMod.STATIC|JMod.FINAL, model.ref(BigInteger.class), name, JExpr._new(model.ref(BigInteger.class)).arg(node.asText()));
  }
  
  static Class<? extends Number> javaNumberType(JsonNode node) {
    switch(node.numberType()) {
    case BIG_DECIMAL: return BigDecimal.class;
    case BIG_INTEGER: return BigInteger.class;
    case DOUBLE: return Double.class;
    case FLOAT: return Float.class;
    case INT: return Integer.class;
    case LONG: return Long.class;
    default: throw new IllegalStateException("unknown number type.");
    }
  }

  static abstract class StringFilterTemplate {
    private JsonNode optionNode;
    private JCodeModel model;
    private JBlock body;
  
    public StringFilterTemplate(JsonNode optionNode, JCodeModel model, JBlock body ) {
      this.optionNode = optionNode;
      this.model = model;
      this.body = body;
    }
  
    public void execute() {
      JsonNode minLength = optionNode.path("minLength");
      JsonNode maxLength = optionNode.path("maxLength");        
      if( !minLength.isMissingNode() || !maxLength.isMissingNode() ) {
        JVar value = body.decl(model.ref(String.class), "value", valueExpr());
        if( !minLength.isMissingNode() ) {
          body._if(value.invoke("length").lt(lit(minLength.asInt())))._then()._return(FALSE);
        }
        if( !maxLength.isMissingNode() ) {
          body._if(value.invoke("length").gt(lit(maxLength.asInt())))._then()._return(FALSE);
        }
      }
    }
    
    public abstract JExpression valueExpr();
  }
  
  public static abstract class OneOfTemplate {
    protected JCodeModel model;
    protected JsonNode propertyNode;
    protected JsonNode oneOf;
    protected JFieldVar field;
    protected JDefinedClass clazz;
    protected JDefinedClass deserContainer;
    protected Schema currentSchema;
    public OneOfTemplate( JCodeModel model, JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode, Schema currentSchema ) {
      this.model = model;
      this.field = field;
      this.clazz = clazz;
      this.propertyNode = propertyNode;
      this.currentSchema = currentSchema;
    }

    public JDefinedClass execute() {
      this.oneOf = propertyNode.get("oneOf");
      if( !oneOf.isArray() ) throw new IllegalArgumentException("oneOf must contain an array");
      this.deserContainer = deserContainer();
      
      try {
        return this.createDeserializer();
      }
      catch(Exception e) {
        throw new GenerationException("could not create deserializer", e);
      }
    }
    
    private JDefinedClass deserContainer() {
      String fieldName = field.name();
      String deserName = fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1);
      return innerClass(clazz, JMod.PUBLIC|JMod.STATIC, deserName);
    }
    
    public abstract JDefinedClass createDeserializer() throws Exception;
  }

  static JDefinedClass innerClass( JDefinedClass clazz, int mods, String name ) {
    try {
      return clazz._class(mods, name);
    } catch( JClassAlreadyExistsException cae ) {
      return cae.getExistingClass();
    }
  }
}
