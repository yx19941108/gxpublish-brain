package com.gxpublish.brain.system.strategy;

import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.common.core.utils.StringUtils;
import com.gxpublish.brain.common.tus.strategy.TusUploadStrategy;
import com.gxpublish.brain.system.domain.vo.SysOssVo;
import com.gxpublish.brain.system.service.ISysOssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 将 TUS 断点续传上来的流转化为 SysOss 系统文件的一项策略实现。
 * 它使基础后台的 OSS 服务能力通过策略模式无缝对接给共通断点续传模块。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SysOssTusUploadStrategy implements TusUploadStrategy {

    private final ISysOssService sysOssService;

    @Override
    public Object finishUpload(String uploadUrl, String originalName, String contentType, InputStream inputStream)
            throws Exception {
        log.info("SysOssTusUploadStrategy finalizing upload for: {}", originalName);
        if (inputStream == null) {
            throw new ServiceException("Uploaded stream is empty");
        }
        Path tempDir = null;
        Path tempFile = null;
        try {
            tempDir = Files.createTempDirectory("tus-finalize-");
            tempFile = tempDir.resolve(sanitizeOriginalName(originalName));
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            // 落盘后复用系统已有 upload(File) 链路，避免大文件全量入内存
            SysOssVo sysOssVo = sysOssService.upload(tempFile.toFile());
            return sysOssVo;
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception e) {
                    log.warn("Failed to delete temp TUS file: {}", tempFile, e);
                }
            }
            if (tempDir != null) {
                try {
                    Files.deleteIfExists(tempDir);
                } catch (Exception e) {
                    log.warn("Failed to delete temp TUS directory: {}", tempDir, e);
                }
            }
        }
    }

    private String sanitizeOriginalName(String originalName) {
        String fileName = StringUtils.defaultIfBlank(originalName, "upload.bin");
        String normalizedPath = fileName.replace("\\", "/");
        if (normalizedPath.contains("/")) {
            fileName = StringUtils.substringAfterLast(normalizedPath, "/");
        } else {
            fileName = normalizedPath;
        }
        fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        return StringUtils.defaultIfBlank(fileName, "upload.bin");
    }
}
