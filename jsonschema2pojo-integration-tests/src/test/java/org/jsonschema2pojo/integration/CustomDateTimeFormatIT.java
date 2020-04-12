/**
 * Copyright © 2010-2020 Nokia
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

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomDateTimeFormatIT {
    @ClassRule
    public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    private static Class<?> classWhenFormatDatesTrue;
    private static Class<?> classWhenFormatDatesFalse;
    private static Class<?> classWithCustomPatterns;

    @BeforeClass
    public static void generateClasses() throws ClassNotFoundException {

        classSchemaRule.generate("/schema/format/customDateTimeFormat.json", "com.example.config_true", config(
                "dateType", "java.util.Date",
                "timeType", "java.util.Date",
                "formatDateTimes", Boolean.TRUE,
                "formatDates", Boolean.TRUE,
                "formatTimes", Boolean.TRUE));

        classSchemaRule.generate("/schema/format/customDateTimeFormat.json", "com.example.config_false", config(
                "dateType", "java.util.Date",
                "timeType", "java.util.Date",
                "formatDateTimes", Boolean.FALSE,
                "formatDates", Boolean.FALSE,
                "formatDates", Boolean.FALSE));

        classSchemaRule.generate("/schema/format/customDateTimeFormat.json", "com.example.config_custom", config(
                "dateType", "java.util.Date",
                "timeType", "java.util.Date",
                "customDatePattern", "yyyy",
                "customTimePattern", "H:mm a",
                "customDateTimePattern", "yyyy-MM-dd HH:mm X",
                "formatDateTimes", Boolean.TRUE,
                "formatDates", Boolean.TRUE,
                "formatTimes", Boolean.TRUE));

        ClassLoader loader = classSchemaRule.compile();

        classWhenFormatDatesTrue = loader.loadClass("com.example.config_true.CustomDateTimeFormat");
        classWhenFormatDatesFalse = loader.loadClass("com.example.config_false.CustomDateTimeFormat");
        classWithCustomPatterns = loader.loadClass("com.example.config_custom.CustomDateTimeFormat");
    }

    @Test
    public void testDefaultFormattedDateWithCustomPattern() throws Exception {

        final Object instance = classWithCustomPatterns.newInstance();
        classWithCustomPatterns.getMethod("setDefaultFormatDate", Date.class).invoke(instance, new Date(999999999999L));

        final String json = new ObjectMapper().writeValueAsString(instance);

        assertThat(json, is("{\"defaultFormatDate\":\"2001\"}"));
    }

    @Test
    public void testDefaultFormattedDateTimeWithCustomPattern() throws Exception {

        final Object instance = classWithCustomPatterns.newInstance();
        classWithCustomPatterns.getMethod("setDefaultFormat", Date.class).invoke(instance, new Date(999999999999L));

        final String json = new ObjectMapper().writeValueAsString(instance);

        assertThat(json, is("{\"defaultFormat\":\"2001-09-09 01:46 Z\"}"));
    }

    @Test
    public void testDefaultWhenFormatDateTimesConfigIsTrue() throws Exception {

        final Object instance = classWhenFormatDatesTrue.newInstance();
        classWhenFormatDatesTrue.getMethod("setDefaultFormat", Date.class).invoke(instance, new Date(999999999999L));

        final String json = new ObjectMapper().writeValueAsString(instance);

        assertThat(json, is("{\"defaultFormat\":\"2001-09-09T01:46:39.999Z\"}"));
    }

    @Test
    public void testDefaultWithCustomTimezoneWhenFormatDateTimesConfigIsTrue() throws Exception {

        final Object instance = classWhenFormatDatesTrue.newInstance();
        classWhenFormatDatesTrue.getMethod("setDefaultFormatCustomTZ", Date.class).invoke(instance, new Date(999999999999L));

        final String json = new ObjectMapper().writeValueAsString(instance);

        assertThat(json, is("{\"defaultFormatCustomTZ\":\"2001-09-08T18:46:39.999-07:00\"}"));
    }

    @Test
    public void testCustomDateTimePatternWithDefaultTimezoneWhenFormatDateTimesConfigIsTrue() throws Exception {
        final Object instance = classWhenFormatDatesTrue.newInstance();
        classWhenFormatDatesTrue.getMethod("setCustomFormatDefaultTZ", Date.class).invoke(instance, new Date(999999999999L));

        final String json = new ObjectMapper().writeValueAsString(instance);

        assertThat(json, is("{\"customFormatDefaultTZ\":\"2001-09-09T01:46:39\"}"));
    }

    @Test
    public void testCustomDateTimePatternWithCustomTimezoneWhenFormatDateTimesConfigIsTrue() throws Exception{
        final Object instance = classWhenFormatDatesTrue.newInstance();
        classWhenFormatDatesTrue.getMethod("setCustomFormatCustomTZ", Date.class).invoke(instance, new Date(999999999999L));

        final String json = new ObjectMapper().writeValueAsString(instance);

        assertThat(json, is("{\"customFormatCustomTZ\":\"2001-09-08T18:46:39\"}"));
    }

    @Test
    public void testDefaultWhenFormatDateTimesConfigIsFalse() throws Exception{
        final Object instance = classWhenFormatDatesFalse.newInstance();
        classWhenFormatDatesFalse.getMethod("setDefaultFormat", Date.class).invoke(instance, new Date(999999999999L));

        final String json = new ObjectMapper().writeValueAsString(instance);

        assertThat(json, is("{\"defaultFormat\":999999999999}"));
    }

    @Test
    public void testCustomDateTimePatternWithDefaultTimezoneWhenFormatDateTimesConfigIsFalse() throws Exception {
        final Object instance = classWhenFormatDatesFalse.newInstance();
        classWhenFormatDatesFalse.getMethod("setCustomFormatDefaultTZ", Date.class).invoke(instance, new Date(999999999999L));

        final String json = new ObjectMapper().writeValueAsString(instance);

        assertThat(json, is("{\"customFormatDefaultTZ\":\"2001-09-09T01:46:39\"}"));
    }

    @Test
    public void testCustomDateTimePatternWithCustomTimezoneWhenFormatDateTimesConfigIsFalse() throws Exception {
        final Object instance = classWhenFormatDatesFalse.newInstance();
        classWhenFormatDatesFalse.getMethod("setCustomFormatCustomTZ", Date.class).invoke(instance, new Date(999999999999L));

        final String json = new ObjectMapper().writeValueAsString(instance);

        assertThat(json, is("{\"customFormatCustomTZ\":\"2001-09-08T18:46:39\"}"));
    }

    @Test
    public void testDefaultWhenFormatDatesConfigIsTrue() throws ReflectiveOperationException, SecurityException, JsonProcessingException {
        final Object instance = classWhenFormatDatesTrue.newInstance();
        classWhenFormatDatesTrue.getMethod("setDefaultFormatDate", Date.class).invoke(instance, new Date(999999999999L));

        final String json = new ObjectMapper().writeValueAsString(instance);

        assertThat(json, is("{\"defaultFormatDate\":\"2001-09-09\"}"));
    }

    @Test
    public void testDefaultWhenFormatTimesConfigIsTrue() throws ReflectiveOperationException, SecurityException, JsonProcessingException {
        final Object instance = classWhenFormatDatesTrue.newInstance();
        classWhenFormatDatesTrue.getMethod("setDefaultFormatTime", Date.class).invoke(instance, new Date(999999999999L));

        final String json = new ObjectMapper().writeValueAsString(instance);

        assertThat(json, is("{\"defaultFormatTime\":\"01:46:39.999\"}"));
    }

    @Test
    public void testDefaultWhenFormatDatesConfigIsFalse() throws ReflectiveOperationException, SecurityException, JsonProcessingException {
        final Object instance = classWhenFormatDatesFalse.newInstance();
        classWhenFormatDatesFalse.getMethod("setDefaultFormatDate", Date.class).invoke(instance, new Date(999999999999L));

        final String json = new ObjectMapper().writeValueAsString(instance);

        assertThat(json, is("{\"defaultFormatDate\":999999999999}"));
    }

    @Test
    public void testDefaultWhenFormatTimesConfigIsFalse() throws ReflectiveOperationException, SecurityException, JsonProcessingException {
        final Object instance = classWhenFormatDatesFalse.newInstance();
        classWhenFormatDatesFalse.getMethod("setDefaultFormatTime", Date.class).invoke(instance, new Date(999999999999L));

        final String json = new ObjectMapper().writeValueAsString(instance);

        assertThat(json, is("{\"defaultFormatTime\":999999999999}"));
    }

    @Test
    public void testCustomDatePattern() throws ReflectiveOperationException, SecurityException, JsonProcessingException {
        final Object instance = classWhenFormatDatesTrue.newInstance();
        classWhenFormatDatesTrue.getMethod("setCustomFormatCustomDate", Date.class).invoke(instance, new Date(999999999999L));

        final String json = new ObjectMapper().writeValueAsString(instance);

        assertThat(json, is("{\"customFormatCustomDate\":\"09-09-2001\"}"));
    }

    @Test
    public void testCustomTimePattern() throws ReflectiveOperationException, SecurityException, JsonProcessingException {
        final Object instance = classWhenFormatDatesTrue.newInstance();
        classWhenFormatDatesTrue.getMethod("setCustomFormatCustomTime", Date.class).invoke(instance, new Date(999999999999L));

        final String json = new ObjectMapper().setLocale(Locale.ENGLISH).writeValueAsString(instance);

        assertThat(json, is("{\"customFormatCustomTime\":\"1:46 AM\"}"));
    }
}
