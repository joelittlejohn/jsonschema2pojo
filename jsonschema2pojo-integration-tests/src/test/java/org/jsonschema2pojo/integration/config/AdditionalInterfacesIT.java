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

package org.jsonschema2pojo.integration.config;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Method;

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.typeCompatibleWith;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.assertThat;

public class AdditionalInterfacesIT {


    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void noInterfacesAddedByDefault() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example");

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        assertThat(generatedType.getInterfaces(), is(emptyArray()));
    }

    @Test
    public void interfacesAddedToObject() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example",
                config("additionalInterfaces", new String[] { Serializable.class.getName() }));

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        assertThat(generatedType.getInterfaces(), arrayWithSize(1));
        assertThat(generatedType, typeCompatibleWith(Serializable.class));
    }

    @Test
    public void interfacesNotAddedToEnum() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/enum/enumWithEmptyString.json", "com.example",
                config("additionalInterfaces", new String[] { Serializable.class.getName() }));

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.EnumWithEmptyString");

        assertThat(generatedType.getInterfaces(), is(emptyArray()));
    }

    @Test
    public void interfacesAreNotAddedToObjectInSubHierarchy() throws ClassNotFoundException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/type/types.json", "com.example",
                config("additionalInterfaces", new String[] { Serializable.class.getName() }));

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.Types");
        Method objectPropertyGetter = generatedType.getMethod("getObjectProperty");

        assertThat(objectPropertyGetter.getReturnType(), not(typeCompatibleWith(Serializable.class)));
    }
}
