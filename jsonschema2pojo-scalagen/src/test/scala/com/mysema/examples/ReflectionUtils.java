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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;


/**
 * @author tiwe
 *
 */
public final class ReflectionUtils {

//    private static final AnnotatedElement EMPTY = new Annotations();

    private ReflectionUtils(){}

    public static AnnotatedElement getAnnotatedElement(Class<?> beanClass, String propertyName, Class<?> propertyClass) {
        Field field = getFieldOrNull(beanClass, propertyName);
        Method method = getGetterOrNull(beanClass, propertyName, propertyClass);
        if (field == null || field.getAnnotations().length == 0) {
            return (method != null && method.getAnnotations().length > 0) ? method : method;
        } else if (method == null || method.getAnnotations().length == 0) {
            return field;
        } else {
            //return new Annotations(field, method);
            return null;
        }
    }

    @Nullable
    private static Field getFieldOrNull(Class<?> clazz, String propertyName) {
        Class<?> beanClass = clazz;
        while (beanClass != null && !beanClass.equals(Object.class)) {
            try {
                return beanClass.getDeclaredField(propertyName);
            } catch (SecurityException | NoSuchFieldException e) {
                // skip
            }
            beanClass = beanClass.getSuperclass();
        }
        return null;
    }

    @Nullable
    public static Method getGetterOrNull(Class<?> clazz, String name, Class<?> type){
        Class<?> beanClass = clazz;
        String methodName = ((type.equals(Boolean.class) || type.equals(boolean.class)) ? "is" : "get") + name;
        while(beanClass != null && !beanClass.equals(Object.class)){
            try {
                return beanClass.getDeclaredMethod(methodName);
            } catch (SecurityException | NoSuchMethodException e) { // skip
            }
            beanClass = beanClass.getSuperclass();
        }
        return null;

    }

    public static int getTypeParameterCount(java.lang.reflect.Type type){
        if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getActualTypeArguments().length;
        }else{
            return 0;
        }
    }

    @Nullable
    public static Class<?> getTypeParameter(java.lang.reflect.Type type, int index) {
        if (type instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) type;
            java.lang.reflect.Type[] targs = ptype.getActualTypeArguments();
            if (targs[index] instanceof WildcardType) {
                WildcardType wildcardType = (WildcardType) targs[index];
                if (wildcardType.getUpperBounds()[0] instanceof Class){
                    return (Class<?>) wildcardType.getUpperBounds()[0];
                }else if (wildcardType.getUpperBounds()[0] instanceof ParameterizedType){
                    return (Class<?>) ((ParameterizedType) wildcardType.getUpperBounds()[0]).getRawType();
                }else{
                    return Object.class;
                }

            } else if (targs[index] instanceof TypeVariable<?>) {
                return (Class<?>) ((TypeVariable<?>) targs[index]).getGenericDeclaration();
            } else if (targs[index] instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) targs[index]).getRawType();
            } else {
                return (Class<?>) targs[index];
            }
        }
        return null;
    }
    
    public static Set<Class<?>> getSuperClasses(Class<?> cl) {
        Set<Class<?>> classes = new HashSet<>();
        Class<?> c = cl;
        while (c != null) {
            classes.add(c);
            c = c.getSuperclass();
        }
        return classes;
    }
    
    public static Set<Field> getFields(Class<?> cl) {
        Set<Field> fields = new HashSet<>();
        Class<?> c = cl;
        while (c != null) {
            for (Field field : c.getDeclaredFields()) {
                fields.add(field);
            }
            c = c.getSuperclass();
        }
        return fields;
    }
    
    public static Set<Class<?>> getImplementedInterfaces(Class<?> cl){
        Set<Class<?>> interfaces = new HashSet<>();
        Deque<Class<?>> classes = new ArrayDeque<>();
        classes.add(cl);
        while (!classes.isEmpty()) {
            Class<?> c = classes.pop();
            interfaces.addAll(Arrays.asList(c.getInterfaces()));
            if (c.getSuperclass() != null) {
                classes.add(c.getSuperclass());
            }
            classes.addAll(Arrays.asList(c.getInterfaces()));
        }
        return interfaces;
    }

}
