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

package org.jsonschema2pojo.integration.util;

import static org.junit.Assert.*;
import static org.skyscreamer.jsonassert.JSONCompare.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

public class JsonAssert {

    public static void assertEqualsJson(String expectedJson, String actualJson) {
        assertEqualsJson(expectedJson, actualJson, JSONCompareMode.STRICT);
    }

    public static void assertEqualsJson(String expectedJson, String actualJson, JSONCompareMode compareMode) {

        try {
            JSONCompareResult result = compareJSON(expectedJson, actualJson, compareMode);

            if (result.failed()) {
                String failureMessage = result.getMessage();
                if (failureMessage != null) {
                    failureMessage = failureMessage.replaceAll(" ; ", "\n");
                }
                failureMessage = "\n================ Expected JSON ================"
                        + new JSONObject(expectedJson).toString(4)
                        + "\n================= Actual JSON ================="
                        + new JSONObject(actualJson).toString(4)
                        + "\n================= Error List ==================\n"
                        + failureMessage + "\n\n";
                fail(failureMessage);
            }
        } catch (JSONException e) {
            throw new RuntimeException("JSON completely failed to parse json", e);
        }
    }
}
