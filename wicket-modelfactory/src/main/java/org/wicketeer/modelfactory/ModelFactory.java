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
 * </code> where person can be an instance of Person class or an IModel<Person>.
 */
public class ModelFactory
{

    private static RequestCycleLocalFrom localFrom = new RequestCycleLocalFrom();

    /**
     * Proxies the given objetc in order to be able to call methods on it to
     * create the property path later-on used by model().
     * 
     * @param <T>
     *            the type of the parameter
     * @param value
     *            the object to be proxied
     * @return a proxy of the value-object
     * @throws NullPointerException
     *             if the given object is null
     */
    @SuppressWarnings("unchecked")
    public static <T> T from(final T value)
    {
        localFrom.set(Preconditions.checkNotNull(value));
        return (T) ArgumentsFactory.createArgument(value.getClass());
    }

    /**
     * Proxies the Model-Object's type in order to be able to call methods on it
     * to create the property path later-on used by model().
     * 
     * @param <T>
     *            type of the model parameter
     * @param model
     *            the model from which to create a proxy
     * @return a proxy of an object of Type <T>
     * @throws NullPointerException
     *             if the model is null
     */
    public static <T> T from(final IModel<T> model)
    {
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
     * creates an actual ProeprtyModel from the path expressed by the given
     * object.
     * 
     * @param path
     *            the object initially created by a from-call
     * @return the actual Model
     */
    public static <T> IModel<T> model(final T path)
    {
        Object t = localFrom.get();
        return new PropertyModel<T>(t, path(path));
    }

    /**
     * @param path
     *            the object initially created by a from-call
     * @return a string denoting the property path expressed by the path object
     */
    public static String path(final Object path)
    {
        try
        {
            Argument<?> a = ArgumentsFactory.actualArgument(path);
            return a.getInkvokedPropertyName();
        }
        finally
        {
            localFrom.remove();
        }
    }

}
