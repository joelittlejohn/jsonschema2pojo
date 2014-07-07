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

package org.jsonschema2pojo.integration.config;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;

import java.io.File;
import java.net.URL;

import org.junit.Test;

public class RemoveOldOutputIT {

    @Test(expected = ClassNotFoundException.class)
    public void removeOldOutputCausesOldTypesToBeDeleted() throws ClassNotFoundException {

        URL schema1 = getClass().getResource("/schema/properties/primitiveProperties.json");
        URL schema2 = getClass().getResource("/schema/properties/orderedProperties.json");

        File outputDirectory = createTemporaryOutputFolder();
        generate(schema1, "com.example", config("removeOldOutput", true), outputDirectory);
        generate(schema2, "com.example", config("removeOldOutput", true), outputDirectory);

        compile(outputDirectory).loadClass("com.example.PrimitiveProperties");

    }

    @Test
    public void byDefaultPluginDoesNotRemoveOldOutput() throws ClassNotFoundException {

        URL schema1 = getClass().getResource("/schema/properties/primitiveProperties.json");
        URL schema2 = getClass().getResource("/schema/properties/orderedProperties.json");

        File outputDirectory = createTemporaryOutputFolder();
        generate(schema1, "com.example", config(), outputDirectory);
        generate(schema2, "com.example", config(), outputDirectory);

        compile(outputDirectory).loadClass("com.example.PrimitiveProperties");

    }

}
