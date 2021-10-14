package com.xiaotao.app.service;

import com.xiaotao.app.interceptor.LoginInterceptor;
import com.xiaotao.summerframework.core.annotation.Autowried;
import com.xiaotao.summerframework.core.annotation.Bean;
import com.xiaotao.summerframework.core.annotation.Component;
import com.xiaotao.summerframework.core.factory.ListenableBeanFactory;
import com.xiaotao.summerframework.web.server.BindingMapping;
import com.xiaotao.summerframework.web.server.HttpServer;
import com.xiaotao.summerframework.web.server.HttpServerBuilder;
import com.xiaotao.summerframework.web.server.MappingAutoRegister;

import java.io.IOException;

@Component
public class HttpService {
    @Autowried
    private ListenableBeanFactory beanFactory;

    @Bean
    public HttpServer httpServer() {
        // 配置路由映射对象
        BindingMapping mapping = new BindingMapping("static");
        MappingAutoRegister listener = mapping.getBeanFactoryListenerRegister();
        mapping.addInterceptor("/api/*", "/api/login", new LoginInterceptor());

        beanFactory.getAllBeanConfigureInfo().forEach(listener::handleCallback);
        beanFactory.addBeanReadyListener(listener);

        HttpServer server = HttpServerBuilder
                .create()
                .setBindingMapping(mapping)
                .build();

        beanFactory.addBeanFinishConstructListener(e -> {
            try {
                server.start();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        return server;
    }
}
