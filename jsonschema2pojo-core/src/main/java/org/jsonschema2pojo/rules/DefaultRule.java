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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import static org.apache.commons.lang3.StringUtils.*;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.jsonschema2pojo.Schema;

/**
 * Applies the "enum" schema rule.
 *
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.20">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.20</a>
 */
public class DefaultRule implements Rule<JFieldVar, JFieldVar> {

    private final RuleFactory ruleFactory;

    public DefaultRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * Default values are implemented by assigning an expression to the given
     * field (so when instances of the generated POJO are created, its fields
     * will then contain their default values).
     * <p>
     * Collections (Lists and Sets) are initialized to an empty collection, even
     * when no default value is present in the schema (node is null).
     *
     * @param nodeName
     *            the name of the property which has (or may have) a default
     * @param node
     *            the default node (may be null if no default node was present
     *            for this property)
     * @param field
     *            the Java field that has added to a generated type to represent
     *            this property
     * @return field, which will have an init expression is appropriate
     */
    @Override
    public JFieldVar apply(String nodeName, JsonNode node, JFieldVar field, Schema currentSchema) {

        boolean defaultPresent = node != null && isNotEmpty(node.asText());

        String fieldType = field.type().fullName();

        if (defaultPresent && !field.type().isPrimitive() && node.isNull()) {
            field.init(JExpr._null());

        } else if (fieldType.startsWith(List.class.getName())) {
            field.init(getDefaultList(field.type(), node));

        } else if (fieldType.startsWith(Set.class.getName())) {
            field.init(getDefaultSet(field.type(), node));
        } else if (fieldType.startsWith(String.class.getName()) && node != null ) {
            field.init(getDefaultValue(field.type(), node));
        } else if (defaultPresent) {
            field.init(getDefaultValue(field.type(), node));

        }

        return field;
    }

    private JExpression getDefaultValue(JType fieldType, JsonNode node) {

        fieldType = fieldType.unboxify();

        if (fieldType.fullName().equals(String.class.getName())) {
            return JExpr.lit(node.asText());

        } else if (fieldType.fullName().equals(int.class.getName())) {
            return JExpr.lit(Integer.parseInt(node.asText()));

        } else if (fieldType.fullName().equals(BigInteger.class.getName())) {
            return JExpr._new(fieldType).arg(JExpr.lit(node.asText()));

        } else if (fieldType.fullName().equals(double.class.getName())) {
            return JExpr.lit(Double.parseDouble(node.asText()));

        } else if (fieldType.fullName().equals(BigDecimal.class.getName())) {
            return JExpr._new(fieldType).arg(JExpr.lit(node.asText()));

        } else if (fieldType.fullName().equals(boolean.class.getName())) {
            return JExpr.lit(Boolean.parseBoolean(node.asText()));

        } else if (fieldType.fullName().equals(getDateTimeType().getName())) {
            long millisecs = parseDateToMillisecs(node.asText());

            JInvocation newDateTime = JExpr._new(fieldType);
            newDateTime.arg(JExpr.lit(millisecs));

            return newDateTime;

        } else if (fieldType.fullName().equals(LocalDate.class.getName()) ||
                   fieldType.fullName().equals(LocalTime.class.getName())) {

            JInvocation stringParseableTypeInstance = JExpr._new(fieldType);
            stringParseableTypeInstance.arg(JExpr.lit(node.asText()));
            return stringParseableTypeInstance;

        } else if (fieldType.fullName().equals(long.class.getName())) {
            return JExpr.lit(Long.parseLong(node.asText()));

        } else if (fieldType.fullName().equals(float.class.getName())) {
            return JExpr.lit(Float.parseFloat(node.asText()));

        } else if (fieldType instanceof JDefinedClass && ((JDefinedClass) fieldType).getClassType().equals(ClassType.ENUM)) {

            return getDefaultEnum(fieldType, node);

        } else {
            return JExpr._null();

        }

    }

    private Class<?> getDateTimeType() {
        return ruleFactory.getGenerationConfig().isUseJodaDates() ? DateTime.class : Date.class;
    }

    /**
     * Creates a default value for a list property by:
     * <ol>
     * <li>Creating a new {@link ArrayList} with the correct generic type
     * <li>Using {@link Arrays#asList(Object...)} to initialize the list with
     * the correct default values
     * </ol>
     *
     * @param fieldType
     *            the java type that applies for this field ({@link List} with
     *            some generic type argument)
     * @param node
     *            the node containing default values for this list
     * @return an expression that creates a default value that can be assigned
     *         to this field
     */
    private JExpression getDefaultList(JType fieldType, JsonNode node) {

        JClass listGenericType = ((JClass) fieldType).getTypeParameters().get(0);

        JClass listImplClass = fieldType.owner().ref(ArrayList.class);
        listImplClass = listImplClass.narrow(listGenericType);

        JInvocation newListImpl = JExpr._new(listImplClass);

        if (node instanceof ArrayNode && node.size() > 0) {
            JInvocation invokeAsList = fieldType.owner().ref(Arrays.class).staticInvoke("asList");
            for (JsonNode defaultValue : node) {
                invokeAsList.arg(getDefaultValue(listGenericType, defaultValue));
            }
            newListImpl.arg(invokeAsList);
        } else if (!ruleFactory.getGenerationConfig().isInitializeCollections()) {
            return JExpr._null();
        }

        return newListImpl;

    }

    /**
     * Creates a default value for a set property by:
     * <ol>
     * <li>Creating a new {@link LinkedHashSet} with the correct generic type
     * <li>Using {@link Arrays#asList(Object...)} to initialize the set with the
     * correct default values
     * </ol>
     *
     * @param fieldType
     *            the java type that applies for this field ({@link Set} with
     *            some generic type argument)
     * @param node
     *            the node containing default values for this set
     * @return an expression that creates a default value that can be assigned
     *         to this field
     */
    private JExpression getDefaultSet(JType fieldType, JsonNode node) {

        JClass setGenericType = ((JClass) fieldType).getTypeParameters().get(0);

        JClass setImplClass = fieldType.owner().ref(LinkedHashSet.class);
        setImplClass = setImplClass.narrow(setGenericType);

        JInvocation newSetImpl = JExpr._new(setImplClass);

        if (node instanceof ArrayNode && node.size() > 0) {
            JInvocation invokeAsList = fieldType.owner().ref(Arrays.class).staticInvoke("asList");
            for (JsonNode defaultValue : node) {
                invokeAsList.arg(getDefaultValue(setGenericType, defaultValue));
            }
            newSetImpl.arg(invokeAsList);
        } else if (!ruleFactory.getGenerationConfig().isInitializeCollections()) {
            return JExpr._null();
        }

        return newSetImpl;

    }

    private JExpression getDefaultEnum(JType fieldType, JsonNode node) {

        JInvocation invokeFromValue = ((JClass) fieldType).staticInvoke("fromValue");
        invokeFromValue.arg(node.asText());

        return invokeFromValue;

    }

    private long parseDateToMillisecs(String valueAsText) {

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
