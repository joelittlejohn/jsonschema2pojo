package com.googlecode.jsonschema2pojo;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

public class FragmentResolverImplTest {

    private FragmentResolverImpl resolver = new FragmentResolverImpl();

    @Test
    public void hashResolvesToRoot() {

        ObjectNode root = new ObjectMapper().createObjectNode();

        root.put("child1", root.objectNode());
        root.put("child2", root.objectNode());
        root.put("child3", root.objectNode());

        assertThat((ObjectNode) resolver.resolve(root, "#"), is(sameInstance(root)));

    }

    @Test
    public void slashDelimitedWordsResolveToChildNodes() {

        ObjectNode root = new ObjectMapper().createObjectNode();

        ObjectNode a = root.objectNode();
        ObjectNode b = root.objectNode();
        ObjectNode c = root.objectNode();

        root.put("a", a);
        root.put("b", b);
        root.put("c", c);

        ObjectNode x = root.objectNode();
        ObjectNode y = root.objectNode();
        ObjectNode z = root.objectNode();

        a.put("x", x);
        a.put("y", y);
        a.put("z", z);

        ObjectNode _0 = root.objectNode();
        ObjectNode _1 = root.objectNode();
        ObjectNode _2 = root.objectNode();

        z.put("0", _0);
        z.put("1", _1);
        z.put("2", _2);

        assertThat((ObjectNode) resolver.resolve(root, "#/a"), is(sameInstance(a)));
        assertThat((ObjectNode) resolver.resolve(root, "#/b"), is(sameInstance(b)));
        assertThat((ObjectNode) resolver.resolve(root, "#/c"), is(sameInstance(c)));

        assertThat((ObjectNode) resolver.resolve(root, "#/a/x"), is(sameInstance(x)));
        assertThat((ObjectNode) resolver.resolve(root, "#/a/y"), is(sameInstance(y)));
        assertThat((ObjectNode) resolver.resolve(root, "#/a/z"), is(sameInstance(z)));

        assertThat((ObjectNode) resolver.resolve(root, "#/a/z/0"), is(sameInstance(_0)));
        assertThat((ObjectNode) resolver.resolve(root, "#/a/z/1"), is(sameInstance(_1)));
        assertThat((ObjectNode) resolver.resolve(root, "#/a/z/2"), is(sameInstance(_2)));

    }

    @Test
    public void pathCanReferToArrayContentsByIndex() {

        ObjectNode root = new ObjectMapper().createObjectNode();

        ArrayNode a = root.arrayNode();
        root.put("a", a);

        a.add(root.objectNode());
        a.add(root.objectNode());
        a.add(root.objectNode());

        assertThat(resolver.resolve(root, "#/a/0"), is(sameInstance(a.get(0))));
        assertThat(resolver.resolve(root, "#/a/1"), is(sameInstance(a.get(1))));
        assertThat(resolver.resolve(root, "#/a/2"), is(sameInstance(a.get(2))));

    }

    @Test
    public void pathCanReferToArrayContentsAtTheDocumentRoot() {
        ArrayNode root = new ObjectMapper().createArrayNode();

        root.add(root.objectNode());
        root.add(root.objectNode());
        root.add(root.objectNode());

        assertThat(resolver.resolve(root, "#/0"), is(sameInstance(root.get(0))));
        assertThat(resolver.resolve(root, "#/1"), is(sameInstance(root.get(1))));
        assertThat(resolver.resolve(root, "#/2"), is(sameInstance(root.get(2))));

    }

    @Test(expected = IllegalArgumentException.class)
    public void missingPathThrowsIllegalArgumentException() {

        ObjectNode root = new ObjectMapper().createObjectNode();

        resolver.resolve(root, "#/a/b/c");

    }

    @Test(expected = IllegalArgumentException.class)
    public void attemptToUsePropertyNameOnArrayNodeThrowsIllegalArgumentException() {

        ObjectNode root = new ObjectMapper().createObjectNode();

        ArrayNode a = root.arrayNode();
        root.put("a", a);

        resolver.resolve(root, "#/a/b");

    }

}
