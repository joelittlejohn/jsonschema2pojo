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

package org.jsonschema2pojo;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class JsonPointerUtilsTest {

    @Test
    public void testEncodeReferenceToken() {
        assertThat(JsonPointerUtils.encodeReferenceToken("com/vsv#..."), is("com~1vsv~2~3~3~3"));
        assertThat(JsonPointerUtils.encodeReferenceToken("~1~2~01~3"), is("~01~02~001~03"));
    }

    @Test
    public void testDecodeReferenceToken() {
        assertThat(JsonPointerUtils.decodeReferenceToken("com~1vsv~2~3~3~3"), is("com/vsv#..."));
        assertThat(JsonPointerUtils.decodeReferenceToken("~01~02~001~03"), is("~1~2~01~3"));
    }
}