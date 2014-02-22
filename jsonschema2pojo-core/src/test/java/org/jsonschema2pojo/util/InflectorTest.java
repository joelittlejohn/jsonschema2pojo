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

        assertThat(Inflector.getInstance().singularize("bison"), is("bison"));
        assertThat(Inflector.getInstance().singularize("buffalo"), is("buffalo"));
        assertThat(Inflector.getInstance().singularize("deer"), is("deer"));
        assertThat(Inflector.getInstance().singularize("fish"), is("fish"));
        assertThat(Inflector.getInstance().singularize("sheep"), is("sheep"));
        assertThat(Inflector.getInstance().singularize("squid"), is("squid"));
        
    }
}
