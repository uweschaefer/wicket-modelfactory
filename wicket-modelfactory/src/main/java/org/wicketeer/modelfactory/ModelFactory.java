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

/**
 * Entry point for creating refacotring safe PropertyModels. Usage:<code>
 * IModel<String> stringModel = model(from(person).getProfile().getName());
 * </code> where person can be a Bean of a Person class or an IModel<Person>.
 */
public class ModelFactory
{

    private static RequestCycleLocalFrom localFrom = new RequestCycleLocalFrom();

    /**
     * @param <T>
     *            the type of the parameter
     * @param value
     *            the object to be proxied
     * @return a proxy of the value-object
     */
    @SuppressWarnings("unchecked")
    public static synchronized <T> T from(final T value)
    {
        localFrom.set(Preconditions.checkNotNull(value));
        return (T) ArgumentsFactory.createArgument(value.getClass());
    }

    /**
     * @param <T>
     *            type of the model parameter
     * @param model
     *            the model from which to create a proxy
     * @return
     * @throws NullPointerException
     *             if the model parameter is null
     */
    public static synchronized <T> T from(final IModel<T> model)
    {
        Preconditions.checkNotNull(model);
        localFrom.set(Preconditions.checkNotNull(model));
        return ArgumentsFactory.createArgument(reflectModelObjectType(model));
    }

    /**
     * kudos to duesklipper for this neat idea.
     */
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

    /**
     * creates an actual ProeprtyModel from the path expressed by the given object.
     * @param path the object initially created by a from-call
     * @return the actual Model
     */
    public static synchronized <T> IModel<T> model(final T path)
    {
        Object t = localFrom.get();
        Argument<T> a = ArgumentsFactory.actualArgument(path);
        String invokedPN = a.getInkvokedPropertyName();
        PropertyModel<T> m = new PropertyModel<T>(t, invokedPN);
        localFrom.remove();
        return m;
    }

}
