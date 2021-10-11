package com.xiaotao.app.controller;

import com.xiaotao.app.entity.User;
import com.xiaotao.summerframework.web.annotation.RequestMapping;
import com.xiaotao.summerframework.web.annotation.RequestParam;
import com.xiaotao.summerframework.web.annotation.WebController;
import com.xiaotao.summerframework.web.enums.HttpMethod;
import com.xiaotao.summerframework.web.http.session.HttpSession;

@WebController
public class ApiController {
    @RequestMapping(value = "/api/login", method = {HttpMethod.POST, HttpMethod.GET})
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession session) {
        if (!username.equals("xiaotao") || !password.equals("123456")) {
            return "登录失败";
        } else {
            session.setAttr("user", new User("xiaotao", "123456"));
            return "欢迎您，" + username;
        }
    }

    @RequestMapping("/api/getUserInfo")
    public String getInfo(HttpSession session) {
        return "您的用户信息为：" + session.getAttr("user");
    }
}
