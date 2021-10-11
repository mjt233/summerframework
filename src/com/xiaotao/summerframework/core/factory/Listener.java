package com.xiaotao.summerframework.core.factory;

@FunctionalInterface
public interface Listener<T> {
    void handleCallback(T t);
}
