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

import org.jsonschema2pojo.exception.GenerationException;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

public class OneOfTemplates {

  public static abstract class IntegerFilterTemplate {
    private JsonNode optionNode;
    private JCodeModel model;
    private JBlock body;
  
    public IntegerFilterTemplate(JsonNode optionNode, JCodeModel model, JBlock body ) {
      this.optionNode = optionNode;
      this.model = model;
      this.body = body;
    }
    public void execute() {
      JsonNode minimum = optionNode.path("minimum");
      JsonNode maximum = optionNode.path("maximum");     
      if( !maximum.isMissingNode() || !minimum.isMissingNode() ) {
        JVar value = body.decl(model.INT, "value", valueExpr());
        if( !minimum.isMissingNode() ) {
          body._if(value.lt(lit(minimum.asInt())))._then()._return(FALSE);
        }
        if( !maximum.isMissingNode() ) {
          body._if(value.gt(lit(maximum.asInt())))._then()._return(FALSE);
        }
      }
    }
    
    public abstract JExpression valueExpr();
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
