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

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JDefinedClass;
import org.jsonschema2pojo.GenerationConfig;

public class AnnotationHelper {

    private static final String JAVA_8_GENERATED = "javax.annotation.Generated";
    private static final String JAVA_9_GENERATED = "javax.annotation.processing.Generated";
    private static final String GENERATOR_NAME = "jsonschema2pojo";

    private static boolean tryToAnnotate(JDefinedClass jclass, String annotationClassName) {
        try {
            Class.forName(annotationClassName);
            AbstractJClass annotationClass = jclass.owner().ref(annotationClassName);
            JAnnotationUse generated = jclass.annotate(annotationClass);
            generated.param("value", GENERATOR_NAME);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }

    }

    public static void addGeneratedAnnotation(GenerationConfig config, JDefinedClass jclass) {
        if (JavaVersion.is9OrLater(config.getTargetVersion())) {
            tryToAnnotate(jclass, JAVA_9_GENERATED);
        } else {
            tryToAnnotate(jclass, JAVA_8_GENERATED);
        }
    }

}
