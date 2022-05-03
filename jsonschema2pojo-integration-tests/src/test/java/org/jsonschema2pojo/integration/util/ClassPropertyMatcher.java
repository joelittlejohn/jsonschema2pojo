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

package org.jsonschema2pojo.integration.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Objects;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher that is successful if the provided class has a property of a given type.
 */
public class ClassPropertyMatcher extends TypeSafeMatcher<Class<?>> {
    private final String propertyName;
    private final String propertyTypeBinaryName;

    /**
     * Create a new matcher for the given property name and type.
     *
     * @param propertyName
     *         the property name
     * @param propertyTypeBinaryName
     *         the expected binary name of the property type
     */
    public ClassPropertyMatcher(String propertyName, String propertyTypeBinaryName) {
        this.propertyName = Objects.requireNonNull(propertyName);
        this.propertyTypeBinaryName = Objects.requireNonNull(propertyTypeBinaryName);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("the class to contain property \"" + propertyName + "\" of type \"" + propertyTypeBinaryName + "\"");
    }

    @Override
    protected void describeMismatchSafely(Class clazz, Description mismatchDescription) {
        final Class<?> propertyType = findPropertyType(clazz, propertyName);
        if (propertyType == null) {
            mismatchDescription.appendText("the property \"" + propertyName + "\" could not be found");
            return;
        }

        if (!propertyTypeBinaryName.equals(propertyType.getName())) {
            mismatchDescription.appendText("the property type was \"" + propertyType.getName() + "\"");
        }
    }

    @Override
    protected boolean matchesSafely(Class<?> clazz) {
        final Class<?> propertyType = findPropertyType(clazz, propertyName);
        return propertyType != null
                && propertyType.getName().equals(this.propertyTypeBinaryName);
    }

    protected Class<?> findPropertyType(Class<?> clazz, String propertyName) {
        try {
            final PropertyDescriptor propertyDescriptor = new PropertyDescriptor(propertyName, clazz);
            return propertyDescriptor.getPropertyType();
        } catch (final IntrospectionException e) {
            return null;
        }
    }

    /**
     * Create a new matcher for the given property name and type.
     *
     * @param propertyName
     *         the property name
     * @param propertyType
     *         the expected property type
     */
    public static Matcher<Class<?>> hasProperty(String propertyName, Class<?> propertyType) {
        return new ClassPropertyMatcher(propertyName, propertyType.getName());
    }

    /**
     * Create a new matcher for the given property name and type.
     *
     * @param propertyName
     *         the property name
     * @param propertyTypeBinaryName
     *         the expected binary name of the property type
     */
    public static Matcher<Class<?>> hasProperty(String propertyName, String propertyTypeBinaryName) {
        return new ClassPropertyMatcher(propertyName, propertyTypeBinaryName);
    }
}
