package com.xiaotao.app.controller;

import com.xiaotao.app.dao.UserDao;
import com.xiaotao.app.entity.User;
import com.xiaotao.summerframework.core.annotation.Autowried;
import com.xiaotao.summerframework.web.annotation.RequestMapping;
import com.xiaotao.summerframework.web.annotation.RequestParam;
import com.xiaotao.summerframework.web.annotation.WebController;
import com.xiaotao.summerframework.web.enums.HttpMethod;
import com.xiaotao.summerframework.web.http.HttpResponse;
import com.xiaotao.summerframework.web.http.session.HttpSession;

@WebController
public class ApiController {
    @Autowried
    private UserDao userDao;

    @RequestMapping(value = "/api/login", method = {HttpMethod.POST, HttpMethod.GET})
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpResponse response,
                        HttpSession session) {

        User user = userDao.getUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttr("user", new User("xiaotao", "123456"));
            return "欢迎您，" + username;
        } else {
            response.setStatus(401, "unauthorized");
            return "登录失败";
        }
    }

    @RequestMapping("/api/getUserInfo")
    public String getInfo(HttpSession session) {
        return "您的用户信息为：" + session.getAttr("user");
    }
}
