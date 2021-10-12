package com.xiaotao.app.service;

import com.xiaotao.summerframework.core.annotation.Autowried;
import com.xiaotao.summerframework.core.annotation.Bean;
import com.xiaotao.summerframework.core.annotation.Component;
import com.xiaotao.summerframework.core.factory.BeanFactory;
import com.xiaotao.summerframework.web.server.BindingMapping;
import com.xiaotao.summerframework.web.server.HttpServer;
import com.xiaotao.summerframework.web.server.HttpServerBuilder;
import com.xiaotao.summerframework.web.server.MappingAutoRegister;

import java.io.IOException;

@Component
public class HttpService {
    @Autowried
    private BeanFactory beanFactory;

    @Bean
    public HttpServer httpServer() throws IOException {
        BindingMapping mapping = new BindingMapping("static");
        MappingAutoRegister listener = mapping.getBeanFactoryListenerRegister();
        beanFactory.getAllBeanConfigureInfo()
                .forEach(listener::handleCallback);
        beanFactory.addBeanReadyListener(listener);
        HttpServer server = HttpServerBuilder.getInstance().setBindingMapping(mapping).build();
        server.start();
        return server;
    }
}
