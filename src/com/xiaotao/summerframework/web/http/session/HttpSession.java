package com.xiaotao.summerframework.web.http.session;

public interface HttpSession {
    String getSessionId();
    Object getAttr(String key);
    Object getAttr(String key, Object defaultVal);
    void setAttr(String key, Object value);
    boolean isExpired();
    long expiredAt();
    void renewal();
}
