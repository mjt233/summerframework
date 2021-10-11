package com.xiaotao.app.interceptor;

import com.xiaotao.summerframework.Logger;
import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;
import com.xiaotao.summerframework.web.http.interceptor.HttpInterceptor;

import java.io.IOException;

public class LoginInterceptor implements HttpInterceptor {
    private final static Logger log = new Logger();
    @Override
    public boolean beforeHandler(HttpRequest request, HttpResponse response) {
        if (request.getSession().getAttr("user") == null) {
            response.setStatus(403, "Forbidden");
            try {
                String msg = "未登录";
                response.setContentLength(msg.getBytes().length);
                response.write(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    @Override
    public void afterHandler(HttpRequest request, HttpResponse response) {

    }


}
