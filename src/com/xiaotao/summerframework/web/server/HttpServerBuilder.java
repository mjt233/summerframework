package com.xiaotao.summerframework.web.server;


import com.xiaotao.summerframework.web.http.converter.*;
import com.xiaotao.summerframework.web.http.session.HttpSessionProvider;
import com.xiaotao.summerframework.web.http.session.NativeHttpSessionProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Http服务器建造器
 */
public class HttpServerBuilder {
    private String ip = "0.0.0.0";
    private int port = 8080;
    private final List<HttpMessageConverter<Object>> converters = new ArrayList<>();
    private HttpSessionProvider sessionProvider;
    private BindingMapping mapping;

    /**
     * 获取一个初始化的Http服务器建造器实例
     * @return 一个初始化的HttpServerBuilder
     */
    public static HttpServerBuilder getInstance() {
        // 添加5个默认的HttpMessageConverter
        return new HttpServerBuilder()
                .addConverter(new StringHttpMessageConverter())
                .addConverter(new NumberHttpMessageConverter())
                .addConverter(new DateHttpMessageConverter())
                .addConverter(new FileHttpMessageConverter());
    }


    /**
     * 设置服务器监听的网络接口IP地址
     * @param ip IP地址
     */
    public HttpServerBuilder setIp(String ip) {
        this.ip = ip;
        return this;
    }

    /**
     * 设置服务器端口号
     */
    public HttpServerBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * 设置路由映射对象（默认使用空Mapping）
     */
    public HttpServerBuilder setBindingMapping(BindingMapping mapping) {
        this.mapping = mapping;
        return this;
    }

    /**
     *  设置Session提供者（默认使用NativeHttpSessionProvider）
     *  @param provider Session提供者
     */
    public HttpServerBuilder setSessionProvider(HttpSessionProvider provider) {
        this.sessionProvider = provider;
        return this;
    }

    /**
     * 一次添加多个HttpMessageConverter
     * @param converters 转换器集合
     */
    public HttpServerBuilder addAllConverter(List<HttpMessageConverter<Object>> converters) {
        this.converters.addAll(converters);
        return this;
    }

    /**
     * 添加一个HttpMessageConverter
     * @param converter 转换器
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public HttpServerBuilder addConverter(HttpMessageConverter converter) {
        this.converters.add(converter);
        return this;
    }

    /**
     * 构建一个Http服务器
     * @return HttpServer
     */
    public HttpServer build() {
        return new HttpServer(ip, port,
                mapping == null ? new BindingMapping() : mapping,
                converters,
                sessionProvider == null ? new NativeHttpSessionProvider(30 * 60 * 60 * 1000) : sessionProvider
            );

    }
}
