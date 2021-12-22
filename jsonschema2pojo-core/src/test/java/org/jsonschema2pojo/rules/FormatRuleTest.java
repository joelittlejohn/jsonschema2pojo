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

package org.jsonschema2pojo.rules;

import com.fasterxml.jackson.databind.node.TextNode;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.SchemaStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class FormatRuleTest {

    private final GenerationConfig config = mock(GenerationConfig.class);
    private final FormatRule rule = new FormatRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));

    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("date-time", Date.class),
                Arguments.of("date", String.class),
                Arguments.of("time", String.class),
                Arguments.of("utc-millisec", Long.class),
                Arguments.of("regex", Pattern.class),
                Arguments.of("color", String.class),
                Arguments.of("style", String.class),
                Arguments.of("phone", String.class),
                Arguments.of("uri", URI.class),
                Arguments.of("email", String.class),
                Arguments.of("ip-address", String.class),
                Arguments.of("ipv6", String.class),
                Arguments.of("host-name", String.class),
                Arguments.of("uuid", UUID.class)
        );
    }


    @ParameterizedTest
    @MethodSource("data")
    public void applyGeneratesTypeFromFormatValue(String formatValue, Class<?> expectedType) {
        TextNode formatNode = TextNode.valueOf(formatValue);

        JType result = rule.apply("fooBar", formatNode, null, new JCodeModel().ref(String.class), null);

        assertThat(result.fullName(), equalTo(expectedType.getName()));
    }

    @Test
    public void applyDefaultsToBaseType() {
        TextNode formatNode = TextNode.valueOf("unknown-format");

        JType baseType = new JCodeModel().ref(Long.class);

        JType result = rule.apply("fooBar", formatNode, null, baseType, null);

        assertThat(result, equalTo(baseType));
    }

}
