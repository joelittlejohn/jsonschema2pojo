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

package org.jsonschema2pojo.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class InflectorTest {

    @Test
    public void testSingularize() {

        assertThat(Inflector.getInstance().singularize("dwarves"), is("dwarf"));
        assertThat(Inflector.getInstance().singularize("curves"), is("curve"));
        assertThat(Inflector.getInstance().singularize("halves"), is("half"));
        assertThat(Inflector.getInstance().singularize("vertices"), is("vertex"));
        assertThat(Inflector.getInstance().singularize("proofs"), is("proof"));
        assertThat(Inflector.getInstance().singularize("moths"), is("moth"));
        assertThat(Inflector.getInstance().singularize("houses"), is("house"));
        assertThat(Inflector.getInstance().singularize("rooves"), is("roof"));
        assertThat(Inflector.getInstance().singularize("elves"), is("elf"));
        assertThat(Inflector.getInstance().singularize("baths"), is("bath"));
        assertThat(Inflector.getInstance().singularize("leaves"), is("leaf"));
        assertThat(Inflector.getInstance().singularize("calves"), is("calf"));
        assertThat(Inflector.getInstance().singularize("lives"), is("life"));
        assertThat(Inflector.getInstance().singularize("knives"), is("knife"));
        assertThat(Inflector.getInstance().singularize("addresses"), is("address"));
        assertThat(Inflector.getInstance().singularize("mattresses"), is("mattress"));
        assertThat(Inflector.getInstance().singularize("databases"), is("database"));

        assertThat(Inflector.getInstance().singularize("bison"), is("bison"));
        assertThat(Inflector.getInstance().singularize("buffalo"), is("buffalo"));
        assertThat(Inflector.getInstance().singularize("deer"), is("deer"));
        assertThat(Inflector.getInstance().singularize("fish"), is("fish"));
        assertThat(Inflector.getInstance().singularize("sheep"), is("sheep"));
        assertThat(Inflector.getInstance().singularize("squid"), is("squid"));
        assertThat(Inflector.getInstance().singularize("mattress"), is("mattress"));
        assertThat(Inflector.getInstance().singularize("address"), is("address"));

        assertThat(Inflector.getInstance().singularize("men"), is("man"));
        assertThat(Inflector.getInstance().singularize("women"), is("woman"));
        assertThat(Inflector.getInstance().singularize("specimen"), is("specimen"));
        assertThat(Inflector.getInstance().singularize("children"), is("child"));

        assertThat(Inflector.getInstance().singularize("s"), is("s"));
        assertThat(Inflector.getInstance().singularize("status"), is("status"));
        assertThat(Inflector.getInstance().singularize("statuses"), is("status"));

        assertThat(Inflector.getInstance().pluralize("mattress"), is("mattresses"));
        assertThat(Inflector.getInstance().pluralize("address"), is("addresses"));

    }

}
