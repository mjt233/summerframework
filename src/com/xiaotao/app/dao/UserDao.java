package com.xiaotao.app.dao;

import com.xiaotao.app.entity.User;
import com.xiaotao.summerframework.core.annotation.Component;

@Component
public class UserDao {
    public User getUserByUsername(String username) {
        if (username == null || !username.equals("xiaotao")) {
            return null;
        } else {
            return new User("xiaotao", "123456");
        }
    }
}
