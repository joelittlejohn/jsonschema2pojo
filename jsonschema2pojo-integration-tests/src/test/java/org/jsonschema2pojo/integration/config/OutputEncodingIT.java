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
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

public class OutputEncodingIT {

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void writeExtendedCharactersAsUtf8SourceCodeByDefault() throws IOException {
        File outputDirectory = schemaRule.generate("/schema/regression/extendedCharacters.json", "com.example");
        File sourceFile = new File(outputDirectory, "com/example/ExtendedCharacters.java");
        String javaSource = IOUtils.toString(new FileInputStream(sourceFile), "utf-8");

        assertThat(javaSource, containsString("ЫЩДђиЊЉЯ"));
    }

    @Test
    public void writeAsUtf8WorksWhenPlatformCharsetIsSingleByte() throws IOException {

        String platformCharset = System.getProperty("file.encoding");

        try {
            System.setProperty("file.encoding", "Cp1252");

            File outputDirectory = schemaRule.generate("/schema/regression/extendedCharacters.json", "com.example");
            File sourceFile = new File(outputDirectory, "com/example/ExtendedCharacters.java");
            String javaSource = IOUtils.toString(new FileInputStream(sourceFile), "utf-8");

            assertThat(javaSource, containsString("ЫЩДђиЊЉЯ"));
        } finally {
            System.setProperty("file.encoding", platformCharset);
        }

    }

    @Test
    public void writeExtendedCharactersAsSingleByteSourceCode() throws IOException {

        File outputDirectory = schemaRule.generate("/schema/regression/extendedCharacters.json", "com.example", config("outputEncoding", "iso-8859-5"));

        File sourceFile = new File(outputDirectory, "com/example/ExtendedCharacters.java");
        String javaSource = IOUtils.toString(new FileInputStream(sourceFile), "iso-8859-5");

        assertThat(javaSource, containsString("ЫЩДђиЊЉЯ"));
    }
}
