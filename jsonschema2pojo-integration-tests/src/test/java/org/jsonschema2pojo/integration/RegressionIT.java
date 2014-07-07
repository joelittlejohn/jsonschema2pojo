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

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;

import org.junit.Test;

public class RegressionIT {

    @Test
    @SuppressWarnings("rawtypes")
    public void pathWithSpacesInTheNameDoesNotFail() throws ClassNotFoundException, MalformedURLException {

        File file = new File("src/test/resources/schema/regression/spaces in path.json");

        File sourcesDirectory = generate(file.toURI().toURL(), "com.example", new HashMap<String, Object>());
        ClassLoader resultsClassLoader = compile(sourcesDirectory);

        Class generatedType = resultsClassLoader.loadClass("com.example.SpacesInPath");
        assertThat(generatedType, is(notNullValue()));

    }

}
