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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.wicketeer.modelfactory.internal.Argument;
import org.wicketeer.modelfactory.internal.ArgumentsFactory;

import com.googlecode.gentyref.GenericTypeReflector;

public class ModelFactory
{

    private static ChainFrom chain = new ChainFrom();

    @SuppressWarnings("unchecked")
    public static synchronized <T> T from(final T value)
    {
        chain.set(Preconditions.checkNotNull(value));
        return (T) ArgumentsFactory.createArgument(value.getClass());
    }

    public static synchronized <T> T from(final IModel<T> model)
    {
        chain.set(Preconditions.checkNotNull(model));
        return ArgumentsFactory.createArgument(reflectModelObjectType(model));
    }

    @SuppressWarnings("unchecked")
    private static <U> Class<U> reflectModelObjectType(final IModel<U> target) throws Error
    {
        final U targetObject = target.getObject();
        if (targetObject == null)
        {
            final Method getObject;
            try
            {
                getObject = target.getClass().getMethod("getObject");
            }
            catch (final NoSuchMethodException e)
            {
                throw new Error();
            }
            final Type type = GenericTypeReflector.getExactReturnType(getObject, target.getClass());
            final Class<U> reflectedType;
            if (type instanceof Class)
            {
                reflectedType = (Class<U>) type;
            }
            else
                if (type instanceof ParameterizedType)
                {
                    // FIXME isnt that wrong? ->raw
                    reflectedType = (Class<U>) ((ParameterizedType) type).getRawType();
                }
                else
                {
                    throw new UnsupportedOperationException("don't know how to find the type");
                }
            return reflectedType; // can't do anything else here
        }
        else
        {
            return (Class<U>) targetObject.getClass();
        }
    }

    public static synchronized <T> IModel<T> model(final T path)
    {
        Object t = chain.get();
        Argument<T> a = ArgumentsFactory.actualArgument(path);
        String invokedPN = a.getInkvokedPropertyName();
        PropertyModel<T> m = new PropertyModel<T>(t, invokedPN);
        chain.remove();
        return m;
    }


}
