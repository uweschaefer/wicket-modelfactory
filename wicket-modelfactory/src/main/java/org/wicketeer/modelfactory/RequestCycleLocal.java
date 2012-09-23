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

package org.wicketeer.modelfactory;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.cycle.RequestCycle;

import static org.wicketeer.modelfactory.Preconditions.checkNotNull;

/**
 * Stores an object with the given key into the RequestCycle. This works
 * basically like a ThreadLocal but uses Wicket's RequestCycle instead of the
 * local Thread as a Context, so that at the end of request-cycle processing,
 * the object is dropped and cannot pop up in another Request (which ThreadLocal
 * could suffer from).
 * 
 * @author uweschaefer
 * @param <T>
 *            Type of the object to store in the RequestCycle.
 */
public class RequestCycleLocal<T>
{
    private final MetaDataKey<T> key;

    /**
     * @param key
     *            used to store the RequestCycleLocal-Object
     * @throws NullPointerException
     *             if the given key is null
     */
    public RequestCycleLocal(final MetaDataKey<T> key)
    {
        this.key = checkNotNull(key);
    }

    /**
     * subclasses migt have a different idea how to get the requestCycle.
     * 
     * @return the currently active RequestCycle
     * @throws IllegalStateException
     *             if there currently is no active RequestCycle. (Remember to
     *             use WicketTester in unit-tests)
     */
    protected RequestCycle getRequestCycle()
    {
        RequestCycle requestCycle = RequestCycle.get();
        if (requestCycle == null)
        {
            throw new IllegalStateException("Outside of request-cycle");
        }
        return requestCycle;
    }

    /**
     * Set the given object into the RequestCycle's Metadata with the key pass
     * on construction.
     * 
     * @param t
     *            the object to set
     * @throws NullPointerException
     *             if the given object is null. (use remove instead)
     */
    public void set(final T t)
    {
        getRequestCycle().setMetaData(key, checkNotNull(t));
    }

    /**
     * @return the formerly set Object, or null if nothing was not set or the
     *         object was removed.
     */
    public T get()
    {
        return getRequestCycle().getMetaData(key);
    }

    /**
     * removes the object from the request cycle.
     */
    public void remove()
    {
        getRequestCycle().setMetaData(key, null);
    }
}
