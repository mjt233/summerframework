package com.xiaotao.summerframework.web.http.session;

/**
 * HttpSession提供者
 */
public interface HttpSessionProvider {
    /**
     * 或者Session ID的Cookie名
     * @return Cookie名
     */
    String getSessionIdCookieName();

    /**
     * 通过Cookie ID获取一个Session，若不存在Session则返回null
     * @param sessionId Session ID
     * @return 对应Session ID的Session
     */
    HttpSession getSession(String sessionId);

    /**
     * 创建一个新的Session
     * @return 新的Session
     */
    HttpSession createSession();

    /**
     * 移除已过期的Session
     */
    void gc();
}
