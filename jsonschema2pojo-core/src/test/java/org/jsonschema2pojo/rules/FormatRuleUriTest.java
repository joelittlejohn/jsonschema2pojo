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

package org.jsonschema2pojo.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.SchemaStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(Parameterized.class)
public class FormatRuleUriTest {

    private GenerationConfig config = mock(GenerationConfig.class);
    private FormatRule rule = new FormatRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));

    private final String patternValue;
    private final Class<?> expectedType;

    @Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
                // URL
                { "http://example.com", URL.class },
                { "https://example.com", URL.class },
                { "https?://example.com", URL.class },
                { "ftp://.+", URL.class },
                { "file://.+", URL.class },
                { "(url|http|https)://.+", URL.class },
                { "(url|https?)://.+", URL.class },
                { "(url)://.+", URL.class },
                { "url://(.+)", URL.class },
                { "url://(foo).+", URL.class },
                { "url://foo(.+", URL.class },
                // URI
                { null, URI.class },
                { "", URI.class },
                { ".+", URI.class },
                { "uri:/uri", URI.class }});
    }

    public FormatRuleUriTest(String patternValue, Class<?> expectedType) {
        this.patternValue = patternValue;
        this.expectedType = expectedType;
    }

    @Test
    public void applyUrlForUriAndHttpsPattern() {
        TextNode formatNode = TextNode.valueOf("uri");
        JsonNode parent = JsonNodeFactory.instance.objectNode()
                .put("pattern", patternValue);

        JType result = rule.apply("fooBar", formatNode, parent, new JCodeModel().ref(String.class), null);

        assertThat(result.fullName(), equalTo(expectedType.getName()));
    }

}
