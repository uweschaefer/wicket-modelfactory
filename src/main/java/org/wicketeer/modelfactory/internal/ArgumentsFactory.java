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

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.Map;

import org.apache.wicket.MetaDataKey;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.wicketeer.modelfactory.RequestCycleLocal;

/**
 * An utility class of static factory methods that creates arguments and binds
 * them with their placeholders
 *
 * @author Mario Fusco
 */
public final class ArgumentsFactory {

    private static final LastArgHolder ARG = new LastArgHolder();
    private static final Objenesis objenesis = new ObjenesisStd(true);
    private static final Map<Class<?>, Object> primitives = new IdentityHashMap<Class<?>, Object>();

    static {
        primitives.put(boolean.class, true);
        primitives.put(int.class, 1);
        primitives.put(double.class, 1D);
        primitives.put(float.class, 1F);
        primitives.put(long.class, 1L);
        primitives.put(short.class, (short) 1);
        primitives.put(byte.class, (byte) 1);
        primitives.put(char.class, 'p');
    }

    private ArgumentsFactory() {
    }

    public static <T> T createArgument(final Class<T> clazz) {
        return createArgument(clazz, new InvocationSequence(clazz));
    }

    @SuppressWarnings("unchecked")
    protected static <T> T createArgument(final Class<T> clazz,
            final InvocationSequence invocationSequence) {
        T placeholder = (T) createPlaceholder(clazz, invocationSequence);
        if (ARG.get().getState() == State.ACTIVE) {
            ARG.get().set(placeholder, new Argument<T>(invocationSequence));
        }
        return placeholder;
    }

    private static Object createPlaceholder(final Class<?> clazz,
            final InvocationSequence invocationSequence) {

        State stateBeforeCreationCall = ARG.get().getState();

        if ((clazz == Void.class) || "void".equals(clazz.getName())) {
            if (stateBeforeCreationCall == State.IGNORE) {
                return null;
            }

            throw new IllegalArgumentException(
                    "void return type encountered on: " + invocationSequence);
        }

        if (clazz.isPrimitive()) {
            return createPrimitivePlaceHolder(clazz, invocationSequence);
        }

        if (clazz.isArray()) {
            Class<?> arrayType = clazz.getComponentType();
            return Array.newInstance(arrayType, 0);
        }

        ARG.get().set(State.IGNORE);
        try {
            if (Modifier.isFinal(clazz.getModifiers())) {
                // This breaks backward comp. because wicket-modelfactory is
                // actually used with final classes
                // TODO

                // Probably safe to create instances of Base objects like String
                // if (!clazz.getName().startsWith("java.")) {
                // throw new IllegalArgumentException(
                // "Modelfactory cannot proxy final Class or Enum '"
                // + clazz + "'");
                // }
                return objenesis.newInstance(clazz);
            }
            else {
                return ProxyUtil.createProxy(
                        new ProxyArgument(clazz, invocationSequence), clazz,
                        false);
            }
        }
        finally {
            ARG.get().set(stateBeforeCreationCall);
        }
    }

    private static Object createPrimitivePlaceHolder(final Class<?> clazz,
            final InvocationSequence invocationSequence) {

        Object ret = primitives.get(clazz);
        if (ret == null)
            throw new IllegalArgumentException("forgotten primitive?");
        else
            return ret;
    }

    private static class ArgumentMapping {
        private Argument<?> lastArgument;

        private Object lastPlaceHolder;

        private State state = State.ACTIVE;

        public State getState() {
            return this.state;
        }

        public void set(final Object placeHolder, final Argument<?> arg) {
            if (this.state == State.ACTIVE) {
                this.lastArgument = arg;
                this.lastPlaceHolder = placeHolder;
            }
        }

        public void set(final State stateToSet) {
            this.state = stateToSet;
        }

        public Argument<?> getAndClear(final Object placeHolder) {
            try {
                return get(placeHolder);
            }
            finally {
                set(null, null);
            }
        }

        public Argument<?> get(final Object placeHolder) {
            if (placeHolder == null) {
                throw new IllegalStateException(
                        "Unknown placeholder " + placeHolder);
            }

            if (placeHolder instanceof Argument) {
                return (Argument<?>) placeHolder;
            }

            if (placeHolder != this.lastPlaceHolder) {
                // fixes problems with double
                if (!placeHolder.equals(this.lastPlaceHolder)) {
                    throw new IllegalStateException(
                            "Unknown placeholder " + placeHolder);
                }
                else {
                    return this.lastArgument;
                }
            }
            else {
                return this.lastArgument;
            }
        }
    }

    private enum State {
        ACTIVE, IGNORE;
    }

    private static class LastArgHolder
            extends RequestCycleLocal<ArgumentMapping> {
        private static final MetaDataKey<ArgumentMapping> LAST_ARG_HOLDER_KEY = new MetaDataKey<ArgumentsFactory.ArgumentMapping>() {

            private static final long serialVersionUID = 1L;
        };

        public LastArgHolder() {
            super(LAST_ARG_HOLDER_KEY);
        }

        @Override
        public ArgumentMapping get() {
            ArgumentMapping target = super.get();
            if (target == null) {
                target = new ArgumentMapping();
                set(target);
            }
            return target;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Argument<T> getAndRemoveArgumentFor(final T placeholder) {
        return (Argument<T>) ARG.get().getAndClear(placeholder);
    }

    @SuppressWarnings("unchecked")
    public static <T> Argument<T> getArgumentFor(final T placeholder) {
        return (Argument<T>) ARG.get().get(placeholder);
    }

}
