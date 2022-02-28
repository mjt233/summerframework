package com.xiaotao.app.service;

import com.xiaotao.app.interceptor.LoginInterceptor;
import com.xiaotao.summerframework.core.annotation.Autowried;
import com.xiaotao.summerframework.core.annotation.Bean;
import com.xiaotao.summerframework.core.annotation.Component;
import com.xiaotao.summerframework.core.factory.ListenableBeanFactory;
import com.xiaotao.summerframework.web.http.session.HttpSession;
import com.xiaotao.summerframework.web.http.session.HttpSessionProvider;
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

        // 获取当前所有已完成装配的Bean配置信息并交给自动注册监听器处理
        beanFactory.getAllBeanConfigureInfo().forEach(listener::handleCallback);

        // 添加路由映射对象的注册监听器到bean工厂的事件监听器列表里，以方便对后续装配的控制器Bean进行路由注册
        beanFactory.addBeanReadyListener(listener);

        // 创建服务器实例
        HttpServer server = HttpServerBuilder
                .create()
                .setBindingMapping(mapping)
                .build();

        // 注册Bean工厂事件回调（其实就是个lambda写的监听器），Bean工厂完成所有Bean装配后，启动服务器
        beanFactory.addBeanFinishConstructListener(e -> {
            try {
                server.start();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        // 返回HTTP服务器实例
        return server;
    }
}
