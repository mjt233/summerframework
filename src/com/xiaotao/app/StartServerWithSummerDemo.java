package com.xiaotao.app;

import com.xiaotao.app.service.ServiceA;
import com.xiaotao.summerframework.core.factory.BeanConfigureInfoFactory;
import com.xiaotao.summerframework.core.factory.BeanFactory;
import com.xiaotao.summerframework.web.annotation.WebController;
import com.xiaotao.summerframework.web.server.BindingMapping;
import com.xiaotao.summerframework.web.server.HttpServerBuilder;
import com.xiaotao.summerframework.web.http.handler.FileResourceHandler;

import java.io.IOException;

/**
 * 整合Summer框架进行开发
 */
public class StartServerWithSummerDemo {
    public static void main(String[] args) throws IOException {
        // 创建Bean工厂和Bean配置信息工厂
        BeanFactory beanFactory = new BeanFactory();
        BeanConfigureInfoFactory configureInfoFactory = new BeanConfigureInfoFactory();

        // 创建路由映射绑定信息类，设定默认的HttpHandler为FileResourcehandler
        BindingMapping mapping = new BindingMapping(new FileResourceHandler("D:\\"));

        // 给Bean工厂添加一个Bean装配监听器，路由映射自动配置在该监听器中处理
        beanFactory.addBeanReadyListener(mapping.getBeanFactoryListenerRegister());

        configureInfoFactory                                    // 使用Bean配置工厂
            .registerComponentAnnotation(WebController.class)   // 把WebController注解注册为组件标记
            .scanPackage("com.xiaotao")             // 扫描com.xiaotao包，通过注解得到该包下的Bean配置信息
            .forEach(beanFactory::registerBeanConfigureInfo);   // 将Bean配置信息注册到Bean工厂

        // 注册好Bean配置信息后，开始装配所有Bean
        beanFactory.factor();

        ServiceA bean = beanFactory.getBean(ServiceA.class);
        System.out.println(bean);
        System.out.println(bean.serviceB);
        System.out.println(bean.serviceB.serviceC);
        System.out.println(bean.serviceB.serviceC.serviceA);

        HttpServerBuilder
                .getInstance()
                .setPort(8081)
                .setBindingMapping(mapping)
                .build()
                .start();
    }
}
