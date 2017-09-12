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

import java.math.BigDecimal;
import java.math.BigInteger;

public final class MathUtils {

    private MathUtils(){}

    @SuppressWarnings("unchecked")
    public static <D extends Number & Comparable<?>> D sum(D num1, Number num2){
        BigDecimal res = new BigDecimal(num1.toString()).add(new BigDecimal(num2.toString()));
        return MathUtils.<D>cast(res, (Class<D>)num1.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <D extends Number & Comparable<?>> D difference(D num1, Number num2){
        BigDecimal res = new BigDecimal(num1.toString()).subtract(new BigDecimal(num2.toString()));
        return MathUtils.<D>cast(res, (Class<D>)num1.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <D extends Number & Comparable<?>> D cast(Number num, Class<D> type){
        Number rv = null;
        if (type.equals(Byte.class)) {
            rv = num.byteValue();
        } else if (type.equals(Double.class)) {
            rv = num.doubleValue();
        } else if (type.equals(Float.class)) {
            rv = num.floatValue();
        } else if (type.equals(Integer.class)) {
            rv = num.intValue();
        } else if (type.equals(Long.class)) {
            rv = num.longValue();
        } else if (type.equals(Short.class)) {
            rv = num.shortValue();
        } else if (type.equals(BigDecimal.class)) {
            if (num instanceof BigDecimal) {
                rv = num;
            } else{
                rv = new BigDecimal(num.toString());
            }
        } else if (type.equals(BigInteger.class)){
            if (num instanceof BigInteger) {
                rv = num;
            } else if (num instanceof BigDecimal) {
                rv = ((BigDecimal)num).toBigInteger();
            } else {
                rv = new BigInteger(num.toString());
            }
        } else {
            throw new IllegalArgumentException(String.format("Illegal type : %s", type.getSimpleName()));
        }
        return (D) rv;
    }
}
