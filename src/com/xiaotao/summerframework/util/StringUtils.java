package com.xiaotao.summerframework.util;

public class StringUtils {
    /**
     * 判断字符串是否为空白字符串（只有空格）或空字符串
     * @param str   输入的字符串
     * @return      true or false
     */
    public static boolean isBlank(String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }

        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (str.charAt(i) != ' ') {
                return false;
            }
        }
        return true;
    }

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
