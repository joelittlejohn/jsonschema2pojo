/*
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
import com.sun.codemodel.*;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.exception.GenerationException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.text.ParseException;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Applies the "enum" schema rule.
 *
 * @see <a href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.20">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.20</a>
 */
public class DefaultRule implements Rule<JFieldVar, JFieldVar> {

    private final RuleFactory ruleFactory;
    private final Map<Matcher, Factory> factories;

    private interface Matcher {
        boolean isMatched(JType fieldType, JsonNode node);
    }

    private interface Factory {
        JExpression build(JType fieldType, JsonNode node);
    }

    public DefaultRule(RuleFactory ruleFactory) {
        this.factories = new LinkedHashMap<Matcher, Factory>();
        this.ruleFactory = ruleFactory;
        initFactories(factories);
    }

    private void initFactories(Map<Matcher, Factory> factories){
        //NULL Matcher
        factories.put(
                new Matcher() {
                    @Override
                    public boolean isMatched(JType fieldType, JsonNode node) {
                        return node != null && node.isNull() && !fieldType.isPrimitive();
                    }
                },
                new Factory() {
                    @Override
                    public JExpression build(JType fieldType, JsonNode node) {
                        return JExpr._null();
                    }
                }
        );
        //List matcher
        factories.put(
                new Matcher() {
                    @Override
                    public boolean isMatched(JType fieldType, JsonNode node) {
                        return fieldType.fullName().startsWith(List.class.getName());
                    }
                },
                new Factory() {
                    @Override
                    public JExpression build(JType fieldType, JsonNode node) {
                        return getArrayExpression(fieldType, node, ArrayList.class);
                    }
                }
        );
        //Set matcher
        factories.put(
                new Matcher() {
                    @Override
                    public boolean isMatched(JType fieldType, JsonNode node) {
                        return fieldType.fullName().startsWith(Set.class.getName());
                    }
                },
                new Factory() {
                    @Override
                    public JExpression build(JType fieldType, JsonNode node) {
                        return getArrayExpression(fieldType, node, LinkedHashSet.class);
                    }
                }
        );
        //String matcher
        factories.put(
                new ObjectMatcher(String.class.getName()),
                new Factory() {
                    @Override
                    public JExpression build(JType fieldType, JsonNode node) {
                        return JExpr.lit(node.asText());
                    }
                }
        );
        //int matcher
        factories.put(
                new ObjectMatcher(int.class.getName()),
                new Factory() {
                    @Override
                    public JExpression build(JType fieldType, JsonNode node) {
                        return JExpr.lit(node.asInt());
                    }
                }
        );
        //BigInteger matcher
        factories.put(
                new ObjectMatcher(BigInteger.class.getName()),
                new Factory() {
                    @Override
                    public JExpression build(JType fieldType, JsonNode node) {
                        return JExpr._new(fieldType).arg(JExpr.lit(node.asText()));
                    }
                }
        );
        //double matcher
        factories.put(
                new ObjectMatcher(double.class.getName()),
                new Factory() {
                    @Override
                    public JExpression build(JType fieldType, JsonNode node) {
                        return JExpr.lit(node.asDouble());
                    }
                }
        );
        //BigDecimal matcher
        factories.put(
                new ObjectMatcher(BigDecimal.class.getName()),
                new Factory() {
                    @Override
                    public JExpression build(JType fieldType, JsonNode node) {
                        return JExpr._new(fieldType).arg(JExpr.lit(node.asText()));
                    }
                }
        );
        //boolean matcher
        factories.put(
                new ObjectMatcher(boolean.class.getName()),
                new Factory() {
                    @Override
                    public JExpression build(JType fieldType, JsonNode node) {
                        return JExpr.lit(node.asBoolean());
                    }
                }
        );
        //DateTime matcher
        factories.put(
                new ObjectMatcher(getDateTimeType().getName()),
                new Factory() {
                    @Override
                    public JExpression build(JType fieldType, JsonNode node) {
                        long millisecs = parseDateToMillisecs(node.asText());
                        JInvocation newDateTime = JExpr._new(fieldType);
                        newDateTime.arg(JExpr.lit(millisecs));
                        return newDateTime;
                    }
                }
        );
        //LocalDate matcher
        factories.put(
                new ObjectMatcher(LocalDate.class.getName()),
                new Factory() {
                    @Override
                    public JExpression build(JType fieldType, JsonNode node) {
                        return getLocalTimeExpression(fieldType, node);
                    }
                }
        );
        //LocalTime matcher
        factories.put(
                new ObjectMatcher(LocalTime.class.getName()),
                new Factory() {
                    @Override
                    public JExpression build(JType fieldType, JsonNode node) {
                        return getLocalTimeExpression(fieldType, node);
                    }
                }
        );
        //long matcher
        factories.put(
                new ObjectMatcher(long.class.getName()),
                new Factory() {
                    @Override
                    public JExpression build(JType fieldType, JsonNode node) {
                        return JExpr.lit(node.asLong());
                    }
                }
        );
        //float matcher
        factories.put(
                new ObjectMatcher(float.class.getName()),
                new Factory() {
                    @Override
                    public JExpression build(JType fieldType, JsonNode node) {
                        return JExpr.lit(Float.parseFloat(node.asText()));
                    }
                }
        );
        //URI matcher
        factories.put(
                new ObjectMatcher(URI.class.getName()),
                new Factory() {
                    @Override
                    public JExpression build(JType fieldType, JsonNode node) {
                        JInvocation invokeCreate = fieldType.owner().ref(URI.class).staticInvoke("create");
                        return invokeCreate.arg(JExpr.lit(node.asText()));
                    }
                }
        );
        //ENUM matcher
        factories.put(
                new Matcher(){
                    @Override
                    public boolean isMatched(JType fieldType, JsonNode node) {
                        return node != null && isNotEmpty(node.asText())
                                && fieldType instanceof JDefinedClass
                                && ((JDefinedClass) fieldType).getClassType().equals(ClassType.ENUM);
                    }
                },
                new Factory() {
                    @Override
                    public JExpression build(JType type, JsonNode node) {
                        return getEnumExpression((JDefinedClass) type, node);
                    }
                }
        );
    }

