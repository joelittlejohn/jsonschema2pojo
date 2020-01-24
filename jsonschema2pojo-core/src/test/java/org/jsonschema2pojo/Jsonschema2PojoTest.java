/**
 * Copyright © 2010-2017 Nokia
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

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class Jsonschema2PojoTest {
    @Test
    public void createOneClassForRepeatedSubSchemas() throws IOException {
        URL schema = this.getClass().getResource("/schema/repeatedSubschemas.json");

        GenerationConfig config = new DefaultGenerationConfig(){
            public Iterator<URL> getSource() {
                ArrayList<URL> sources = new ArrayList<>();
                sources.add(schema);

                return sources.iterator();
            }

            @Override
            public AnnotationStyle getAnnotationStyle() {
                return AnnotationStyle.GSON;
            }

            public String getTargetPackage() {
                return "com.example";
            }

            public File getTargetDirectory() {
                return new File("/Users/schruben/dev/jsonschema2pojo/jsonschema2pojo-core/target");
            }
        };

        Jsonschema2Pojo.generate(config, new RuleLogger() {
            @Override
            public void debug(String msg) {

            }

            @Override
            public void error(String msg) {

            }

            @Override
            public void info(String msg) {

            }

            @Override
            public void trace(String msg) {

            }

            @Override
            public void warn(String msg) {

            }

            @Override
            public boolean isDebugEnabled() {
                return false;
            }

            @Override
            public boolean isErrorEnabled() {
                return false;
            }

            @Override
            public boolean isInfoEnabled() {
                return false;
            }

            @Override
            public boolean isTraceEnabled() {
                return false;
            }

            @Override
            public boolean isWarnEnabled() {
                return false;
            }
        });
    }
}
