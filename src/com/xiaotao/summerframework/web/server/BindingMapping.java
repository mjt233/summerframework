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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BindingMapping {
    private static final Logger logger  = new Logger(BindingMapping.class);
    private final HashMap<String, HttpHandler> mapping = new HashMap<>();
    private HttpHandler defaultHandler;
    private final MappingAutoRegister register = new MappingAutoRegister(this);
    private final ArrayList<HttpInterceptorConfiguration> interceptors = new ArrayList<>();
    public BindingMapping() {
        this(new FileResourceHandler(""));
    }

    public BindingMapping(HttpHandler defaultHandler) {
        setDefaultHandler(defaultHandler);
    }

    public MappingAutoRegister getBeanFactoryListenerRegister() {
        return register;
    }

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
