package com.xiaotao.summerframework.web.http.handler;

import com.xiaotao.summerframework.Logger;
import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地文件静态资源操作器，可用作默认操作器实现静态文件资源的访问
 */
public class FileResourceHandler implements HttpHandler {
    private static final Logger logger = new Logger();

    // 资源不存在时的响应内容
    private static final String MSG = "<h1><center>404 Not Found</center></h1><hr><p><center>Easy Http Server by Xiaotao</center></p>";

    // 站点根目录
    private final String root;

    /**
     * @param root 站点根目录
     */
    public FileResourceHandler(String root) {
        logger.debug("root directory: " + root);
        this.root = root;
    }

    @Override
    public Object handle(HttpRequest request, HttpResponse response) throws IOException {
        // 获取HTTP请求URL对应本地的物理文件路径
        Path path = Paths.get((root + "/" + request.getURL()).replaceAll("//+", "/"));

        // 判断是否请求了一个文件夹
        if(Files.exists(path) && Files.isDirectory(path)) {
            // 是的话就在本地路径中加index.html访问默认主页
            path = Paths.get(path + "/index.html");

            // 为了确保主页文件使用的文件相对路径访问正常，重定向让浏览器在URL末尾加上/
            if (!request.getOriginURL().endsWith("/")) {
                response.redirect(request.getURL() + "/");
                return null;
            }
        }

        //  若本地文件路径对应的文件存在且不是文件夹，则返回这个文件的Path，默认的HttpMessageConverter有针对Path对象的处理
        if (Files.exists(path) && !Files.isDirectory(path)) {
            return path;
        } else {
            response.setStatus(404, "Not Found");
            response.write(MSG);
            logger.info("资源不存在访问：" + request.getURL());
        }
        return null;
    }
}
