/*
 * Copyright 2011, Mysema Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mysema.examples;

import static java.util.Collections.unmodifiableList;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Ops provides the operators for the fluent query grammar.
 *
 * @author tiwe
 */
public final class Ops {

    private static final List<Class<?>> BOOLEAN_X_2 = unmodifiableList(Arrays.<Class<?>> asList(Boolean.class, Boolean.class));

    private static final List<Class<?>> COMPARABLE_X_2 = unmodifiableList(Arrays.<Class<?>> asList(Comparable.class, Comparable.class));

    private static final List<Class<?>> COMPARABLE_X_3 = unmodifiableList(Arrays.<Class<?>> asList(Comparable.class, Comparable.class,Comparable.class));

    private static final List<Class<?>> OBJECT_X_2 = unmodifiableList(Arrays.<Class<?>> asList(Object.class, Object.class));

    private static final List<Class<?>> NUMBER_X_2 = unmodifiableList(Arrays.<Class<?>> asList(Number.class, Number.class));

    private static final List<Class<?>> STRING_X_2 = unmodifiableList(Arrays.<Class<?>> asList(String.class, String.class));

    // general
    public static final Operator<Boolean> EQ_PRIMITIVE = new OperatorImpl<>("EQ_PRIMITIVE", OBJECT_X_2);
    public static final Operator<Boolean> EQ_OBJECT = new OperatorImpl<>("EQ_OBJECT", OBJECT_X_2);
    public static final Operator<Boolean> IS_NOT_NULL = new OperatorImpl<>("IS_NOT_NULL", Object.class);
    public static final Operator<Boolean> IS_NULL = new OperatorImpl<>("IS_NULL", Object.class);
    public static final Operator<Boolean> INSTANCE_OF = new OperatorImpl<>("INSTANCE_OF");
    public static final Operator<Boolean> NE_PRIMITIVE = new OperatorImpl<>("NE_PRIMITIVE", OBJECT_X_2);
    public static final Operator<Boolean> NE_OBJECT = new OperatorImpl<>("NE_OBJECT", OBJECT_X_2);
    public static final Operator<Number> NUMCAST = new OperatorImpl<>("NUMCAST");
    public static final Operator<String> STRING_CAST = new OperatorImpl<>("STING_CAST", Object.class);
    public static final Operator<Object> ALIAS = new OperatorImpl<>("ALIAS");
    public static final Operator<Object> LIST = new OperatorImpl<>("LIST");
    public static final Operator<Integer> ORDINAL = new OperatorImpl<>("ORDINAL");
    public static final Operator<Object> DELEGATE = new OperatorImpl<>("DELEGATE");
    public static final Operator<Object> WRAPPED = new OperatorImpl<>("WRAPPED");

    // collection
    public static final Operator<Boolean> IN = new OperatorImpl<>("IN", OBJECT_X_2); // cmp. contains
    public static final Operator<Boolean> COL_IS_EMPTY = new OperatorImpl<>("COL_IS_EMPTY", Object.class);
    public static final Operator<Number> COL_SIZE = new OperatorImpl<>("COL_SIZE", Object.class);

    // array
    public static final Operator<Number> ARRAY_SIZE = new OperatorImpl<>("ARRAY_SIZE", Object.class);

    // map
    public static final Operator<Boolean> CONTAINS_KEY = new OperatorImpl<>("CONTAINS_KEY", OBJECT_X_2);
    public static final Operator<Boolean> CONTAINS_VALUE = new OperatorImpl<>("CONTAINS_VALUE", OBJECT_X_2);
    public static final Operator<Number> MAP_SIZE = new OperatorImpl<>("MAP_SIZE", Object.class);
    public static final Operator<Boolean> MAP_IS_EMPTY = new OperatorImpl<>("MAP_IS_EMPTY", Object.class);

    // Boolean
    public static final Operator<Boolean> AND = new OperatorImpl<>("AND", BOOLEAN_X_2);
    public static final Operator<Boolean> NOT = new OperatorImpl<>("NOT", Boolean.class);
    public static final Operator<Boolean> OR = new OperatorImpl<>("OR", BOOLEAN_X_2);
    public static final Operator<Boolean> XNOR = new OperatorImpl<>("XNOR", BOOLEAN_X_2);
    public static final Operator<Boolean> XOR = new OperatorImpl<>("XOR", BOOLEAN_X_2);

