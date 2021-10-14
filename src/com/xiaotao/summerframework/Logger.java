package com.xiaotao.summerframework;

import java.util.Date;

public class Logger {

    // 缩略类名
    private String name;

    public Logger() {
        try {
            // 通过创建一个异常来获取函数调用栈，得知实例化者的类名
            StackTraceElement[] trace = new Throwable().getStackTrace();
            Class<?> clazz = Class.forName(trace[1].getClassName());
            this.name = parseClassName(clazz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            this.name = parseClassName(Logger.class);
        }
    }

    private String parseClassName(Class<?> clazz) {
        String[] names = clazz.getName().split("\\.");
        // 构造缩略显示的类名，只完整显示最后的包名和类名
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < names.length - 2; i++) {
            name.append(names[i], 0, 1).append('.');
        }
        for (int i = names.length - 2; i < names.length; i++) {
            name.append(names[i]).append('.');
        }
        name.setLength(name.length() - 1);
        return name.toString();
    }

    private void printMsg(String type, String msg) {
        StackTraceElement ele = new Throwable().getStackTrace()[2];
        System.out.println("[" + type + "][" + new Date() + "]" + "[" + Thread.currentThread().getName() + "]" +
                "[" + name + "" +
                "#" + ele.getMethodName() + "()]: " + msg);
    }

    public void info(String msg) {
        printMsg("INFO", msg);
    }

    public void debug(String msg) {
        printMsg("DEBUG", msg);
    }
}
