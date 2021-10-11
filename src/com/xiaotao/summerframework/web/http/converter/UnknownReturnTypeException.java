package com.xiaotao.summerframework.web.http.converter;

import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;

/**
 * 当控制器方法返回值类型不受支持时，可调用该转换器直接抛出异常
 */
public class UnknownReturnTypeException extends RuntimeException implements HttpMessageConverter<Object> {
    public UnknownReturnTypeException(String message) {
        super(message);
    }

    @Override
    public boolean canWrite(Object obj) {
        return true;
    }


    @Override
    public void handleConvertWrite(Object o, HttpRequest request, HttpResponse response) {
        throw this;
    }
}
