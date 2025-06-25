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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.jsonschema2pojo.Schema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.joinWith;
import static org.apache.commons.lang3.StringUtils.splitByCharacterTypeCamelCase;

public class ConstRule implements Rule<JFieldVar, JFieldVar> {

    private final RuleFactory ruleFactory;
    private final JDefinedClass jclass;

    public ConstRule(RuleFactory ruleFactory, JDefinedClass jclass) {
        this.ruleFactory = ruleFactory;
        this.jclass = jclass;
    }

    @Override
    public JFieldVar apply(String nodeName, JsonNode node, JsonNode parent, JFieldVar field, Schema currentSchema) {
        boolean constPresent = node != null && isNotEmpty(node.asText());
        JType propertyType = field.type();

        if (!constPresent) {
            return field;
        }

        String constName = joinWith("_", splitByCharacterTypeCamelCase(nodeName)).toUpperCase();
        JFieldVar myConstant = jclass.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL,
                                            propertyType,
                                            constName,
                                            getValue(propertyType, node));

        return field;
    }

    static JExpression getValue(JType type, JsonNode node) {
        Objects.requireNonNull(node);
        String fieldType = type.fullName();
        if (fieldType.startsWith(String.class.getName())) {
            return getConstValue(type, node);
        } else {
            return getConstValue(type, node);
        }
    }

    static JExpression getConstValue(JType fieldType, JsonNode node) {
        return getConstValue(fieldType, node.asText());
    }

    static JExpression getConstValue(JType fieldType, String value) {

        fieldType = fieldType.unboxify();

        if (fieldType.fullName().equals(String.class.getName())) {
            return JExpr.lit(value);

        } else if (fieldType.fullName().equals(int.class.getName())) {
            return JExpr.lit(Integer.parseInt(value));

        } else if (fieldType.fullName().equals(BigInteger.class.getName())) {
            return JExpr._new(fieldType).arg(JExpr.lit(value));

        } else if (fieldType.fullName().equals(double.class.getName())) {
            return JExpr.lit(Double.parseDouble(value));

        } else if (fieldType.fullName().equals(BigDecimal.class.getName())) {
            return JExpr._new(fieldType).arg(JExpr.lit(value));

        } else if (fieldType.fullName().equals(boolean.class.getName())) {
            return JExpr.lit(Boolean.parseBoolean(value));

        } else if (fieldType.fullName().equals(DateTime.class.getName()) || fieldType.fullName().equals(Date.class.getName())) {
            long millisecs = parseDateToMillisecs(value);

            JInvocation newDateTime = JExpr._new(fieldType);
            newDateTime.arg(JExpr.lit(millisecs));

            return newDateTime;

        } else if (fieldType.fullName().equals(LocalDate.class.getName()) || fieldType.fullName().equals(LocalTime.class.getName())) {

            JInvocation stringParseableTypeInstance = JExpr._new(fieldType);
            stringParseableTypeInstance.arg(JExpr.lit(value));
            return stringParseableTypeInstance;

        } else if (fieldType.fullName().equals(long.class.getName())) {
            return JExpr.lit(Long.parseLong(value));

        } else if (fieldType.fullName().equals(float.class.getName())) {
            return JExpr.lit(Float.parseFloat(value));

        } else if (fieldType.fullName().equals(URI.class.getName())) {
            JInvocation invokeCreate = fieldType.owner().ref(URI.class).staticInvoke("create");
            return invokeCreate.arg(JExpr.lit(value));
        } else {
            return JExpr._null();

        }

    }

    private static long parseDateToMillisecs(String valueAsText) {

        try {
            return Long.parseLong(valueAsText);
        } catch (NumberFormatException nfe) {
            try {
                return new StdDateFormat().parse(valueAsText).getTime();
            } catch (ParseException pe) {
                throw new IllegalArgumentException("Unable to parse this string as a date: " + valueAsText);
            }
        }

    }

}
