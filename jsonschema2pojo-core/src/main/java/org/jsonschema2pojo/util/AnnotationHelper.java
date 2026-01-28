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

import org.jsonschema2pojo.GenerationConfig;
import static net.mfjassociates.tools.JaCoCoGenerated.JACOCO_GENERATED_CLASS_NAME;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;

public class AnnotationHelper {

    private static final String JAVA_8_GENERATED = "javax.annotation.Generated";
    private static final String JAVA_9_GENERATED = "javax.annotation.processing.Generated";
    private static final String GENERATOR_NAME = "jsonschema2pojo";

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

    /**
     * Annotate class with custom annotation.  Do not test for annotation class existence since
     * this annotation type will be included in the JCodeModel that is generated and is not
     * available at this time.
     * 
     * @param jclass - The class that needs to have the annotation added to it.
     * @param annotationClassName - the annotation type to add
     */
    private static void annotateCustom(JDefinedClass jclass, String annotationClassName) {
        JClass annotationClass = jclass.owner().ref(annotationClassName);
        jclass.annotate(annotationClass);
    }

    public static void addGeneratedAnnotation(GenerationConfig config, JDefinedClass jclass) {
        if (JavaVersion.is9OrLater(config.getTargetVersion())) {
            tryToAnnotate(jclass, JAVA_9_GENERATED);
        } else {
            tryToAnnotate(jclass, JAVA_8_GENERATED);
        }
    }

    public static void addRuntimeGeneratedAnnotation(GenerationConfig config, JDefinedClass jclass) {
    	annotateCustom(jclass, jclass.getPackage().name()+"."+JACOCO_GENERATED_CLASS_NAME);
    }

}
