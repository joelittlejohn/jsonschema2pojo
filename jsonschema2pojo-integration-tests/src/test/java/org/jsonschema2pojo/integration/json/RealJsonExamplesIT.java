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

package org.jsonschema2pojo.integration.json;

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

@RunWith(Parameterized.class)
public class RealJsonExamplesIT {

    @Parameterized.Parameters(name="{0}")
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "json", new ObjectMapper()},
                { "yaml", new ObjectMapper(new YAMLFactory()) }
        });
    }

    @Rule 
    public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private final String format;
    private final ObjectMapper objectMapper;

    public RealJsonExamplesIT(final String format, final ObjectMapper objectMapper) {
        this.format = format;
        this.objectMapper = objectMapper;
    }

    private String filePath(String baseName) {
        return "/" + format + "/" + baseName + "." + format;
    }

    @Test
    public void getUserDataProducesValidTypes() throws Exception {

        final String filePath = filePath("examples/GetUserData");
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(filePath, "com.example",
                config("sourceType", format, "useLongIntegers", true));

        Class<?> userDataType = resultsClassLoader.loadClass("com.example.GetUserData");

        Object userData = objectMapper.readValue(this.getClass().getResourceAsStream(filePath), userDataType);
        Object result = userDataType.getMethod("getResult").invoke(userData);
        Object data = result.getClass().getMethod("getData").invoke(result);
        Object userUIPref = data.getClass().getMethod("getUserUIPref").invoke(data);

        assertThat(userUIPref.getClass().getMethod("getPimColor").invoke(userUIPref).toString(), is("blue"));

        Object externalAccounts = data.getClass().getMethod("getExternalAccounts").invoke(data);
        Object extAccount = externalAccounts.getClass().getMethod("getExtAccount").invoke(externalAccounts);
        Object extAccount0 = ((List<?>) extAccount).get(0);
        assertThat(extAccount0.getClass().getMethod("getFolder").invoke(extAccount0).toString(), is("Inbox"));

    }

    @Test
    public void torrentProducesValidTypes() throws Exception {

        final String filePath = filePath("examples/torrent");
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(filePath, "com.example",
                config("sourceType", format,
                        "propertyWordDelimiters", "_"));

        Class<?> torrentType = resultsClassLoader.loadClass("com.example.Torrent");

        Object torrent = objectMapper.readValue(this.getClass().getResourceAsStream(filePath), torrentType);

        Object props = torrentType.getMethod("getProps").invoke(torrent);
        Object prop0 = ((List<?>) props).get(0);
        assertThat((Integer) prop0.getClass().getMethod("getSeedRatio").invoke(prop0), is(1500));

    }
}
