package com.xiaotao.app.service;

import com.xiaotao.summerframework.core.annotation.Autowried;
import com.xiaotao.summerframework.core.annotation.Service;

@Service
public class ServiceA {
    @Autowried
    public ServiceB serviceB;
}
