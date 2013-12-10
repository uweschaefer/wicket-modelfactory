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

import java.lang.reflect.Method;

/**
 * @author Mario Fusco
 */
class ProxyArgument extends InvocationInterceptor {

    private final Class<?> proxiedClass;

    private final InvocationSequence invocationSequence;

    ProxyArgument(final Class<?> proxiedClass, final InvocationSequence invocationSequence) {
        this.proxiedClass = proxiedClass;
        this.invocationSequence = invocationSequence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {

        String name = method.getName();
        if (name.equals("hashCode")) {
            return invocationSequence.hashCode();
        }
        if (name.equals("finalize")) {
            return null;
        }
        if (name.equals("wait")) {
            return null;
        }
        if (name.equals("notify")) {
            return null;
        }
        if (name.equals("notifyAll")) {
            return null;
        }
        if (name.equals("equals")) {
            return invocationSequence.equals(args[0]);
        }
        Class<?> returnType = method.getReturnType();

        // Adds this invocation to the current invocation sequence and creates a
        // new proxy propagating the invocation sequence
        return ArgumentsFactory.createArgument(returnType, new InvocationSequence(
                invocationSequence, new Invocation(proxiedClass, method, args)));
    }
}
