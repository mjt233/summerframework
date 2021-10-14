package com.xiaotao.app;

import com.xiaotao.summerframework.web.server.BindingMapping;
import com.xiaotao.summerframework.web.server.HttpServerBuilder;

import java.io.IOException;

/**
 * 演示直接添加一个最小最简单的纯静态Http服务器示例
 */
public class SimpleHttpServerDemo {
    public static void main(String[] args) throws IOException {
        BindingMapping mapping = new BindingMapping("static");
        HttpServerBuilder.create()
                .setBindingMapping(mapping)
                .build()
                .start();
    }
}
