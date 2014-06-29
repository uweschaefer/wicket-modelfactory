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

/**
 * Thanks to Mockito guys for some modifications. This class has been further
 * modified for use in lambdaj. Mockito License for redistributed, modified
 * file. Copyright (c) 2007 Mockito contributors This program is made available
 * under the terms of the MIT License. jMock License for original distributed
 * file Copyright (c) 2000-2007, jMock.org All rights reserved. Redistribution
 * and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met: Redistributions of
 * source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer. Redistributions in binary form must reproduce
 * the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the
 * distribution. Neither the name of jMock nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.wicketeer.modelfactory.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.core.DefaultNamingPolicy;
import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.core.Predicate;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.NoOp;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

/**
 * Thanks to jMock guys for this handy class that wraps all the cglib magic. In
 * particular it workarounds a cglib limitation by allowing to proxy a class
 * even if the misses a no args constructor.
 * 
 * @author Mario Fusco
 * @author Sebastian Jancke
 */
final class ClassImposterizer {

    static final ClassImposterizer INSTANCE = new ClassImposterizer();

    private ClassImposterizer() {
    }

    private final Objenesis objenesis = new ObjenesisStd();

    private static final NamingPolicy DEFAULT_POLICY = new DefaultNamingPolicy() {
        /**
         * {@inheritDoc}
         */
        @Override
        protected String getTag() {
            return "ByLambdajWithCGLIB";
        }
    };

    private static final NamingPolicy SIGNED_CLASSES_POLICY = new DefaultNamingPolicy() {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getClassName(final String prefix, final String source,
                final Object key, final Predicate names) {
            return "codegen." + super.getClassName(prefix, source, key, names);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String getTag() {
            return "ByLambdajWithCGLIB";
        }
    };

    private static final CallbackFilter IGNORE_BRIDGE_METHODS = new CallbackFilter() {
        @Override
        public int accept(final Method method) {
            return method.isBridge() ? 1 : 0;
        }
    };

    <T> T imposterise(final Callback callback, final Class<T> mockedType,
            final Class<?>... ancillaryTypes) {
        setConstructorsAccessible(mockedType, true);
        Class<?> proxyClass = createProxyClass(mockedType, ancillaryTypes);
        return mockedType.cast(createProxy(proxyClass, callback));
    }

    private void setConstructorsAccessible(final Class<?> mockedType,
            final boolean accessible) {
        for (Constructor<?> constructor : mockedType.getDeclaredConstructors()) {
            constructor.setAccessible(accessible);
        }
    }

    private Class<?> createProxyClass(Class<?> mockedType,
            final Class<?>... interfaces) {
        if (mockedType == Object.class) {
            mockedType = ClassWithSuperclassToWorkAroundCglibBug.class;
        }

        Enhancer enhancer = new ClassEnhancer();
        enhancer.setUseFactory(true);
        enhancer.setSuperclass(mockedType);
        enhancer.setInterfaces(interfaces);

        enhancer.setCallbackTypes(new Class[] { MethodInterceptor.class,
                NoOp.class });
        enhancer.setCallbackFilter(IGNORE_BRIDGE_METHODS);
        enhancer.setNamingPolicy(mockedType.getSigners() != null ? SIGNED_CLASSES_POLICY
                : DEFAULT_POLICY);

        return enhancer.createClass();
    }

    private static class ClassEnhancer extends Enhancer {
        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("rawtypes")
        @Override
        protected void filterConstructors(final Class sc,
                final List constructors) {
        }
    }

    private Object createProxy(final Class<?> proxyClass,
            final Callback callback) {
        Factory proxy = (Factory) objenesis.newInstance(proxyClass);
        proxy.setCallbacks(new Callback[] { callback, NoOp.INSTANCE });
        return proxy;
    }

    /**
     * Class With Superclass To WorkAround Cglib Bug
     */
    public static class ClassWithSuperclassToWorkAroundCglibBug {
    }
}
