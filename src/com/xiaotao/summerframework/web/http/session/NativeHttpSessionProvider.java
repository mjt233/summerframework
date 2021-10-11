package com.xiaotao.summerframework.web.http.session;

import com.xiaotao.summerframework.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单的NativeSession提供者
 */
public class NativeHttpSessionProvider implements HttpSessionProvider {
    private final static Logger log = new Logger();
    private final Map<String, HttpSession> sessionStore = new ConcurrentHashMap<>();
    private final long liveAge;
    private final String sessionIdCookieName;

    /**
     * 初始化提供者
     * @param liveAge Session生存时间（单位毫秒）
     */
    public NativeHttpSessionProvider(long liveAge) {
        this(liveAge, "ezSessionId");
    }

    /**
     * 初始化提供者
     * @param liveAge Session生存时间（单位毫秒）
     * @param sessionIdCookieName Session的Cookie标识名
     */
    public NativeHttpSessionProvider(long liveAge, String sessionIdCookieName) {
        this.liveAge = liveAge;
        this.sessionIdCookieName = sessionIdCookieName;
    }



    @Override
    public String getSessionIdCookieName() {
        return sessionIdCookieName;
    }

    @Override
    public HttpSession getSession(String sessionId) {
        return sessionStore.get(sessionId);
    }

    @Override
    public HttpSession createSession() {
        String sessionId = UUID.randomUUID().toString();
        HttpSession session = new NativeSession(sessionId, liveAge);
        sessionStore.put(sessionId, session);
        return session;
    }

    @Override
    public void gc() {
        sessionStore.forEach((k,v) -> {
            if(v.isExpired()) {
                sessionStore.remove(k);
            }
        });
    }
}
