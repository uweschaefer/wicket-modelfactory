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

/**
 * Registers a sequence of method invocations
 * 
 * @author Mario Fusco
 * @author Frode Carlsen
 */
final class InvocationSequence implements Invoker {

    private final Class<?> rootInvokedClass;
    private String inkvokedPropertyName;
    Invocation lastInvocation;
    private int hashCode;

    private Invoker invoker = this;

    InvocationSequence(final Class<?> rootInvokedClass) {
        this.rootInvokedClass = rootInvokedClass;
    }

    InvocationSequence(final InvocationSequence sequence,
            final Invocation invocation) {
        rootInvokedClass = sequence.getRootInvokedClass();
        invocation.previousInvocation = sequence.lastInvocation;
        lastInvocation = invocation;
    }

    Class<?> getRootInvokedClass() {
        return rootInvokedClass;
    }

    String getInkvokedPropertyName() {
        if (inkvokedPropertyName == null) {
            inkvokedPropertyName = calcInkvokedPropertyName();
        }
        return inkvokedPropertyName;
    }

    private String calcInkvokedPropertyName() {
        if (null == lastInvocation) {
            return "";
        }
        StringBuilder sb = new StringBuilder();

        calcInkvokedPropertyName(lastInvocation,
                lastInvocation.previousInvocation, sb);
        return sb.substring(1);
    }

    private void calcInkvokedPropertyName(final Invocation inv,
            final Invocation prevInv, final StringBuilder sb) {
        if (prevInv != null) {
            calcInkvokedPropertyName(prevInv, prevInv.previousInvocation, sb);
        }
        sb.append(".").append(inv.getInvokedPropertyName());
    }

    Class<?> getReturnType() {
        return lastInvocation.getReturnType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object object) {
        return object == this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (hashCode != 0) {
            return hashCode;
        }
        hashCode = 13 * rootInvokedClass.hashCode();
        int factor = 17;
        for (Invocation invocation = lastInvocation; invocation != null; invocation = invocation.previousInvocation) {
            hashCode += factor * invocation.hashCode();
            factor += 2;
        }
        return hashCode;
    }

    public Object evaluate(final Object object) {
        // if (!jitDone && needsJitting.compareAndSet(true, false)) {
        // jitDone = true;
        // if (executor != null) {
        // executor.submit(new Runnable() {
        // public void run() {
        // invoker = new InvokerJitter(object,
        // InvocationSequence.this).jitInvoker();
        // }
        // });
        // }
        // }
        return invoker.invokeOn(object);
    }

    @Override
    public Object invokeOn(final Object object) {
        return invokeOn(lastInvocation, object);
    }

    private Object invokeOn(final Invocation invocation, Object value) {
        if (invocation == null) {
            return value;
        }
        if (invocation.previousInvocation != null) {
            value = invokeOn(invocation.previousInvocation, value);
        }
        return invocation.invokeOn(value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("[");
        if (lastInvocation == null) {
            sb.append(rootInvokedClass);
        }
        else {
            toString(lastInvocation, lastInvocation.previousInvocation, sb,
                    true);
        }
        sb.append("]");
        return sb.toString();
    }

    private void toString(final Invocation inv, final Invocation prevInv,
            final StringBuilder sb, final boolean first) {
        if (prevInv != null) {
            toString(prevInv, prevInv.previousInvocation, sb, false);
        }
        sb.append(inv);
        if (!first) {
            sb.append(", ");
        }
    }
}