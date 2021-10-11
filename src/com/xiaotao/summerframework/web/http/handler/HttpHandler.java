package com.xiaotao.summerframework.web.http.handler;

import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;

@FunctionalInterface
public interface HttpHandler {
    Object handle(HttpRequest request, HttpResponse response) throws Exception;
}
