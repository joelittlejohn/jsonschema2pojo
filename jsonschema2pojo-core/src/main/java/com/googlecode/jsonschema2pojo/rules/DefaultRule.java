package com.googlecode.jsonschema2pojo.rules;

import static org.apache.commons.lang.StringUtils.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.util.StdDateFormat;
import org.codehaus.jackson.node.ArrayNode;

import com.googlecode.jsonschema2pojo.Schema;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JType;

/**
 * Applies the "enum" schema rule.
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.20">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.20</a>
 */
public class DefaultRule implements SchemaRule<JFieldVar, JFieldVar> {

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

        boolean defaultPresent = node != null && isNotEmpty(node.getValueAsText());

        String fieldType = field.type().fullName();

        if (fieldType.startsWith(List.class.getName())) {
            field.init(getDefaultList(field.type(), node));

        } else if (fieldType.startsWith(Set.class.getName())) {
            field.init(getDefaultSet(field.type(), node));

        } else if (defaultPresent) {
            field.init(getDefaultValue(field.type(), node));

        }

        return field;
    }

    private JExpression getDefaultValue(JType fieldType, JsonNode node) {

        fieldType = fieldType.unboxify();

        if (fieldType.fullName().equals(String.class.getName())) {
            return JExpr.lit(node.getValueAsText());

        } else if (fieldType.fullName().equals(int.class.getName())) {
            return JExpr.lit(Integer.parseInt(node.getValueAsText()));

        } else if (fieldType.fullName().equals(double.class.getName())) {
            return JExpr.lit(Double.parseDouble(node.getValueAsText()));

        } else if (fieldType.fullName().equals(boolean.class.getName())) {
            return JExpr.lit(Boolean.parseBoolean(node.getValueAsText()));

        } else if (fieldType.fullName().equals(Date.class.getName())) {
            long millisecs = parseDateToMillisecs(node.getValueAsText());

            JInvocation newDate = JExpr._new(fieldType.owner().ref(Date.class));
            newDate.arg(JExpr.lit(millisecs));

            return newDate;

        } else if (fieldType.fullName().equals(long.class.getName())) {
            return JExpr.lit(Long.parseLong(node.getValueAsText()));

        } else if (fieldType instanceof JDefinedClass && ((JDefinedClass) fieldType).getClassType().equals(ClassType.ENUM)) {

            return getDefaultEnum(fieldType, node);

        } else {
            return JExpr._null();

        }

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

        if (node instanceof ArrayNode) {
            JInvocation invokeAsList = fieldType.owner().ref(Arrays.class).staticInvoke("asList");
            for (JsonNode defaultValue : node) {
                invokeAsList.arg(getDefaultValue(listGenericType, defaultValue));
            }
            newListImpl.arg(invokeAsList);
        }

        return newListImpl;

    }

    /**
     * Creates a default value for a set property by:
     * <ol>
     * <li>Creating a new {@link HashSet} with the correct generic type
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

        JClass setImplClass = fieldType.owner().ref(HashSet.class);
        setImplClass = setImplClass.narrow(setGenericType);

        JInvocation newSetImpl = JExpr._new(setImplClass);

        if (node instanceof ArrayNode) {
            JInvocation invokeAsList = fieldType.owner().ref(Arrays.class).staticInvoke("asList");
            for (JsonNode defaultValue : node) {
                invokeAsList.arg(getDefaultValue(setGenericType, defaultValue));
            }
            newSetImpl.arg(invokeAsList);
        }

        return newSetImpl;

    }

    private JExpression getDefaultEnum(JType fieldType, JsonNode node) {

        JInvocation invokeFromValue = ((JClass) fieldType).staticInvoke("fromValue");
        invokeFromValue.arg(node.getValueAsText());

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
