package com.xiaotao.summerframework.web.http.session;

/**
 * HttpSession
 */
public interface HttpSession {
    /**
     * 获取Session ID
     * @return Session ID
     */
    String getSessionId();

    /**
     * 从Session中根据key获取一个对象
     * @param key key
     * @return 获取到的对象，若不存在则为null
     */
    Object getAttr(String key);


    /**
     * 从Session中根据key获取一个对象
     * @param key key
     * @param defaultVal 默认值
     * @return 获取到的对象，若不存在则为默认值
     */
    Object getAttr(String key, Object defaultVal);

    /**
     * 设置一个对象到Session中
     * @param key 键
     * @param value 值
     */
    void setAttr(String key, Object value);

    /**
     * 判断该Session是否已过期
     * @return true - 已过期，false - 有效
     */
    boolean isExpired();

    /**
     * 获取Session当前过期时间，这个值可能会随着renewal方法的执行而更新
     * @return 过期时间（Unix时间戳，毫秒）
     */
    long expiredAt();

    /**
     * 对Session有效期续约
     */
    void renewal();
}
