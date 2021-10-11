package com.xiaotao.app.controller;

import com.xiaotao.summerframework.web.annotation.RequestMapping;
import com.xiaotao.summerframework.web.annotation.WebController;
import com.xiaotao.summerframework.web.http.session.HttpSession;

@WebController
public class HelloController {

    @RequestMapping("/session")
    public String test(HttpSession session) {
        String attr = (String)session.getAttr("key", "未取得");
        session.setAttr("key", "好耶！");
        return "从session获得的值：" + attr;
    }
}
