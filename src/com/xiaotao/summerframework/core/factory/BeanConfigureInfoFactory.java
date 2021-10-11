package com.xiaotao.summerframework.core.factory;

import com.xiaotao.summerframework.core.annotation.Component;
import com.xiaotao.summerframework.core.annotation.Service;
import com.xiaotao.summerframework.web.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class BeanConfigureInfoFactory {
    private final List<Class<? extends Annotation>> componentAnnotations = new ArrayList<>();

    // 注册两个默认的装配标记注解
    {
        componentAnnotations.add(Component.class);
        componentAnnotations.add(Service.class);
    }

    public BeanConfigureInfoFactory registerComponentAnnotation(Class<? extends Annotation> clazz) {
        componentAnnotations.add(clazz);
        return this;
    }

    public List<BeanConfigureInfoInfo> scanPackage(String basePackage) {
        ArrayList<BeanConfigureInfoInfo> res = new ArrayList<>();
        try {
            ClassUtils.scanClass(basePackage)
                    .stream()
                    .filter(e -> {
                        for (Class<? extends Annotation> a : componentAnnotations) {
                            if (e.getAnnotation(a) != null) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .forEach(e -> res.add(BeanConfigureInfoInfo.getByClass(e)));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return res;
    }
}
