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

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URL;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

public class RemoveTargetPackageIT {

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    /**
     * When this option is on, the the classes in the parent packages are not deleted during regeneration. 
     * That allows for regenerating code to a directory containing code.
     * @throws ClassNotFoundException
     */
    @Test
    public void removeTargetPackageDoesNotDeleteOldTypesFromNonEmptyPackages() throws ClassNotFoundException {

        URL schema1 = getClass().getResource("/schema/properties/primitiveProperties.json");
        URL schema2 = getClass().getResource("/schema/properties/orderedProperties.json");

        schemaRule.generate(schema1, "com.example", config("removeTargetPackage", true));
        schemaRule.generate(schema2, "com.example.test", config("removeTargetPackage", true));

        ClassLoader compilation = schemaRule.compile();
        compilation.loadClass("com.example.PrimitiveProperties");
        compilation.loadClass("com.example.test.OrderedProperties");
    }
    
    /**
     * Ensures we're wiping out the classes in subpackages when the remove target package option is on. 
     * Leaving them out there might cause unused classes to remain around after regeneration if 
     * significant changes take place.
     * @throws ClassNotFoundException
     */
    @Test
    public void removeTargetPackageDeletesOldTypesFromNonEmptyChildPackages() throws ClassNotFoundException {

        URL schema1 = getClass().getResource("/schema/properties/primitiveProperties.json");
        URL schema2 = getClass().getResource("/schema/properties/orderedProperties.json");

        schemaRule.generate(schema1, "com.example.test", config("removeTargetPackage", true));
        schemaRule.generate(schema2, "com.example", config("removeTargetPackage", true));

        ClassLoader compilation = schemaRule.compile();
        compilation.loadClass("com.example.OrderedProperties");
        try {
            compilation.loadClass("com.example.test.PrimitiveProperties");
            fail("Should have thrown a ClassNotFoundException.");
        } catch (ClassNotFoundException e) {
            assertEquals("com.example.test.PrimitiveProperties", e.getMessage());
        }

    }
    
    /**
     * Ensures the target package is wiped out on regeneration when the option is on.
     * @throws ClassNotFoundException
     */
    @Test
    public void removeTargetPackageDeletesOldTypesInSamePackage() throws ClassNotFoundException {

        URL schema1 = getClass().getResource("/schema/properties/primitiveProperties.json");
        URL schema2 = getClass().getResource("/schema/properties/orderedProperties.json");

        schemaRule.generate(schema1, "com.example", config("removeTargetPackage", true));
        schemaRule.generate(schema2, "com.example", config("removeTargetPackage", true));

        
        ClassLoader compilation = schemaRule.compile();
        compilation.loadClass("com.example.OrderedProperties");
        try {
            compilation.loadClass("com.example.PrimitiveProperties");
            fail("Should have thrown a ClassNotFoundException.");
        } catch (ClassNotFoundException e) {
            assertEquals("com.example.PrimitiveProperties", e.getMessage());
        }
        

    }
    

    /**
     * Ensures the old output is unchanged if option is off.
     * @throws ClassNotFoundException
     */
    @Test
    public void byDefaultPluginDoesNotRemoveOldOutput() throws ClassNotFoundException {

        URL schema1 = getClass().getResource("/schema/properties/primitiveProperties.json");
        URL schema2 = getClass().getResource("/schema/properties/orderedProperties.json");

        schemaRule.generate(schema1, "com.example", config());
        schemaRule.generate(schema2, "com.example", config());

        schemaRule.compile().loadClass("com.example.PrimitiveProperties");

    }

}
