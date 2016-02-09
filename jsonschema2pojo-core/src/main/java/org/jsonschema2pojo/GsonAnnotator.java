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

/**
 * 
 */
package org.jsonschema2pojo;

import static com.sun.codemodel.JExpr.TRUE;
import static com.sun.codemodel.JExpr._new;

import org.jsonschema2pojo.exception.GenerationException;
import org.jsonschema2pojo.rules.RuleFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

/**
 * Annotates generated Java types using Gson. The annotations used here are most
 * useful when the JSON fields have characters (like underscores) that are
 * poorly suited for beans. By using the {@link SerializedName} annotation, we
 * are able to preserve the original format. Use this in conjunction with
 * {@link GenerationConfig#getPropertyWordDelimiters} to filter out underscores
 * or other unwanted delimiters but still marshal/unmarshal the same content.
 * 
 * @see <a
 *      href="https://code.google.com/p/google-gson/">https://code.google.com/p/google-gson/</a>
 */
public class GsonAnnotator extends AbstractAnnotator {

    @Override
    public void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
        field.annotate(SerializedName.class).param("value", propertyName);
        field.annotate(Expose.class);
    }

    @Override
    public void enumConstant(JEnumConstant constant, String value) {
        constant.annotate(SerializedName.class).param("value", value);
    }

    @Override
    public boolean isAdditionalPropertiesSupported() {
        return false;
    }
    
    @Override
    public void propertyDeserializer(final RuleFactory ruleFactory, JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode, Schema currentSchema) {
      if (propertyNode.has("oneOf")) {
        JClass typeAdapterFactory = addOneOfTypeFactory(ruleFactory, field, clazz, propertyName, propertyNode, currentSchema);
        field.annotate(JsonAdapter.class).param("value", typeAdapterFactory);
      }
    }
    
    static JDefinedClass addOneOfTypeFactory(final RuleFactory ruleFactory, JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode, Schema currentSchema) {
      return new OneOfTemplates.OneOfTemplate(clazz.owner(), field, clazz, propertyName, propertyNode, currentSchema) {

        @Override
        public JDefinedClass createDeserializer() throws Exception {
          JClass gson = model.ref(Gson.class);
          JClass typeT = model.ref("T");
          JClass typeTokenField = model.ref(TypeToken.class).narrow(field.type());
          JClass typeAdapterT = model.ref(TypeAdapter.class).narrow(typeT);
          JClass typeAdapterField = model.ref(TypeAdapter.class).narrow(field.type());
          JClass typeAdapterFactory = model.ref(TypeAdapterFactory.class);
          JClass jsonElement = model.ref(JsonElement.class);

          JDefinedClass adapterImpl = this.deserContainer._class(JMod.STATIC, "GsonTypeAdapter");
          adapterImpl._extends(typeAdapterField);

          JFieldVar gsonField = adapterImpl.field(JMod.PRIVATE, gson, "gson");
          JFieldVar typeField = adapterImpl.field(JMod.PRIVATE, typeTokenField, "type");

          JMethod adapterImplConstructor = adapterImpl.constructor(JMod.PUBLIC);
          JVar gsonConstructorVar = adapterImplConstructor.param(gson, "gson");
          JVar typeConstructorVar = adapterImplConstructor.param(typeTokenField, "type");
          adapterImplConstructor.body().assign(JExpr.refthis(gsonField.name()), gsonConstructorVar);
          adapterImplConstructor.body().assign(JExpr.refthis(typeField.name()), typeConstructorVar);

          JMethod adapterImplWrite = adapterImpl.method(JMod.PUBLIC, model.VOID, "write");
          JVar writerVar = adapterImplWrite.param(model.ref(JsonWriter.class), "writer");
          JVar valueVar = adapterImplWrite.param(field.type(), "value");
          adapterImplWrite.body().add(gsonField.invoke("toJson").arg(valueVar).arg(typeField.invoke("getType")).arg(writerVar));

          JMethod adapterImplRead = adapterImpl.method(JMod.PUBLIC, field.type(), "read");
          JVar readerVar = adapterImplRead.param(model.ref(JsonReader.class), "reader");
          JBlock readBody = adapterImplRead.body();

          // read from Gson as JsonElement.
          JVar elementVar = readBody.decl(jsonElement, "element", gsonField.invoke("fromJson").arg(readerVar).arg(JExpr.dotclass(jsonElement)));

          // do type testing
          for( int i = 0; i < oneOf.size(); i++ ) {
            JType optionType = ruleFactory.getSchemaRule().apply(field.name()+"Option"+i, oneOf.get(i), clazz.parentContainer(), currentSchema);
            JMethod acceptMethod = acceptMethod(adapterImpl, i, oneOf.get(i) );
            JConditional ifAccepted = readBody._if(adapterImpl.staticInvoke(acceptMethod).arg(elementVar));
            JBlock ifAcceptedThen = ifAccepted._then();
            JClass typeRefClass = model.ref(TypeToken.class).narrow(optionType);
            JVar typeRef = ifAcceptedThen.decl(typeRefClass, "typeRef", _new(model.anonymousClass(typeRefClass)));
            ifAcceptedThen._return(
                gsonField.invoke("fromJson")
                .arg(elementVar)
                .arg(typeRef.invoke("getType")));
          }
          // read JsonElement to target type using Gson.
          readBody._return(gsonField.invoke("fromJson").arg(elementVar).arg(typeField.invoke("getType")));

          JDefinedClass factoryImpl = deserContainer._class(JMod.PUBLIC|JMod.STATIC, "GsonTypeAdapterFactory");
          factoryImpl._implements(typeAdapterFactory);

          JMethod createMethod = factoryImpl.method(JMod.PUBLIC, typeAdapterT, "create");
          createMethod.generify("T");
          JVar gsonVar = createMethod.param(model.ref(Gson.class), "gson");
          JVar typeTokenVar = createMethod.param(model.ref(TypeToken.class).narrow(typeT), "typeToken");

          createMethod.body()._return(JExpr.cast(typeAdapterT, JExpr._new(adapterImpl).arg(gsonVar).arg(JExpr.cast(typeTokenField, typeTokenVar))));
          return factoryImpl;
        }
      }.execute();

    }

    static JMethod acceptMethod(JDefinedClass deserClass, int optionIndex, JsonNode optionNode) {
      JCodeModel model = deserClass.owner();
      JMethod acceptMethod = deserClass.method(JMod.PRIVATE|JMod.STATIC, model.BOOLEAN, "isOption"+optionIndex);
      JVar jsonElementVar = acceptMethod.param(model.ref(JsonElement.class), "element");
      
      JBlock body = acceptMethod.body();
      JsonNode typeNode = optionNode.path("type");
      String type = typeNode.isMissingNode() ? "object" : typeNode.asText();
      
      if( "string".equals(type) ) {
        final JVar jsonPrimitive = filterNotPrimitive(model, body, jsonElementVar);
        body._if(jsonPrimitive.invoke("isString").not())._then()._return(JExpr.FALSE);
        new OneOfTemplates.StringFilterTemplate(optionNode, model, body) {
          @Override public JExpression valueExpr() {return jsonPrimitive.invoke("getAsString");}
        }.execute();
        body._return(JExpr.TRUE);
      }
      else if( "integer".equals(type) ) {
        final JVar jsonPrimitive = filterNotPrimitive(model, body, jsonElementVar);
        body._if(jsonPrimitive.invoke("isNumber").not())._then()._return(JExpr.FALSE);
        new OneOfTemplates.IntegerFilterTemplate(optionNode, optionIndex, model, deserClass, body) {
          @Override public JExpression valueExpr() { return jsonPrimitive.invoke("getAsBigInteger");}
        }.execute();
        body._return(TRUE);      }
      else {
        body._return(JExpr.FALSE);
      }
      
      return acceptMethod;
    }
    
    static JVar filterNotPrimitive(JCodeModel model, JBlock body, JVar jsonElementVar) {
      body._if(jsonElementVar.invoke("isJsonPrimitive").not())._then()._return(JExpr.FALSE);
      return body.decl(model.ref(JsonPrimitive.class), "jsonPrimitive", jsonElementVar.invoke("getAsJsonPrimitive"));
    }
    

    static JDefinedClass innerClass( JDefinedClass clazz, int mods, String name ) {
      try {
        return clazz._class(mods, name);
      } catch( JClassAlreadyExistsException cae ) {
        return cae.getExistingClass();
      }
    }
}
