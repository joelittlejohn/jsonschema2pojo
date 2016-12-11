package org.jsonschema2pojo.integration;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RunWith(Parameterized.class)
public class CustomDateTimeFormatIT {
    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();
    
    private static Class<?> classWhenConfigIsTrue;
    private static Class<?> classWhenConfigIsFalse;
    
    @Parameters(name="{0}")
    public static List<Object[]> data() throws ParseException {
        String defaultFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS";
        String defaultTZ = "UTC";
        SimpleDateFormat df1 = new SimpleDateFormat(defaultFormat);
        df1.setTimeZone(TimeZone.getTimeZone(defaultTZ));
        
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df2.setTimeZone(TimeZone.getTimeZone(defaultTZ));
        
        SimpleDateFormat df3 = new SimpleDateFormat("yyyy-MM-dd");
        df3.setTimeZone(TimeZone.getTimeZone("PST"));
        
        return asList(new Object[][] {
                /* { propertyName,          config_isFormatDateTime, isAnnotated, format, timezone, jsonValue, javaValue } */
                { "defaultFormat",          Boolean.TRUE, Boolean.TRUE, defaultFormat,            defaultTZ,  "2016-11-06T00:00:00.000", df1.parse("2016-11-06T00:00:00.000") },
                { "customFormatDefaultTZ",  Boolean.TRUE, Boolean.TRUE, "yyyy-MM-dd'T'HH:mm:ss",  defaultTZ,  "2016-11-06T00:00:00", df2.parse("2016-11-06T00:00:00") },
                { "customFormatCustomTZ",   Boolean.TRUE, Boolean.TRUE, "yyyy-MM-dd",             "PST",      "2016-11-06", df3.parse("2016-11-06") },
                
                { "defaultFormat",         Boolean.FALSE, Boolean.FALSE, null, null, null, null },
                { "customFormatDefaultTZ", Boolean.FALSE, Boolean.TRUE, "yyyy-MM-dd'T'HH:mm:ss", defaultTZ, "2016-11-06T00:00:00", df2.parse("2016-11-06T00:00:00") },
                { "customFormatCustomTZ",  Boolean.FALSE, Boolean.TRUE, "yyyy-MM-dd",            "PST",     "2016-11-06", df3.parse("2016-11-06") }
        });
    }

    private String propertyName;
    private Boolean config_isFormatDateTime;
    private Boolean isAnnotated;
    private String format;
    private String timezone;
    private Object jsonValue;
    private Object javaValue;

    public CustomDateTimeFormatIT(String propertyName, Boolean config_isFormatDateTime, Boolean isAnnotated, String format, String timezone, Object jsonValue, Object javaValue) {
        this.propertyName = propertyName;
        this.config_isFormatDateTime = config_isFormatDateTime;
        this.isAnnotated = isAnnotated;
        this.format = format;
        this.timezone = timezone;
        this.jsonValue = jsonValue;
        this.javaValue = javaValue;
    }

    /**
     * We are going to generate the same class twice:
     * Once with the configuration option formatDateTime set to TRUE
     * Once with the configuration option formatDateTime set to FALSE
     * 
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @BeforeClass
    public static void generateClasses() throws ClassNotFoundException, IOException {
        Map<String, Object> configValues = new HashMap<String, Object>();
        
        configValues.put("formatDateTime", Boolean.TRUE);
        classSchemaRule.generate("/schema/format/customDateTimeFormat.json", "com.example.config_true", configValues);
        
        configValues.put("formatDateTime", Boolean.FALSE);
        classSchemaRule.generate("/schema/format/customDateTimeFormat.json", "com.example.config_false", configValues);
        
        ClassLoader loader = classSchemaRule.compile();
        
        // Class generated when formatDateTime is set to TRUE in configuration
        classWhenConfigIsTrue = loader.loadClass("com.example.config_true.CustomDateTimeFormat");
        // Class generated when formatDateTime is set to FALSE in configuration
        classWhenConfigIsFalse = loader.loadClass("com.example.config_false.CustomDateTimeFormat");
    }
    
    /**
     * Test whether the annotation is supposed to be generated.
     * If it is supposed to be generated, test if the pattern and timezone attributes are as expected.
     * 
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Test
    public void formatValueProducesExpectedAnnotation() throws NoSuchFieldException, SecurityException, InstantiationException, IllegalAccessException {
        Field field;
        if (config_isFormatDateTime) {
            field = classWhenConfigIsTrue.getDeclaredField(propertyName);
        }
        else {
            field = classWhenConfigIsFalse.getDeclaredField(propertyName);
        }
        
        if (isAnnotated) {
            JsonFormat annotation = field.getAnnotation(JsonFormat.class);
            
            assertThat(annotation, notNullValue());
            // Assert that the patterns match
            assertEquals(format, annotation.pattern());
            // Assert that the timezones match
            assertEquals(timezone, annotation.timezone());
        }
        else {
            // Verify that no annotation is generated
            assertEquals(Boolean.FALSE, field.isAnnotationPresent(JsonFormat.class));
        }
        
    }

    @Test
    public void valueCanBeSerializedAndDeserialized() throws NoSuchMethodException, IOException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        @SuppressWarnings("rawtypes")
        Class generatedClass;
        if (config_isFormatDateTime) {
            generatedClass = classWhenConfigIsTrue;
        }
        else {
            generatedClass = classWhenConfigIsFalse;
        }
        
        // Assert only if annotation is generated
        if (isAnnotated) {
            ObjectMapper objectMapper = new ObjectMapper();
            
            if (timezone != null){
                objectMapper.setTimeZone(TimeZone.getTimeZone(timezone));
            }
            
            ObjectNode node = objectMapper.createObjectNode();
            node.put(propertyName, jsonValue.toString());

            @SuppressWarnings("unchecked")
            Object pojo = objectMapper.treeToValue(node, generatedClass);
            
            Method getter = new PropertyDescriptor(propertyName, generatedClass).getReadMethod();
            
            // Assert that the Date object in the deserialized class is as expected
            assertEquals(javaValue.toString(), getter.invoke(pojo).toString());
            
            JsonNode jsonVersion = objectMapper.valueToTree(pojo);
            
            // Assert that when the class is serialized, the date object is serialized as expected 
            assertEquals(jsonValue.toString(), jsonVersion.get(propertyName).asText());
        }
        
    }
}
