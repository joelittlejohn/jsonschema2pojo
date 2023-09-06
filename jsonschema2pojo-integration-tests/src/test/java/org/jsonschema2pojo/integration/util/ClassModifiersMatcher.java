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

import java.lang.reflect.Modifier;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher that is successful if the provided class has given modifiers.
 */
public class ClassModifiersMatcher extends TypeSafeMatcher<Class<?>> {
    private final int modifiers;

    /**
     * Create a new matcher for the given modifiers.
     *
     * @param modifiers
     *         the modifiers to be matched
     */
    public ClassModifiersMatcher(int modifiers) {
        if ((modifiers | Modifier.classModifiers()) != Modifier.classModifiers()) {
            throw new IllegalArgumentException("Invalid class modifiers");
        }
        this.modifiers = modifiers;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("the class has the following modifiers: " + Modifier.toString(modifiers));
    }

    @Override
    protected void describeMismatchSafely(Class clazz, Description mismatchDescription) {
        final int matchedModifiers = clazz.getModifiers() & modifiers;
        if (matchedModifiers != modifiers) {
            mismatchDescription.appendText("the class does not have following modifiers: "
                    + Modifier.toString(matchedModifiers ^ modifiers));
        }
    }

    @Override
    protected boolean matchesSafely(Class<?> clazz) {
        final int matchedModifiers = clazz.getModifiers() & modifiers;
        return matchedModifiers == modifiers;
    }

    /**
     * Create a new matcher for the given modifiers.
     *
     * @param modifiers
     *         the modifiers to be matched
     */
    public static Matcher<Class<?>> hasModifiers(int modifiers) {
        return new ClassModifiersMatcher(modifiers);
    }

}
