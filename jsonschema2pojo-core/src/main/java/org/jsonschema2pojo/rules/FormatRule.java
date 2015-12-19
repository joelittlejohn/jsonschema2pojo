/**
 * Copyright © 2010-2014 Nokia
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

import java.net.URI;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.exception.GenerationException;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JType;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static java.lang.String.format;

/**
 * Applies the "format" schema rule.
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.23">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.23</a>
 */
public class FormatRule implements Rule<JType, JType> {

    private final RuleFactory ruleFactory;

    protected FormatRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * This rule maps format values to Java types:
     * <ul>
     * <li>"format":"date-time" =&gt; {@link java.util.Date} or {@link org.joda.time.DateTime} (if config useJodaDates is set)
     * <li>"format":"date" =&gt; {@link String} or {@link org.joda.time.LocalDate} (if config useJodaLocalDates is set)
     * <li>"format":"time" =&gt; {@link String} or {@link org.joda.time.LocalTime} (if config useJodaLocalTimes is set)
     * <li>"format":"utc-millisec" =&gt; <code>long</code>
     * <li>"format":"regex" =&gt; {@link java.util.regex.Pattern}
     * <li>"format":"color" =&gt; {@link String}
     * <li>"format":"style" =&gt; {@link String}
     * <li>"format":"phone" =&gt; {@link String}
     * <li>"format":"uri" =&gt; {@link java.net.URI}
     * <li>"format":"email" =&gt; {@link String}
     * <li>"format":"ip-address" =&gt; {@link String}
     * <li>"format":"ipv6" =&gt; {@link String}
     * <li>"format":"host-name" =&gt; {@link String}
     * <li>"format":"uuid" =&gt; {@link java.util.UUID}
     * <li>other (unrecognised format) =&gt; baseType
     * </ul>
     * 
     * @param nodeName
     *            the name of the node to which this format is applied
     * @param node
     *            the format node
     * @param baseType
     *            the type which which is being formatted e.g. for
     *            <code>{ "type" : "string", "format" : "uri" }</code> the
     *            baseType would be java.lang.String
     * @return the Java type that is appropriate for the format value
     */
    @Override
    public JType apply(String nodeName, JsonNode node, JType baseType, Schema schema) {

        if (node.asText().equals("date-time")) {
            return baseType.owner().ref(getDateTimeType());

        } else if (node.asText().equals("date")) {
            return baseType.owner().ref(getDateOnlyType());

        } else if (node.asText().equals("time")) {
            return baseType.owner().ref(getTimeOnlyType());

        } else if (node.asText().equals("utc-millisec")) {
            return unboxIfNecessary(baseType.owner().ref(Long.class), ruleFactory.getGenerationConfig());

        } else if (node.asText().equals("regex")) {
            return baseType.owner().ref(Pattern.class);

        } else if (node.asText().equals("color")) {
            return baseType.owner().ref(String.class);

        } else if (node.asText().equals("style")) {
            return baseType.owner().ref(String.class);

        } else if (node.asText().equals("phone")) {
            return baseType.owner().ref(String.class);

        } else if (node.asText().equals("uri")) {
            return baseType.owner().ref(URI.class);

        } else if (node.asText().equals("email")) {
            return baseType.owner().ref(String.class);

        } else if (node.asText().equals("ip-address")) {
            return baseType.owner().ref(String.class);

        } else if (node.asText().equals("ipv6")) {
            return baseType.owner().ref(String.class);

        } else if (node.asText().equals("host-name")) {
            return baseType.owner().ref(String.class);
        }
          else if (node.asText().equals("uuid")) {
                return baseType.owner().ref(UUID.class);
        }
         else {
            return baseType;
        }

    }

    private Class<?> getDateTimeType() {
        String type=ruleFactory.getGenerationConfig().getDateTimeType();
        if (!isEmpty(type)){
            try {
                Class<?> clazz=Class.forName(type);
                return clazz;
            }
            catch (ClassNotFoundException e) {
                throw new GenerationException(format("could not load java type %s for date-time format", type), e);
            }
        }
        return ruleFactory.getGenerationConfig().isUseJodaDates() ? DateTime.class : Date.class;
    }

    private Class<?> getDateOnlyType() {
        String type=ruleFactory.getGenerationConfig().getDateType();
        if (!isEmpty(type)){
            try {
                Class<?> clazz=Class.forName(type);
                return clazz;
            }
            catch (ClassNotFoundException e) {
                throw new GenerationException(format("could not load java type %s for date format", type), e);
            }
        }
        return ruleFactory.getGenerationConfig().isUseJodaLocalDates() ? LocalDate.class : String.class;
    }

    private Class<?> getTimeOnlyType() {
        String type=ruleFactory.getGenerationConfig().getTimeType();
        if (!isEmpty(type)){
            try {
                Class<?> clazz=Class.forName(type);
                return clazz;
            }
            catch (ClassNotFoundException e) {
                throw new GenerationException(format("could not load java type %s for time format", type), e);
            }
        }
        return ruleFactory.getGenerationConfig().isUseJodaLocalTimes() ? LocalTime.class : String.class;
    }

    private JType unboxIfNecessary(JType type, GenerationConfig config) {
        if (config.isUsePrimitives()) {
            return type.unboxify();
        } else {
            return type;
        }
    }

}
