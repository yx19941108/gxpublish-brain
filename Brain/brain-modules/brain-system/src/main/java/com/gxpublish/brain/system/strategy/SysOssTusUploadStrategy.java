package com.gxpublish.brain.system.strategy;

import cn.hutool.core.io.IoUtil;
import com.gxpublish.brain.common.core.utils.SpringUtils;
import com.gxpublish.brain.common.tus.strategy.TusUploadStrategy;
import com.gxpublish.brain.system.domain.vo.SysOssVo;
import com.gxpublish.brain.system.service.ISysOssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

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

        // 读取完整流内容并构建标准的 MultipartFile 代理对象，复用现有 OSS 上传通道
        byte[] bytes = IoUtil.readBytes(inputStream);
        MultipartFile multipartFile = new TusMultipartFile(bytes, "file", originalName, contentType);

        // 调用标准文件上传，并持久化 SysOss 表记录
        SysOssVo sysOssVo = sysOssService.upload(multipartFile);

        return sysOssVo;
    }
}
