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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

/**
 * MultiIterator provides a cartesian view on the given iterators
 *
 * <pre>
 * e.g. (1,2) and (100, 200, 300)
 * are expanded to (1, 100) (1, 200) (1, 300) (2, 100) (2, 200) (2, 300)
 * </pre>
 *
 * @author tiwe
 */
public class MultiIterator<T> implements Iterator<Object[]> {

    @Nullable
    private Boolean _hasNext;

    private int index = 0;

    private final List<? extends Iterable<T>> iterables;

    private final List<Iterator<T>> iterators;

    private final boolean[] lastEntry;

    private final Object[] values;

    public MultiIterator(List<? extends Iterable<T>> iterables){
        this.iterables = iterables;
        this.iterators = new ArrayList<Iterator<T>>(iterables.size());
        for (int i = 0; i < iterables.size(); i++) {
            iterators.add(null);
        }
        this.lastEntry = new boolean[iterables.size()];
        this.values = new Object[iterables.size()];
    }

    @Override
    public boolean hasNext() {
        while (_hasNext == null) {
            produceNext();
        }
        return _hasNext.booleanValue();
    }

    @Override
    public Object[] next() {
        while (_hasNext == null) {
            produceNext();
        }
        if (_hasNext.booleanValue()) {
            _hasNext = null;
            return values.clone();
        } else {
            throw new NoSuchElementException();
        }
    }

    private void produceNext() {
        for (int i = index; i < iterables.size(); i++) {
            if (iterators.get(i) == null || (!iterators.get(i).hasNext() && i > 0)) {
                iterators.set(i, iterables.get(i).iterator());
            }
            if (!iterators.get(i).hasNext()) {
                _hasNext = i == 0 ? Boolean.FALSE : null;
                return;
            }
            values[i] = iterators.get(i).next();
            lastEntry[i] = !iterators.get(i).hasNext();
            _hasNext = Boolean.TRUE;
        }
        index = iterables.size() - 1;
        while (lastEntry[index] && index > 0) {
            index--;
        }
    }

    @Override
    public void remove() {
        // do nothing
    }

}
