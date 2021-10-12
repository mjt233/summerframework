package com.xiaotao.summerframework.web.server;

import com.xiaotao.summerframework.core.factory.BeanConfiguration;
import com.xiaotao.summerframework.core.factory.Listener;
import com.xiaotao.summerframework.web.annotation.WebController;

/**
 * 路由映射自动注册器，通过监听BeanFactory装配事件实现自动注册路由
 * 当一个带有@WebController注解的Bean被装配时，会将其作为一个控制器对象注册到路由映射器
 */
public class MappingAutoRegister implements Listener<BeanConfiguration> {
    private final BindingMapping mapping;

    public MappingAutoRegister(BindingMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public void handleCallback(BeanConfiguration i) {
        if(i.getClazz().getAnnotation(WebController.class) == null) return;
        mapping.addControllerObj(i.inst);
    }
}
