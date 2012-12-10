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

import java.lang.reflect.Modifier;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

/**
 * An utility class of static factory methods that creates arguments and binds
 * them with their placeholders
 * 
 * @author Mario Fusco
 */
public final class ArgumentsFactory
{

    private ArgumentsFactory()
    {
    }

    public static <T> T createArgument(final Class<T> clazz)
    {
        return createArgument(clazz, new InvocationSequence(clazz));
    }

    @SuppressWarnings("unchecked")
    static <T> T createArgument(final Class<T> clazz, final InvocationSequence invocationSequence)
    {
        T placeholder = (T) createPlaceholder(clazz, invocationSequence);
        if (ARG.get().getState() == State.ACTIVE)
        {
            ARG.get().set(placeholder, new Argument<T>(invocationSequence));
        }
        return placeholder;
    }

    private static Object createPlaceholder(final Class<?> clazz, final InvocationSequence invocationSequence)
    {

        State stateBeforeCreationCall = ARG.get().getState();

        if (clazz == Void.class || "void".equals(clazz.getName()))
        {
            if (stateBeforeCreationCall == State.IGNORE)
            {
                return null;
            }

            throw new IllegalArgumentException("void return type encountered on: " + invocationSequence);
        }

        if (clazz.isPrimitive())
        {
            return createPrimitivePlaceHolder(clazz, invocationSequence);
        }

        ARG.get().set(State.IGNORE);
        try
        {
            if (Modifier.isFinal(clazz.getModifiers()))
            {
                return objenesis.newInstance(clazz);
            }
            else
            {
                return ProxyUtil.createProxy(new ProxyArgument(clazz, invocationSequence), clazz, false);
            }
        }
        finally
        {
            ARG.get().set(stateBeforeCreationCall);
        }
    }

    private static Object createPrimitivePlaceHolder(final Class<?> clazz, final InvocationSequence invocationSequence)
    {

        if (clazz == boolean.class)
        {
            return true;
        }

        if (clazz == int.class)
        {
            return 1;
        }
        if (clazz == double.class)
        {
            return 1d;
        }
        if (clazz == long.class)
        {
            return 1L;
        }

        if (clazz == short.class)
        {
            return (short) 1;
        }

        if (clazz == byte.class)
        {
            return (byte) 1;
        }

        if (clazz == float.class)
        {
            return 1f;
        }

        if (clazz == char.class)
        {
            return 'p';
        }

        throw new IllegalArgumentException("forgotten primitive?");

    }

    private static class ArgumentMapping
    {
        private Argument lastArgument;
        private Object lastPlaceHolder;
        private State state = State.ACTIVE;

        public State getState()
        {
            return state;
        }

        public void set(final Object placeHolder, final Argument arg)
        {
            if (state == State.ACTIVE)
            {
                lastArgument = arg;
                lastPlaceHolder = placeHolder;
            }
        }

        public void set(final State stateToSet)
        {
            state = stateToSet;
        }

        public Argument getAndClear(final Object placeHolder)
        {
            try
            {
                if (placeHolder == null)
                {
                    throw new IllegalStateException("Unknown placeholder " + placeHolder);
                }

                if (placeHolder instanceof Argument)
                {
                    return (Argument) placeHolder;
                }

                if (lastPlaceHolder != placeHolder)
                {
                    throw new IllegalStateException("Unknown placeholder " + placeHolder);
                }
                else
                {
                    return lastArgument;
                }
            }
            finally
            {
                set(null, null);
            }
        }
    }
    private enum State {
        ACTIVE, IGNORE;
    }

    private static LastArgHolder ARG = new LastArgHolder();
    private static class LastArgHolder extends ThreadLocal<ArgumentMapping>
    {
        @Override
        protected ArgumentMapping initialValue()
        {
            return new ArgumentMapping();
        }

    }

    public static <T> Argument<T> actualArgument(final T placeholder)
    {
        return ARG.get().getAndClear(placeholder);
    }
    private static final Objenesis objenesis = new ObjenesisStd(true);

}
