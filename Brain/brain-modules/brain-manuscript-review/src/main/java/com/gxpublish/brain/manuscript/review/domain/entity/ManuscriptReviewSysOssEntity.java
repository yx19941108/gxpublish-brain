package com.gxpublish.brain.manuscript.review.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 系统文件快照实体。
 *
 * <p>v6.26 追加改动：稿件审校模块为避免跨模块直接依赖 brain-system service，
 * 仅在本模块内读取 `sys_oss` 的最小必要字段，用于提交时回填附件资源信息。</p>
 *
 * @author Codex
 * @since 2026-03-26
 */
@Data
@TableName("sys_oss")
public class ManuscriptReviewSysOssEntity {

    /**
     * OSS 主键。
     */
    @TableId(value = "oss_id")
    private Long ossId;

    /**
     * 文件存储名称。
     */
    private String fileName;

    /**
     * 原始文件名。
     */
    private String originalName;

    /**
     * 文件访问地址。
     */
    private String url;

    /**
     * OSS 服务标识。
     */
    private String service;

    /**
     * 扩展字段 JSON。
     */
    private String ext1;
}
