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

package org.jsonschema2pojo.integration.config;

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

public class UseTitleAsClassnameIT {

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void defaultUseTitleAsClassnameIsFalse() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/objectPropertiesTitle.json", "com.example");

        Class generatedType = resultsClassLoader.loadClass("com.example.ObjectPropertiesTitle");

        assertThat(generatedType, is(notNullValue()));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void useTitleAsClassnameWorksForObjects() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/objectPropertiesTitle.json", "com.example",
                config("useTitleAsClassname", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.FooObject");

        Method getter = generatedType.getMethod("getA");
        assertThat(getter.getReturnType(), typeCompatibleWith(resultsClassLoader.loadClass("com.example.BarObject")));

    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void useTitleAsClassnameWorksForEnums() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/enum/enumWithTitle.json", "com.example",
                config("useTitleAsClassname", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.FooEnum");
        assertThat(generatedType, is(notNullValue()));
    }

}
