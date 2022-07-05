package com.xiaotao.summerframework.core.factory;

import com.xiaotao.summerframework.Logger;
import com.xiaotao.summerframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean工厂，IoC容器的核心
 */
public class ListenableBeanFactory implements BeanFactory {

    /**
     * 可监听put方法执行的HashMap，直接继承了ConcurrentHashMap
     */
    private static class ListenableHashMap
            extends ConcurrentHashMap<String, BeanConfiguration> {

        private final List<Listener<BeanConfiguration>> listeners = new ArrayList<>();


        /**
         * 添加put一个事件监听器，put入对象时触发
         * @param listener  监听器
         * @return  用于流式API调用的自己
         */
        public ListenableHashMap addListener(Listener<BeanConfiguration> listener) {
            synchronized (listeners) {
                listeners.add(listener);
            }
            return this;
        }

        /**
         * 放入一个已完成装配的Bean对象，同时触发所有的监听器
         * @param key   Bean名
         * @param value Bean对象实例
         * @return 用于流式API调用的自己
         */
        @Override
        public BeanConfiguration put(String key, BeanConfiguration value) {
            if (value.inst == null) return null;
            logger.debug("finish bean construct:" + value.getClazz().getName());
            BeanConfiguration info = super.put(key, value);
            synchronized (listeners) {
                for (Listener<BeanConfiguration> listener : listeners) {
                    listener.handleCallback(value);
                }
            }
            return info;
        }
    }
    private final static Logger logger = new Logger();

    /**
     * 完成装配的单例Bean容器
     */
    private final ListenableHashMap container = new ListenableHashMap();

    /**
     * 等待实例化的Bean
     */
    private final Map<String, BeanConfiguration> waiting = new ConcurrentHashMap<>();

    /**
     * 已完成实例化但存在未解决的依赖的半成品Bean
     */
    private final Map<String, BeanConfiguration> creating = new ConcurrentHashMap<>();

    private final List<Listener<BeanFactory>> finishListeners = new ArrayList<>();

    private final static String banner =
            "     _____                                     \n" +
            "    / ____|                                    \n" +
            "   | (___  _   _ _ __ ___  _ __ ___   ___ _ __ \n" +
            "    \\___ \\| | | | '_ ` _ \\| '_ ` _ \\ / _ \\ '__|\n" +
            "    ____) | |_| | | | | | | | | | | |  __/ |   \n" +
            "   |_____/ \\__,_|_| |_| |_|_| |_| |_|\\___|_|   \n" +
            "  ========:: A Fake Spring Framework ::======== \n" +
            "                                             ";

    /**
     * 向容器中直接添加一个Bean
     * @param bean 要添加的Bean
     */
    @Override
    public BeanFactory addBeanInst(Object bean) {
        BeanConfiguration beanInfo = BeanConfiguration.getByClass(bean.getClass());
        beanInfo.inst = bean;
        container.put(StringUtils.toSmallCamelCase(bean.getClass().getSimpleName()), beanInfo);
        return this;
    }

    /**
     * 通过Bean名称获取一个Bean
     * @param name 名称
     * @return Bean对象，若不存在则为null
     */
    @Override
    public Object getBean(String name) {
        BeanConfiguration obj = container.get(name);
        return obj == null ? null : obj.inst;
    }

    /**
     * 获取一个指定类型的Bean
     * @param t 类型
     * @return Bean对象，若不存在则为null
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> t) {
        Object bean = getBean(StringUtils.toSmallCamelCase(t.getSimpleName()));
        if (bean != null) return (T)bean;
        else return null;
    }

    /**
     * 注册一个需要装配的Bean信息
     * @param info Bean信息
     */
    @Override
    public void registerBeanConfigureInfo(BeanConfiguration info) {
        logger.debug("register bean: " + info);

        if (info.getSubBean() != null && info.getSubBean().size() > 0) {
            info.getSubBean().forEach(this::registerBeanConfigureInfo);
        }

        // 如果Bean信息中已经包含了Bean实例就直接put到容器
        if (info.inst != null) {
            container.put(info.getName(), info);
            return;
        }

        BeanConfiguration.Type type = info.getType();
        if (type == BeanConfiguration.Type.METHOD) {
            waiting.put(info.getName(), info);
        } else if(type == BeanConfiguration.Type.METHOD_CALL) {
            waiting.put(UUID.randomUUID().toString(), info);
        } else if(type == BeanConfiguration.Type.CONSTRUCTOR) {
            // 放入待实例化容器
            waiting.put(info.getName(), info);
        }

    }

