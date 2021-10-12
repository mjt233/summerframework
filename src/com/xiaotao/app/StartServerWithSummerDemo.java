package com.xiaotao.app;

import com.xiaotao.summerframework.core.factory.BeanConfigureInfoFactory;
import com.xiaotao.summerframework.core.factory.BeanFactory;
import com.xiaotao.summerframework.web.annotation.WebController;

/**
 * 整合Summer框架进行开发
 */
public class StartServerWithSummerDemo {
    public static void main(String[] args) {
        // 创建Bean工厂和Bean配置信息工厂
        BeanFactory beanFactory = new BeanFactory();
        BeanConfigureInfoFactory configureInfoFactory = new BeanConfigureInfoFactory();

        configureInfoFactory                                    // 使用Bean配置工厂
            .registerComponentAnnotation(WebController.class)   // 把WebController注解注册为组件标记
            .scanPackage("com.xiaotao")             // 扫描com.xiaotao包，通过注解得到该包下的Bean配置信息
            .forEach(beanFactory::registerBeanConfigureInfo);   // 将Bean配置信息注册到Bean工厂

        // 注册好Bean配置信息后，开始装配所有Bean
        beanFactory.factor();
    }
}
