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

import static com.google.common.base.Preconditions.checkNotNull;

public class RequestCycleLocal<T>
{
    private MetaDataKey<T> key;

    public RequestCycleLocal(final MetaDataKey<T> key)
    {
        this.key = checkNotNull(key);
    }

    public void set(final T t)
    {
        getRequestCycle().setMetaData(key, checkNotNull(t));
    }

    protected RequestCycle getRequestCycle()
    {
        RequestCycle requestCycle = RequestCycle.get();
        if (requestCycle == null)
        {
            throw new IllegalStateException("Outside of request-cycle");
        }
        return requestCycle;
    }

    public T get()
    {
        return getRequestCycle().getMetaData(key);
    }

    public void remove()
    {
        getRequestCycle().setMetaData(key, null);
    }
}
