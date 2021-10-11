package com.xiaotao.summerframework.web.http.handler;

import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;

/**
 * 最低级的HTTP处理接口，相当于servlet
 */
@FunctionalInterface
public interface HttpHandler {
    Object handle(HttpRequest request, HttpResponse response) throws Exception;
}
