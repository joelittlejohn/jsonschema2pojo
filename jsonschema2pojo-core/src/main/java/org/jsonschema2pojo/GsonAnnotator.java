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

import java.io.Writer;

import org.jsonschema2pojo.exception.GenerationException;
import org.jsonschema2pojo.rules.RuleFactory;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
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
    public void propertyDeserializer(RuleFactory ruleFactory, JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode, Schema currentSchema) {
      if (propertyNode.has("oneOf")) {
        JClass typeAdapterFactory = addOneOfTypeFactory(ruleFactory, field, clazz, propertyName, propertyNode, currentSchema);
        field.annotate(JsonAdapter.class).param("value", typeAdapterFactory);
      }
    }
    
    static JDefinedClass addOneOfTypeFactory(RuleFactory ruleFactory, JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode, Schema currentSchema) {
      JCodeModel model = clazz.owner();
      JsonNode oneOf = propertyNode.get("oneOf");
      String fieldName = field.name();
      if( !oneOf.isArray() ) throw new IllegalArgumentException("oneOf must contain an array");
      
      String deserContainerName = fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1);
      JDefinedClass deserContainer = innerClass(clazz, JMod.PUBLIC|JMod.STATIC, deserContainerName);
      
      JClass gson = model.ref(Gson.class);
      JClass typeT = model.ref("T");
      JClass typeTokenT = model.ref(TypeToken.class).narrow(typeT);
      JClass typeTokenField = model.ref(TypeToken.class).narrow(field.type());
      JClass typeAdapterT = model.ref(TypeAdapter.class).narrow(typeT);
      JClass typeAdapterWild = model.ref(TypeAdapter.class).narrow(model.wildcard());
      JClass typeAdapterField = model.ref(TypeAdapter.class).narrow(field.type());
      JClass typeAdapterFactory = model.ref(TypeAdapterFactory.class);
      JClass jsonElement = model.ref(JsonElement.class);
      try {
        JDefinedClass adapterImpl = deserContainer._class(JMod.STATIC, "GsonTypeAdapter");
        adapterImpl._implements(typeAdapterField);
        
        JFieldVar gsonField = adapterImpl.field(JMod.PRIVATE, gson, "gson");
        JFieldVar typeField = adapterImpl.field(JMod.PRIVATE, typeTokenField, "type");
        
        JMethod adapterImplConstructor = adapterImpl.constructor(JMod.PUBLIC);
        JVar gsonConstructorVar = adapterImplConstructor.param(gson, "gson");
        JVar typeConstructorVar = adapterImplConstructor.param(typeTokenField, "type");
        adapterImplConstructor.body().assign(gsonField, gsonConstructorVar);
        adapterImplConstructor.body().assign(typeField, typeConstructorVar);
        
        JMethod adapterImplWrite = adapterImpl.method(JMod.PUBLIC, model.VOID, "write");
        JVar writerVar = adapterImplWrite.param(model.ref(JsonWriter.class), "writer");
        JVar valueVar = adapterImplWrite.param(field.type(), "value");
        adapterImplWrite.body().add(gsonField.invoke("toJson").arg(valueVar).arg(typeField).arg(writerVar));
        
        JMethod adapterImplRead = adapterImpl.method(JMod.PUBLIC, field.type(), "read");
        JVar readerVar = adapterImplRead.param(model.ref(JsonReader.class), "reader");
        JBlock readBody = adapterImplRead.body();
        
        // read from Gson as JsonElement.
        JVar elementVar = readBody.decl(jsonElement, "element", gsonField.invoke("fromJson").arg(readerVar).arg(JExpr.dotclass(model.ref(Class.class).narrow(jsonElement))));
        
        // do type testing
        
        // read JsonElement to target type using Gson.
        readBody._return(gsonField.invoke("fromJson").arg(elementVar).arg(typeField));
        
        JDefinedClass factoryImpl = deserContainer._class(JMod.PUBLIC|JMod.STATIC, "GsonTypeAdapterFactory");
        factoryImpl._implements(typeAdapterFactory);
        
        JMethod createMethod = factoryImpl.method(JMod.PUBLIC, typeAdapterT, "create");
        createMethod.generify("T");
        JVar gsonVar = createMethod.param(model.ref(Gson.class), "gson");
        JVar typeTokenVar = createMethod.param(model.ref(TypeToken.class).narrow(typeT), "typeToken");
        
        createMethod.body()._return(JExpr.cast(typeAdapterT, JExpr._new(adapterImpl).arg(gsonVar).arg(JExpr.cast(typeTokenField, typeTokenVar))));
        return factoryImpl;
      } catch( Exception e ) {
        throw new GenerationException("could not generate gson oneOf implementation", e);
      }
    }

    static JDefinedClass innerClass( JDefinedClass clazz, int mods, String name ) {
      try {
        return clazz._class(mods, name);
      } catch( JClassAlreadyExistsException cae ) {
        return cae.getExistingClass();
      }
    }
}
