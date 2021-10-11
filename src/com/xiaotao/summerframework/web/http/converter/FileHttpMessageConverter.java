package com.xiaotao.summerframework.web.http.converter;

import com.xiaotao.summerframework.web.http.HttpRequest;
import com.xiaotao.summerframework.web.http.HttpResponse;
import com.xiaotao.summerframework.web.http.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileHttpMessageConverter implements HttpMessageConverter<Object> {
    @Override
    public boolean canWrite(Object obj) {
        return obj instanceof Path || obj instanceof File;
    }

    @Override
    public void handleConvertWrite(Object file, HttpRequest request, HttpResponse response) throws Exception {
        String name;
        long length;
        InputStream is;
        if (file instanceof Path) {
            Path p = (Path)file;
            if (Files.isDirectory(p)) throw new UnsupportedOperationException("unsupported file resource type: Directory");
            name = p.getFileName().toString();
            is = Files.newInputStream(p);
            length = Files.size(p);
        } else if (file instanceof File) {
            File f = (File)file;
            if (f.isDirectory()) throw new UnsupportedOperationException("unsupported file resource type: Directory");
            name = f.getName();
            is = new FileInputStream(f);
            length = f.length();
        } else {
            throw new IllegalArgumentException("unsupported resource class type:" + file.getClass().getName());
        }

        String disposition = "inline;filename*=UTF-8''"+ URLEncoder.encode(name, StandardCharsets.UTF_8);
        response.setContentType(MimeTypeMap.getContentType(name.substring(name.lastIndexOf('.')+1)))
                .setContentLength(length)
                .setHeader("Content-Disposition", disposition);

        byte[] buffer = new byte[8192];
        int cnt;
        while ( (cnt = is.read(buffer)) != -1 ) {
            response.write(buffer, 0, cnt);
        }
    }
}
