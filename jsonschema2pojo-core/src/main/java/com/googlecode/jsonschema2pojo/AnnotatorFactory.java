/**
 * Copyright © 2010-2011 Nokia
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

package com.googlecode.jsonschema2pojo;

/**
 * Factory object for creating {@link Annotater}s for all the supported
 * annotation styles.
 */
public class AnnotatorFactory {

    /**
     * Create a new {@link Annotator} that can create annotations according to
     * the given style.
     * 
     * @param style
     *            the annotation style that dictates what kind of annotations
     *            are required.
     */
    public Annotator getAnnotator(AnnotationStyle style) {

        switch (style) {
            case JACKSON:
            case JACKSON2:
                return new Jackson2Annotator();
            case JACKSON1:
                return new Jackson1Annotator();
            case NONE:
                return new NoopAnnotator();
            default:
                throw new IllegalArgumentException("Unrecognised annotation style: " + style);
        }

    }
}
