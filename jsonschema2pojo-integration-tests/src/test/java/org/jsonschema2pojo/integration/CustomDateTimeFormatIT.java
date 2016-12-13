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

package org.jsonschema2pojo.integration;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This test utilizes the json schema customDateTimeFormat.json located in src/test/resources/schema/format/
 * 
 * It generates 2 classes located in target/jsonschema2pojo/CustomDateTimeFoormatIT/
 * 1. com.example.config_true.CustomDateTimeFormat - generated with config option formatDateTimes set to True
 * 2. com.example.config_false.CustomDateTimeFormat - generated with config option formatDateTimes set to False
 * 
 * The data used here is tightly coupled with the schema defined in customDateTimeFormat.json
 * Any modifications here must be synced up with the json schema and vice versa
 * 
 * @author shrpurohit
 *
 */
public class CustomDateTimeFormatIT {
    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();
    
    private static Class<?> classWhenConfigIsTrue;
    private static Class<?> classWhenConfigIsFalse;
    
    private static SimpleDateFormat dateTimeMilliSecFormatter;
    private static SimpleDateFormat dateTimeFormatter;
    private static SimpleDateFormat dateFormatter;

    /**
     * We are going to generate the same class twice:
     * Once with the configuration option formatDateTimes set to TRUE
     * Once with the configuration option formatDateTimes set to FALSE
     * 
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @BeforeClass
    public static void generateClasses() throws ClassNotFoundException, IOException {
        // The SimpleDateFormat instances created and configured here are based on the json schema defined in customDateTimeFormat.json
        dateTimeMilliSecFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        dateTimeMilliSecFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateTimeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("PST"));
        
        Map<String, Object> configValues = new HashMap<String, Object>();
        
        // Generate class with config option formatDateTimes = TRUE
        configValues.put("formatDateTimes", Boolean.TRUE);
        classSchemaRule.generate("/schema/format/customDateTimeFormat.json", "com.example.config_true", configValues);
        
        // Generate class with config option formatDateTimes = FALSE
        configValues.put("formatDateTimes", Boolean.FALSE);
        classSchemaRule.generate("/schema/format/customDateTimeFormat.json", "com.example.config_false", configValues);
        
        ClassLoader loader = classSchemaRule.compile();
        
        // Class generated when formatDateTimes = TRUE in configuration
        classWhenConfigIsTrue = loader.loadClass("com.example.config_true.CustomDateTimeFormat");
        // Class generated when formatDateTimes = FALSE in configuration
        classWhenConfigIsFalse = loader.loadClass("com.example.config_false.CustomDateTimeFormat");
    }
    
    /**
     * This tests the class generated when formatDateTimes config option is set to TRUE
     * The field should have @JsonFormat annotation with iso8601 date time pattern and UTC timezone
     * It also tests the serialization and deserialization process
     * 
     * @throws Exception
     */
    @Test
    public void testDefaultWhenFormatDateTimesConfigIsTrue() throws Exception {
        Field field = classWhenConfigIsTrue.getDeclaredField("defaultFormat");
        JsonFormat annotation = field.getAnnotation(JsonFormat.class);
        
        assertThat(annotation, notNullValue());
        // Assert that the patterns match
        assertEquals("yyyy-MM-dd'T'HH:mm:ss.SSS", annotation.pattern());
        // Assert that the timezones match
        assertEquals("UTC", annotation.timezone());
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        ObjectNode node = objectMapper.createObjectNode();
        node.put("defaultFormat", "2016-11-06T00:00:00.000");

        Object pojo = objectMapper.treeToValue(node, classWhenConfigIsTrue);
        
        Method getter = new PropertyDescriptor("defaultFormat", classWhenConfigIsTrue).getReadMethod();
        
        // Assert that the Date object in the deserialized class is as expected
        assertEquals(dateTimeMilliSecFormatter.parse("2016-11-06T00:00:00.000").toString(), getter.invoke(pojo).toString());
        
        JsonNode jsonVersion = objectMapper.valueToTree(pojo);
        
        // Assert that when the class is serialized, the date object is serialized as expected 
        assertEquals("2016-11-06T00:00:00.000", jsonVersion.get("defaultFormat").asText());
    }
    
    /**
     * This tests the class generated when formatDateTimes config option is set to TRUE
     * The field should have @JsonFormat annotation with pattern defined in json schema and UTC timezone
     * It also tests the serialization and deserialization process
     * 
     * @throws Exception
     */
    @Test
    public void testCustomDateTimePatternWithDefaultTimezoneWhenFormatDateTimesConfigIsTrue() throws Exception {
        Field field = classWhenConfigIsTrue.getDeclaredField("customFormatDefaultTZ");
        JsonFormat annotation = field.getAnnotation(JsonFormat.class);
        
        assertThat(annotation, notNullValue());
        // Assert that the patterns match
        assertEquals("yyyy-MM-dd'T'HH:mm:ss", annotation.pattern());
        // Assert that the timezones match
        assertEquals("UTC", annotation.timezone());
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        ObjectNode node = objectMapper.createObjectNode();
        node.put("customFormatDefaultTZ", "2016-11-06T00:00:00");

        Object pojo = objectMapper.treeToValue(node, classWhenConfigIsTrue);
        
        Method getter = new PropertyDescriptor("customFormatDefaultTZ", classWhenConfigIsTrue).getReadMethod();
        
        // Assert that the Date object in the deserialized class is as expected
        assertEquals(dateTimeFormatter.parse("2016-11-06T00:00:00").toString(), getter.invoke(pojo).toString());
        
        JsonNode jsonVersion = objectMapper.valueToTree(pojo);
        
        // Assert that when the class is serialized, the date object is serialized as expected 
        assertEquals("2016-11-06T00:00:00", jsonVersion.get("customFormatDefaultTZ").asText());
    }
    
