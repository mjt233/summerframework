package com.xiaotao.summerframework.web.http;


import com.xiaotao.summerframework.Logger;

import java.io.IOException;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Http响应对象
 */
public class HttpResponse {
    private final static Logger logger = new Logger();
    public int code;
    public String msg;
    protected HashMap<String,String> headers = new HashMap<>();
    protected Socket socket;
    protected boolean headerSend = false;
    private final List<Cookie> cookies = new ArrayList<>();

    /**
     * 初始化一个Http响应对象
     * @param socket 客户端Socket
     */
    public HttpResponse(Socket socket) {
        this.socket = socket;
        // 设定一些默认的Header
        setStatus(200, "OK");
        setHeader("Connection", "close");
        setHeader("Content-Type", "text/html;charset=utf-8");
        setHeader("Server", "Easy HTTP Server");
    }

    /**
     * 响应让客户端重定向
     * @param url 要重定向到的目的URL
     */
    public void redirect(String url) throws IOException {
        StringBuilder sb = new StringBuilder();

        // 重新拆分组装成一个安全的，特殊字符使用URL编码的URL
        for (String e : url.split("/+")) {
            if(!e.isBlank()) {
                sb.append('/').append(URLEncoder.encode(e, StandardCharsets.UTF_8));
            }
        }
        if (url.endsWith("/")) sb.append('/');
        url = sb.toString();
        logger.debug("重定向 " + HttpContextHolder.getRequest().getURL() + " ====> " + url);


        // 设置响应码和对应的HTTP响应头并发送响应
        setStatus(302, "Found");
        setHeader("Location", url);
        socket.getOutputStream().write(getHeaderContext().getBytes());
    }

    public HttpResponse addCookie(Cookie cookie) {
        cookies.add(cookie);
        return this;
    }

    /**
     * 设置HTTP响应报文的响应标头（header）
     */
    public HttpResponse setHeader(String key, String value){
        this.headers.put(key, value);

        //  return this用于实现链式调用（也叫流式调用）
        return this;
    }
    /**
     * 设置HTTP响应报文初始行的数据
     * @param code  响应码
     * @param msg   响应消息
     */
    public HttpResponse setStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
        return this;
    }

    public HttpResponse setContentLength(long length) {
        setHeader("Content-Length", "" + length);
        return this;
    }

    public HttpResponse setContentType(String type) {
        setHeader("Content-Type", type);
        return this;
    }

    /**
     * 获取HTTP报文Header内容字符串（带空行）
     */
    public String getHeaderContext() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(this.code).append(" ").append(this.msg).append("\r\n");
        for (String key: headers.keySet()) {
            sb.append(key).append(": ").append(headers.get(key)).append("\r\n");
        }
        if (!cookies.isEmpty()) {
            for (Cookie cookie : cookies) {
                sb.append("Set-Cookie: ").append(cookie).append("\r\n");
            }
        }
        sb.append("\r\n");
        return sb.toString();
    }

    /**
     * 向HTTP报文响应体发送字符串
     * @param str 要发送的字符串
     */
    public HttpResponse write(String str) throws IOException {
        write(str.getBytes());
        return this;
    }

    /**
     * 向HTTP报文响应体发送二进制字节数据
     * @param data 要发送的二进制数据数组
     */
    public HttpResponse write(byte[] data) throws IOException {
        write(data,0 , data.length);
        return this;
    }

    /**
     * 向HTTP报文响应体发送二进制字节数据
     * @param data 数据
     * @param off 从data数组的第off位开始
     * @param len 长度
     */
    public HttpResponse write(byte[] data, int off, int len) throws IOException {
        // 如果没发送过header 则先发送header
        if (!headerSend) {
            socket.getOutputStream().write(getHeaderContext().getBytes());
            headerSend = true;
        }
        socket.getOutputStream().write(data, off, len);
        return this;
    }

    public boolean isHeaderSend() {
        return headerSend;
    }
}
