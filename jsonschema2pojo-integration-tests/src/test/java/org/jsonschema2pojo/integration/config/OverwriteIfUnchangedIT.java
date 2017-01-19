/**
 * Copyright Â© 2010-2014 Nokia
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

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;

import java.io.File;
import java.net.URL;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class OverwriteIfUnchangedIT {

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void doNotAlterFilesIfTheContentIsTheSame() {

        URL schema1 = getClass().getResource("/schema/properties/primitiveProperties.json");

        File generateDir = schemaRule.generate(schema1, "com.example", config("overwriteEvenIfUnchanged", false));

        File source = new File(generateDir, "com/example/PrimitiveProperties.java");
        Assert.assertTrue("Source ought to have been created", source.exists());
        source.setLastModified(0L);

        Assert.assertEquals("Test can't be fully trusted, can't change lastModified of a File", 0L, source.lastModified());

        schemaRule.generate(schema1, "com.example", config("overwriteEvenIfUnchanged", false));

        File actual = new File(generateDir, "com/example/PrimitiveProperties.java");
        Assert.assertTrue("Result file ought to have been created", actual.exists());
        Assert.assertEquals("File ought not to have been overwritten", 0L, actual.lastModified());

    }

    @Test
    public void byDefaultPluginDoesOverwriteEvenIfUnchanged() {

        URL schema1 = getClass().getResource("/schema/properties/primitiveProperties.json");

        File generateDir = schemaRule.generate(schema1, "com.example", config("overwriteEvenIfUnchanged", true));

        File source = new File(generateDir, "com/example/PrimitiveProperties.java");
        Assert.assertTrue("Source ought to have been created", source.exists());
        source.setLastModified(0L);

        Assert.assertEquals("Test can't be fully trusted, can't change lastModified of a File", 0L, source.lastModified());

        schemaRule.generate(schema1, "com.example", config());

        File actual = new File(generateDir, "com/example/PrimitiveProperties.java");
        Assert.assertTrue("Source ought to have been created", actual.exists());
        Assert.assertNotEquals("File ought to have been overwritten", 0L, actual.lastModified());
    }

}
