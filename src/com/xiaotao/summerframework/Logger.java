package com.xiaotao.summerframework;

import java.util.Date;

public class Logger {
    public static boolean enable = true;
    private Class<?> clazz;
    public Logger(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Logger() {
        try {
            StackTraceElement[] trace = new Throwable().getStackTrace();
            this.clazz = Class.forName(trace[1].getClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            this.clazz = Logger.class;
        }
    }

    private void printMsg(String type, String msg) {
        if (!enable) return;
        StackTraceElement ele = new Throwable().getStackTrace()[2];
        String[] names = clazz.getName().split("\\.");

        // 构造缩略显示的类名
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < names.length - 2; i++) {
            name.append(names[i], 0, 1).append('.');
        }
        for (int i = names.length - 2; i < names.length; i++) {
            name.append(names[i]).append('.');
        }
        name.setLength(name.length() - 1);
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