    /**
     * This tests the class generated when formatDateTimes config option is set to TRUE
     * The field should have @JsonFormat annotation with pattern and timezone defined in json schema
     * It also tests the serialization and deserialization process
     * 
     * @throws Exception
     */
    @Test
    public void testCustomDateTimePatternWithCustomTimezoneWhenFormatDateTimesConfigIsTrue() throws Exception{
        Field field = classWhenConfigIsTrue.getDeclaredField("customFormatCustomTZ");
        JsonFormat annotation = field.getAnnotation(JsonFormat.class);
        
        assertThat(annotation, notNullValue());
        // Assert that the patterns match
        assertEquals("yyyy-MM-dd", annotation.pattern());
        // Assert that the timezones match
        assertEquals("PST", annotation.timezone());
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setTimeZone(TimeZone.getTimeZone("PST"));
        
        ObjectNode node = objectMapper.createObjectNode();
        node.put("customFormatCustomTZ", "2016-11-06");

        Object pojo = objectMapper.treeToValue(node, classWhenConfigIsTrue);
        
        Method getter = new PropertyDescriptor("customFormatCustomTZ", classWhenConfigIsTrue).getReadMethod();
        
        // Assert that the Date object in the deserialized class is as expected
        assertEquals(dateFormatter.parse("2016-11-06").toString(), getter.invoke(pojo).toString());
        
        JsonNode jsonVersion = objectMapper.valueToTree(pojo);
        
        // Assert that when the class is serialized, the date object is serialized as expected 
        assertEquals("2016-11-06", jsonVersion.get("customFormatCustomTZ").asText());
    }
    
    /**
     * This tests the class generated when formatDateTimes config option is set to FALSE
     * The field should not have @JsonFormat annotation
     * 
     * @throws Exception
     */
    @Test
    public void testDefaultWhenFormatDateTimesConfigIsFalse() throws Exception{
        Field field = classWhenConfigIsFalse.getDeclaredField("defaultFormat");
        // Verify that no annotation is generated
        assertEquals(Boolean.FALSE, field.isAnnotationPresent(JsonFormat.class));
    }
    
    /**
     * This tests the class generated when formatDateTimes config option is set to FALSE
     * The field should have @JsonFormat annotation with pattern defined in json schema and UTC timezone
     * It also tests the serialization and deserialization process
     * 
     * @throws Exception
     */
    @Test
    public void testCustomDateTimePatternWithDefaultTimezoneWhenFormatDateTimesConfigIsFalse() throws Exception {
        Field field = classWhenConfigIsFalse.getDeclaredField("customFormatDefaultTZ");
        JsonFormat annotation = field.getAnnotation(JsonFormat.class);
        
        assertThat(annotation, notNullValue());
        // Assert that the patterns match
        assertEquals("yyyy-MM-dd'T'HH:mm:ss", annotation.pattern());
        // Assert that the timezones match
        assertEquals("UTC", annotation.timezone());
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        ObjectNode node = objectMapper.createObjectNode();
        node.put("customFormatDefaultTZ", "2016-11-06T00:00:00");

        Object pojo = objectMapper.treeToValue(node, classWhenConfigIsFalse);
        
        Method getter = new PropertyDescriptor("customFormatDefaultTZ", classWhenConfigIsFalse).getReadMethod();
        
        // Assert that the Date object in the deserialized class is as expected
        assertEquals(dateTimeFormatter.parse("2016-11-06T00:00:00").toString(), getter.invoke(pojo).toString());
        
        JsonNode jsonVersion = objectMapper.valueToTree(pojo);
        
        // Assert that when the class is serialized, the date object is serialized as expected 
        assertEquals("2016-11-06T00:00:00", jsonVersion.get("customFormatDefaultTZ").asText());
    }
    
    /**
     * This tests the class generated when formatDateTimes config option is set to FALSE
     * The field should have @JsonFormat annotation with pattern and timezone defined in json schema
     * It also tests the serialization and deserialization process
     * 
     * @throws Exception
     */
    @Test
    public void testCustomDateTimePatternWithCustomTimezoneWhenFormatDateTimesConfigIsFalse() throws Exception {
        Field field = classWhenConfigIsFalse.getDeclaredField("customFormatCustomTZ");
        JsonFormat annotation = field.getAnnotation(JsonFormat.class);
        
        assertThat(annotation, notNullValue());
        // Assert that the patterns match
        assertEquals("yyyy-MM-dd", annotation.pattern());
        // Assert that the timezones match
        assertEquals("PST", annotation.timezone());
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setTimeZone(TimeZone.getTimeZone("PST"));
        
        ObjectNode node = objectMapper.createObjectNode();
        node.put("customFormatCustomTZ", "2016-11-06");

        Object pojo = objectMapper.treeToValue(node, classWhenConfigIsFalse);
        
        Method getter = new PropertyDescriptor("customFormatCustomTZ", classWhenConfigIsFalse).getReadMethod();
        
        // Assert that the Date object in the deserialized class is as expected
        assertEquals(dateFormatter.parse("2016-11-06").toString(), getter.invoke(pojo).toString());
        
        JsonNode jsonVersion = objectMapper.valueToTree(pojo);
        
        // Assert that when the class is serialized, the date object is serialized as expected 
        assertEquals("2016-11-06", jsonVersion.get("customFormatCustomTZ").asText());
    }
}
