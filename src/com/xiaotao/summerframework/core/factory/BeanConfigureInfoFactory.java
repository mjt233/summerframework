package com.xiaotao.summerframework.core.factory;

import com.xiaotao.summerframework.core.annotation.Component;
import com.xiaotao.summerframework.core.annotation.Service;
import com.xiaotao.summerframework.web.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean配置信息工厂，用于方便地创建Bean配置信息
 */
public class BeanConfigureInfoFactory {
    private final List<Class<? extends Annotation>> componentAnnotations = new ArrayList<>();

    // 注册两个默认的装配标记注解
    {
        componentAnnotations.add(Component.class);
        componentAnnotations.add(Service.class);
    }

    /**
     * 添加一个标记对象需要被装配的注解
     * @param clazz 注解类
     * @return 用户流式API调用的自己
     */
    public BeanConfigureInfoFactory registerComponentAnnotation(Class<? extends Annotation> clazz) {
        componentAnnotations.add(clazz);
        return this;
    }

    /**
     * 通过扫描包获取包下的需要被装配的Bean信息
     * @param basePackage 包
     * @return Bean配置信息类集合
     */
    public List<BeanConfiguration> scanPackage(String basePackage) {
        ArrayList<BeanConfiguration> res = new ArrayList<>();
        try {
            // 扫描包下的所有类（不完善，仅限本地.class文件）
            ClassUtils.scanClass(basePackage)
                    .stream()
                    .filter(e -> {
                        // 筛选出具有装配注解的类
                        for (Class<? extends Annotation> a : componentAnnotations) {
                            if (e.getAnnotation(a) != null) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .forEach(e -> res.add(BeanConfiguration.getByClass(e)));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return res;
    }
}
