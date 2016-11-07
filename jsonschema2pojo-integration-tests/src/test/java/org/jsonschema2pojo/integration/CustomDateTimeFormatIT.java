package org.jsonschema2pojo.integration;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.joda.time.DateTime;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.annotation.JsonFormat;

import edu.emory.mathcs.backport.java.util.Arrays;

@RunWith(Parameterized.class)
public class CustomDateTimeFormatIT {
    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    private static Class<?> classWithFormattedProperties;
    
    @Parameters(name="{0}")
    public static List<Object[]> data() {
        return asList(new Object[][] {
                /* { propertyName, expectedType, jsonValue, timezone, javaValue } */
                { "stringAsDateTime", Date.class, "2016-11-06", "PST", new Date(new GregorianCalendar(2016, 10, 06, 5, 0, 0).getTimeInMillis()) },
                { "stringAsDateTime2", DateTime.class, "2016-11-06", "PST", new DateTime(2016, 10, 06, 5, 0, 0) }});
    }

    private String propertyName;
    private Class<?> expectedType;
    private Object jsonValue;
    private String timezone;
    private Object javaValue;

    public CustomDateTimeFormatIT(String propertyName, Class<?> expectedType, Object jsonValue, String timezone, Object javaValue) {
        this.propertyName = propertyName;
        this.expectedType = expectedType;
        this.jsonValue = jsonValue;
        this.timezone = timezone;
        this.javaValue = javaValue;
    }

    @BeforeClass
    public static void generateClasses() throws ClassNotFoundException, IOException {

        ClassLoader resultsClassLoader = classSchemaRule.generateAndCompile("/schema/format/customDateTimeFormat.json", "com.example");

        classWithFormattedProperties = resultsClassLoader.loadClass("com.example.CustomDateTimeFormat");

    }

    @Test
    public void formatValueProducesExpectedType() throws NoSuchMethodException, IntrospectionException {

        Method getter = new PropertyDescriptor(propertyName, classWithFormattedProperties).getReadMethod();

        assertThat(getter.getReturnType().getName(), is(this.expectedType.getName()));

    }
    
    @Test
    public void formatValueProducesExpectedAnnotation() throws NoSuchFieldException, SecurityException, InstantiationException, IllegalAccessException {

    	Field field = classWithFormattedProperties.getDeclaredField(propertyName);
    	
    	JsonFormat expected = field.getAnnotation(JsonFormat.class);
    	
    	assertThat(expected, notNullValue());
    }

    /*
     * TODO: Need to fix this test 
    @Test
    public void valueCanBeSerializedAndDeserialized() throws NoSuchMethodException, IOException, IntrospectionException, IllegalAccessException, InvocationTargetException {

        ObjectMapper objectMapper = new ObjectMapper();
        
        if (timezone != null){
        	objectMapper.setTimeZone(TimeZone.getTimeZone(timezone));
        }
        
        ObjectNode node = objectMapper.createObjectNode();
        node.put(propertyName, jsonValue.toString());

        Object pojo = objectMapper.treeToValue(node, classWithFormattedProperties);
        
        Method getter = new PropertyDescriptor(propertyName, classWithFormattedProperties).getReadMethod();
        assertThat(getter.invoke(pojo).toString(), is(equalTo(javaValue.toString())));

        JsonNode jsonVersion = objectMapper.valueToTree(pojo);
        assertThat(jsonVersion.get(propertyName).asText(), is(equalTo(jsonValue.toString())));
    }
     */
}
