package com.xiaotao.app;

import com.xiaotao.app.controller.TestController;
import com.xiaotao.app.interceptor.LoginInterceptor;
import com.xiaotao.summerframework.web.enums.HttpMethod;
import com.xiaotao.summerframework.web.server.BindingMapping;
import com.xiaotao.summerframework.web.server.HttpServerBuilder;

import java.io.IOException;

/**
 * 演示添加HttpHandler的示例
 */
public class DirectStartServerDemo {
    public static void main(String[] args) throws IOException {

        // 使用文件资源操作器作为默认的操作器，并设定网站根目录项目目录下的static
        BindingMapping mapping = new BindingMapping("static");
        mapping
                // 添加一个控制器对象来注册路由和操作
                .addControllerObj(new TestController())
                // 一个简单HttpHandler添加示例
                .addMapping(HttpMethod.GET, "/simple", (request, response) -> "通过lambda创建HttpHandler并直接添加路由映射")
                // 添加简单的登录拦截器
                .addInterceptor("/api/*", "/api/login", new LoginInterceptor());

        // 创建服务器并启动
        HttpServerBuilder.create()
                .setBindingMapping(mapping)
                .build().start();
    }
}
