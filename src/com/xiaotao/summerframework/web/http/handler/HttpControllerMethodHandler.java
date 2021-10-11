package com.xiaotao.summerframework.web.http.handler;

import com.xiaotao.summerframework.web.annotation.RequestParam;
import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;
import com.xiaotao.summerframework.web.http.session.HttpSession;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class HttpControllerMethodHandler implements HttpHandler {
    private final Object obj;
    private final Method method;
    private final Parameter[] params;

    public HttpControllerMethodHandler(Object obj, Method method) {
        this.obj = obj;
        this.method = method;
        params = method.getParameters();
    }

    @Override
    public Object handle(HttpRequest request, HttpResponse response) throws Exception {
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            if (param.getType() == HttpRequest.class) {
                args[i] = request;
            } else if (param.getType() == HttpResponse.class) {
                args[i] = response;
            } else if (param.getType() == HttpSession.class || param.getType().isAssignableFrom(HttpSession.class)) {
                args[i] = request.getSession();
            } else {
                RequestParam rp = param.getAnnotation(RequestParam.class);
                if (rp == null) args[i] = null;
                else args[i] = request.getParameter(rp.value());
            }
        }
        try {
             return method.invoke(obj, args);
        } catch (InvocationTargetException e) {
            throw new Exception(e.getCause());
        }
    }
}
