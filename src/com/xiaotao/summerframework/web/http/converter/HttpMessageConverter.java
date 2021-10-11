package com.xiaotao.summerframework.web.http.converter;

import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;

/**
 * 控制器方法返回值消息转换器，实现针对特定Java数据类型的HTTP响应处理
 * @param <T>
 */
public interface HttpMessageConverter<T> {

    /**
     * 判断对象能否被该转换器执行转换操作
     * @param obj 传入要判断的对象
     * @return 转换器支持则返回true，否则返回false
     */
    boolean canWrite(Object obj);

    /**
     * 处理传入的Java数据对象，可按照自定义的方式对HTTP响应进行处理
     * @param t         待处理对象
     * @param request   HTTP请求
     * @param response  HTTP响应
     * @throws Exception 处理异常
     */
    void handleConvertWrite(T t, HttpRequest request, HttpResponse response) throws Exception;
}
