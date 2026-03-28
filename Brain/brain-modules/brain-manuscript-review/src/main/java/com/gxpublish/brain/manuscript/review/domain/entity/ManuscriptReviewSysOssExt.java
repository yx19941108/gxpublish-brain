package com.gxpublish.brain.manuscript.review.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * 系统文件扩展字段快照对象。
 *
 * <p>v6.26 追加改动：本模块只解析文件大小、MIME 类型等最小必要字段，
 * 避免直接依赖 brain-system 的实现类型。</p>
 *
 * @author Codex
 * @since 2026-03-26
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManuscriptReviewSysOssExt {

    /**
     * 文件大小。
     */
    private Long fileSize;

    /**
     * MIME 类型。
     */
    private String contentType;

    /**
     * 视频总时长（单位：秒）。
     */
    private Integer videoDurationSeconds;
}
