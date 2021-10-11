package com.xiaotao.summerframework.core.factory;

/**
 * 监听器接口，某些事件触发时执行
 * @param <T> 事件参数
 */
@FunctionalInterface
public interface Listener<T> {
    void handleCallback(T t);
}
