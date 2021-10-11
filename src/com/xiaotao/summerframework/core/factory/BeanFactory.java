package com.xiaotao.summerframework.core.factory;

import com.xiaotao.summerframework.Logger;
import com.xiaotao.summerframework.core.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeanFactory {

    private static class ListenableHashMap
            extends ConcurrentHashMap<String, BeanConfigureInfoInfo> {

        private final List<Listener<BeanConfigureInfoInfo>> listeners = new ArrayList<>();


        public ListenableHashMap addListener(Listener<BeanConfigureInfoInfo> listener) {
            synchronized (listeners) {
                listeners.add(listener);
            }
            return this;
        }

        @Override
        public BeanConfigureInfoInfo put(String key, BeanConfigureInfoInfo value) {
            BeanConfigureInfoInfo info = super.put(key, value);
            synchronized (listeners) {
                for (Listener<BeanConfigureInfoInfo> listener : listeners) {
                    logger.debug("finish bean construct:" + value.getClazz().getName());
                    listener.handleCallback(value);
                }
            }
            return info;
        }
    }
    private final static Logger logger = new Logger(BeanFactory.class);
    private final ListenableHashMap container = new ListenableHashMap();
    private final Map<String, BeanConfigureInfoInfo> waiting = new ConcurrentHashMap<>();
    private final Map<String, BeanConfigureInfoInfo> creating = new ConcurrentHashMap<>();

    private final static String banner =
            "     _____                                     \n" +
            "    / ____|                                    \n" +
            "   | (___  _   _ _ __ ___  _ __ ___   ___ _ __ \n" +
            "    \\___ \\| | | | '_ ` _ \\| '_ ` _ \\ / _ \\ '__|\n" +
            "    ____) | |_| | | | | | | | | | | |  __/ |   \n" +
            "   |_____/ \\__,_|_| |_| |_|_| |_| |_|\\___|_|   \n" +
            "  ========:: A Fake Spring Framework ::======== \n" +
            "                                             ";
    private boolean bannerPrint = false;

    public BeanFactory() {
    }

    /**
     * 向容器中直接添加一个Bean
     * @param bean 要添加的Bean
     */
    public BeanFactory addBeanInst(Object bean) {
        BeanConfigureInfoInfo beanInfo = BeanConfigureInfoInfo.getByClass(bean.getClass());
        beanInfo.inst = bean;
        container.put(StringUtils.toSmallCamelCase(bean.getClass().getSimpleName()), beanInfo);
        return this;
    }

    public Object getBean(String name) {
        return container.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> t) {
        Object bean = getBean(StringUtils.toSmallCamelCase(t.getSimpleName()));
        if (bean != null) return (T)((BeanConfigureInfoInfo)bean).inst;
        else return null;
    }

    public void registerBeanConfigureInfo(BeanConfigureInfoInfo info) {
        logger.debug("register bean: " + info);
        if (info.inst != null) {
            container.put(info.getName(), info);
            return;
        }
        try {
            String[] depends = info.getConstructorDepends();
            if(depends.length == 0) {
                Object inst = info.getClazz().getConstructor().newInstance();
                info.inst = inst;
                if (info.getFieldDepends().length == 0) {
                    container.put(info.getName(), info);
                } else {
                    creating.put(info.getName(), info);
                }
                info.inst = inst;
            } else {
                waiting.put(info.getName(), info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryConstructWaiting() {
        waiting.forEach((key, value) -> {
            int cnt = 0;
            String[] deps = value.getConstructorDepends();
            Object[] args = new Object[deps.length];
            for (int i = 0; i < deps.length; i++) {
                BeanConfigureInfoInfo arg = container.get(deps[i]);
                if (arg != null) {
                    args[i] = arg.inst;
                    cnt++;
                }
            }
            if (deps.length == cnt) {
                try {
                    value.inst = value.getConstructor().newInstance(args);
                    if (value.getFieldDepends().length == 0) {
                        container.put(key, value);
                    } else {
                        creating.put(key, value);
                    }
                    waiting.remove(key);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    private void tryConstructCreating() {
        creating.forEach((k,v) -> {
            String[] deps = v.getFieldDepends();
            int cnt = 0;
            for (String dep : deps) {
                BeanConfigureInfoInfo depInstInfo = container.getOrDefault(dep, creating.get(dep));
                if (depInstInfo != null) {
                    try {
                        v.getClazz().getField(StringUtils.toSmallCamelCase(dep)).set(v.inst, depInstInfo.inst);
                        cnt++;
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (cnt == deps.length){
                container.put(k, v);
                creating.remove(k);
            }
        });
    }

    public void factor() {
        // 注册自己
        if (container.get("beanFactory") == null) {
            BeanConfigureInfoInfo self = BeanConfigureInfoInfo.getByClass(BeanFactory.class);
            self.inst = this;
            container.put("beanFactory", self);
        }

        tryConstructCreating();
        int size = waiting.size();
        // 对通过构造方法实例化的Bean进行构造
        while (size > 0) {
            tryConstructWaiting();
            if (size == waiting.size()) {
                StringBuilder sb = new StringBuilder();
                waiting.forEach((k, v) -> sb.append(k).append(" "));
                throw new IllegalCallerException("Failed finish bean construct: " + sb);
            } else {
                size = waiting.size();
            }
        }

        tryConstructCreating();
        if (creating.size() > 0) {
            throw new IllegalCallerException("Failed finish bean filed inject");
        }
        if (!bannerPrint) {
            bannerPrint = true;
            System.out.println(banner);
        }
    }

    public BeanFactory addBeanReadyListener(Listener<BeanConfigureInfoInfo> listener) {
        container.addListener(listener);
        return this;
    }
}
