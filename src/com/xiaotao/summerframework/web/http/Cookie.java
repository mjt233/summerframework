package com.xiaotao.summerframework.web.http;

import java.util.Date;

public class Cookie {
    /**
     * 同源策略类型
     */
    public enum SameSiteType {
        /**
         * 允许跨域发送，但必须是https且设置Secure属性
         */
        None,

        /**
         * 严格模式，禁止跨域，其他站点发起的请求不会带上Cookie
         */
        Strict,

        /**
         * 默认值，允许与顶级导航一起发送，并将与第三方网站发起的GET请求一起发送
         */
        Lax
    }
    public String name;
    public String value;
    public String domain;
    public String path;
    public Integer maxAge;
    public Boolean httpOnly;
    public Boolean secure;
    public SameSiteType sameSite;

    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Cookie() {}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append('=').append(value);
        if(maxAge != null) sb.append("; Max-Age").append('=').append(maxAge);
        if(domain != null) sb.append("; Domain").append('=').append(domain);
        if(path != null) sb.append("; Path").append('=').append(path);
        if(sameSite != null) sb.append("; SameSite").append('=').append(sameSite.toString());
        if(secure != null) sb.append("; Secure");
        if(httpOnly != null) sb.append("; HttpOnly");

        return sb.toString();
    }
}
