package com.xiaotao.summerframework.web.http.converter;

import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;

public interface HttpMessageConverter<T> {
    boolean canWrite(Object obj);
    void handleConvertWrite(T t, HttpRequest request, HttpResponse response) throws Exception;
}
