package com.xiaotao.summerframework.web.server;

import com.xiaotao.summerframework.core.factory.BeanConfigureInfoInfo;
import com.xiaotao.summerframework.core.factory.Listener;
import com.xiaotao.summerframework.web.annotation.WebController;

public class MappingAutoRegister implements Listener<BeanConfigureInfoInfo> {
    private final BindingMapping mapping;

    public MappingAutoRegister(BindingMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public void handleCallback(BeanConfigureInfoInfo i) {
        if(i.getClazz().getAnnotation(WebController.class) == null) return;
        mapping.addControllerObj(i.inst);
    }
}
