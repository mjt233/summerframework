package com.xiaotao.summerframework.web.http.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class NativeSession implements HttpSession {
    private final String id;
    private final AtomicLong expiredAt = new AtomicLong();
    private final long liveAge;
    private final Map<String, Object> store = new ConcurrentHashMap<>();
    public NativeSession(String id, long liveAge) {
        this.id = id;
        this.liveAge = liveAge;
        renewal();
    }

    @Override
    public String getSessionId() {
        return id;
    }

    @Override
    public Object getAttr(String key) {
        return getAttr(key, null);
    }

    @Override
    public Object getAttr(String key, Object defaultVal) {
        return store.getOrDefault(key, defaultVal);
    }

    @Override
    public void setAttr(String key, Object value) {
        store.put(key, value);
    }

    @Override
    public boolean isExpired() {
        return System.currentTimeMillis() > expiredAt.get();
    }

    @Override
    public long expiredAt() {
        return expiredAt.get();
    }

    @Override
    public void renewal() {
        this.expiredAt.set(System.currentTimeMillis() + liveAge);
    }
}
