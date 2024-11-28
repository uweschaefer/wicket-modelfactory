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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Mario Fusco
 */
class ProxyArgument implements InvocationHandler {

    private final Class<?> proxiedClass;

    private final InvocationSequence invocationSequence;

    ProxyArgument(final Class<?> proxiedClass,
            final InvocationSequence invocationSequence) {
        this.proxiedClass = proxiedClass;
        this.invocationSequence = invocationSequence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(final Object proxy, final Method method,
            final Object[] args) {

        String name = method.getName();
        if ("hashCode".equals(name)) {
            return this.invocationSequence.hashCode();
        }
        if ("finalize".equals(name)) {
            return null;
        }
        if ("wait".equals(name)) {
            return null;
        }
        if ("notify".equals(name)) {
            return null;
        }
        if ("notifyAll".equals(name)) {
            return null;
        }
        if ("equals".equals(name)) {
            return this.invocationSequence.equals(args[0]);
        }
        Class<?> returnType = method.getReturnType();

        // Adds this invocation to the current invocation sequence and creates a
        // new proxy propagating the invocation sequence
        return ArgumentsFactory.createArgument(returnType,
                new InvocationSequence(this.invocationSequence,
                        new Invocation(this.proxiedClass, method, args)));
    }
}
