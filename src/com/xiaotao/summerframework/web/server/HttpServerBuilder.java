package com.xiaotao.summerframework.web.server;


import com.xiaotao.summerframework.web.http.converter.*;
import com.xiaotao.summerframework.web.http.session.HttpSessionProvider;
import com.xiaotao.summerframework.web.http.session.NativeHttpSessionProvider;

import java.util.ArrayList;
import java.util.List;

public class HttpServerBuilder {
    private String ip = "0.0.0.0";
    private int port = 8080;
    private final List<HttpMessageConverter<Object>> converters = new ArrayList<>();
    private HttpSessionProvider sessionProvider;
    private BindingMapping mapping;

    public static HttpServerBuilder getInstance() {
        return new HttpServerBuilder()
                .addConverter(new StringHttpMessageConverter())
                .addConverter(new NumberHttpMessageConverter())
                .addConverter(new DateHttpMessageConverter())
                .addConverter(new FileHttpMessageConverter());
    }

    public HttpServerBuilder setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public HttpServerBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public HttpServerBuilder setBindingMapping(BindingMapping mapping) {
        this.mapping = mapping;
        return this;
    }

    public HttpServerBuilder setSessionProvider(HttpSessionProvider provider) {
        this.sessionProvider = provider;
        return this;
    }

    public HttpServerBuilder addAllConverter(List<HttpMessageConverter<Object>> converters) {
        this.converters.addAll(converters);
        return this;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public HttpServerBuilder addConverter(HttpMessageConverter converter) {
        this.converters.add(converter);
        return this;
    }

    public HttpServer build() {
        return new HttpServer(ip, port,
                mapping == null ? new BindingMapping() : mapping,
                converters,
                sessionProvider == null ? new NativeHttpSessionProvider(30 * 60 * 60 * 1000) : sessionProvider
            );

    }
}
