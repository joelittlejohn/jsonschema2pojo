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

package org.jsonschema2pojo.integration;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static java.lang.String.format;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.hamcrest.Matcher;
import org.jsonschema2pojo.AbstractAnnotator;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;

public class MediaIT {
    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();
    private static Class<?> classWithMediaProperties;
    private static Class<byte[]> BYTE_ARRAY = byte[].class;

    @BeforeClass
    public static void generateAndCompileClass() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = classSchemaRule.generateAndCompile("/schema/media/mediaProperties.json", "com.example", config("customAnnotator", QuotedPrintableAnnotator.class.getName()));

        classWithMediaProperties = resultsClassLoader.loadClass("com.example.MediaProperties");
    }

    @Test
    public void shouldCreateByteArrayField() throws SecurityException, NoSuchFieldException {
        Field field = classWithMediaProperties.getDeclaredField("minimalBinary");

        assertThat("the minimal binary field has type byte[]", field.getType(), equalToType(BYTE_ARRAY));
    }

    @Test
    public void shouldCreateByteArrayGetter() throws SecurityException, NoSuchMethodException {
        Method getter = classWithMediaProperties.getDeclaredMethod("getMinimalBinary");

        assertThat("the minimal binary getter has return type byte[]", getter.getReturnType(), equalToType(BYTE_ARRAY));
    }

    @Test
    public void shouldCreateByteArraySetter() throws SecurityException, NoSuchMethodException {
        Method setter = classWithMediaProperties.getDeclaredMethod("setMinimalBinary", BYTE_ARRAY);

        assertThat("the minimal binary setter has return type void", setter.getReturnType(), equalToType(Void.TYPE));
    }

    @Test
    public void shouldCreateByteArrayFieldWithAnyEncoding() throws SecurityException, NoSuchFieldException {
        Field field = classWithMediaProperties.getDeclaredField("anyBinaryEncoding");
        JsonSerialize serAnnotation = field.getAnnotation(JsonSerialize.class);
        JsonDeserialize deserAnnotation = field.getAnnotation(JsonDeserialize.class);

        assertThat("any binary encoding field has type byte[]", field.getType(), equalToType(BYTE_ARRAY));
        assertThat("any binary encoding has a serializer", serAnnotation, notNullValue());
        assertThat("any binary encoding has a deserializer", deserAnnotation, notNullValue());
    }

    @Test
    public void shouldCreateByteArrayGetterWithAnyEncoding() throws SecurityException, NoSuchMethodException {
        Method getter = classWithMediaProperties.getDeclaredMethod("getAnyBinaryEncoding");

        assertThat("any binary encoding getter has return type byte[]", getter.getReturnType(), equalToType(BYTE_ARRAY));
    }

    @Test
    public void shouldCreateByteArraySetterWithAnyEncoding() throws SecurityException, NoSuchMethodException {
        Method setter = classWithMediaProperties.getDeclaredMethod("setAnyBinaryEncoding", BYTE_ARRAY);

        assertThat("any binary encoding setter has return type void", setter.getReturnType(), equalToType(Void.TYPE));
    }

    @Test
    public void shouldCreateStringFieldWithoutEncoding() throws SecurityException, NoSuchFieldException {
        Field field = classWithMediaProperties.getDeclaredField("unencoded");

        assertThat("unencoded field has type String", field.getType(), equalToType(String.class));
    }

    @Test
    public void shouldCreateStringGetterWithoutEncoding() throws SecurityException, NoSuchMethodException {
        Method getter = classWithMediaProperties.getDeclaredMethod("getUnencoded");

        assertThat("unencoded getter has return type String", getter.getReturnType(), equalToType(String.class));
    }

    @Test
    public void shouldCreateStringSetterWithoutEncoding() throws SecurityException, NoSuchMethodException {
        Method setter = classWithMediaProperties.getDeclaredMethod("setUnencoded", String.class);

        assertThat("unencoded setter has return type void", setter.getReturnType(), equalToType(Void.TYPE));
    }
    
    @Test
    public void shouldCreateUriFieldWithUriFormat() throws SecurityException, NoSuchFieldException {
        Field field = classWithMediaProperties.getDeclaredField("withUriFormat");

        assertThat("withUriFormat field has type URI", field.getType(), equalToType(URI.class));
    }
    
    @Test
    public void shouldHandleUnencodedDefault() throws Exception {
        Method getter = classWithMediaProperties.getDeclaredMethod("getUnencodedWithDefault");
        
        Object object = new ObjectMapper().readValue("{}", classWithMediaProperties);
        String value = (String)getter.invoke(object);

        assertThat("unencodedWithDefault has the default value",
                value, equalTo("default value"));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void shouldReasonablyHandleBase64Default() throws Exception {
        Method getter = classWithMediaProperties.getDeclaredMethod("getBase64WithDefault");
        
        Object object = new ObjectMapper().readValue("{}", classWithMediaProperties);
        byte[] value = (byte[])getter.invoke(object);

        // if we got here, then at least defaults do not blow up the code.  Make sure
        // we get null or the default.  Users should not depend on the functionality in
        // this situation, as it is unsupported.
        assertThat("base64WithDefault is null or the default value",
                value, 
                anyOf(
                        nullValue(), 
                        equalTo(new byte[] { (byte)0xFF, (byte)0xF0, (byte)0x0F, (byte)0x00})));
    }
    
    @Test
    public void shouldRoundTripBase64Field() throws Exception {
        roundTripAssertions(
                new ObjectMapper(),
                "minimalBinary",
                "//APAA==",
                new byte[] { (byte)0xFF, (byte)0xF0, (byte)0x0F, (byte)0x00});
    }
    
    @Test
    public void shouldRoundTripUnencodedField() throws Exception {
        roundTripAssertions(
                new ObjectMapper(),
                "unencoded",
                "some text",
                "some text");
    }
    
    @Test
    public void shouldRoundTripQuotedPrintableField() throws Exception {
        roundTripAssertions(
                new ObjectMapper(),
                "anyBinaryEncoding",
                "\"=E3=82=A8=E3=83=B3=E3=82=B3=E3=83=BC=E3=83=89=E3=81=95=E3=82=8C=E3=81=9F=E6=96=87=E5=AD=97=E5=88=97\" is Japanese for \"encoded string\"",
                "\"エンコードされた文字列\" is Japanese for \"encoded string\"".getBytes("UTF-8"));
    }
    
    @Test
    public void shouldRoundTripQuotedPrintableFieldWithNoFieldVisibility() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibilityChecker(
                mapper.getVisibilityChecker().withFieldVisibility(Visibility.NONE));

        roundTripAssertions(
                new ObjectMapper(),
                "anyBinaryEncoding",
                "\"=E3=82=A8=E3=83=B3=E3=82=B3=E3=83=BC=E3=83=89=E3=81=95=E3=82=8C=E3=81=9F=E6=96=87=E5=AD=97=E5=88=97\" is Japanese for \"encoded string\"",
                "\"エンコードされた文字列\" is Japanese for \"encoded string\"".getBytes("UTF-8"));
    }
    
    /**
     * Returns a matcher that tests for equality to the specified type.
     * @param type the type to check.
     * @return a matcher that tests for equality to the specified type.
     */
    @SuppressWarnings("rawtypes")
    public static Matcher<Class> equalToType( Class<?> type ) {
      return equalTo((Class)type);
    }
    
    public static void roundTripAssertions( ObjectMapper objectMapper, String propertyName, String jsonValue, Object javaValue) throws Exception {
     
        ObjectNode node = objectMapper.createObjectNode();
        node.put(propertyName, jsonValue);

        Object pojo = objectMapper.treeToValue(node, classWithMediaProperties);

        Method getter = new PropertyDescriptor(propertyName, classWithMediaProperties).getReadMethod();

        assertThat(getter.invoke(pojo), is(equalTo(javaValue)));

        JsonNode jsonVersion = objectMapper.valueToTree(pojo);

        assertThat(jsonVersion.get(propertyName).asText(), is(equalTo(jsonValue)));
    }
    
    /**
     * An example annotator that supports the quoted printable encoding, from RFC 2045.
     * 
     * @author Christian Trimble
     * @see <a href="http://tools.ietf.org/html/rfc2045#section-6.7">Quoted-Printable Content-Transfer-Encoding, Multipurpose Internet Mail Extensions (MIME) Part One: Format of Internet Message Bodies</a>
     */
    public static class QuotedPrintableAnnotator extends AbstractAnnotator {
        public static final String TYPE = "type";
        public static final String STRING = "string";
        public static final String MEDIA = "media";
        public static final String BINARY_ENCODING = "binaryEncoding";
        public static final String QUOTED_PRINTABLE = "quoted-printable";
        public static final String USING = "using";
        public static final String INCLUDE = "include";
        @Override
        public void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
            if( isQuotedPrintableProperty(propertyNode) ) {
                field.annotate(JsonSerialize.class)
                  .param(USING, QuotedPrintableSerializer.class)
                  .param(INCLUDE, JsonSerialize.Inclusion.NON_NULL);
                field.annotate(JsonDeserialize.class).param(USING, QuotedPrintableDeserializer.class);
            }
        }
        
        private static boolean isQuotedPrintableProperty( JsonNode propertyNode ) {
            return propertyNode.has(TYPE) &&
                    STRING.equals(propertyNode.get(TYPE).asText()) &&
            propertyNode.has(MEDIA) && 
            isQuotedPrintable(propertyNode.get(MEDIA));
            
        }
        
        private static boolean isQuotedPrintable( JsonNode mediaNode ) {
            return mediaNode.has(BINARY_ENCODING) &&
                    QUOTED_PRINTABLE.equalsIgnoreCase(mediaNode.get(BINARY_ENCODING).asText());
        }
        
    }
    
    public static class QuotedPrintableSerializer
      extends StdSerializer<byte[]>
      {
        private static QuotedPrintableCodec codec = new QuotedPrintableCodec();
        
        public QuotedPrintableSerializer() {
            super(byte[].class);
        }

        @Override
        public void serialize(byte[] value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeString(new String(codec.encode(value), "UTF-8"));
        }
        
      }

    @SuppressWarnings("serial")
    public static class QuotedPrintableDeserializer
      extends StdDeserializer<byte[]>
      {
        private static QuotedPrintableCodec codec = new QuotedPrintableCodec();
          
        public QuotedPrintableDeserializer() {
            super(byte[].class);
        }

        @Override
        public byte[] deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            try {
                return codec.decode(jp.getText().getBytes("UTF-8"));
            } catch (DecoderException e) {
                throw new IOException(format("could not decode quoted string in %s", jp.getCurrentName()), e);
            }
        }
        
      }

}
