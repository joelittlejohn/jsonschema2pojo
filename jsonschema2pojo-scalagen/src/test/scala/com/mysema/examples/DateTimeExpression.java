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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import com.mysema.query.types.Expression;
import com.mysema.query.types.Operation;
import com.mysema.query.types.OperationImpl;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;
import com.mysema.query.types.Visitor;
import com.mysema.query.types.expr.NumberExpression;
import com.mysema.query.types.expr.NumberOperation;
import com.mysema.query.types.expr.TemporalExpression;

/**
 * DateTimeExpression represents Date / Time expressions
 * The date representation is compatible with the Gregorian calendar.
 *
 * @param <T> expression type
 *
 * @author tiwe
 * @see <a href="http://en.wikipedia.org/wiki/Gregorian_calendar">Gregorian calendar</a>
 */
public abstract class DateTimeExpression<T extends Comparable<T>> extends TemporalExpression<T> {

    private static final DateTimeExpression<Date> CURRENT_DATE = currentDate(Date.class);

    private static final DateTimeExpression<Date> CURRENT_TIMESTAMP = currentTimestamp(Date.class);

    private static final long serialVersionUID = -6879277113694148047L;

    /**
     * Get an expression representing the current date as a EDateTime instance
     *
     * @return
     */
    public static DateTimeExpression<Date> currentDate() {
        return CURRENT_DATE;
    }

    /**
     * Get an expression representing the current date as a EDateTime instance
     *
     * @return
     */
    public static <T extends Comparable<T>> DateTimeExpression<T> currentDate(Class<T> cl) {
        return DateTimeOperation.<T>create(cl, Ops.DateTimeOps.CURRENT_DATE);
    }

    /**
     * Get an expression representing the current time instant as a EDateTime instance
     *
     * @return
     */
    public static DateTimeExpression<Date> currentTimestamp() {
        return CURRENT_TIMESTAMP;
    }

    /**
     * Get an expression representing the current time instant as a EDateTime instance
     *
     * @return
     */
    public static <T extends Comparable<T>> DateTimeExpression<T> currentTimestamp(Class<T> cl) {
        return DateTimeOperation.<T>create(cl, Ops.DateTimeOps.CURRENT_TIMESTAMP);
    }


    @Nullable
    private volatile NumberExpression<Integer> dayOfMonth, dayOfWeek, dayOfYear;

    @Nullable
    private volatile NumberExpression<Integer> hour, minute, second, milliSecond;


    @Nullable
    private volatile DateTimeExpression<T> min, max;

    @Nullable
    private volatile NumberExpression<Integer> week, month, year, yearMonth;

    public DateTimeExpression(Class<T> type) {
        super(type);
    }

//    @Override
//    public DateTimeExpression<T> as(Path<T> alias) {
//        return DateTimeOperation.<T>create((Class<T>)getType(), Ops.ALIAS, this, alias);
//    }

//    @Override
//    public DateTimeExpression<T> as(String alias) {
//        return as(new PathImpl<T>(getType(), alias));
//    }

    /**
     * Get a day of month expression (range 1-31)
     *
     * @return
     */
    public NumberExpression<Integer> dayOfMonth(){
        if (dayOfMonth == null) {
            dayOfMonth = NumberOperation.create(Integer.class, Ops.DateTimeOps.DAY_OF_MONTH, this);
        }
        return dayOfMonth;
    }

    /**
     * Get a day of week expression (range 1-7 / SUN-SAT)
     * <p>NOT supported in JDOQL and not in Derby</p>
     *
     * @return
     */
    public NumberExpression<Integer> dayOfWeek() {
        if (dayOfWeek == null) {
            dayOfWeek = NumberOperation.create(Integer.class, Ops.DateTimeOps.DAY_OF_WEEK, this);
        }
        return dayOfWeek;
    }

    /**
     * Get a day of year expression (range 1-356)
     * <p>NOT supported in JDOQL and not in Derby</p>
     *
     * @return
     */
    public NumberExpression<Integer> dayOfYear() {
        if (dayOfYear == null){
            dayOfYear = NumberOperation.create(Integer.class, Ops.DateTimeOps.DAY_OF_YEAR, this);
        }
        return dayOfYear;
    }

