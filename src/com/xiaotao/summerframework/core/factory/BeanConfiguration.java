package com.xiaotao.summerframework.core.factory;

import com.xiaotao.summerframework.core.annotation.Autowried;
import com.xiaotao.summerframework.core.annotation.Bean;
import com.xiaotao.summerframework.core.util.StringUtils;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bean装配配置信息类
 */
public class BeanConfiguration {
    /**
     * Bean实例化方式
     */
    public enum InstanceType {
        /**
         * 通过构造方法实例化
         */
        CONSTRUCTOR,

        /**
         * 通过另一个Bean的方法调用的返回值获取实例
         */
        METHOD,

        /**
         * 仅执行方法调用，不处理返回值
         * 通常可用于Bean的setter方法注入
         */
        METHOD_CALL
    }
    private final InstanceType instanceType;

    /**
     * 如果采用方法调用，则存储方法对象，否则未null
     */
    private Method constructMethod;

    /**
     * 若采用方法调用，则应该由一个已完成装配的Bean对象进行调用
     * 这里存储调用者的Bean名称
     */
    private String callerBeanName;

    /**
     * 目标对象的class对象
     */
    private final Class<?> clazz;

    /**
     * bean名称
     */
    private final String name;

    /**
     * 构造器方法或Bean实例化方法的有序依赖集合（依赖的Bean名称集合）
     */
    private final String[] constructorDepends;

    /**
     * 字段依赖无序集合（Bean名称集合）
     */
    private String[] fieldDepends;

    /**
     * 构造方法
     */
    private Constructor<?> constructor;

    /**
     * Bean对象实例
     */
    public Object inst;

    private List<BeanConfiguration> subBean;

    /**
     * 通过.class获取整个类的依赖信息，会将类的构造方法和带有@Autowried注解的字段作为依赖
     * @param clazz 要解析的类
     * @return Bean配置信息
     */
    public static BeanConfiguration getByClass(Class<?> clazz) {
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
        return new BeanConfiguration(clazz, constructor, name);
    }

    public BeanConfiguration(Class<?> clazz, Method method, String beanName, String callerBeanName) {
        this.clazz = clazz;
        this.name = beanName;
        this.instanceType = InstanceType.METHOD;
        this.callerBeanName = callerBeanName;
        fieldDepends = new String[0];

        Parameter[] params = method.getParameters();
        String[] deps = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            deps[i] = StringUtils.toSmallCamelCase(params[i].getType().getSimpleName());
        }

        this.constructorDepends = deps;
        this.constructMethod = method;
    }

    /**
     * 初始化一个Bean的依赖配置信息
     * @param clazz         类对象
     * @param constructor   使用的构造器
     * @param name          Bean名称
     */
    public BeanConfiguration(Class<?> clazz, Constructor<?> constructor, String name) {
        this.clazz = clazz;
        this.name = name;
        this.constructor = constructor;
        this.instanceType = InstanceType.CONSTRUCTOR;

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

        // 读取@Bean注解标记的方法依赖注入
        Method[] methods = clazz.getDeclaredMethods();
        subBean = Arrays.stream(methods)
                .filter(e -> e.getAnnotation(Bean.class) != null)
                .map(e -> {
                    Class<?> targetClazz = e.getReturnType();
                    return new BeanConfiguration(
                            targetClazz, e, StringUtils.toSmallCamelCase(targetClazz.getSimpleName()), name
                    );
                }).collect(Collectors.toList());

    }


    /**
     * 创建Bean实例
     * @param obj   当Bean在另一个Bean对象中使用@Bean注解的方法创建时，需要对象方法的调用者对象，通过构造器方法实例化的Bean此时可为null
     * @param args  方法调用参数列表
     * @return      Bean实例
     */
    public Object constructInst(Object obj, Object...args) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (instanceType == InstanceType.CONSTRUCTOR) {
            return constructor.newInstance(args);
        } else {
            return constructMethod.invoke(obj, args);
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

    public InstanceType getInstanceType() {
        return instanceType;
    }

    public String getCallerBeanName() {
        return callerBeanName;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public List<BeanConfiguration> getSubBean() {
        return subBean;
    }

    @Override
    public String toString() {
        return "BeanConfigureInfoInfo{" +
                "clazz=" + clazz +
                ", name='" + name + '\'' +
                '}';
    }
}
