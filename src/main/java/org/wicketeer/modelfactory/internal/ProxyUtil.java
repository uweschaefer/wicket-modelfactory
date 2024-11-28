package org.wicketeer.modelfactory.internal;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.util.Collection;

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

    protected static <T> T createProxy(final InvocationHandler interceptor,
            final Class<T> clazz, final boolean failSafe,
            final Class<?>... implementedInterface) {
        if (clazz.isInterface()) {
            return (T) createNativeJavaProxy(clazz.getClassLoader(),
                    interceptor, concatClasses(new Class<?>[] { clazz },
                            implementedInterface));
        }
        try {
            return (T) createByteBuddyProxy(clazz, interceptor, implementedInterface);
        }
        catch (IllegalArgumentException iae) {
            if (isProxable(clazz)) {
                return createByteBuddyProxy(clazz, interceptor, implementedInterface);
            }
            return manageUnproxableClass(clazz, failSafe);
        }
    }

    public static String enumerate(final Collection<?> l,
            final String delimiter) {
        StringBuilder sb = new StringBuilder(128);
        boolean first = true;
        for (Object object : l) {
            if (!first) {
                sb.append(delimiter);
            } else {
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

    private static <T> T createByteBuddyProxy(Class<T> clazz, InvocationHandler interceptor,
            Class<?>... interfaces) {
        try {
            DynamicType.Builder<T> builder = new ByteBuddy()
                    .subclass(clazz)
                    .implement(interfaces)
                    .method(ElementMatchers.any())
                    .intercept(InvocationHandlerAdapter.of((InvocationHandler) interceptor));

            return builder.make()
                    .load(clazz.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Error creating proxy with ByteBuddy", e);
        }
    }

    private static Object createNativeJavaProxy(final ClassLoader classLoader,
            final InvocationHandler interceptor, final Class<?>... interfaces) {
        return java.lang.reflect.Proxy.newProxyInstance(classLoader, interfaces, interceptor);
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
