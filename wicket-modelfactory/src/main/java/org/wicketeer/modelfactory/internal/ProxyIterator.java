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

package org.wicketeer.modelfactory.internal;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Proxies a list of objects in order to seamlessly iterate on them by exposing
 * the API of a single object.
 * 
 * @author Mario Fusco
 * @author Mattias Jiderhamn, adding ability to disable or enable
 */
public class ProxyIterator<T> extends InvocationInterceptor implements Iterable<T>
{

    private final ResettableIterator<? extends T> proxiedIterator;

    /**
     * Set to true (default) the interceptor will work on the proxiedIterator,
     * if set to false it will ignore any method invocations.
     */
    protected boolean enabled = true;

    /**
     * Creates a proxy that wraps the given Iterator in order to seamlessly
     * iterate on them by exposing the API of a single object
     * 
     * @param proxiedIterator
     *            The Iterator to be proxied
     */
    protected ProxyIterator(final ResettableIterator<? extends T> proxiedIterator)
    {
        this.proxiedIterator = proxiedIterator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(final Object obj, final Method method, final Object[] args)
    {
        if (method.getName().equals("iterator"))
        {
            return iterator();
        }
        if (enabled)
        {
            return createProxyIterator(iterateOnValues(method, args), (Class<Object>) method.getReturnType());
        }
        return null;
    }

    /**
     * Invokes the given method with the given arguments on all the object in
     * the iterator wrapped by this proxy
     * 
     * @param method
     *            The method to be invoked
     * @param args
     *            The arguments used to invoke the given method
     * @return An Iterator over the results on all the invoctions of the given
     *         method
     */
    protected ResettableIterator<Object> iterateOnValues(final Method method, final Object[] args)
    {
        if (method.getName().equals("finalize"))
        {
            return null;
        }
        method.setAccessible(true);
        proxiedIterator.reset();
        List<Object> list = new LinkedList<Object>();
        while (proxiedIterator.hasNext())
        {
            try
            {
                list.add(method.invoke(proxiedIterator.next(), args));
            }
            catch (Exception e)
            {
                Throwable cause = e.getCause();
                if (cause != null)
                {
                    if (cause instanceof RuntimeException)
                    {
                        throw (RuntimeException) cause;
                    }
                    else
                    {
                        throw new RuntimeException(cause);
                    }
                }
                else
                {
                    throw new RuntimeException(e);
                }
            }
        }
        return new ResettableIteratorOnIterable(list);
    }

    /**
     * Creates a ProxyIterator of the given class that wraps the given Iterator
     * 
     * @param proxiedIterator
     *            The Iterator to be proxied
     * @param clazz
     *            The class dinamically implemented by the newly created proxy
     * @return The newly created proxy
     */
    public static <T> T createProxyIterator(final ResettableIterator<? extends T> proxiedIterator, final Class<T> clazz)
    {
        return ProxyUtil.createIterableProxy(new ProxyIterator<T>(proxiedIterator), clazz);
    }

    /**
     * Creates a ProxyIterator of the same class of the given item that wraps
     * the given Iterator
     * 
     * @param proxiedIterator
     *            The Iterator to be proxied
     * @param firstItem
     *            An instance of the class dinamically implemented by the newly
     *            created proxy
     * @return The newly created proxy
     */
    public static <T> T createProxyIterator(final ResettableIterator<? extends T> proxiedIterator, final T firstItem)
    {
        T proxy = createProxyIterator(proxiedIterator, (Class<T>) firstItem.getClass());
        proxiedIterator.reset();
        return proxy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator()
    {
        return (Iterator<T>) proxiedIterator;
    }
}
