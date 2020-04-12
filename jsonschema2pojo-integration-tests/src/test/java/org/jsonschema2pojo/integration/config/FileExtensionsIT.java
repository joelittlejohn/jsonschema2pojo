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

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;

import java.net.URL;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

public class FileExtensionsIT {

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void extensionsCanBeRemovedFromNames() throws ClassNotFoundException {

        URL schema1 = getClass().getResource("/schema/fileExtensions/nameWithExtension.schema.json");
        URL schema2 = getClass().getResource("/schema/fileExtensions/nameWithNoExtension");

        schemaRule.generate(schema1, "com.example", config("fileExtensions", new String[] {".schema.json"}));
        schemaRule.generate(schema2, "com.example", config("fileExtensions", new String[] {".schema.json"}));

        ClassLoader loader = schemaRule.compile();
        loader.loadClass("com.example.NameWithExtension");
        loader.loadClass("com.example.NameWithNoExtension");
    }

    @Test
    public void byDefaultOnlyfirstExtensionRemoved() throws ClassNotFoundException {

        URL schema1 = getClass().getResource("/schema/fileExtensions/nameWithExtension.schema.json");
        URL schema2 = getClass().getResource("/schema/fileExtensions/nameWithNoExtension");

        schemaRule.generate(schema1, "com.example", config());
        schemaRule.generate(schema2, "com.example", config());

        ClassLoader loader = schemaRule.compile();
        loader.loadClass("com.example.NameWithExtensionSchema");
        loader.loadClass("com.example.NameWithNoExtension");
    }

}
