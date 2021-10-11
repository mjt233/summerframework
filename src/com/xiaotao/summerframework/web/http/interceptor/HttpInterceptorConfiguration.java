package com.xiaotao.summerframework.web.http.interceptor;

import java.util.regex.Pattern;

/**
 * 拦截器与匹配路径和排除路径的数据绑定类型
 */
public class HttpInterceptorConfiguration {
    private final Pattern pattern;
    private Pattern exceptPattern;
    private final HttpInterceptor interceptor;

    /**
     * 初始化一个Http拦截器配置信息类
     * @param pattern       匹配的URL正则表达式
     * @param exceptPattern 排除的URL正则表达式
     * @param interceptor   拦截器
     */
    public HttpInterceptorConfiguration(String pattern, String exceptPattern, HttpInterceptor interceptor) {
        this.pattern = Pattern.compile(pattern);
        if (exceptPattern != null) {
            this.exceptPattern = Pattern.compile(exceptPattern);
        }
        this.interceptor = interceptor;
    }

    /**
     * 判断一个URL是否应该经由拦截器
     * @param url   待测试的URL
     * @return 若URL与该拦截器的配置路由匹配，则返回true，否则返回false
     */
    public boolean match(String url) {
        return pattern.matcher(url).find() && (exceptPattern == null || !exceptPattern.matcher(url).find());
    }

    /**
     * 获取拦截器
     * @return 拦截器
     */
    public HttpInterceptor getInterceptor() {
        return interceptor;
    }



}
