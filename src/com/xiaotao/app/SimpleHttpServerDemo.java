package com.xiaotao.app;

import com.xiaotao.summerframework.web.server.BindingMapping;
import com.xiaotao.summerframework.web.server.HttpServerBuilder;

import java.io.IOException;

public class SimpleHttpServerDemo {
    public static void main(String[] args) throws IOException {
        BindingMapping mapping = new BindingMapping("static");
        HttpServerBuilder.create()
                .setBindingMapping(mapping)
                .build()
                .start();
    }
}
