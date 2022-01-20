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

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

public class RegressionIT {
    
    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void pathWithSpacesInTheNameDoesNotFail() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/regression/spaces in path.json", "com.example", Collections.emptyMap());

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.SpacesInPath");
        assertThat(generatedType, is(notNullValue()));

    }

    @Test
    public void underscoresInPropertyNamesRemainIntact() throws ReflectiveOperationException, SecurityException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/regression/underscores.json", "com.example", config("sourceType", "json", "propertyWordDelimiters", ""));

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.Underscores");
        generatedType.getMethod("getName");
        generatedType.getMethod("get_name");
    }

    @Test
    public void filesWithExtensionPrefixesAreNotTruncated() throws ClassNotFoundException, SecurityException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/regression/foo.baz.json", "com.example", Collections.emptyMap());

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.FooBaz");
        assertThat(generatedType, is(notNullValue()));
    }

    @Test
    public void extendsChoosesCorrectSupertypeWhenTypeIsAlreadyGenerated() throws ClassNotFoundException, SecurityException, MalformedURLException {
        URL filteredSchemaUrl = new File("src/test/resources/schema/regression/extends").toURI().toURL();

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(filteredSchemaUrl, "com.example", Collections.emptyMap());

        Class<?> parent = resultsClassLoader.loadClass("org.hawkular.bus.common.BasicMessage");
        Class<?> subClass = resultsClassLoader.loadClass("org.abc.AuthMessage");
        Class<?> subSubClass = resultsClassLoader.loadClass("org.abc.SimpleMessage");

        assertThat(subClass.getSuperclass().getName(), is(parent.getName()));
        assertThat(subSubClass.getSuperclass().getName(), is(subClass.getName()));
    }

    @Test
    public void extendsIsSupportedInArrayItemType() throws ReflectiveOperationException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/schema/regression/extends_in_array",
                "com.example",
                Collections.emptyMap());

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.Flowerarray");
        Class<?> parent = resultsClassLoader.loadClass("com.example.Flower");
        Class<?> subClass = resultsClassLoader.loadClass("com.example.Flower__1");

        Type flowersArrayType = generatedType.getDeclaredField("flowers").getGenericType();
        assertThat(flowersArrayType, is(instanceOf(ParameterizedType.class)));
        assertThat(((ParameterizedType) flowersArrayType).getActualTypeArguments()[0].getTypeName(), is(subClass.getName()));
        assertThat(subClass.getSuperclass().getName(), is(parent.getName()));
    }
}