    // Comparable
    public static final Operator<Boolean> BETWEEN = new OperatorImpl<>("BETWEEN", COMPARABLE_X_3);
    public static final Operator<Boolean> GOE = new OperatorImpl<>("GOE", COMPARABLE_X_2);
    public static final Operator<Boolean> GT = new OperatorImpl<>("GT", COMPARABLE_X_2);
    public static final Operator<Boolean> LOE = new OperatorImpl<>("LOE", COMPARABLE_X_2);
    public static final Operator<Boolean> LT = new OperatorImpl<>("LT", COMPARABLE_X_2);

    // Date / Comparable
    public static final Operator<Boolean> AFTER = new OperatorImpl<>("AFTER", COMPARABLE_X_2);
    public static final Operator<Boolean> BEFORE = new OperatorImpl<>("BEFORE", COMPARABLE_X_2);
    public static final Operator<Boolean> AOE = new OperatorImpl<>("AOE", COMPARABLE_X_2);
    public static final Operator<Boolean> BOE = new OperatorImpl<>("BOE", COMPARABLE_X_2);

    // Number
    public static final Operator<Number> NEGATE = new OperatorImpl<>("NEGATE", Number.class);
    public static final Operator<Number> ADD = new OperatorImpl<>("ADD", NUMBER_X_2);
    public static final Operator<Number> DIV = new OperatorImpl<>("DIV", NUMBER_X_2);
    public static final Operator<Number> MULT = new OperatorImpl<>("MULT", NUMBER_X_2);
    public static final Operator<Number> SUB = new OperatorImpl<>("SUB", NUMBER_X_2);
    public static final Operator<Number> MOD = new OperatorImpl<>("MOD", NUMBER_X_2);

    // String
    public static final Operator<Character> CHAR_AT = new OperatorImpl<>("CHAR_AT");
    public static final Operator<String> CONCAT = new OperatorImpl<>("CONCAT", STRING_X_2);
    public static final Operator<String> LOWER = new OperatorImpl<>("LOWER", String.class);
    public static final Operator<String> SUBSTR_1ARG = new OperatorImpl<>("SUBSTR");
    public static final Operator<String> SUBSTR_2ARGS = new OperatorImpl<>("SUBSTR2");
    public static final Operator<String> TRIM = new OperatorImpl<>("TRIM", String.class);
    public static final Operator<String> UPPER = new OperatorImpl<>("UPPER", String.class);
    public static final Operator<Boolean> MATCHES = new OperatorImpl<>("MATCHES", STRING_X_2);
    public static final Operator<Boolean> MATCHES_IC = new OperatorImpl<>("MATCHES_IC", STRING_X_2);
    public static final Operator<Number> STRING_LENGTH = new OperatorImpl<>("STRING_LENGTH", String.class);
    public static final Operator<Boolean> STRING_IS_EMPTY = new OperatorImpl<>("STRING_IS_EMPTY", String.class);
    public static final Operator<Boolean> STARTS_WITH = new OperatorImpl<>("STARTS_WITH", STRING_X_2);
    public static final Operator<Boolean> STARTS_WITH_IC = new OperatorImpl<>("STATS_WITH_IC", STRING_X_2);
    public static final Operator<Number> INDEX_OF_2ARGS = new OperatorImpl<>("INDEX_OF2");
    public static final Operator<Number> INDEX_OF = new OperatorImpl<>("INDEX_OF");
    public static final Operator<Boolean> EQ_IGNORE_CASE = new OperatorImpl<>("EQ_IGNORE_CASE", STRING_X_2);
    public static final Operator<Boolean> ENDS_WITH = new OperatorImpl<>("ENDS_WITH", STRING_X_2);
    public static final Operator<Boolean> ENDS_WITH_IC = new OperatorImpl<>("ENDS_WITH_IC", STRING_X_2);
    public static final Operator<Boolean> STRING_CONTAINS = new OperatorImpl<>("STRING_CONTAINS", STRING_X_2);
    public static final Operator<Boolean> STRING_CONTAINS_IC = new OperatorImpl<>("STRING_CONTAINS_IC", STRING_X_2);
    public static final Operator<Boolean> LIKE = new OperatorImpl<>("LIKE", STRING_X_2);

    // case
    public static final Operator<Object> CASE = new OperatorImpl<>("CASE", Object.class);
    public static final Operator<Object> CASE_WHEN = new OperatorImpl<>("CASE_WHEN");
    public static final Operator<Object> CASE_ELSE = new OperatorImpl<>("CASE_ELSE", Object.class);