    /**
     * 进行一轮尝试，对未实例化的Bean进行实例化
     */
    private void tryConstructWaiting() {
        // 对所有待实例化的Bean集合进行遍历
        waiting.forEach((key, value) -> {
            String callerBeanName = value.getCallerBeanName();
            Object callerBean = null;
            if (callerBeanName != null && (callerBean = getBean(callerBeanName)) == null) {
                return;
            }
            // 找到的构造方法参数依赖计数
            int cnt = 0;
            String[] deps = value.getConstructorDepends();
            Object[] args = new Object[deps.length];

            // 开始查找依赖
            for (int i = 0; i < deps.length; i++) {
                BeanConfiguration arg = container.get(deps[i]);
                if (arg != null) {
                    args[i] = arg.inst;
                    cnt++;
                }
            }

            if (deps.length == cnt) {
                // 现有依赖满足构造方法需求则进行实例化操作
                try {
                    value.inst = value.constructInst(callerBean, args);

                    // 依据字段依赖的有无选择放入完成装配容器或半成品容器
                    if (value.getFieldDepends().length == 0 && value.getType() != BeanConfiguration.Type.METHOD_CALL) {
                        container.put(key, value);
                    } else {
                        creating.put(key, value);
                    }
                    // 从待实例化容器中移除自己
                    waiting.remove(key);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    /**
     * 尝试进行一轮半成品Bean的完全装配操作
     */
    private void tryConstructCreating() {

        // 对所有待装配的Bean集合进行遍历
        creating.forEach((k,v) -> {
            String[] deps = v.getFieldDepends();
            // 已解决的依赖计数
            int cnt = 0;

            // 查找存在的依赖
            for (String dep : deps) {
                BeanConfiguration depInstInfo = container.getOrDefault(dep, creating.get(dep));
                if (depInstInfo != null) {
                    // 找到依赖，注入到字段，已解决的依赖计数加一
                    try {
                        Field field = v.getClazz().getDeclaredField(StringUtils.toSmallCamelCase(dep));
                        field.setAccessible(true);
                        field.set(v.inst, depInstInfo.inst);
                        cnt++;
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                }
            }

            // 已解决的依赖计数与依赖数相同时，表示该Bean已完成完全装配
            if (cnt == deps.length){
                // 将Bean从半成品容器移动到完全装配容器
                creating.remove(k);
                container.put(k, v);
            }
        });
    }

    /**
     * 开始进行Bean装配工作，将解决所有待实例化和半成品Bean的依赖问题，若无法解决将抛出异常
     */
    @Override
    public void factor() {

        // 注册自己到容器
        if (container.get("beanFactory") == null) {
            BeanConfiguration self = BeanConfiguration.getByClass(ListenableBeanFactory.class);
            self.inst = this;
            container.put("beanFactory", self);
        } else {
            // 已存在beanFactory的话说明已经执行过构造了
            throw new IllegalStateException("bean factory has already finish bean construct");
        }


        int size = waiting.size();


        // 对未实例化的Bean进行实例化操作
        while (size > 0) {
            // 先对半成品Bean进行初次完全装配
            // 因为可能有一些未实例化的Bean依赖的Bean处于半成品状态但其实依赖已完善，而未满足完全依赖的半成品Bean，可能依赖了未实例化的Bean
            tryConstructCreating();

            // 进行一轮实例化尝试操作操作
            tryConstructWaiting();

            // 进行一轮尝试后，待实例化的Bean数量未发生变化说明存在不可满足的依赖
            // 此时只能摆烂了
            if (size == waiting.size()) {
                StringBuilder sb = new StringBuilder();
                waiting.forEach((k, v) -> sb.append(k).append(" "));
                throw new IllegalStateException("Failed finish bean construct: " + sb);
            } else {
                size = waiting.size();
            }
        }

        // 对剩下的半成品Bean进行最后一次完全装配尝试
        tryConstructCreating();

        // 因为半成品Bean都只剩下了字段注入，若所有依赖均被满足，则应该会完成所有Bean的装配
        // 若仍然存在半成品Bean未完成完全装配，则说明存在未被满足的依赖，摆烂
        if (creating.size() > 0) {
            throw new IllegalStateException("Failed finish bean filed inject");
        }

        System.out.println(banner);

        for (Listener<BeanFactory> l : finishListeners) {
            l.handleCallback(this);
        }

    }

    /**
     * 添加一个Bean完成装配时候的监听器
     * @param listener 监听器，Bean完成装配时触发
     * @return  用于流式API调用的自己
     */
    public BeanFactory addBeanReadyListener(Listener<BeanConfiguration> listener) {
        container.addListener(listener);
        return this;
    }

    public BeanFactory addBeanFinishConstructListener(Listener<BeanFactory> listener) {
        synchronized (finishListeners) {
            finishListeners.add(listener);
        }
        return this;
    }

    /**
     * 获取所有已完成装配的Bean配置信息
     * @return 已完成装配的Bean信息
     */
    @Override
    public List<BeanConfiguration> getAllBeanConfigureInfo() {
        return new ArrayList<>(container.values());
    }
}
