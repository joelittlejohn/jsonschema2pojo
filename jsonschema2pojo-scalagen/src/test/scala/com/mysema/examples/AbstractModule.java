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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author tiwe
 *
 */
public abstract class AbstractModule {

    private final Map<Class<?>, Object> instances = new HashMap<>();

    private final Map<Class<?>, Class<?>> bindings = new HashMap<>();

    private final Map<String, Object> namedInstances = new HashMap<>();

    private final Map<String, Class<?>> namedBindings = new HashMap<>();

    public AbstractModule() {
        configure();
    }

    public final <T> AbstractModule bind(Class<T> clazz){
        if (clazz.isInterface()) {
            throw new IllegalArgumentException("Interfaces can't be instantiated");
        }
        bind(clazz, clazz);
        return this;
    }

    public final <T> AbstractModule bind(String name, Class<? extends T> implementation){
        namedBindings.put(name, implementation);
        return this;
    }

    public final <T> AbstractModule bind(String name, T implementation){
        namedInstances.put(name, implementation);
        return this;
    }

    public final <T> AbstractModule bind(Class<T> iface, Class<? extends T> implementation){
        bindings.put(iface, implementation);
        return this;
    }

    public final <T> AbstractModule bind(Class<T> iface, T implementation){
        instances.put(iface, implementation);
        return this;
    }

    protected abstract void configure();

    @SuppressWarnings("unchecked")
    public final <T> T get(Class<T> iface){
        if (instances.containsKey(iface)) {
            return (T)instances.get(iface);
        } else if (bindings.containsKey(iface)) {
            Class<?> implementation = bindings.get(iface);
            T instance = (T)createInstance(implementation);
            instances.put(iface, instance);
            return instance;
        } else {
            throw new IllegalArgumentException(iface.getName() + " is not registered");
        }
    }

    @SuppressWarnings("unchecked")
    public final <T> T get(Class<T> iface, String name){
        if (namedInstances.containsKey(name)) {
            return (T)namedInstances.get(name);
        } else if (namedBindings.containsKey(name)) {
            Class<?> implementation = namedBindings.get(name);
            if (implementation != null) {
                T instance = (T)createInstance(implementation);
                namedInstances.put(name, instance);
                return instance;    
            } else {
                return (T)null;
            }            
        } else {
            throw new IllegalArgumentException(iface.getName() + " " + name + " is not registered");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(Class<? extends T> implementation) {
        Constructor<?> constructor = null;
        for (Constructor<?> c : implementation.getConstructors()) {
            if (c.getAnnotation(Inject.class) != null) {
                constructor = c;
                break;
            }
        }

        // fallback to default constructor
        if (constructor == null) {
            try {
                // FIXME
                constructor = implementation.getConstructor(Object.class);
            } catch (SecurityException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        if (constructor != null) {
            Object[] args = new Object[constructor.getParameterTypes().length];
            for (int i = 0; i < constructor.getParameterTypes().length; i++) {
                Named named = getNamedAnnotation(constructor.getParameterAnnotations()[i]);
                if (named != null) {
                    args[i] = get(constructor.getParameterTypes()[i], named.value());
                } else {
                    args[i] = get(constructor.getParameterTypes()[i]);
                }
            }
            try {
                return (T) constructor.newInstance(args);

            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        } else {
            throw new IllegalArgumentException("Got no annotated constructor for " + implementation.getName());
        }

    }

    private Named getNamedAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(Named.class)) {
                return (Named)annotation;
            }
        }
        return null;
    }



}
