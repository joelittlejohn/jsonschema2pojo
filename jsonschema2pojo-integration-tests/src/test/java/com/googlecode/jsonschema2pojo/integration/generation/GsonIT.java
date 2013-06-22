/**
 * Copyright ¬© 2010-2013 Nokia
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

package com.googlecode.jsonschema2pojo.integration.generation;

import static com.googlecode.jsonschema2pojo.integration.util.CodeGenerationHelper.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.gson.Gson;

@RunWith(Parameterized.class)
public class GsonIT extends GenerationTestSupport {

    private String schema;
    private static final Gson gson = new Gson();

    public GsonIT(String schema) {
        this.schema = schema;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "Person.json" },
                { "ProductSet.json" },
                { "FstabEntry.json" }
        });
    }

    @Override
    protected Map<String, Object> getConfigValues() {
        return config("annotationStyle", "gson",
                "propertyWordDelimiters", "_- ");
    }

    @Override
    protected String getSchemaFileName() {
        return schema;
    }

    @Override
    protected Object getTestInstanceId() {
        return schema;
    }

    @Override
    protected String marshalExample(Object unmarshalledResult, Class<?> jsonExampleClass) {
        return gson.toJson(unmarshalledResult, jsonExampleClass);
    }

    @Override
    protected <T> T unmarshalExample(String jsonExample, Class<T> jsonExampleClass) {
        return gson.fromJson(jsonExample, jsonExampleClass);
    }

}
