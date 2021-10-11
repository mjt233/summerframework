package com.xiaotao.summerframework.web.http.interceptor;

import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;
import com.xiaotao.summerframework.web.http.handler.HttpHandler;

import java.util.Collection;
import java.util.Iterator;

public class InterceptorChains {
    private final Iterator<HttpInterceptor> iterator;
    private final HttpHandler handler;
    private boolean executed = false;
    public InterceptorChains(Collection<HttpInterceptor> interceptors, HttpHandler handler) {
        this.handler = handler;
        this.iterator = interceptors.iterator();
    }

    public Object doChains(HttpRequest request, HttpResponse response) throws Exception {
        Object ret;
        if (iterator.hasNext()) {
            ret = doChains(request, response, iterator.next());
        } else {
            ret = handler.handle(request, response);
        }
        return ret;
    }

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
