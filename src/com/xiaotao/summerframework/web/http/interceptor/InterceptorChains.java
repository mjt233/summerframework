package com.xiaotao.summerframework.web.http.interceptor;

import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;
import com.xiaotao.summerframework.web.http.handler.HttpHandler;

import java.util.Collection;
import java.util.Iterator;

/**
 * 拦截器链，包含了一系列有序拦截器
 */
public class InterceptorChains {
    private final Iterator<HttpInterceptor> iterator;
    private final HttpHandler handler;

    /**
     * 初始化一个拦截器链
     * @param interceptors  HTTP请求动作将要尝试通过的拦截器集合
     * @param handler       目的HttpHandler
     */
    public InterceptorChains(Collection<HttpInterceptor> interceptors, HttpHandler handler) {
        this.handler = handler;
        this.iterator = interceptors.iterator();
    }

    /**
     * 开始让请求动作通过执行拦截器链
     * @param request   HTTP请求
     * @param response  HTTP响应
     * @return HttpHandler的返回值
     * @throws Exception 执行过程出现异常
     */
    public Object doChains(HttpRequest request, HttpResponse response) throws Exception {
        Object ret;
        if (iterator.hasNext()) {
            // 开始通过拦截器链
            ret = doChains(request, response, iterator.next());
        } else {
            // 无拦截器则直接调用HttpHandler
            ret = handler.handle(request, response);
        }
        return ret;
    }

    /**
     * 通过一个指定的拦截器，通过后将递归调用下一个拦截器，直到所有拦截器调用完毕后，将最终调用HttpHandler
     * @param request       HTTP请求
     * @param response      HTTP响应
     * @param interceptor   目标指定拦截器
     * @return  HttpHandler的执行返回值。若拦截器链未完全通过，则返回null
     */
    private Object doChains(HttpRequest request, HttpResponse response, HttpInterceptor interceptor) {
        if (interceptor == null) return null;

        Object ret = null;
        if (interceptor.beforeHandler(request, response)) {
            if (iterator.hasNext()) {
                ret = doChains(request, response, iterator.next());
            } else {
                try {
                    ret = handler.handle(request, response);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            interceptor.afterHandler(request, response);
        }
        return ret;
    }
}
