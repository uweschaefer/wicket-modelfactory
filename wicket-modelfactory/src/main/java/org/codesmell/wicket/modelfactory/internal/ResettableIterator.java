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
 * An Iterator that can reset its cursor to its initial position
 * @author Mario Fusco
 */
public abstract class ResettableIterator<T> implements Iterator<T> {

    /**
     * Resets the cursor of this Iterator to its initial position
     */
    public abstract void reset();

    /**
     * {@inheritDoc}
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
