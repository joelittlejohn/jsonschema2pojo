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

package org.jsonschema2pojo.util;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

public class AnnotationHelper {

    private static final String JAVA_8_GENERATED = "javax.annotation.Generated";
    private static final String JAVA_9_GENERATED = "javax.annotation.processing.Generated";
    private static final String GENERATOR_NAME = "jsonschema2pojo";

    public static void tryToAnnotateSilently(JDefinedClass jclass, String annotationClassName) {
        try {
            if (hasTypeAsTarget(annotationClassName)) {
                JClass annotationClass = jclass.owner().ref(annotationClassName);
                jclass.annotate(annotationClass);
            }
        } catch (Exception e) {
            // No-op
        }
    }

    private static boolean tryToAnnotate(JDefinedClass jclass, String annotationClassName) {
        try {
            Class.forName(annotationClassName);
            JClass annotationClass = jclass.owner().ref(annotationClassName);
            JAnnotationUse generated = jclass.annotate(annotationClass);
            generated.param("value", GENERATOR_NAME);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }

    }

    public static void addGeneratedAnnotation(JDefinedClass jclass) {
        if (!tryToAnnotate(jclass, JAVA_9_GENERATED)) {
            tryToAnnotate(jclass, JAVA_8_GENERATED);
        }
    }

    /**
     * @return Is the annotation we are trying to use a valid one, i.e. can it be used on class level,
     * i.e. has it TYPE within the Target values?
     */
    private static boolean hasTypeAsTarget(String annotationClassName) {
        Class<?> clazz;
        try {
            clazz = Class.forName(annotationClassName);
        } catch (Exception e) {
            return false;
        }

        Target annotation = clazz.getAnnotation(Target.class);
        if (annotation == null) {
            return false;
        }

        for (ElementType elementType : annotation.value()) {
            if (ElementType.TYPE.equals(elementType)) {
                return true;
            }
        }
        return false;
    }

}
