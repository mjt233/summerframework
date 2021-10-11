package com.xiaotao.summerframework.web.util;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Java类的工具类
 */
public class ClassUtils {
    /**
     * 扫描包下的所有类（依赖文件系统中的class文件）
     * 使用链表实现迭代法文件系统广度优先遍历
     * @param basePackage  要扫描的包
     * @return 类集合
     */
    public static List<Class<?>> scanClass(String basePackage) throws ClassNotFoundException {
        String origin = "/" + basePackage.replace(".", "/");
        // 结果集合
        List<Class<?>> res = new LinkedList<>();

        // 待搜索的目录集合
        LinkedList<String> dirs = new LinkedList<>();

        // 当前处于的目录
        String cur = "";

        // 获取basePackage在项目目录下的文件系统路径
        String basePath = ClassUtils.class.getResource("/").getPath() + "/" + origin;
        StringBuilder fullPath = new StringBuilder();
        StringBuilder fullClassName = new StringBuilder();
        dirs.addLast(cur);
        do {
            cur = dirs.getFirst();
            fullPath.setLength(0);
            fullPath.append(basePath).append("/").append(cur);

            File dir = new File(fullPath.toString());
            File[] files = dir.listFiles();
            if (files == null) continue;
            for(File file: files){
                if (file.isDirectory()) {
                    dirs.addLast(cur + "/" + file.getName());
                } else if (file.getName().endsWith(".class")){
                    // 组装成类名+.class
                    fullClassName.setLength(0);
                    fullClassName.append(basePackage).append(cur.replace("/",".")).append(".").append(file.getName().replace(".class",""));
                    res.add(Class.forName(fullClassName.toString()));
                }
            }
            dirs.removeFirst();
        } while ( !dirs.isEmpty() );
        return res;
    }
}
