package com.xiaotao.summerframework.util.json;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 简单JSON编码器
 */
public class SimpleJsonEncoder implements JSONEncoder {
    private final static Map<Class<?>, Field[]> fieldCache = new ConcurrentHashMap<>();

    @Override
    public String encode(Object obj) {
        StringBuilder sb = new StringBuilder();
        try {
            encode(obj, sb);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private void encode(Object obj, StringBuilder sb) throws IllegalAccessException {
        if (obj == null) return;
        if (obj instanceof CharSequence) {
            // 字符串
            sb.append("\"").append(obj).append("\"");
        } else if (obj instanceof Boolean) {
            // 布尔
            sb.append((Boolean)obj ? "true" : "false");
        } else if (obj instanceof Long || obj instanceof Integer || obj instanceof Double || obj instanceof Float) {
            // 数字
            sb.append(obj);
        } else if (obj instanceof Date) {
            // 日期
            sb.append(((Date) obj).getTime());
        } else if (obj instanceof Collection) {
            // 集合
            sb.append('[');
            boolean b = false;
            for (Object o : (Collection<Object>) obj) {
                b = true;
                encode(o, sb);
                sb.append(',');
            }
            if (b) sb.setLength(sb.length() - 1);
            sb.append(']');
        } else if (obj instanceof Map) {
            // Map
            Class<?> cla = obj.getClass();
            boolean b = false;
            Iterator<Map.Entry<Object, Object>> iterator = ((Map<Object, Object>) obj).entrySet().iterator();
            sb.append('{');
            while (iterator.hasNext()) {
                Map.Entry<Object, Object> ent = iterator.next();
                b = true;
                sb.append('\"').append(ent.getKey()).append("\":");
                encode(ent.getValue(), sb);
                sb.append(',');
            }
            if (b) sb.setLength(sb.length() - 1);
            sb.append('}');
        } else {
            // 普通Java对象
            Class<?> cla = obj.getClass();
            if (cla.getAnnotation(JSONObject.class) == null) {
                sb.append("{}");
                return;
            }
            Field[] fields = fieldCache.computeIfAbsent(cla, k -> {
                Field[] fs = cla.getDeclaredFields();
                for (Field f : fs) {
                    f.setAccessible(true);

                }
                return fs;
            });
            boolean b = false;
            sb.append('{');
            for (Field field : fields) {
                Object o = field.get(obj);
                if (o != null) {
                    sb.append('\"').append(field.getName()).append('\"').append(':');
                    encode(o, sb);
                    sb.append(',');
                    b = true;
                }
            }
            if (b) sb.setLength(sb.length() - 1);
            sb.append('}');
        }

    }



}
