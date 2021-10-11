package com.xiaotao.summerframework.web.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * QueryString解析器
 */
public class QueryStringParser {
    /**
     * 解析QueryString为map类型集合
     * @param qs queryString
     * @return 解析后的map集合
     */
    public static Map<String, String> parse(String qs) {
        HashMap<String, String> map = new HashMap<>();
        String[] part = qs.split("&");
        for (String string: part ) {
            String[] split = string.split("=");
            if (split.length != 2) {
                continue;
            }
            map.put(split[0], URLDecoder.decode(split[1], StandardCharsets.UTF_8));
        }
        return map;
    }
}
