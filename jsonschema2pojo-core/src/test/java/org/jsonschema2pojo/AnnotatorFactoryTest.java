/**
 * Copyright © 2010-2014 Nokia
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

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.AnnotationStyle.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

public class AnnotatorFactoryTest {

    private AnnotatorFactory factory = new AnnotatorFactory(new DefaultGenerationConfig());

    @Test
    public void canCreateCorrectAnnotatorFromAnnotationStyle() {

        assertThat(factory.getAnnotator(JACKSON1), is(instanceOf(Jackson1Annotator.class)));
        assertThat(factory.getAnnotator(JACKSON), is(instanceOf(Jackson2Annotator.class)));
        assertThat(factory.getAnnotator(JACKSON2), is(instanceOf(Jackson2Annotator.class)));
        assertThat(factory.getAnnotator(GSON), is(instanceOf(GsonAnnotator.class)));
        assertThat(factory.getAnnotator(MOSHI1), is(instanceOf(Moshi1Annotator.class)));
        assertThat(factory.getAnnotator(NONE), is(instanceOf(NoopAnnotator.class)));

    }

    @Test
    public void canCreateCorrectAnnotatorFromClass() {

        assertThat(factory.getAnnotator(Jackson1Annotator.class), is(instanceOf(Jackson1Annotator.class)));

    }

    @Test
    public void canCreateCompositeAnnotator() {

        Annotator annotator1 = mock(Annotator.class);
        Annotator annotator2 = mock(Annotator.class);

        CompositeAnnotator composite = factory.getAnnotator(annotator1, annotator2);

        assertThat(composite.annotators.length, equalTo(2));
        assertThat(composite.annotators[0], is(equalTo(annotator1)));
        assertThat(composite.annotators[1], is(equalTo(annotator2)));

    }

    /**
     * Test uses reflection to get passed the generic type constraints and
     * invoke as if invoked through typical configuration.
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToCreateAnnotatorFromIncompatibleClassCausesIllegalArgumentException() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        Method factoryMethod = AnnotatorFactory.class.getMethod("getAnnotator", Class.class);
        factoryMethod.invoke(String.class);

    }

}
