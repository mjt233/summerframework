package com.xiaotao.app.service;

import com.xiaotao.summerframework.core.annotation.Autowried;
import com.xiaotao.summerframework.core.annotation.Service;
import com.xiaotao.summerframework.web.server.HttpServer;

@Service
public class ServiceA {
    @Autowried
    public ServiceB serviceB;

    @Autowried
    public void httpServer(HttpServer server) {
        System.out.println("通过setter注入：" + server);
    }

}
