package com.xiaotao.summerframework.web.http.interceptor;

import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;

public interface HttpInterceptor {
    boolean beforeHandler(HttpRequest request, HttpResponse response);
    void afterHandler(HttpRequest request, HttpResponse response);
}
