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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IObjectClassAwareModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.wicketeer.modelfactory.internal.Argument;
import org.wicketeer.modelfactory.internal.ArgumentsFactory;

import com.googlecode.gentyref.GenericTypeReflector;

/**
 * Entry point for creating refactoring safe PropertyModels. Usage:<code>
 * IModel<String> stringModel = model(from(person).getProfile().getName());
 * </code> where person can be an instance of Person class or an IModel<Person>.
 */
public class ModelFactory {

    private static RequestCycleLocalFrom localFrom = new RequestCycleLocalFrom();

    /**
     * Proxies the given object in order to be able to call methods on it to
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
    public static <T> T from(final T value) {
        Preconditions.checkNotNull(value);
        Class<T> type = (Class<T>) value.getClass();

        IModel<T> model = new AbstractReadOnlyModel<T>() {

            private static final long serialVersionUID = 1L;

            @Override
            public T getObject() {
                return value;
            }
        };

        return from(model, type);
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T from(final IModel<T> model) {

        Class<? extends IModel> c = model.getClass();

        Class<T> type = null;

        if (LoadableDetachableModel.class.isAssignableFrom(c)) {
            try {
                Method method;
                method = c.getDeclaredMethod("load");
                type = (Class<T>) method.getReturnType();
                if ((type == Object.class) || (type == Serializable.class)) {
                    type = null;
                }
            }
            catch (Throwable e) {
                throw new WicketRuntimeException(e);
            }
        }

        if ((type == null) && IModel.class.isAssignableFrom(c)) {

            Method method;
            try {
                method = c.getMethod("getObject");
                type = (Class<T>) method.getReturnType();

                if ((type == Object.class) || (type == Serializable.class)) {
                    type = null;
                }
            }
            catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if ((type == null) && (model instanceof IObjectClassAwareModel)) {
            type = ((IObjectClassAwareModel) model).getObjectClass();
        }

        if ((type == null) && c.isAnonymousClass()) {
            type = (Class<T>) tryReflectFromAnonClass(c);
        }

        if (type == null) {
            // last possibility
            T modelObject = model.getObject();
            if (modelObject != null) {
                type = (Class<T>) modelObject.getClass();
            }
            else {
                throw new IllegalArgumentException(
                        "Cannot find proper type definition for model given. Please use from(model,Class).");
            }

        }
        return from(model, type);
    }

    private static Class<?> tryReflectFromAnonClass(
            final Class<? extends IModel> c) {
        TypeVariable<?>[] params = c.getSuperclass().getTypeParameters();
        if ((params != null) && (params.length == 1)) {
            // we might try
            Type typeParameter = GenericTypeReflector.getTypeParameter(c,
                    (TypeVariable<? extends Class<?>>) params[0]);
            if (typeParameter instanceof Class) {
                return (Class<?>) typeParameter;
            }
        }
        return null;
    }

    /**
     * creates an actual PropertyModel from the path expressed by the given
     * object.
     *
     * @param path
     *            the object initially created by a from-call
     * @return the actual Model
     */
    public static <T> IModel<T> model(final T path) {
        Object t = ModelFactory.localFrom.get();
        if (t == RequestCycleLocalFrom.FROM_CLASS) {
            throw new IllegalStateException(
                    "proxy has no staring point, please use path() to get a path expression or use from(IModel)");
        }

        Argument<T> arg = ArgumentsFactory.getArgumentFor(path);
        Class<T> type = arg.getReturnType();

        return new TypedPropertyModel<T>(t, path(path), type);
    }

    /**
     * @param path
     *            the object initially created by a from-call
     * @return a string denoting the property path expressed by the path object
     */
    public static String path(final Object path) {
        try {
            Argument<?> a = ArgumentsFactory.getAndRemoveArgumentFor(path);
            return a.getInkvokedPropertyName();
        }
        finally {
            ModelFactory.localFrom.remove();
        }
    }

    /**
     * starts recording from a class.
     * this will return a proxy of Type clazz, that should be evaluated by
     * path(x), rather than model(x). A common usecase is to evaluate to a path
     * expression, that is used in subsequent PropertyModel constructions.
     * <code>new PropertyModel(myModel, ModelFactory.path(x));</code>
     *
     * @param clazz
     * @return
     */
    public static <T> T fromClass(final Class<T> clazz) {
        ModelFactory.localFrom.set(RequestCycleLocalFrom.FROM_CLASS);
        return ArgumentsFactory.createArgument(Preconditions
                .checkNotNull(clazz));
    }

    /**
     * In cases where you need to hint the Type of the model passed, ecause it
     * cannot be reflected, you can use this method and provide the model
     * objects expected type as parameter.
     *
     * @param model
     *            the model from which to create a proxy
     * @param clazz
     *            the type of the object backed by the model
     * @param <T>
     *            type of the model parameter
     * @return proxy for property path generation
     * @throws NullPointerException
     *             if the model is null
     */
    public static <T> T from(final IModel<? extends T> model,
            final Class<T> type) {
        ModelFactory.localFrom.set(Preconditions.checkNotNull(model));
        return ArgumentsFactory
                .createArgument(Preconditions.checkNotNull(type));
    }

    /**
     * @return true if current invocation sequence recording was
     *         started from a root reference (in contrast to being start with
     *         <code>fromClass(Class)</code>.
     * @throws IllegalStateException
     *             if not currently in recording thread (either from() has not
     *             been called, of model() has already been called
     */
    public static boolean hasRootReference() {
        return ModelFactory.localFrom.get() != RequestCycleLocalFrom.FROM_CLASS;
    }
}
