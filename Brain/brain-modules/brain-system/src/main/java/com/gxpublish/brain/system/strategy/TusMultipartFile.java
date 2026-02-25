package com.gxpublish.brain.system.strategy;

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * 自定义的分片上传完整流包装类，兼容 Spring 的 MultipartFile
 */
public class TusMultipartFile implements MultipartFile {

    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    public TusMultipartFile(byte[] content, String name, String originalFilename, String contentType) {
        this.content = content != null ? content : new byte[0];
        this.name = name != null ? name : "";
        this.originalFilename = originalFilename != null ? originalFilename : "";
        this.contentType = contentType;
    }

    @Override
    @NonNull
    public String getName() {
        return this.name;
    }

    @Override
    public String getOriginalFilename() {
        return this.originalFilename;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public boolean isEmpty() {
        return this.content.length == 0;
    }

    @Override
    public long getSize() {
        return this.content.length;
    }

    @Override
    @NonNull
    public byte[] getBytes() {
        return this.content;
    }

    @Override
    @NonNull
    public InputStream getInputStream() {
        return new ByteArrayInputStream(this.content);
    }

    @Override
    public void transferTo(@NonNull File dest) throws IOException, IllegalStateException {
        Files.write(dest.toPath(), this.content);
    }
}
