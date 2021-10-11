package com.xiaotao.summerframework.web.http;


import com.xiaotao.summerframework.Logger;
import com.xiaotao.summerframework.web.server.BindingMapping;
import com.xiaotao.summerframework.web.enums.HttpMethod;
import com.xiaotao.summerframework.web.http.session.HttpSession;
import com.xiaotao.summerframework.web.http.session.HttpSessionProvider;
import com.xiaotao.summerframework.web.util.QueryStringParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpRequest {
    private final static Logger logger = new Logger();
    private final HashMap<String,String> headers = new HashMap<>();
    private final HashMap<String,String> parameters = new HashMap<>();
    private final BufferedReader br;
    private final HashMap<String, String> cookies = new HashMap<>();
    private final HttpSessionProvider sessionProvider;
    private HttpSession httpSession;

    public HttpMethod method;
    public String URL;
    public String originURL;
    public String protocol;
    public String queryString;
    public HttpRequest(Socket socket, HttpSessionProvider sessionProvider) throws IOException {
        this.sessionProvider = sessionProvider;
        this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        parseRequest();
        String ssid = getCookie(sessionProvider.getSessionIdCookieName());
        if (ssid != null) {
            httpSession = sessionProvider.getSession(ssid);
            logger.debug("get session:" + ssid + " obj:" + httpSession);
        }
    }

    public HttpSession getSession() {
        if(httpSession == null) {
            httpSession = sessionProvider.createSession();
            Cookie cookie = new Cookie( sessionProvider.getSessionIdCookieName(), httpSession.getSessionId());
            cookie.path = "/";
            cookie.httpOnly = true;
            HttpContextHolder.getResponse().addCookie(cookie);
        }
        httpSession.renewal();
        return httpSession;
    }

    /**
     * 取HTTP请求方式
     */
    public HttpMethod getMethod() {
        return method;
    }

    /**
     * 取请求的资源路径（以/开头）
     */
    public String getURL() {
        return URL;
    }

    /**
     * 取请求协议版本
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * 取请求头
     * @param key 请求头名
     * @return 请求头值，不存在则为null
     */
    public String getHeader(String key) {
        return headers.get(key);
    }

    /**
     * 取出所有请求头的键
     * @return 请求头键集合
     */
    public Set<String> getHeaderKeys() {
        return headers.keySet();
    }

    /**
     * 取表单值
     * @param name 表单字段名
     * @return 对应表单字段的值，若不存在则为null
     */
    public String getParameter(String name) {
        return this.parameters.get(name);
    }

    /**
     * 取出表单中所有的字段名
     * @return 表单字段名集合
     */
    public Set<String> getParameterNames() {
        return this.parameters.keySet();
    }

    private void parseRequest () throws IOException {
        parseHeader();
        logger.debug("收到请求：" + BindingMapping.getRouteDescribe(method, URL));
        parseParameter();
        if(getMethod() == HttpMethod.POST) {
            parseBody();
        }
    }

    private void parseHeader() throws IOException  {
        // ====== 解析初始行 ========
        // 解析初始行 按空格分割
        String[] headLine = br.readLine().split(" ");

        // 获取请求方法
        method = HttpMethod.valueOf(headLine[0].toUpperCase());

        // 获取请求URL
        int point = headLine[1].indexOf('?');
        originURL = point == -1 ? headLine[1] : headLine[1].substring(0, point);
        queryString = point == -1 ? "" : headLine[1].substring(point + 1);

        // 获取请求协议版本
        protocol = headLine[2];

        StringBuilder uriBuilder = new StringBuilder();

        // 对原始URL重复和末尾的/进行去除
        for (String s : originURL.split("/")) {
            if(s.isBlank()) continue;
            uriBuilder.append('/').append(URLDecoder.decode(s, StandardCharsets.UTF_8));
        }
        URL = uriBuilder.length() == 0 ? "/" : uriBuilder.toString();

        // ======= 解析Header =======
        String header;
        while ( !(header = br.readLine()).isEmpty() ) {
            String[] split = header.split(":");
            headers.put(split[0], split[1].trim());
        }

        // ======= 解析Cookie ========
        String originCookies = headers.get("Cookie");
        if (originCookies != null) {
            for (String s : originCookies.split(";(\\s?)+")) {
                String[] kv = s.split("=", 2);
                if (kv.length < 2) continue;
                cookies.put(kv[0], kv[1]);
            }
        }
    }

    public String getCookie(String name) {
        return getCookie(name, null);
    }

    public String getCookie(String name, String defaultValue) {
        return cookies.getOrDefault(name, defaultValue);
    }

    public Set<Map.Entry<String, String>> getCookies() {
        return cookies.entrySet();
    }

    /**
     * 解析表单参数
     */
    private void parseParameter() {
        this.parameters.putAll(QueryStringParser.parse(queryString));
    }

    private void parseBody() throws IOException {
        String type = getHeader("Content-Type");
        String length = getHeader("Content-Length");
        int l = length == null ? 0 : Integer.parseInt(length);
        if (type != null && type.lastIndexOf("x-www-form-urlencoded") != -1 && length != null) {
            parseForm();
        } else {
            if (length == null)
                throw new UnsupportedOperationException("Miss Content-Length");
            if (type == null && l != 0) throw new UnsupportedOperationException("Miss Content-Type");
            if (type != null && type.lastIndexOf("x-www-form-urlencoded") == -1)
                throw new UnsupportedOperationException("unsupported Content-Type:" + type);
        }
    }

    private void parseForm() throws IOException {
        int len = Integer.parseInt(getHeader("Content-Length"));
        char[] data = new char[len];
        if (br.read(data) == -1) throw new IOException("read body error");
        String body = new String(data);
        this.parameters.putAll(QueryStringParser.parse(body));
    }
}
