package com.xiaotao.summerframework.web.http.converter;

import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;

import java.util.Date;

public class DateHttpMessageConverter implements HttpMessageConverter<Date> {
    @Override
    public boolean canWrite(Object obj) {
        return obj instanceof Date;
    }

    @Override
    public void handleConvertWrite(Date date, HttpRequest request, HttpResponse response) throws Exception {
        String s = date + "";
        response.setContentLength(s.getBytes().length);
        response.write(s);
    }
}
