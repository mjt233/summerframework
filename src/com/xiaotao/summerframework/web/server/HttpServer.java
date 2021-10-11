package com.xiaotao.summerframework.web.server;

import com.xiaotao.summerframework.Logger;
import com.xiaotao.summerframework.web.http.HttpContextHolder;
import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;
import com.xiaotao.summerframework.web.http.converter.HttpMessageConverter;
import com.xiaotao.summerframework.web.http.converter.UnknownReturnTypeException;
import com.xiaotao.summerframework.web.http.handler.HttpHandler;
import com.xiaotao.summerframework.web.http.session.HttpSessionGuard;
import com.xiaotao.summerframework.web.http.session.HttpSessionProvider;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class HttpServer {
    private static final Logger logger = new Logger(HttpServer.class);
    private ServerSocket serverSocket;
    private boolean inited;

    private final String ip;
    private final int port;

    private final byte[] ipBytes = new byte[4];

    private final BindingMapping mapping;
    private final List<HttpMessageConverter<Object>> converters;
    private final HttpSessionProvider sessionProvider;
    private final Map<Class<?>, HttpMessageConverter<Object>> converterCache = new ConcurrentHashMap<>();
    private final Thread sessionGCThread;

    private final ExecutorService pool = Executors.newCachedThreadPool(new ThreadFactory() {
        private final AtomicLong threadNum = new AtomicLong();
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("easy-http-server-pool-" + threadNum.getAndIncrement());
            return thread;
        }
    });

    public HttpServer(String ip, int port,
                      BindingMapping mapping, List<HttpMessageConverter<Object>> converters,
                      HttpSessionProvider sessionProvider
    ) {
        this.ip = ip;
        this.port = port;
        this.mapping = mapping;
        this.converters = converters;
        this.sessionProvider = sessionProvider;
        this.sessionGCThread = new Thread(new HttpSessionGuard(sessionProvider));
        int cnt = 0;
        for(String s:ip.split("\\.")) {
            ipBytes[cnt++] = (byte)Integer.parseInt(s);
        }
    }

    @SuppressWarnings("all")
    public void start() throws IOException {
        init();
        logger.info("HTTP服务器已启动，端口:" + port + " 绑定地址：" + ip);
        while (true) {
            Socket client = serverSocket.accept();
            pool.submit(() -> handleConnect(client));
        }
    }

    private void handleConnect(Socket client) {
        HttpRequest request;
        HttpResponse response = null;
        // 注意：不可替换为try with写法，会导致进入cache块时socket被关闭而无法响应错误信息
        try {
            response = new HttpResponse(client);
            try {
                request = new HttpRequest(client, sessionProvider);
            } catch (Exception e) {
                e.printStackTrace();
                String msg = "<h1 style=\"color: red\">Bad Request</h1><p>" + e.getMessage() + "</p>";
                response.setStatus(400, "Bad Request");
                response.setContentLength(msg.getBytes().length);
                response.write(msg);
                return;
            }
            HttpContextHolder.setRequest(request);
            HttpContextHolder.setResponse(response);
            HttpHandler handler = mapping.getHandler(request.getMethod(), request.getURL());
            Object returnValue = handler.handle(request, response);

            logger.debug("HttpHandler返回值：" + returnValue);

            // 若HttpHandler中未发送响应头，则在此处发送默认的响应头
            // 若HttpHandler中已发送过响应头（任何HttpResponse的Write方法都会导致响应头发送），此处将忽略HttpHandler的返回值
            if (!response.isHeaderSend()) {
                // 处理HttpHandler的返回值
                if (returnValue != null) handleReturnValue(returnValue, request, response);
                else {
                    response.setContentLength(0);
                    response.write("");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (response != null) {
                try {
                    StringBuilder msgBuilder = new StringBuilder();
                    response.setStatus(500, "Error");
                    String msg = msgBuilder
                            .append("<h1 style=\"color: red\">Server Error<h1>")
                            .append("<h3>Exception:")
                            .append(e.getClass().getName())
                            .append("</h3>")
                            .append("<h4>Message:")
                            .append(e.getMessage())
                            .append("</h4>")
                            .append(Arrays.toString(e.getStackTrace()))
                            .toString();
                    response.setContentLength(msg.getBytes().length);
                    response.write(msg);
                } catch (Exception e2) {e2.printStackTrace();}
            }
        } finally {
            HttpContextHolder.clear();
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 对HttpHandler执行后的返回值结果进行操作
     * @param ret   返回值
     * @param req   http请求
     * @param resp  http响应
     * @throws UnknownReturnTypeException 返回值不受支持时抛出
     */
    @SuppressWarnings("unchecked")
    private void handleReturnValue(Object ret, HttpRequest req, HttpResponse resp) throws Exception {
        if (ret == null) return;
        HttpMessageConverter<Object> converter;

        // 尝试从缓存中获取返回值操作器
        converter = converterCache.get(ret.getClass());
        if (converter == null) {
            // 缓存未命中，开始遍历转换器表搜索
            for (HttpMessageConverter<?> c: converters) {
                if (c.canWrite(ret)) {
                    // 搜索成功
                    converter = (HttpMessageConverter<Object>)c;
                    break;
                }
            }
            // 搜索失败，类型不可转换，标记为不可转换（异常）并缓存该结果
            if (converter == null) {
                // 为了区分缓存未命中与不受支持两种情况，该异常类同时也是个转换器，执行转换方法后抛出自己
                converter = new UnknownReturnTypeException(ret.getClass().getName());
            }
            // 更新缓存
            converterCache.put(ret.getClass(), converter);
        }

        converter.handleConvertWrite(ret, req, resp);
    }

    private void init() throws IOException {
        if (!inited) inited = true;
        else throw new IllegalStateException("重复初始化");

        serverSocket = new ServerSocket(port, 64, Inet4Address.getByAddress(ipBytes));
        sessionGCThread.start();
    }
}
