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

package org.jsonschema2pojo.util;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import org.junit.Test;

public class JavaVersionTest {

    @Test
    public void testParse() {
        assertThat(JavaVersion.parse("1.8.0_362"), is("1.8"));
        assertThat(JavaVersion.parse("11.0.18"), is("11"));
        assertThat(JavaVersion.parse("9.0.4"), is("9"));
        assertThat(JavaVersion.parse("10.0.1"), is("10"));
    }

    @Test
    public void testIs9OrLater() {
        assertThat(JavaVersion.is9OrLater(null), is(false));
        assertThat(JavaVersion.is9OrLater(""), is(false));
        assertThat(JavaVersion.is9OrLater("1.1"), is(false));
        assertThat(JavaVersion.is9OrLater("1.2"), is(false));
        assertThat(JavaVersion.is9OrLater("1.3"), is(false));
        assertThat(JavaVersion.is9OrLater("1.4"), is(false));
        assertThat(JavaVersion.is9OrLater("1.5"), is(false));
        assertThat(JavaVersion.is9OrLater("5"), is(false));
        assertThat(JavaVersion.is9OrLater("1.6"), is(false));
        assertThat(JavaVersion.is9OrLater("6"), is(false));
        assertThat(JavaVersion.is9OrLater("1.7"), is(false));
        assertThat(JavaVersion.is9OrLater("7"), is(false));
        assertThat(JavaVersion.is9OrLater("1.8"), is(false));
        assertThat(JavaVersion.is9OrLater("8"), is(false));
        assertThat(JavaVersion.is9OrLater("1.9"), is(true));
        assertThat(JavaVersion.is9OrLater("9"), is(true));
        assertThat(JavaVersion.is9OrLater("10"), is(true));
        assertThat(JavaVersion.is9OrLater("11"), is(true));
    }

}
