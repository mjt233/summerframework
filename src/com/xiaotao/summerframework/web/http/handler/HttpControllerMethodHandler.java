package com.xiaotao.summerframework.web.http.handler;

import com.xiaotao.summerframework.web.annotation.RequestParam;
import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;
import com.xiaotao.summerframework.web.http.session.HttpSession;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Http控制器方法执行器，直接使用某个对象的方法作为HttpHandler的处理逻辑（设计模式：代理模式）
 * 同时还会根据目标方法的参数类型自动填充HttpRequest，HttpResponse，HttpSession对象
 * 被@RequestParam注解标记的参数还会自动填充为指定HTTP表单参数的值
 */
public class HttpControllerMethodHandler implements HttpHandler {
    private final Object obj;
    private final Method method;
    private final Parameter[] params;

    /**
     * 设定被代理的对象方法，创建一个HttpHandler实例
     * @param obj       被代理对象
     * @param method    被代理方法
     */
    public HttpControllerMethodHandler(Object obj, Method method) {
        this.obj = obj;
        this.method = method;
        params = method.getParameters();
    }

    @Override
    public Object handle(HttpRequest request, HttpResponse response) throws Exception {
        // 执行被代理方法所需的参数数据列表
        Object[] args = new Object[params.length];

        // 开始填充方法参数
        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            if (param.getType() == HttpRequest.class) {
                args[i] = request;
            } else if (param.getType() == HttpResponse.class) {
                args[i] = response;
            } else if (param.getType() == HttpSession.class || param.getType().isAssignableFrom(HttpSession.class)) {
                args[i] = request.getSession();
            } else {
                // 若参数被@RequestParam标记，则从request中获取对应的表单参数的值
                RequestParam rp = param.getAnnotation(RequestParam.class);
                if (rp == null) args[i] = null;
                else args[i] = request.getParameter(rp.value());
            }
        }
        try {
            // 调用目标对象的方法
             return method.invoke(obj, args);
        } catch (InvocationTargetException e) {
            // 捕获调用的方法抛出异常，获取源异常后（通过反射调用的方法抛出异常时，源异常被包装了，这里需要拆出来），重新在此层抛出，以让上层得知异常的原因
            throw new Exception(e.getCause());
        }
    }
}
