package com.xiaotao.summerframework.web.http.converter;

import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;

public class NumberHttpMessageConverter implements HttpMessageConverter<Object> {
    @Override
    public boolean canWrite(Object obj) {
        return obj instanceof Integer ||
                obj instanceof Long ||
                obj instanceof Short ||
                obj instanceof Float ||
                obj instanceof Double;
    }

    @Override
    public void handleConvertWrite(Object o, HttpRequest request, HttpResponse response) throws Exception {
        response.write(o + "");
    }
}