    // case for eq
    public static final Operator<Object> CASE_EQ = new OperatorImpl<>("CASE_EQ", Object.class);
    public static final Operator<Object> CASE_EQ_WHEN = new OperatorImpl<>("CASE_EQ_WHEN");
    public static final Operator<Object> CASE_EQ_ELSE = new OperatorImpl<>("CASE_EQ_ELSE", Object.class);

    // coalesce
    public static final Operator<Object> COALESCE = new OperatorImpl<>("COALESCE", Object.class);

    // subquery operations
    public static final Operator<Boolean> EXISTS = new OperatorImpl<>("EXISTS", Object.class);

    public static final List<Operator<?>> equalsOps = unmodifiableList(Arrays.<Operator<?>> asList(EQ_OBJECT, EQ_PRIMITIVE));

    public static final List<Operator<?>> notEqualsOps = unmodifiableList(Arrays.<Operator<?>> asList(NE_OBJECT, NE_PRIMITIVE));

    public static final List<Operator<?>> compareOps = unmodifiableList(Arrays.<Operator<?>> asList(EQ_OBJECT, EQ_PRIMITIVE,LT, GT, GOE, LOE));

    /**
     * Aggreation operators
     */
    @SuppressWarnings("unchecked")
    public static final class AggOps{
        public static final Operator<Comparable> MAX_AGG = new OperatorImpl<>("MAX_AGG", Comparable.class);
        public static final Operator<Comparable> MIN_AGG = new OperatorImpl<>("MIN_AGG", Comparable.class);
        public static final Operator<Number> AVG_AGG = new OperatorImpl<>("AVG_AGG", Number.class);
        public static final Operator<Number> SUM_AGG = new OperatorImpl<>("SUM_AGG", Number.class);
        public static final Operator<Number> COUNT_AGG = new OperatorImpl<>("COUNT_AGG", Object.class);
        public static final Operator<Number> COUNT_DISTINCT_AGG = new OperatorImpl<>("COUNT_DISTINCT_AGG", Object.class);
        public static final Operator<Number> COUNT_ALL_AGG = new OperatorImpl<>("COUNT_ALL_AGG");
        private AggOps() {}
    }

    /**
     * Date and time operators
     */
    @SuppressWarnings("unchecked")
    public static final class DateTimeOps {
        public static final Operator<Comparable> CURRENT_DATE = new OperatorImpl<>("CURRENT_DATE");
        public static final Operator<Comparable> CURRENT_TIME = new OperatorImpl<>("CURRENT_TIME");
        public static final Operator<Comparable> CURRENT_TIMESTAMP = new OperatorImpl<>("CURRENT_TIMESTAMP");
        public static final Operator<Integer> HOUR = new OperatorImpl<>("HOUR", java.util.Date.class);
        public static final Operator<Integer> MINUTE = new OperatorImpl<>("MINUTE", java.util.Date.class);
        public static final Operator<Integer> MONTH = new OperatorImpl<>("MONTH", java.util.Date.class);
        public static final Operator<Integer> SECOND = new OperatorImpl<>("SECOND", java.util.Date.class);
        public static final Operator<Integer> MILLISECOND = new OperatorImpl<>("MILLISECOND", java.util.Date.class);
        public static final Operator<Comparable> SYSDATE = new OperatorImpl<>("SYSDATE");
        public static final Operator<Integer> YEAR = new OperatorImpl<>("YEAR", java.util.Date.class);
        public static final Operator<Integer> YEAR_MONTH = new OperatorImpl<>("YEAR_MONTH", java.util.Date.class);
        public static final Operator<Integer> WEEK = new OperatorImpl<>("WEEK", java.util.Date.class);
        public static final Operator<Integer> DAY_OF_WEEK = new OperatorImpl<>("DAY_OF_WEEK", java.util.Date.class);
        public static final Operator<Integer> DAY_OF_MONTH = new OperatorImpl<>("DAY_OF_MONTH", java.util.Date.class);
        public static final Operator<Integer> DAY_OF_YEAR = new OperatorImpl<>("DAY_OF_YEAR", java.util.Date.class);
        private DateTimeOps() {}
    }

