package com.gxpublish.brain.common.tus.strategy;

import java.io.InputStream;

/**
 * TUS上传完成后的处理策略接口
 * <p>
 * 不同的业务模块（例如 admin 基础后台或独立业务系统）可以实现此接口，
 * 将从 TUS 接收到的完整文件流持久化到 OSS、数据库或其它存储媒介。
 * </p>
 */
public interface TusUploadStrategy {

    /**
     * TUS上传完成回调
     *
     * @param uploadUrl    TUS 上传后的唯一标识 / URL
     * @param originalName 原始文件名
     * @param contentType  文件 MIME 类型
     * @param inputStream  通过 TUS 接收到的完整文件数据流
     * @return 持久化后的业务对象（例如 SysOssVo 等），会直接由 Controller 返回给前端
     * @throws Exception 上传策略执行中出现的异常
     */
    Object finishUpload(String uploadUrl, String originalName, String contentType, InputStream inputStream) throws Exception;

}