    /**
     * Get a hours expression (range 0-23)
     *
     * @return
     */
    public NumberExpression<Integer> hour(){
        if (hour == null) {
            hour = NumberOperation.create(Integer.class, Ops.DateTimeOps.HOUR, this);
        }
        return hour;
    }

    /**
     * Get the maximum value of this expression (aggregation)
     *
     * @return max(this)
     */
    public DateTimeExpression<T> max(){
        if (max == null) {
            max = DateTimeOperation.<T>create((Class<T>)getType(), Ops.AggOps.MAX_AGG, this);
        }
        return max;
    }

    /**
     * Get a milliseconds expression (range 0-999)
     * <p>Is always 0 in HQL and JDOQL modules</p>
     *
     * @return
     */
    public NumberExpression<Integer> milliSecond(){
        if (milliSecond == null) {
            milliSecond = NumberOperation.create(Integer.class, Ops.DateTimeOps.MILLISECOND, this);
        }
        return milliSecond;
    }

    /**
     * Get the minimum value of this expression (aggregation)
     *
     * @return min(this)
     */
    public DateTimeExpression<T> min(){
        if (min == null) {
            min = DateTimeOperation.<T>create((Class<T>)getType(), Ops.AggOps.MIN_AGG, this);
        }
        return min;
    }

    /**
     * Get a minutes expression (range 0-59)
     *
     * @return
     */
    public NumberExpression<Integer> minute(){
        if (minute == null) {
            minute = NumberOperation.create(Integer.class, Ops.DateTimeOps.MINUTE, this);
        }
        return minute;
    }

    /**
     * Get a month expression (range 1-12 / JAN-DEC)
     *
     * @return
     */
    public NumberExpression<Integer> month(){
        if (month == null) {
            month = NumberOperation.create(Integer.class, Ops.DateTimeOps.MONTH, this);
        }
        return month;
    }

    /**
     * Get a seconds expression (range 0-59)
     *
     * @return
     */
    public NumberExpression<Integer> second(){
        if (second == null) {
            second = NumberOperation.create(Integer.class, Ops.DateTimeOps.SECOND, this);
        }
        return second;
    }

    /**
     * Get a week expression
     *
     * @return
     */
    public NumberExpression<Integer> week() {
        if (week == null) {
            week = NumberOperation.create(Integer.class, Ops.DateTimeOps.WEEK,  this);
        }
        return week;
    }

    /**
     * Get a year expression
     *
     * @return
     */
    public NumberExpression<Integer> year(){
        if (year == null) {
            year = NumberOperation.create(Integer.class, Ops.DateTimeOps.YEAR, this);
        }
        return year;
    }

    /**
     * Get a year / month expression
     *
     * @return
     */
    public NumberExpression<Integer> yearMonth(){
        if (yearMonth == null) {
            yearMonth = NumberOperation.create(Integer.class, Ops.DateTimeOps.YEAR_MONTH, this);
        }
        return yearMonth;
    }

}

class DateTimeOperation<T extends Comparable<T>> extends DateTimeExpression<T> implements Operation<T> {

    private static final long serialVersionUID = 6523293814317168556L;

    /**
     * Factory method
     *
     * @param <D>
     * @param type
     * @param op
     * @param args
     * @return
     */
    public static <D extends Comparable<D>> DateTimeExpression<D> create(Class<D> type, Operator<? super D> op, Expression<?>... args) {
        return new DateTimeOperation<D>(type, op, Arrays.asList(args));
    }

    private final Operation<T> opMixin;

//    protected DateTimeOperation(Class<T> type, Operator<? super T> op, Expression<?>... args) {
//        this(type, op, Arrays.asList(args));
//    }

    protected DateTimeOperation(Class<T> type, Operator<? super T> op, List<Expression<?>> args) {
        super(type);
        this.opMixin = new OperationImpl<T>(type, op, args);
    }

    @Override
    public <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(this, context);
    }

    @Override
    public Expression<?> getArg(int index) {
        return opMixin.getArg(index);
    }

    @Override
    public List<Expression<?>> getArgs() {
        return opMixin.getArgs();
    }

    @Override
    public Operator<? super T> getOperator() {
        return opMixin.getOperator();
    }

    @Override
    public boolean equals(Object o) {
        return opMixin.equals(o);
    }

    @Override
    public int hashCode() {
        return getType().hashCode();
    }

}
