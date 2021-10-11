package com.xiaotao.summerframework.web.http.interceptor;

import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;

/**
 * HTTP拦截器
 */
public interface HttpInterceptor {
    /**
     * 在HttpHandler执行前，该方法被执行，若该方法返回false，将会终止拦截器链，不会再执行后续的拦截器和HttpHandler
     * @param request   HTTP请求
     * @param response  HTTP响应
     * @return 允许该次请求通过拦截器则返回true，要终止请求继续前进和终止拦截器链则返回false
     */
    boolean beforeHandler(HttpRequest request, HttpResponse response);

    /**
     * 当HttpHandler被执行后，该方法被执行
     * @param request   HTTP请求
     * @param response  HTTP响应
     */
    void afterHandler(HttpRequest request, HttpResponse response);
}
