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
    protected Invocation lastInvocation;
    private int hashCode;

    private Invoker invoker = this;

    protected InvocationSequence(final Class<?> rootInvokedClass) {
        this.rootInvokedClass = rootInvokedClass;
    }

    protected InvocationSequence(final InvocationSequence sequence,
            final Invocation invocation) {
        this.rootInvokedClass = sequence.getRootInvokedClass();
        invocation.previousInvocation = sequence.lastInvocation;
        this.lastInvocation = invocation;
    }

    protected Class<?> getRootInvokedClass() {
        return this.rootInvokedClass;
    }

    protected String getInkvokedPropertyName() {
        if (this.inkvokedPropertyName == null) {
            this.inkvokedPropertyName = calcInkvokedPropertyName();
        }
        return this.inkvokedPropertyName;
    }

    private String calcInkvokedPropertyName() {
        if (null == this.lastInvocation) {
            return "";
        }
        StringBuilder sb = new StringBuilder();

        calcInkvokedPropertyName(this.lastInvocation,
                this.lastInvocation.previousInvocation, sb);
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
        return this.lastInvocation.getReturnType();
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
        if (this.hashCode != 0) {
            return this.hashCode;
        }
        this.hashCode = 13 * this.rootInvokedClass.hashCode();
        int factor = 17;
        for (Invocation invocation = this.lastInvocation; invocation != null; invocation = invocation.previousInvocation) {
            this.hashCode += factor * invocation.hashCode();
            factor += 2;
        }
        return this.hashCode;
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
        return this.invoker.invokeOn(object);
    }

    @Override
    public Object invokeOn(final Object object) {
        return invokeOn(this.lastInvocation, object);
    }

    private Object invokeOn(final Invocation invocation, Object value) {

        Object ret = value;

        if (invocation == null) {
            return ret;
        }
        if (invocation.previousInvocation != null) {
            ret = invokeOn(invocation.previousInvocation, ret);
        }
        return invocation.invokeOn(ret);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("[");
        if (this.lastInvocation == null) {
            sb.append(this.rootInvokedClass);
        }
        else {
            toString(this.lastInvocation,
                    this.lastInvocation.previousInvocation, sb, true);
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