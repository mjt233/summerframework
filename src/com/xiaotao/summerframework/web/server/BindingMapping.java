package com.xiaotao.summerframework.web.server;

import com.xiaotao.summerframework.Logger;
import com.xiaotao.summerframework.web.annotation.RequestMapping;
import com.xiaotao.summerframework.web.enums.HttpMethod;
import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;
import com.xiaotao.summerframework.web.http.handler.FileResourceHandler;
import com.xiaotao.summerframework.web.http.handler.HttpControllerMethodHandler;
import com.xiaotao.summerframework.web.http.handler.HttpHandler;
import com.xiaotao.summerframework.web.http.interceptor.HttpInterceptor;
import com.xiaotao.summerframework.web.http.interceptor.HttpInterceptorConfiguration;
import com.xiaotao.summerframework.web.http.interceptor.InterceptorChains;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BindingMapping {
    private static final Logger logger  = new Logger();
    private final HashMap<String, HttpHandler> mapping = new HashMap<>();
    private HttpHandler defaultHandler;
    private final MappingAutoRegister register = new MappingAutoRegister(this);
    private final ArrayList<HttpInterceptorConfiguration> interceptors = new ArrayList<>();
    public BindingMapping() {
        setDefaultHandler(new FileResourceHandler(new File("").getAbsolutePath()));
    }

    public BindingMapping(String root) {
        root = new File(root).getAbsolutePath();
        setDefaultHandler(new FileResourceHandler(root));
    }

    public BindingMapping(HttpHandler defaultHandler) {
        setDefaultHandler(defaultHandler);
    }

    /**
     * 获取一个通过监听Summer Bean工厂装配事件自动注册路由的监听器
     * @return 路由映射自动注册监听器器
     */
    public MappingAutoRegister getBeanFactoryListenerRegister() {
        return register;
    }

    /**
     * 添加一个拦截器
     * @param urlPattern    匹配的URL
     * @param exceptPattern 排除的URL
     * @param interceptor   拦截器
     * @return 用于流式API调用的本身
     */
    public BindingMapping addInterceptor(String urlPattern, String exceptPattern, HttpInterceptor interceptor) {
        interceptors.add(new HttpInterceptorConfiguration(urlPattern, exceptPattern, interceptor));
        return this;
    }

    /**
     * 设置默认操作器，当call方法找不到路由映射时，将调用该操作器
     * @param defaultHandler 默认操作器
     * @return BindingMapping本身，方便流式调用
     */
    public BindingMapping setDefaultHandler(HttpHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
        logger.debug("初始默认操作器" + defaultHandler.getClass().getName());
        return this;
    }

    /**
     * 获取路由映射描述字符串
     * @param method    请求方法
     * @param url       请求路由
     */
    public static String getRouteDescribe(HttpMethod method, String url) {
        return method + "::" + url;
    }

    /**
     * 添加一个路由映射规则
     * @param method    请求方法
     * @param url       请求路由
     * @param handler   请求操作器
     * @return BindingMapping本身，方便流式调用
     */
    public BindingMapping addMapping(HttpMethod method, String url, HttpHandler handler) {
        String routeDescribe = getRouteDescribe(method, url);
        logger.debug("路由注册：" + routeDescribe);
        mapping.put(routeDescribe, handler);
        return this;
    }

    /**
     * 直接添加一个控制器对象，将通过对象中的注解创建对应的路由映射规则
     * @param controllerInst 控制器对象实例
     * @return BindingMapping本身，用于流式调用
     */
    public BindingMapping addControllerObj(Object controllerInst) {
        Class<?> controllerClass = controllerInst.getClass();
        try {
            // 获取对象上的@RequestMapping注解，用作请求路径的前缀，没有的话就空
            RequestMapping objAnno = controllerClass.getAnnotation(RequestMapping.class);
            String basePath = objAnno == null ? "" : objAnno.value();

            // 通过反射获取该对象的所有方法，并筛选出具有@RequestMapping注解的方法为其设置路由映射
            for (Method method : controllerClass.getDeclaredMethods()) {
                // 获取控制器类的方法RequestMapping注解，拼接URL添加映射
                RequestMapping methodAnno = method.getAnnotation(RequestMapping.class);
                if (methodAnno == null) continue;
                for (HttpMethod httpMethod : methodAnno.method()) {
                    String url = ("/" + basePath + "/" + methodAnno.value()).replaceAll("//+", "/");
                    addMapping(httpMethod, url, new HttpControllerMethodHandler(controllerInst, method));
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return this;
    }

    /**
     * 将原HttpHandler与拦截器链进行绑定，并以一个HttpHandler的形式对外暴露（设计模式：代理模式）
     * 当handle方法被执行时，会先依次执行拦截器链前置处理，完全通过后，会再执行原HttpHandler，最后以原路返回的形式执行拦截器后置处理
     */
    private static class HttpInterceptorHandler implements HttpHandler {
        private final HttpHandler handler;
        private final List<HttpInterceptorConfiguration> configurations;
        private final List<HttpInterceptor> matchedInterceptors = new ArrayList<>();

        public HttpInterceptorHandler(HttpHandler handler, List<HttpInterceptorConfiguration> interceptors) {
            this.handler = handler;
            this.configurations = interceptors;
        }

        @Override
        public Object handle(HttpRequest request, HttpResponse response) throws Exception {
            String url = request.getURL();
            for (HttpInterceptorConfiguration c : configurations) {
                if (c.match(url)) matchedInterceptors.add(c.getInterceptor());
            }
            return new InterceptorChains(matchedInterceptors, handler).doChains(request, response);
        }
    }

    /**
     * 获取指定URL和HTTP请求方法所对应的HttpHandler
     * @param method    HTTP请求方法
     * @param url       请求URL
     * @return          返回绑定了URL和方法的HttpHandler，若指定的URL和HTTP请求未匹配到一个映射，则返回默认的HttpHandler
     */
    public HttpHandler getHandler(HttpMethod method, String url) {
        // 尝试在映射表中找到对应的操作器
        HttpHandler handler = mapping.get(getRouteDescribe(method, url));
        if(handler == null) {
            return new HttpInterceptorHandler(defaultHandler, interceptors);
        } else {
            return new HttpInterceptorHandler(handler, interceptors);
        }
    }
}
