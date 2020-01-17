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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class FragmentResolverTest {

    private FragmentResolver resolver = new FragmentResolver();

    @Test
    public void hashResolvesToRoot() {

        ObjectNode root = new ObjectMapper().createObjectNode();

        root.set("child1", root.objectNode());
        root.set("child2", root.objectNode());
        root.set("child3", root.objectNode());

        assertThat((ObjectNode) resolver.resolve(root, "#", "#/."), is(sameInstance(root)));

    }

    @Test
    public void slashDelimitedWordsResolveToChildNodes() {

        ObjectNode root = new ObjectMapper().createObjectNode();

        ObjectNode a = root.objectNode();
        ObjectNode b = root.objectNode();
        ObjectNode c = root.objectNode();

        root.set("a", a);
        root.set("b", b);
        root.set("c", c);

        ObjectNode x = root.objectNode();
        ObjectNode y = root.objectNode();
        ObjectNode z = root.objectNode();

        a.set("x", x);
        a.set("y", y);
        a.set("z", z);

        ObjectNode _0 = root.objectNode();
        ObjectNode _1 = root.objectNode();
        ObjectNode _2 = root.objectNode();

        z.set("0", _0);
        z.set("1", _1);
        z.set("2", _2);

        assertThat((ObjectNode) resolver.resolve(root, "#/a", "#/."), is(sameInstance(a)));
        assertThat((ObjectNode) resolver.resolve(root, "#/b", "#/."), is(sameInstance(b)));
        assertThat((ObjectNode) resolver.resolve(root, "#/c", "#/."), is(sameInstance(c)));

        assertThat((ObjectNode) resolver.resolve(root, "#/a/x", "#/."), is(sameInstance(x)));
        assertThat((ObjectNode) resolver.resolve(root, "#/a/y", "#/."), is(sameInstance(y)));
        assertThat((ObjectNode) resolver.resolve(root, "#/a/z", "#/."), is(sameInstance(z)));

        assertThat((ObjectNode) resolver.resolve(root, "#/a/z/0", "#/."), is(sameInstance(_0)));
        assertThat((ObjectNode) resolver.resolve(root, "#/a/z/1", "#/."), is(sameInstance(_1)));
        assertThat((ObjectNode) resolver.resolve(root, "#/a/z/2", "#/."), is(sameInstance(_2)));

    }

    @Test
    public void pathCanReferToArrayContentsByIndex() {

        ObjectNode root = new ObjectMapper().createObjectNode();

        ArrayNode a = root.arrayNode();
        root.set("a", a);

        a.add(root.objectNode());
        a.add(root.objectNode());
        a.add(root.objectNode());

        assertThat(resolver.resolve(root, "#/a/0", "#/."), is(sameInstance(a.get(0))));
        assertThat(resolver.resolve(root, "#/a/1", "#/."), is(sameInstance(a.get(1))));
        assertThat(resolver.resolve(root, "#/a/2", "#/."), is(sameInstance(a.get(2))));

    }

    @Test
    public void pathCanReferToArrayContentsAtTheDocumentRoot() {
        ArrayNode root = new ObjectMapper().createArrayNode();

        root.add(root.objectNode());
        root.add(root.objectNode());
        root.add(root.objectNode());

        assertThat(resolver.resolve(root, "#/0", "#/."), is(sameInstance(root.get(0))));
        assertThat(resolver.resolve(root, "#/1", "#/."), is(sameInstance(root.get(1))));
        assertThat(resolver.resolve(root, "#/2", "#/."), is(sameInstance(root.get(2))));

    }

    @Test(expected = IllegalArgumentException.class)
    public void missingPathThrowsIllegalArgumentException() {

        ObjectNode root = new ObjectMapper().createObjectNode();

        resolver.resolve(root, "#/a/b/c", "#/.");

    }

    @Test(expected = IllegalArgumentException.class)
    public void attemptToUsePropertyNameOnArrayNodeThrowsIllegalArgumentException() {

        ObjectNode root = new ObjectMapper().createObjectNode();

        ArrayNode a = root.arrayNode();
        root.set("a", a);

        resolver.resolve(root, "#/a/b", "#/.");

    }

}
