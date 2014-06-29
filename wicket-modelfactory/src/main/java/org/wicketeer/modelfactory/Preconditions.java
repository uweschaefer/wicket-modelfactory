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

/**
 *
 */
package org.wicketeer.modelfactory;

/**
 * Helper Class to use guava-style code like
 * <code>String lower = checkNotNull(stringParameter).toLowerCase();</code>
 *
 * @author uweschaefer
 */
final class Preconditions {
    /**
     * hide.
     */
    private Preconditions() {
    }

    /**
     * @param t
     *            object to test
     * @return the unchanged reference passed to checkNotNull
     * @throws NullPointerException
     *             if the given object was null
     */
    static <T> T checkNotNull(final T t) {
        if (t == null) {
            throw new NullPointerException();
        }
        return t;
    }

    /**
     * @param t
     *            object to test
     * @return the unchanged reference passed to checkNull
     * @throws IllegalStateException
     *             if the given object was NOT null
     */
    static <T> T checkNull(final T t) {
        if (t != null) {
            throw new IllegalStateException();
        }
        return t;
    }
}
