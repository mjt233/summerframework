package com.xiaotao.app.service;

import com.xiaotao.summerframework.core.annotation.Autowried;
import com.xiaotao.summerframework.core.annotation.Service;

@Service
public class ServiceC {
    @Autowried
    public ServiceA serviceA;

//    public ServiceC(ServiceA c) {
//        System.out.println(c);
//    }
}
