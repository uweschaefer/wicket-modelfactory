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

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;

/**
 * Registers a method invocation
 *
 * @author Mario Fusco
 * @author Frode Carlsen
 */
final class Invocation {

    private final Class<?> invokedClass;
    private final Method invokedMethod;
    private String invokedPropertyName;
    private ParameterReference[] weakArgs;
    private transient int hashCode;
    Invocation previousInvocation;

    Invocation(final Class<?> invokedClass, final Method invokedMethod,
            final Object[] args) {
        this.invokedClass = invokedClass;
        this.invokedMethod = invokedMethod;
        invokedMethod.setAccessible(true);
        if ((args != null) && (args.length > 0)) {
            weakArgs = new ParameterReference[args.length];
            for (int i = 0; i < args.length; i++) {
                weakArgs[i] = invokedMethod.getParameterTypes()[i]
                        .isPrimitive() ? new StrongParameterReference(args[i])
                        : new WeakParameterReference(args[i]);
            }
        }
    }

    boolean hasArguments() {
        return weakArgs != null;
    }

    private Object[] getConcreteArgs() {
        if (weakArgs == null) {
            return new Object[0];
        }
        Object[] args = new Object[weakArgs.length];
        for (int i = 0; i < weakArgs.length; i++) {
            args[i] = weakArgs[i].get();
        }
        return args;
    }

    Class<?> getInvokedClass() {
        return invokedClass;
    }

    Method getInvokedMethod() {
        return invokedMethod;
    }

    Class<?> getReturnType() {
        return invokedMethod.getReturnType();
    }

    String getInvokedPropertyName() {
        if (invokedPropertyName == null) {
            invokedPropertyName = getPropertyName(invokedMethod);
        }
        return invokedPropertyName;
    }

    Object invokeOn(final Object object) {
        try {
            return object == null ? null : invokedMethod.invoke(object,
                    getConcreteArgs());
        }
        catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (weakArgs == null) {
            return invokedMethod.toString();
        }
        StringBuilder sb = new StringBuilder(invokedMethod.toString());
        sb.append(" with args ");
        boolean first = true;
        for (ParameterReference arg : weakArgs) {
            sb.append(first ? "" : ", ").append(arg.get());
            first = false;
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (hashCode != 0) {
            return hashCode;
        }
        hashCode = (13 * invokedClass.hashCode())
                + (17 * invokedMethod.hashCode());
        if (weakArgs != null) {
            hashCode += 19 * weakArgs.length;
        }
        if (previousInvocation != null) {
            hashCode += 23 * previousInvocation.hashCode();
        }
        return hashCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        Invocation otherInvocation = (Invocation) object;
        return areNullSafeEquals(invokedClass,
                otherInvocation.getInvokedClass())
                && areNullSafeEquals(invokedMethod,
                        otherInvocation.getInvokedMethod())
                && areNullSafeEquals(previousInvocation,
                        otherInvocation.previousInvocation)
                && Arrays.equals(weakArgs, otherInvocation.weakArgs);
    }

    static boolean areNullSafeEquals(final Object first, final Object second) {
        return (first == second)
                || ((first != null) && (second != null) && first.equals(second));
    }

    private static abstract class ParameterReference {
        protected abstract Object get();

        @Override
        public final boolean equals(final Object obj) {
            return (obj instanceof ParameterReference)
                    && areNullSafeEquals(get(),
                            ((ParameterReference) obj).get());
        }
    }

    private static final class StrongParameterReference extends
            ParameterReference {
        private final Object strongRef;

        private StrongParameterReference(final Object referent) {
            strongRef = referent;
        }

        @Override
        protected Object get() {
            return strongRef;
        }
    }

    private static final class WeakParameterReference extends
            ParameterReference {
        private final WeakReference<Object> weakRef;

        private WeakParameterReference(final Object referent) {
            weakRef = new WeakReference<Object>(referent);
        }

        @Override
        protected Object get() {
            return weakRef.get();
        }
    }

    public static String getPropertyName(final Method invokedMethod) {
        String methodName = invokedMethod.getName();
        if ((methodName.startsWith("get") || methodName.startsWith("set"))
                && (methodName.length() > 3)) {
            methodName = methodName.substring(3);
        }
        else
            if (methodName.startsWith("is") && (methodName.length() > 2)) {
                methodName = methodName.substring(2);
            }
        return methodName.substring(0, 1).toLowerCase(Locale.getDefault())
                + methodName.substring(1);
    }
}
