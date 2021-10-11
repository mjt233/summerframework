package com.xiaotao.summerframework.web.http.session;

import com.xiaotao.summerframework.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NativeHttpSessionProvider implements HttpSessionProvider {
    private final static Logger log = new Logger();
    private final Map<String, HttpSession> sessionStore = new ConcurrentHashMap<>();
    private final long liveAge;
    private final String sessionIdKey;

    public NativeHttpSessionProvider(long liveAge) {
        this(liveAge, "ezSessionId");
    }

    public NativeHttpSessionProvider(long liveAge, String sessionIdKey) {
        this.liveAge = liveAge;
        this.sessionIdKey = sessionIdKey;
    }



    @Override
    public String getSessionIdCookieName() {
        return sessionIdKey;
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
