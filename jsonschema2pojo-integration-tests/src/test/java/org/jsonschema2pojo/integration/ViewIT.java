/**
 * Copyright Â© 2010-2014 Nokia
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.junit.Test;

public class ViewIT {
    static abstract class JsonViewTest {
        String annotationStyle;
        Class<?> jsonViewClass;

        public JsonViewTest(String annotationStyle) {
            this.annotationStyle = annotationStyle;
            if (annotationStyle.equals("jackson1")) {
                this.jsonViewClass = org.codehaus.jackson.map.annotate.JsonView.class; 
            } else {
                this.jsonViewClass = com.fasterxml.jackson.annotation.JsonView.class;
            }
        }

        public void test() throws ClassNotFoundException, NoSuchFieldException {
            ClassLoader resultsClassLoader = generateAndCompile(
                "/schema/views/views.json", 
                "com.example", 
                config("annotationStyle", annotationStyle));

            Class<?> pojo = resultsClassLoader.loadClass("com.example.Views");
            Field inView = pojo.getDeclaredField("inView");
            assertThat(getJsonViewAnnotation(inView), notNullValue());

            assertThat(getJsonViewAnnotationValue(inView), equalTo("MyJsonViewClass"));

            Field notInView = pojo.getDeclaredField("notInView");
            assertThat(getJsonViewAnnotation(notInView), nullValue());
        }

        protected abstract <T extends Annotation> T getJsonViewAnnotation(Field f);
        protected abstract String getJsonViewAnnotationValue(Field f);
    }

    @Test
    public void checkViewJackson1() throws Exception {
        new JsonViewTest("jackson1") {
            @Override
            @SuppressWarnings({"unchecked"})
            protected <T extends Annotation> T getJsonViewAnnotation(Field f)  {
                return (T) f.getAnnotation(org.codehaus.jackson.map.annotate.JsonView.class);
            }

            @Override
            protected String getJsonViewAnnotationValue(Field f) {
                org.codehaus.jackson.map.annotate.JsonView jv = 
                    f.getAnnotation(org.codehaus.jackson.map.annotate.JsonView.class);
                return jv.value()[0].getSimpleName();
            }
        }.test();
    }

    @Test
    public void checkViewJackson2() throws Exception {
        new JsonViewTest("jackson2") {
            @Override
            @SuppressWarnings({"unchecked"})
            protected <T extends Annotation> T getJsonViewAnnotation(Field f)  {
                return (T) f.getAnnotation(com.fasterxml.jackson.annotation.JsonView.class);
            }

            @Override
            protected String getJsonViewAnnotationValue(Field f) {
                com.fasterxml.jackson.annotation.JsonView jv = 
                    f.getAnnotation(com.fasterxml.jackson.annotation.JsonView.class);
                return jv.value()[0].getSimpleName();
            }
        }.test();
    }
}
