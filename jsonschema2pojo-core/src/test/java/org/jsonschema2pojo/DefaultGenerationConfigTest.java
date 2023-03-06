package org.jsonschema2pojo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class DefaultGenerationConfigTest {

    private DefaultGenerationConfig config = new DefaultGenerationConfig();

    @Test
    public void defaultGenerationCongigDefaults() {

        assertThat(config.isIncludeGeneratedAnnotation(), is(true));
        assertThat(config.isIncludeRuntimeGeneratedAnnotation(), is(false));
    }

}
