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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;

import java.util.List;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoTestBase;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RealJsonExamplesIT extends Jsonschema2PojoTestBase {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void getUserDataProducesValidTypes() throws Exception {

        ClassLoader resultsClassLoader = generateAndCompile("/json/examples/GetUserData.json", "com.example",
                config("sourceType", "json",
                        "useLongIntegers", true));

        Class<?> userDataType = resultsClassLoader.loadClass("com.example.GetUserData");

        Object userData = OBJECT_MAPPER.readValue(this.getClass().getResourceAsStream("/json/examples/GetUserData.json"), userDataType);
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

        ClassLoader resultsClassLoader = generateAndCompile("/json/examples/torrent.json", "com.example",
                config("sourceType", "json",
                        "propertyWordDelimiters", "_"));

        Class<?> torrentType = resultsClassLoader.loadClass("com.example.Torrent");

        Object torrent = OBJECT_MAPPER.readValue(this.getClass().getResourceAsStream("/json/examples/torrent.json"), torrentType);

        Object props = torrentType.getMethod("getProps").invoke(torrent);
        Object prop0 = ((List<?>) props).get(0);
        assertThat((Integer) prop0.getClass().getMethod("getSeedRatio").invoke(prop0), is(1500));

    }
}
