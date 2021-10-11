package com.xiaotao.summerframework.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记对象在IoC容器中装配时，获取依赖Bean对象的资源
 * 当在字段中使用时，依赖的Bean会在目标对象被实例化后注入，允许出现循环依赖
 * 当在对象构造方法中使用时，依赖的Bean会注入到构造方法参数，若在构造方法中出现循环依赖，IoC容器将初始化失败
 */
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowried {
}
