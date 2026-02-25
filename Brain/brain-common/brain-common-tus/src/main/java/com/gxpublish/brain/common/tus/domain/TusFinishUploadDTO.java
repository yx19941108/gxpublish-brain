package com.gxpublish.brain.common.tus.domain;

import lombok.Data;

/**
 * TUS上传完成请求参数
 */
@Data
public class TusFinishUploadDTO {
    
    /**
     * TUS分配的上传URL
     */
    private String uploadUrl;
    
    /**
     * 原始文件名
     */
    private String originalName;
    
    /**
     * 文件MIME类型
     */
    private String contentType;
}
