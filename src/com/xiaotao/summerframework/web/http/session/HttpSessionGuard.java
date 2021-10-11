package com.xiaotao.summerframework.web.http.session;

import com.xiaotao.summerframework.Logger;

/**
 * HttpSession守卫，定期执行过期Session移除
 */
public class HttpSessionGuard implements Runnable {
    private final static Logger logger = new Logger();
    private final HttpSessionProvider provider;
    public HttpSessionGuard(HttpSessionProvider provider) {
        this.provider = provider;
    }

    @Override
    @SuppressWarnings("all")
    public void run() {
        logger.debug("Session守护服务启动");
        while (true) {
            try {
                Thread.sleep(60 * 1000);
                provider.gc();
            } catch (InterruptedException e) {
                break;
            }
        }
        logger.debug("Session守护服务结束");
    }
}
