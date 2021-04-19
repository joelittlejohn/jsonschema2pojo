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

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Originally generated to test the functionality created for issue 1123 (Please create objects in the definition section to be used).
 * <p>
 * Ths unit test will will confirm that schemas with additional schemas defined as part of their definitions element will
 * parse correctly and generate both the original schema class and any schema classes included in the definitions
 */
public class DefinitionSchemaIT {
    @ClassRule
    public static Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void testSchemaWithDefinition() throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/definitions/schemaWithDefinitions.json", "com.example");

        // Validate the the SchemaWithDefinitions was generated correctly
        Class<?> schemaWithDefinitionType = resultsClassLoader.loadClass("com.example.SchemaWithDefinitions");
        assertThat(schemaWithDefinitionType.newInstance(), notNullValue());

        // Validate that the defined type class is accessible outside of the SchemaWithDefinitions
        Class<?> definedType = resultsClassLoader.loadClass("com.example.DefinedType");
        assertThat(definedType.newInstance(), notNullValue());

        // Validate that the property on the schema with definitions matches the expected type
        Field definedTypeInstance = schemaWithDefinitionType.getDeclaredField("definedTypeInstance");
        assertThat(definedTypeInstance.getType(), equalTo(definedType));
    }

}
