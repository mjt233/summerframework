package com.xiaotao.summerframework.core.factory;

import java.util.List;

public interface BeanFactory {
    /**
     * 向容器中直接添加一个Bean
     * @param bean 要添加的Bean
     */
    BeanFactory addBeanInst(Object bean);

    /**
     * 通过Bean名称获取一个Bean
     * @param name 名称
     * @return Bean对象，若不存在则为null
     */
    Object getBean(String name);

    /**
     * 获取一个指定类型的Bean
     * @param t 类型
     * @return Bean对象，若不存在则为null
     */
    @SuppressWarnings("unchecked")
    <T> T getBean(Class<T> t);

    /**
     * 开始进行Bean装配工作，将解决所有待实例化和半成品Bean的依赖问题，若无法解决将抛出异常
     */
    void factor();

    /**
     * 获取所有已完成装配的Bean配置信息
     * @return 已完成装配的Bean信息
     */
    List<BeanConfiguration> getAllBeanConfigureInfo();

    /**
     * 注册一个需要装配的Bean信息
     * @param info Bean信息
     */
    void registerBeanConfigureInfo(BeanConfiguration info);
}
