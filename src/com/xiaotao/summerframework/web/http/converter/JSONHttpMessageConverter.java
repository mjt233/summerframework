package com.xiaotao.summerframework.web.http.converter;

import com.xiaotao.summerframework.util.json.JSONEncoder;
import com.xiaotao.summerframework.util.json.JSONObject;
import com.xiaotao.summerframework.util.json.SimpleJsonEncoder;
import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;

public class JSONHttpMessageConverter implements HttpMessageConverter<Object> {
    private final static JSONEncoder encoder = new SimpleJsonEncoder();
    @Override
    public boolean canWrite(Object obj) {
        return obj.getClass().getAnnotation(JSONObject.class) != null;
    }

    @Override
    public void handleConvertWrite(Object o, HttpRequest request, HttpResponse response) throws Exception {
        String msg = encoder.encode(o);
        response.setContentLength(msg.getBytes().length);
        response.setContentType("application/json;charset=utf-8");
        response.write(msg);
    }
}
