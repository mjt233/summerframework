package com.xiaotao.summerframework.web.http.interceptor;

import java.util.regex.Pattern;

public class HttpInterceptorConfiguration {
    private final Pattern pattern;
    private Pattern exceptPattern;
    private final HttpInterceptor interceptor;

    public HttpInterceptorConfiguration(String pattern, String exceptPattern, HttpInterceptor interceptor) {
        this.pattern = Pattern.compile(pattern);
        if (exceptPattern != null) {
            this.exceptPattern = Pattern.compile(exceptPattern);
        }
        this.interceptor = interceptor;
    }

    public boolean match(String url) {
        return pattern.matcher(url).find() && (exceptPattern == null || !exceptPattern.matcher(url).find());
    }

    public HttpInterceptor getInterceptor() {
        return interceptor;
    }



}
