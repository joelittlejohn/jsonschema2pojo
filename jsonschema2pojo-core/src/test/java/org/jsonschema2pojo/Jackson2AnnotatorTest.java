/**
 * Copyright © 2010-2017 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sun.codemodel.*;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class Jackson2AnnotatorTest {

    @Test
    public void shouldIncludeTimeZoneByDefault() throws Exception {
        GenerationConfig generationConfig = new DefaultGenerationConfig() {
            @Override
            public boolean isFormatDateTimes() {
                return true;
            }
        };
        Jackson2Annotator annotator = new Jackson2Annotator(generationConfig);
        JDefinedClass clazz = new JCodeModel()._class("com.example.Test");
        JFieldVar field = clazz.field(0, java.util.Date.class, "myDateTime");
        JsonNode jsonNode = new TextNode("");
        assertTrue(field.annotations().isEmpty());
        annotator.dateTimeField(field, clazz, jsonNode);
        assertEquals(1, field.annotations().size());
        JAnnotationUse annotation = field.annotations().iterator().next();
        Map<String, JAnnotationValue> annotationMembers = annotation.getAnnotationMembers();
        JAnnotationValue timezoneAnnotation = annotationMembers.get("timezone");
        final AtomicReference<String> value = new AtomicReference<>();
        timezoneAnnotation.generate(new JFormatter(new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                value.set(new String(cbuf, off, len));
            }

            @Override
            public void flush() throws IOException {

            }

            @Override
            public void close() throws IOException {

            }
        }));
        assertEquals("\"UTC\"", value.get());
    }

    @Test
    public void shouldExcludeTimeZoneWhenSpecified() throws Exception {
        GenerationConfig generationConfig = new DefaultGenerationConfig() {
            @Override
            public boolean isFormatDateTimes() {
                return true;
            }

            @Override
            public boolean isExcludeTimezoneFromDateTimeFormat() {
                return true;
            }
        };
        Jackson2Annotator annotator = new Jackson2Annotator(generationConfig);
        JDefinedClass clazz = new JCodeModel()._class("com.example.Test");
        JFieldVar field = clazz.field(0, java.util.Date.class, "myDateTime");
        JsonNode jsonNode = new TextNode("");
        assertTrue(field.annotations().isEmpty());
        annotator.dateTimeField(field, clazz, jsonNode);
        assertEquals(1, field.annotations().size());
        JAnnotationUse annotation = field.annotations().iterator().next();
        Map<String, JAnnotationValue> annotationMembers = annotation.getAnnotationMembers();
        JAnnotationValue timezoneAnnotation = annotationMembers.get("timezone");
        assertNull(timezoneAnnotation);
    }
}