    private JExpression getEnumExpression(JDefinedClass fieldType, JsonNode node) {
        final JInvocation invokeFromValue = fieldType.staticInvoke(EnumRule.FROM_VALUE_METHOD_NAME);
        final JType paramType = getParameterType(fieldType, EnumRule.FROM_VALUE_METHOD_NAME).unboxify();

        for (Map.Entry<Matcher, Factory> e : factories.entrySet()) {
            if (e.getKey().isMatched(paramType, node)) {
                final JExpression expression = e.getValue().build(paramType, node);
                if (expression != null) {
                    invokeFromValue.arg(expression);
                    return invokeFromValue;
                }
            }
        }
        return invokeFromValue;
    }

    private JExpression getLocalTimeExpression(JType fieldType, JsonNode node) {
        JInvocation stringParseableTypeInstance = JExpr._new(fieldType);
        stringParseableTypeInstance.arg(JExpr.lit(node.asText()));
        return stringParseableTypeInstance;
    }

    private static class ObjectMatcher implements Matcher{
        private final String className;

        private ObjectMatcher(String className) {
            this.className = className;
        }

        @Override
        public boolean isMatched(JType fieldType, JsonNode node) {
            return node != null && isNotEmpty(node.asText())
                    && fieldType.unboxify().fullName().equals(className);
        }
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

        for (Map.Entry<Matcher, Factory> e : factories.entrySet()) {
            if (e.getKey().isMatched(field.type(), node)) {
                final JExpression expression = e.getValue().build(field.type(), node);
                if (expression != null) {
                    field.init(expression);
                    return field;
                }
            }
        }
        return field;
    }


    private Class<?> getDateTimeType() {
        return ruleFactory.getGenerationConfig().isUseJodaDates() ? DateTime.class : Date.class;
    }

    /**
     * Creates a default value for a set\list property by:
     * <ol>
     * <li>Creating a new {@link ArrayList} with the correct generic type for List
     * <li>Creating a new {@link LinkedHashSet} with the correct generic type for Set
     * <li>Using {@link Arrays#asList(Object...)} to initialize the set with the
     * correct default values
     * </ol>
     *
     * @param fieldType
     *            the java type that applies for this field ({@link Set} with
     *            some generic type argument)
     * @param defaultNode
     *            the node containing default values for this set
     * @param theClass
     *            one of {@link ArrayList} or {@link LinkedHashSet} class object
     * @return an expression that creates a default value that can be assigned
     *         to this field
     */
    private JExpression getArrayExpression(JType fieldType, JsonNode defaultNode, Class<?> theClass) {

        JClass genericType = ((JClass) fieldType).getTypeParameters().get(0);
        JClass implClass = fieldType.owner().ref(theClass).narrow(genericType);
        JInvocation invocation = JExpr._new(implClass);

        if (defaultNode instanceof ArrayNode && defaultNode.size() > 0) {
            JInvocation invokeAsList = fieldType.owner().ref(Arrays.class).staticInvoke("asList");
            for (JsonNode defaultValue : defaultNode) {
                for (Map.Entry<Matcher, Factory> e : factories.entrySet()) {
                    if (e.getKey().isMatched(genericType, defaultValue)) {
                        final JExpression expression = e.getValue().build(genericType, defaultValue);
                        if (expression != null) {
                            invokeAsList.arg(expression);
                            break;
                        }
                    }
                }
            }
            invocation.arg(invokeAsList);
        } else if (!ruleFactory.getGenerationConfig().isInitializeCollections()) {
            return JExpr._null();
        }

        return invocation;

    }

    private JType getParameterType(JDefinedClass fieldType, String methodName) {

        for (JMethod method : fieldType.methods()) {
            if (methodName.equals(method.name())
                    && (method.mods().getValue() & JMod.PUBLIC) == JMod.PUBLIC
                    && (method.mods().getValue() & JMod.STATIC) == JMod.STATIC) {
                final JType[] type = method.listParamTypes();
                if (type.length != 1)
                    throw new GenerationException("Factory method '" + EnumRule.FROM_VALUE_METHOD_NAME + "' should has only one parameter in " + fieldType);
                return type[0];
            }
        }
        throw new GenerationException("Factory method '" + EnumRule.FROM_VALUE_METHOD_NAME + "' is not found in " + fieldType);

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
