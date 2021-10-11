package com.xiaotao.summerframework.web.annotation;

import com.xiaotao.summerframework.web.enums.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记控制器或方法使用的路由映射
 * 当控制器和方法同时出现时，路径将拼接，HTTP请求方法以对象方法注解中的为准
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    String value();
    HttpMethod[] method() default HttpMethod.GET;
}
