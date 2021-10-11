package com.xiaotao.summerframework.core.factory;

import com.xiaotao.summerframework.core.annotation.Autowried;
import com.xiaotao.summerframework.core.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bean装配配置信息类
 */
public class BeanConfigureInfoInfo {

    /**
     * 目标对象的class对象
     */
    private final Class<?> clazz;

    /**
     * bean名称
     */
    private final String name;

    /**
     * 构造方法的有序依赖集合（依赖的Bean名称集合）
     */
    private final String[] constructorDepends;

    /**
     * 字段依赖无序集合（Bean名称集合）
     */
    private final String[] fieldDepends;

    /**
     * 构造方法
     */
    private final Constructor<?> constructor;

    /**
     * Bean对象实例
     */
    public Object inst;

    /**
     * 通过.class获取整个类的依赖信息，会将类的构造方法和带有@Autowried注解的字段作为依赖
     * @param clazz 要解析的类
     * @return Bean配置信息
     */
    public static BeanConfigureInfoInfo getByClass(Class<?> clazz) {
        // Bean名称，将类名转换为小驼峰命名即可
        String name = StringUtils.toSmallCamelCase(clazz.getSimpleName());

        // 获取对象的所有构造方法，找到@Autowired标记的构造方法或参数数量最多的构造方法
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

    /**
     * 初始化一个Bean的依赖配置信息
     * @param clazz         类对象
     * @param constructor   使用的构造器
     * @param name          Bean名称
     */
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
                .filter(e -> e.getAnnotation(Autowried.class) != null) // 筛选出具有@Autowried注解的字段
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
