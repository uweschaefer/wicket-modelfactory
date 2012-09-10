package org.wicketeer.modelfactory;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.RequestCycle;

import static com.google.common.base.Preconditions.checkNotNull;

public class RequestCycleLocalLocal<T>
{
    private MetaDataKey<T> key;

    public RequestCycleLocalLocal(final MetaDataKey<T> key)
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