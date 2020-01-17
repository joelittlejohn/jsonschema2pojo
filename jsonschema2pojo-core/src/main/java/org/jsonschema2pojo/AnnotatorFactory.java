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

package org.jsonschema2pojo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Factory object for creating {@link Annotator}s for all the supported
 * annotation styles.
 */
public class AnnotatorFactory {

    private final GenerationConfig generationConfig;

    public AnnotatorFactory(GenerationConfig generationConfig) {
        this.generationConfig = generationConfig;
    }

    /**
     * Create a new {@link Annotator} that can create annotations according to
     * the given style.
     *
     * @param style
     *            the annotation style that dictates what kind of annotations
     *            are required.
     * @return an annotator matching to given style
     */
    public Annotator getAnnotator(AnnotationStyle style) {

        switch (style) {
            case JACKSON:
            case JACKSON2:
                return new Jackson2Annotator(generationConfig);
            case JACKSON1:
                return new Jackson1Annotator(generationConfig);
            case GSON:
                return new GsonAnnotator(generationConfig);
            case MOSHI1:
                return new Moshi1Annotator(generationConfig);
            case NONE:
                return new NoopAnnotator();
            default:
                throw new IllegalArgumentException("Unrecognised annotation style: " + style);
        }

    }

    /**
     * Create a new custom {@link Annotator} from the given class.
     *
     * @param clazz
     *            A class implementing {@link Annotator}.
     * @return an instance of the given annotator type
     */
    public Annotator getAnnotator(Class<? extends Annotator> clazz) {

        if (!Annotator.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("The class name given as a custom annotator (" + clazz.getName() + ") does not refer to a class that implements " + Annotator.class.getName());
        }

        try {
            try {
                Constructor<? extends Annotator> constructor = clazz.getConstructor(GenerationConfig.class);
                return constructor.newInstance(generationConfig);
            } catch (NoSuchMethodException e) {
                return clazz.newInstance();
            }
        } catch (InvocationTargetException | InstantiationException e) {
            throw new IllegalArgumentException("Failed to create a custom annotator from the given class. An exception was thrown on trying to create a new instance.", e.getCause());
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Failed to create a custom annotator from the given class. It appears that we do not have access to this class - is both the class and its no-arg constructor marked public?", e);
        }

    }

    public CompositeAnnotator getAnnotator( Annotator... annotators ) {
        return new CompositeAnnotator(annotators);
    }

}
