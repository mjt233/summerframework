package com.xiaotao.summerframework.web.http;

import com.xiaotao.summerframework.web.http.session.HttpSession;

public class HttpContextHolder {
    private static final ThreadLocal<HttpRequest> request = new ThreadLocal<>();
    private static final ThreadLocal<HttpResponse> response = new ThreadLocal<>();


    public static HttpSession getSession() { return HttpContextHolder.request.get().getSession(); }

    public static HttpRequest getRequest() {
        return request.get();
    }

    public static void setRequest(HttpRequest request) {
        HttpContextHolder.request.set(request);
    }

    public static HttpResponse getResponse() {
        return response.get();
    }

    public static void setResponse(HttpResponse response) {
        HttpContextHolder.response.set(response);
    }

    public static void clear() {
        response.remove();
        request.remove();
    }
}
