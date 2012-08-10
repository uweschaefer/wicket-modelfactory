//
//
// Copyright 2012-2012 Uwe Schäfer <uwe@codesmell.de>
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package org.codesmell.wicket.modelfactory.internal;

import java.util.*;

/**
 * A ResettabkeIterator that iterates over a wrapped Iterable
 * @author Mario Fusco
 */
public class ResettableIteratorOnIterable<T> extends ResettableIterator<T> {

    private final Iterable<T> iterable;

    private Iterator<T> iterator;

    /**
     * Creates a ResettableIterator that wraps the given Iterable
     * @param iterable The Iterable to be wrapped
     */
    public ResettableIteratorOnIterable(Iterable<T> iterable) {
        this.iterable = iterable;
        reset();
    }

    /**
     * {@inheritDoc}
     */
    public final void reset() {
        iterator = iterable.iterator();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    public T next() {
        return iterator.next();
    }
}