    /**
     * Math operators
     *
     */
    public static final class MathOps {
        public static final Operator<Number> ABS = new OperatorImpl<>("ABS", Number.class);
        public static final Operator<Number> ACOS = new OperatorImpl<>("ACOS", Number.class);
        public static final Operator<Number> ASIN = new OperatorImpl<>("ASIN", Number.class);
        public static final Operator<Number> ATAN = new OperatorImpl<>("ATAN", Number.class);
        public static final Operator<Number> CEIL = new OperatorImpl<>("CEIL", Number.class);
        public static final Operator<Number> COS = new OperatorImpl<>("COS", Number.class);
        public static final Operator<Number> TAN = new OperatorImpl<>("TAN", Number.class);
        public static final Operator<Number> SQRT = new OperatorImpl<>("SQRT", Number.class);
        public static final Operator<Number> SIN = new OperatorImpl<>("SIN", Number.class);
        public static final Operator<Number> ROUND = new OperatorImpl<>("ROUND", Number.class);
        public static final Operator<Number> RANDOM = new OperatorImpl<>("RANDOM");
        public static final Operator<Number> POWER = new OperatorImpl<>("POWER", NUMBER_X_2);
        public static final Operator<Number> MIN = new OperatorImpl<>("MIN", NUMBER_X_2);
        public static final Operator<Number> MAX = new OperatorImpl<>("MAX", NUMBER_X_2);
        public static final Operator<Number> LOG10 = new OperatorImpl<>("LOG10", Number.class);
        public static final Operator<Number> LOG = new OperatorImpl<>("LOG", Number.class);
        public static final Operator<Number> FLOOR = new OperatorImpl<>("FLOOR", Number.class);
        public static final Operator<Number> EXP = new OperatorImpl<>("EXP", Number.class);
        private MathOps() {}
    }

    /**
     * String operators
     */
    public static final class StringOps {
        public static final Operator<String> LTRIM = new OperatorImpl<>("LTRIM", String.class);
        public static final Operator<String> RTRIM = new OperatorImpl<>("RTRIM", String.class);
        public static final Operator<String> SPACE = new OperatorImpl<>("SPACE", Integer.class);
        public static final Operator<String[]> SPLIT = new OperatorImpl<>("SPLIT", STRING_X_2);
        public static final Operator<Number> LAST_INDEX_2ARGS = new OperatorImpl<>("LAST_INDEX2");
        public static final Operator<Number> LAST_INDEX = new OperatorImpl<>("LAST_INDEX", STRING_X_2);
        private StringOps() {}
    }

    /**
     * Quantification operators
     */
    @SuppressWarnings("unchecked")
    public static final class QuantOps {
        public static final Operator<Comparable> AVG_IN_COL = new OperatorImpl<>("AVG_IN_COL", Collection.class);
        public static final Operator<Comparable> MAX_IN_COL = new OperatorImpl<>("MAX_IN_COL", Collection.class);
        public static final Operator<Comparable> MIN_IN_COL = new OperatorImpl<>("MIN_IN_COL", Collection.class);

        // some / any = true for any
        // all = true for all
        // exists = true is subselect matches
        // not exists = true if subselect doesn't match
        public static final Operator<Object> ANY = new OperatorImpl<>("ANY", Object.class);
        public static final Operator<Object> ALL = new OperatorImpl<>("ALL", Object.class);
        private QuantOps() {}
    }
    
    /**
     * Operator represents operator symbols
     *
     * @author tiwe
     *
     * @param <T> related expression type
     */
    interface Operator<T> extends Serializable{

        /**
         * Get the unique id for this Operator
         *
         * @return
         */
        String getId();

        /**
         * Get the types related to this operator symbols
         *
         * @return
         */
        List<Class<?>> getTypes();

    }

    /**
     * OperatorImpl is the default implementation of the {@link Operator}  interface
     */
    static class OperatorImpl<T> implements Operator<T> {

        private static final long serialVersionUID = -2435035383548549877L;

        private final String id;
        
        private final List<Class<?>> types;

        public OperatorImpl(String id, Class<?>... types) {
            this(id, Arrays.<Class<?>> asList(types));
        }

        public OperatorImpl(String id, List<Class<?>> types) {
            this.id = id;
            this.types = types;
        }

        @Override
        public String getId(){
            return id;
        }
        
        @Override
        public List<Class<?>> getTypes() {
            return types;
        }
        
        @Override
        public boolean equals(Object o){
            if (o == this) {
                return true;
            } else if (o instanceof Operator<?>) {
                return ((Operator<?>)o).getId().equals(id);
            } else {
                return false;
            }
        }
        
        @Override
        public int hashCode(){
            return id.hashCode();
        }
        
        @Override
        public String toString(){
            return id;
        }
    }
    
    private Ops() {}
}

