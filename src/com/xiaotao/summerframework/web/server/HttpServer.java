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

/**
 * Http服务器
 */
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

    /**
     * HTTP服务器线程池，通过自定义的线程工厂实现自定义线程名
     */
    private final ExecutorService pool = Executors.newCachedThreadPool(new ThreadFactory() {
        private final AtomicLong threadNum = new AtomicLong();
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("Jerry-mouse-pool-" + threadNum.getAndIncrement());
            return thread;
        }
    });

    /**
     * 创建一个HTTP服务器实例
     * @param ip        绑定的网络接口IP地址
     * @param port      绑定的端口号
     * @param mapping   路由映射对象
     * @param converters    Http消息转换器集合
     * @param sessionProvider Session提供者
     */
    public HttpServer(String ip, int port,
                      BindingMapping mapping, List<HttpMessageConverter<Object>> converters,
                      HttpSessionProvider sessionProvider
    ) {
        this.ip = ip;
        this.port = port;
        this.mapping = mapping;
        this.converters = converters;
        this.sessionProvider = sessionProvider;

        // 创建一个Session自动回收线程，用于定期移除过期Session
        this.sessionGCThread = new Thread(new HttpSessionGuard(sessionProvider));

        // 将IP地址字符串转换成4Byte的IP地址字节序列
        int cnt = 0;
        for(String s:ip.split("\\.")) {
            ipBytes[cnt++] = (byte)Integer.parseInt(s);
        }
    }

    /**
     * 启动HTTP服务
     * @throws IOException
     */
    @SuppressWarnings("all")
    public void start() throws IOException {
        // 初始化服务器
        init();
        logger.info("Jerry Mouse HTTP服务器已启动，端口:" + port + " 绑定地址：" + ip);
        pool.submit(() -> {
            while (true) {
                // 开始等待用户网络接入
                Socket client = serverSocket.accept();

                // 用户接入后，将连接处理任务提交到线程池执行
                pool.submit(() -> handleConnect(client));
            }
        });
    }

    /**
     * 处理用户连接
     * @param client 客户端Socket
     */
    private void handleConnect(Socket client) {
        // 先声明请求与响应对象
        HttpRequest request;
        HttpResponse response = null;

        // 注意：不可替换为try with写法，会导致进入cache块时socket被关闭而无法响应错误信息
        try {
            // 初始化响应对象
            response = new HttpResponse(client);

            try {
                // 初始化请求对象，HTTP请求报文的解析同时也在这里进行
                request = new HttpRequest(client, sessionProvider);
            } catch (Exception e) {
                // 报文解析出错，响应400错误码
                e.printStackTrace();
                String msg = "<h1 style=\"color: red\">Bad Request</h1><p>" + e.getMessage() + "</p>";
                response.setStatus(400, "Bad Request");
                response.setContentLength(msg.getBytes().length);
                response.write(msg);
                return;
            }

            // 绑定Request和Response对象到上下文Holder
            HttpContextHolder.setRequest(request);
            HttpContextHolder.setResponse(response);

            // 通过路由映射绑定对象获取将要处理的HttpHandler，并执行处理
            HttpHandler handler = mapping.getHandler(request.getMethod(), request.getURL());
            Object returnValue = handler.handle(request, response);

            logger.debug("HttpHandler返回值：" + returnValue);

            // 若HttpHandler中未发送响应头，则在此处发送默认的响应头
            // 若HttpHandler中已发送过响应头（任何HttpResponse的Write方法都会导致响应头发送），此处将忽略HttpHandler的返回值
            if (!response.isHeaderSend()) {
                // 处理HttpHandler的返回值
                if (returnValue != null) handleReturnValue(returnValue, request, response);
                else {
                    // HttpHandler无返回值或返回Null，无需响应任何内容，触发一次Http响应头的发送即可
                    response.setContentLength(0);
                    response.write("");
                }
            }
            // HTTP流程正常结束
        } catch (Exception e) {
            // Http处理流程出现异常，响应默认的服务器错误信息
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
            // 移除当前线程的HttpContextHolder绑定的Request和Response对象并关闭HTTP连接
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

    // 初始化服务器
    private void init() throws IOException {
        if (!inited) inited = true;
        else throw new IllegalStateException("重复初始化");

        // 监听Socket
        serverSocket = new ServerSocket(port, 64, Inet4Address.getByAddress(ipBytes));

        // Session自动回收守护线程启动
        sessionGCThread.start();
    }
}
