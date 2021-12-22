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

package org.jsonschema2pojo.rules;

import jakarta.validation.constraints.*;
import org.jsonschema2pojo.test.JUnitTestBase;

import java.lang.annotation.Annotation;

public abstract class AnnotationTestBase extends JUnitTestBase {

    protected static Class<? extends Annotation> getSizeClass(boolean useJakartaValidation) {
        return useJakartaValidation ? Size.class : javax.validation.constraints.Size.class;
    }

    protected static Class<? extends Annotation> getPatterClass(boolean useJakartaValidation) {
        return useJakartaValidation ? Pattern.class : javax.validation.constraints.Pattern.class;
    }

    protected static Class<? extends Annotation> getDecimalMaxClass(boolean useJakartaValidation) {
        return useJakartaValidation ? DecimalMax.class : javax.validation.constraints.DecimalMax.class;
    }

    protected static Class<? extends Annotation> getDecimalMinClass(boolean useJakartaValidation) {
        return useJakartaValidation ? DecimalMin.class : javax.validation.constraints.DecimalMin.class;
    }

    protected static Class<? extends Annotation> getDigitsClass(boolean useJakartaValidation) {
        return useJakartaValidation ? Digits.class : javax.validation.constraints.Digits.class;
    }

}
