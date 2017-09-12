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

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Config defines serialization flags for annotated domain types and packages
 *
 * @author tiwe
 *
 */
@Documented
@Target({PACKAGE,TYPE})
@Retention(RUNTIME)
public @interface Config {

    /**
     * Created entity field initialization accessors
     *
     * @return
     */
    boolean entityAccessors() default false;

    /**
     * Create accessors for indexed list access
     *
     * @return
     */
    boolean listAccessors() default false;

    /**
     * Create accessors for keyed map access
     *
     * @return
     */
    boolean mapAccessors() default false;

    /**
     * Create default variable in query type
     *
     * @return
     */
    boolean createDefaultVariable() default true;

}
