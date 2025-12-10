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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.AnnotationStyle.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AnnotatorFactoryTest {

    private final AnnotatorFactory factory = new AnnotatorFactory(new DefaultGenerationConfig());

    @Test
    public void canCreateCorrectAnnotatorFromAnnotationStyle() {

        assertThat(factory.getAnnotator(JACKSON), is(instanceOf(Jackson2Annotator.class)));
        assertThat(factory.getAnnotator(JACKSON2), is(instanceOf(Jackson2Annotator.class)));
        assertThat(factory.getAnnotator(JACKSON3), is(instanceOf(Jackson3Annotator.class)));
        assertThat(factory.getAnnotator(GSON), is(instanceOf(GsonAnnotator.class)));
        assertThat(factory.getAnnotator(MOSHI1), is(instanceOf(Moshi1Annotator.class)));
        assertThat(factory.getAnnotator(NONE), is(instanceOf(NoopAnnotator.class)));

    }

    @Test
    public void canCreateCorrectAnnotatorFromClass() {

        assertThat(factory.getAnnotator(Jackson2Annotator.class), is(instanceOf(Jackson2Annotator.class)));

    }

    @Test
    public void canCreateCompositeAnnotator() {

        Annotator annotator1 = Mockito.mock(Annotator.class);
        Annotator annotator2 = Mockito.mock(Annotator.class);

        CompositeAnnotator composite = factory.getAnnotator(annotator1, annotator2);

        assertThat(composite.annotators.length, equalTo(2));
        assertThat(composite.annotators[0], is(equalTo(annotator1)));
        assertThat(composite.annotators[1], is(equalTo(annotator2)));

    }

    /**
     * Test uses reflection to get passed the generic type constraints and
     * invoke as if invoked through typical configuration.
     */
    @Test
    public void attemptToCreateAnnotatorFromIncompatibleClassCausesIllegalArgumentException() throws Throwable {
        Method factoryMethod = AnnotatorFactory.class.getMethod("getAnnotator", Class.class);
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> factoryMethod.invoke(factory, String.class));
        assertThat(exception.getTargetException(), is(instanceOf(IllegalArgumentException.class)));
        assertThat(
                exception.getTargetException().getMessage(),
                is(equalTo("The class name given as a custom annotator (java.lang.String) does not refer to a class that implements org.jsonschema2pojo.Annotator")));
    }

}
