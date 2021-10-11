package com.xiaotao.summerframework.web.http.converter;

import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;

public class StringHttpMessageConverter implements HttpMessageConverter<Object> {
    @Override
    public boolean canWrite(Object obj) {
        return obj instanceof CharSequence;
    }

    @Override
    public void handleConvertWrite(Object s, HttpRequest request, HttpResponse response) throws Exception {
        response.write(s.toString());
    }
}
