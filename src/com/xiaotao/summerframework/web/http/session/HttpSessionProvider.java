package com.xiaotao.summerframework.web.http.session;

public interface HttpSessionProvider {
    String getSessionIdCookieName();
    HttpSession getSession(String sessionId);
    HttpSession createSession();
    void gc();
}
