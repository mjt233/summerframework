package com.xiaotao.summerframework.web.http;

import java.util.Date;

public class Cookie {
    public enum SameSiteType {
        None, Strict, Lax
    }
    public String name;
    public String value;
    public String domain;
    public String path;
    public Integer maxAge;
    public Boolean httpOnly;
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
        if(httpOnly != null) sb.append("; HttpOnly");

        return sb.toString();
    }
}
