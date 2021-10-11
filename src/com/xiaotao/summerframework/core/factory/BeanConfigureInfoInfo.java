package com.xiaotao.summerframework.core.factory;

import com.xiaotao.summerframework.core.annotation.Autowried;
import com.xiaotao.summerframework.core.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BeanConfigureInfoInfo {
    private final Class<?> clazz;
    private final String name;
    private final String[] constructorDepends;
    private final String[] fieldDepends;
    private final Constructor<?> constructor;
    public Object inst;

    public static BeanConfigureInfoInfo getByClass(Class<?> clazz) {
        String name = StringUtils.toSmallCamelCase(clazz.getSimpleName());
        Constructor<?>[] constructors = clazz.getConstructors();
        Constructor<?> constructor = null, maxLenConstructor = null;
        int maxLen = 0;
        if (constructors.length == 1) {
            constructor = constructors[0];
        } else {

            // 无注解标记构造方法时，使用参数长度最长的构造方法
            for (Constructor<?> c : constructors) {
                if (c.getAnnotation(Autowried.class) != null) {
                    constructor = c;
                    break;
                } else if (c.getParameters().length >= maxLen) {
                    maxLen = c.getParameters().length;
                    maxLenConstructor = c;
                }
            }
            if (constructor == null) {
                constructor = maxLenConstructor;
            }
        }
        return new BeanConfigureInfoInfo(clazz, constructor, name);

    }

    public BeanConfigureInfoInfo(Class<?> clazz, Constructor<?> constructor, String name) {
        this.clazz = clazz;
        this.name = name;
        this.constructor = constructor;

        // 读取构造器依赖
        Parameter[] parameters = constructor.getParameters();
        constructorDepends = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            constructorDepends[i] = StringUtils.toSmallCamelCase(parameters[i].getType().getSimpleName());
        }

        // 读取字段注入依赖
        List<Field> fields = Arrays.stream(clazz.getDeclaredFields())
                .filter(e -> e.getAnnotation(Autowried.class) != null)
                .collect(Collectors.toList());
        fieldDepends = new String[fields.size()];

        int i = 0;
        for (Field field : fields) {
            fieldDepends[i++] = field.getName();
        }

    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getName() {
        return name;
    }

    public String[] getConstructorDepends() {
        return constructorDepends;
    }

    public String[] getFieldDepends() {
        return fieldDepends;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    @Override
    public String toString() {
        return "BeanConfigureInfoInfo{" +
                "clazz=" + clazz +
                ", name='" + name + '\'' +
                '}';
    }
}
