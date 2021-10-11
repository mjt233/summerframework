package com.xiaotao.summerframework.web.http.handler;

import com.xiaotao.summerframework.Logger;
import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileResourceHandler implements HttpHandler {
    private static final Logger logger = new Logger(FileResourceHandler.class);
    private static final String MSG = "<h1><center>404 Not Found</center></h1><hr><p><center>Easy Http Server by Xiaotao</center></p>";
    private final String root;
    public FileResourceHandler(String root) {
        logger.debug("root directory: " + root);
        this.root = root;
    }

    @Override
    public Object handle(HttpRequest request, HttpResponse response) throws IOException {
        Path path = Paths.get((root + "/" + request.getURL()).replaceAll("//+", "/"));
        if(Files.exists(path) && Files.isDirectory(path)) {
            path = Paths.get(path + "/index.html");
            if (!request.originURL.endsWith("/")) {
                response.redirect(request.getURL() + "/");
                return null;
            }
        }
        if (Files.exists(path) && !Files.isDirectory(path)) {
            return path;
        } else {
            response.setStatus(404, "Not Found");
            response.write(MSG);
            logger.info("资源不存在访问：" + request.URL);
        }
        return null;
    }
}
