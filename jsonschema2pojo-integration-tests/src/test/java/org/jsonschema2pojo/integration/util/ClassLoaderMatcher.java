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

import java.util.Objects;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher that is successful if the provided class can be loaded with the given classloader.
 */
public class ClassLoaderMatcher extends TypeSafeMatcher<ClassLoader> {
    private final String className;

    /**
     * Create a new matcher for the given binary class name.
     *
     * @param className
     *         the binary name of the class
     */
    public ClassLoaderMatcher(String className) {
        this.className = Objects.requireNonNull(className);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("the class \"" + className + "\" can be loaded");
    }

    @Override protected void describeMismatchSafely(ClassLoader item, Description mismatchDescription) {
        mismatchDescription.appendText("the class \"" + className + "\" could not be loaded");
    }

    @Override
    protected boolean matchesSafely(ClassLoader s) {
        try {
            s.loadClass(className);
        } catch (final ReflectiveOperationException e) {
            return false;
        }

        return true;
    }

    /**
     * Create a new matcher for the given binary class name.
     *
     * @param className
     *         the binary name of the class
     */
    public static ClassLoaderMatcher canLoad(String className) {
        return new ClassLoaderMatcher(className);
    }
}
