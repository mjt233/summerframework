package com.xiaotao.app.controller;

import com.xiaotao.summerframework.web.annotation.RequestMapping;
import com.xiaotao.summerframework.web.annotation.WebController;
import com.xiaotao.summerframework.web.http.Cookie;
import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;

import java.io.File;
import java.util.Date;

@WebController
public class TestController {

    @RequestMapping("/")
    public String home() { return "Hello World"; }

    @RequestMapping("/unix")
    public long unix() { return System.currentTimeMillis(); }

    @RequestMapping("/date")
    public Date date() { return new Date(); }

    @RequestMapping("/img")
    public File img() {
        return new File("static/img.jpg");
    }

    @RequestMapping("/cookie")
    public String cookie(HttpRequest request, HttpResponse response) {
        Cookie cookie = new Cookie("id", "hello world");
        cookie.maxAge = 3600;
        cookie.path = "/";
        response.addCookie(cookie);
        if (request.getCookie("id") != null) {
            return "你的ID Cookie：<h1>" + request.getCookie("id") + "</h1>";
        }
        return "你还没有ID Cookie";
    }

    @RequestMapping("/cookie/deleteme")
    public String delCookie(HttpResponse response) {
        Cookie cookie = new Cookie("id", null);
        cookie.maxAge = 0;
        cookie.path = "/";
        response.addCookie(cookie);
        return "已删除";
    }
}
