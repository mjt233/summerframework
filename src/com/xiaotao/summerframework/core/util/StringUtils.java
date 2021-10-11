package com.xiaotao.summerframework.core.util;

public class StringUtils {
    /**
     * 大驼峰转小驼峰命名
     * @param input 大驼峰字符串
     * @return 小驼峰字符串
     */
    public static String toSmallCamelCase(String input) {
        StringBuilder sb = new StringBuilder();
        char c = input.charAt(0);
        sb.append((char)(c >= 'a' ? c: c + 32)).append(input, 1, input.length());
        return sb.toString();
    }
    /**
     * 小驼峰转大驼峰命名
     * @param input 小驼峰字符串
     * @return 大驼峰字符串
     */
    public static String toBigCamelCase(String input) {
        StringBuilder sb = new StringBuilder();
        char c = input.charAt(0);
        sb.append((char)(c < 'a' ? c: c - 32)).append(input, 1, input.length());
        return sb.toString();
    }
}
