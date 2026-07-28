package com.mnemos.account.support

import java.lang.reflect.Method
import java.lang.reflect.Proxy

inline fun <reified T> interfaceProxy(noinline handler: (Method, Array<out Any?>) -> Any?): T =
    Proxy.newProxyInstance(
        T::class.java.classLoader,
        arrayOf(T::class.java),
    ) { proxy, method, arguments ->
        when (method.name) {
            "toString" -> "TestProxy(${T::class.java.simpleName})"
            "hashCode" -> System.identityHashCode(proxy)
            "equals" -> proxy === arguments?.firstOrNull()
            else -> handler(method, arguments ?: emptyArray())
        }
    } as T
