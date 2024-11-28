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
    protected Invocation previousInvocation;

    Invocation(final Class<?> invokedClass, final Method invokedMethod,
            final Object[] args) {
        this.invokedClass = invokedClass;
        this.invokedMethod = invokedMethod;
        invokedMethod.setAccessible(true);
        if ((args != null) && (args.length > 0)) {
            this.weakArgs = new ParameterReference[args.length];
            for (int i = 0; i < args.length; i++) {
                this.weakArgs[i] = invokedMethod.getParameterTypes()[i]
                        .isPrimitive() ? new StrongParameterReference(args[i])
                                : new WeakParameterReference(args[i]);
            }
        }
    }

    protected boolean hasArguments() {
        return this.weakArgs != null;
    }

    private Object[] getConcreteArgs() {
        if (this.weakArgs == null) {
            return new Object[0];
        }
        Object[] args = new Object[this.weakArgs.length];
        for (int i = 0; i < this.weakArgs.length; i++) {
            args[i] = this.weakArgs[i].get();
        }
        return args;
    }

    protected Class<?> getInvokedClass() {
        return this.invokedClass;
    }

    protected Method getInvokedMethod() {
        return this.invokedMethod;
    }

    protected Class<?> getReturnType() {
        return this.invokedMethod.getReturnType();
    }

    protected String getInvokedPropertyName() {
        if (this.invokedPropertyName == null) {
            this.invokedPropertyName = getPropertyName(this.invokedMethod);
        }
        return this.invokedPropertyName;
    }

    protected Object invokeOn(final Object object) {
        try {
            return object == null ? null
                    : this.invokedMethod.invoke(object, getConcreteArgs());
        }
        catch (RuntimeException re) {
            throw re;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (this.weakArgs == null) {
            return this.invokedMethod.toString();
        }
        StringBuilder sb = new StringBuilder(this.invokedMethod.toString());
        sb.append(" with args ");
        boolean first = true;
        for (ParameterReference arg : this.weakArgs) {
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
        if (this.hashCode != 0) {
            return this.hashCode;
        }
        this.hashCode = (13 * this.invokedClass.hashCode())
                + (17 * this.invokedMethod.hashCode());
        if (this.weakArgs != null) {
            this.hashCode += 19 * this.weakArgs.length;
        }
        if (this.previousInvocation != null) {
            this.hashCode += 23 * this.previousInvocation.hashCode();
        }
        return this.hashCode;
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
        return areNullSafeEquals(this.invokedClass,
                otherInvocation.getInvokedClass())
                && areNullSafeEquals(this.invokedMethod,
                        otherInvocation.getInvokedMethod())
                && areNullSafeEquals(this.previousInvocation,
                        otherInvocation.previousInvocation)
                && Arrays.equals(this.weakArgs, otherInvocation.weakArgs);
    }

    protected static boolean areNullSafeEquals(final Object first,
            final Object second) {
        return (first == second) || ((first != null) && (second != null)
                && first.equals(second));
    }

    private static abstract class ParameterReference {
        protected abstract Object get();

        @Override
        public final boolean equals(final Object obj) {
            return (obj instanceof ParameterReference) && areNullSafeEquals(
                    get(), ((ParameterReference) obj).get());
        }
    }

    private static final class StrongParameterReference
            extends ParameterReference {
        private final Object strongRef;

        private StrongParameterReference(final Object referent) {
            this.strongRef = referent;
        }

        @Override
        protected Object get() {
            return this.strongRef;
        }
    }

    private static final class WeakParameterReference
            extends ParameterReference {
        private final WeakReference<Object> weakRef;

        private WeakParameterReference(final Object referent) {
            this.weakRef = new WeakReference<Object>(referent);
        }

        @Override
        protected Object get() {
            return this.weakRef.get();
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
