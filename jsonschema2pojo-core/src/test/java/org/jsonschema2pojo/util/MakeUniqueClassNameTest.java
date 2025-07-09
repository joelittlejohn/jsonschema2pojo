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

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MakeUniqueClassNameTest {

    @Test
    public void testClassNameStrategy() {
        assertThat(MakeUniqueClassName.makeUnique("NodeMode"), equalTo("NodeMode__1"));
        assertThat(MakeUniqueClassName.makeUnique("NodeMode__5"), equalTo("NodeMode__6"));
        assertThat(MakeUniqueClassName.makeUnique("NodeMode__10"), equalTo("NodeMode__11"));
        assertThat(MakeUniqueClassName.makeUnique("NodeMode__100"), equalTo("NodeMode__101"));
    }

}
