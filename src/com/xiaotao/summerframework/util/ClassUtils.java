package com.xiaotao.summerframework.util;

import java.util.*;

public class ClassUtils {
    /**
     * 获取类的所有父类和实现的接口
     * @param clazz 要搜索的类
     */
    public static Collection<Class<?>> getAllParentClass(Class<?> clazz) {
        List<Class<?>> res = new LinkedList<>();
        LinkedList<Class<?>> t = new LinkedList<>();
        t.add(clazz);
        while (!t.isEmpty()) {
            Class<?> c = t.pop();
            res.add(c);
            Class<?> s = c.getSuperclass();
            if (s != null && s != Object.class) {
                t.add(s);
            }
            t.addAll(Arrays.asList(c.getInterfaces()));
        }
        return res;
    }
}
