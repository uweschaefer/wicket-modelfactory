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
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Collection;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

/**
 * An utility class of static factory methods that provide facilities to create
 * proxies
 * 
 * @author Mario Fusco
 * @author Sebastian Jancke
 */
@SuppressWarnings("unchecked")
public final class ProxyUtil {

    private ProxyUtil() {
    }

    public static boolean isProxable(final Class<?> clazz) {
        return !clazz.isPrimitive() && !Modifier.isFinal(clazz.getModifiers())
                && !clazz.isAnonymousClass();
    }

    static <T> T createProxy(final InvocationInterceptor interceptor,
            final Class<T> clazz, final boolean failSafe,
            final Class<?>... implementedInterface) {
        if (clazz.isInterface()) {
            return (T) createNativeJavaProxy(clazz.getClassLoader(),
                    interceptor, concatClasses(new Class<?>[] { clazz },
                            implementedInterface));
        }
        try {
            Enhancer e = createEnhancer(interceptor, clazz,
                    implementedInterface);
            return (T) e.create();
        }
        catch (IllegalArgumentException iae) {
            if (Proxy.isProxyClass(clazz)) {
                return (T) createNativeJavaProxy(clazz.getClassLoader(),
                        interceptor, concatClasses(implementedInterface,
                                clazz.getInterfaces()));
            }
            if (isProxable(clazz)) {
                return ClassImposterizer.INSTANCE.imposterise(interceptor,
                        clazz, implementedInterface);
            }
            return manageUnproxableClass(clazz, failSafe);
        }

    }

    public static String enumerate(final Collection<?> l,
            final String delimiter) {
        StringBuffer sb = new StringBuffer(128);
        boolean first = true;
        for (Object object : l) {
            if (!first) {
                sb.append(delimiter);
            }
            else {
                first = false;
            }
            sb.append(object);
        }
        return sb.toString();
    }

    private static <T> T manageUnproxableClass(final Class<T> clazz,
            final boolean failSafe) {
        if (failSafe) {
            return null;
        }
        throw new UnproxableClassException(clazz);
    }

    // ////////////////////////////////////////////////////////////////////////
    // /// Private
    // ////////////////////////////////////////////////////////////////////////

    private static Enhancer createEnhancer(final MethodInterceptor interceptor,
            final Class<?> clazz, final Class<?>... interfaces) {
        Enhancer enhancer = new Enhancer();
        enhancer.setCallback(interceptor);
        enhancer.setSuperclass(clazz);
        if (interfaces != null && interfaces.length > 0) {
            enhancer.setInterfaces(interfaces);
        }
        return enhancer;
    }

    private static Object createNativeJavaProxy(final ClassLoader classLoader,
            final InvocationHandler interceptor, final Class<?>... interfaces) {
        return Proxy.newProxyInstance(classLoader, interfaces, interceptor);
    }

    private static Class<?>[] concatClasses(final Class<?>[] first,
            final Class<?>[] second) {
        if (first == null || first.length == 0) {
            return second;
        }
        if (second == null || second.length == 0) {
            return first;
        }
        Class<?>[] concatClasses = new Class[first.length + second.length];
        System.arraycopy(first, 0, concatClasses, 0, first.length);
        System.arraycopy(second, 0, concatClasses, first.length, second.length);
        return concatClasses;
    }
}
