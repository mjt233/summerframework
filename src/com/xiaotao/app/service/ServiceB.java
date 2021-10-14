package com.xiaotao.app.service;

import com.xiaotao.summerframework.core.annotation.Autowried;
import com.xiaotao.summerframework.core.annotation.Service;

@Service
public class ServiceB {
    public ServiceC serviceC;

    @Autowried
    public ServiceB(ServiceC serviceC) {
        this.serviceC = serviceC;
    }
}
