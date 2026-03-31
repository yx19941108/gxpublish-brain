package com.gxpublish.brain.manuscript.review.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.gxpublish.brain.common.core.domain.R;
import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.manuscript.review.controller.request.ManuscriptReviewSubmitRequest;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewDetailResponse;
import com.gxpublish.brain.manuscript.review.domain.command.AddManuscriptReviewResourceCommand;
import com.gxpublish.brain.manuscript.review.domain.command.AddManuscriptReviewVideoMarkCommand;
import com.gxpublish.brain.manuscript.review.domain.command.CreateManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.command.DisableManuscriptReviewResourceCommand;
import com.gxpublish.brain.manuscript.review.domain.command.DisableManuscriptReviewVideoMarkCommand;
import com.gxpublish.brain.manuscript.review.domain.command.EnableManuscriptReviewResourceCommand;
import com.gxpublish.brain.manuscript.review.domain.command.EnableManuscriptReviewVideoMarkCommand;
import com.gxpublish.brain.manuscript.review.domain.command.ResubmitManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.command.SubmitAndStartManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.command.UpdateManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;
import com.gxpublish.brain.manuscript.review.service.IManuscriptReviewService;
import com.gxpublish.brain.manuscript.review.service.ManuscriptReviewReadableService;

import lombok.Getter;
import lombok.Setter;

/**
 * 稿件审校写侧接口控制器。
 *
 * <p>v6.26 追加改动：
 * 1. 提交入口仍冻结为 /submitAndFlowStart；
 * 2. 新增页提交改为携带主表字段 + 资源参数；
 * 3. update 保存也支持主表字段 + 追加资源参数的一次性提交；
 * 4. shared 输出形态保持不变。</p>
 *
 * @author Codex
 * @since 2026-03-26
 */
@Validated
@RestController
@ConditionalOnBean(IManuscriptReviewService.class)
@RequestMapping("/workflow/manuscript-review")
public class ManuscriptReviewApiController {

    private IManuscriptReviewService manuscriptReviewService;
    private ManuscriptReviewReadableService manuscriptReviewReadableService;

    /**
     * 注入稿件审校写侧服务。
     *
     * @param manuscriptReviewService 稿件审校写侧服务
     * @return 无返回值
     */
    @Autowired
    public void setManuscriptReviewService(IManuscriptReviewService manuscriptReviewService) {
        this.manuscriptReviewService = manuscriptReviewService;
    }

    /**
     * 注入稿件审校读侧服务。
     *
     * @param manuscriptReviewReadableService 稿件审校读侧服务
     * @return 无返回值
     */
    @Autowired
    public void setManuscriptReviewReadableService(ManuscriptReviewReadableService manuscriptReviewReadableService) {
        this.manuscriptReviewReadableService = manuscriptReviewReadableService;
    }

    /**
     * 兼容旧草稿创建入口。
     *
     * <p>v6.26 追加改动：该接口只保留边界内最小兼容能力，
     * 新增页正式提交流程不再以该接口为主入口。</p>
     *
     * @param request 主单保存请求
     * @return 新建流程单主键
     */
    @PostMapping
    @SaCheckPermission("manuscript:review:submit")
    public R<Long> create(@RequestBody ManuscriptReviewSubmitRequest request) {
        Long reviewId = manuscriptReviewService.create(CreateManuscriptReviewCommand.builder()
            .processType(resolveProcessType(request.getProcessType()))
            .externalManuscriptCode(request.getExternalManuscriptCode())
            .title(request.getTitle())
            .mediaChannel(request.getMediaChannel())
            .submitDepartment(request.getSubmitDepartment())
            .authorName(request.getAuthorName())
            .remark(request.getRemark())
            .contentBody(request.getContentBody())
            .build());
        return R.ok(reviewId);
    }

    /**
     * 修改保存整表单。
     *
     * <p>v6.26 追加改动：修改保存一次性接收主表字段与追加资源参数，
     * 只做表单校验、主表保存、追加资源保存与历史写入，不触发 BPM 状态推进。</p>
     *
     * @param request 修改保存请求
     * @return 无返回值
     */
    @PutMapping
    @SaCheckPermission("manuscript:review:edit")
    public R<Void> update(@RequestBody ManuscriptReviewSubmitRequest request) {
        manuscriptReviewService.update(UpdateManuscriptReviewCommand.builder()
            .id(request.getId())
            .processType(resolveProcessType(request.getProcessType()))
            .externalManuscriptCode(request.getExternalManuscriptCode())
            .title(request.getTitle())
            .mediaChannel(request.getMediaChannel())
            .submitDepartment(request.getSubmitDepartment())
            .authorName(request.getAuthorName())
            .remark(request.getRemark())
            .contentBody(request.getContentBody())
            .attachmentResources(request.getAttachmentResources() == null ? List.of()
                : request.getAttachmentResources().stream()
                    .map(item -> SubmitAndStartManuscriptReviewCommand.SubmitAttachmentResourceCommand.builder()
                        .resourceType(item.getResourceType())
                        .displayName(item.getDisplayName())
                        .ossId(item.getOssId())
                        .build())
                    .toList())
            .externalLinks(request.getExternalLinks() == null ? List.of()
                : request.getExternalLinks().stream()
                    .map(item -> SubmitAndStartManuscriptReviewCommand.SubmitExternalLinkCommand.builder()
                        .displayName(item.getDisplayName())
                        .externalUrl(item.getExternalUrl())
                        .build())
                    .toList())
            .build());
        return R.ok();
    }

    /**
     * 新增并提交一体化入口，同时保留旧草稿提交流程兼容分支。
     *
     * @param request 提交请求
     * @return 最新详情响应
     */
    @PostMapping("/submitAndFlowStart")
    @SaCheckPermission("manuscript:review:submit")
    public R<ManuscriptReviewDetailResponse> submitAndFlowStart(@RequestBody ManuscriptReviewSubmitRequest request) {
        Long reviewId = request.getId();
        if (isLegacyDraftSubmit(request)) {
            manuscriptReviewService.submitAndFlowStart(requireReviewId(reviewId));
            return R.ok(manuscriptReviewReadableService.getDetail(reviewId));
        }

        reviewId = manuscriptReviewService.submitAndFlowStart(SubmitAndStartManuscriptReviewCommand.builder()
            .id(reviewId)
            .processType(resolveProcessType(request.getProcessType()))
            .externalManuscriptCode(request.getExternalManuscriptCode())
            .title(request.getTitle())
            .mediaChannel(request.getMediaChannel())
            .submitDepartment(request.getSubmitDepartment())
            .authorName(request.getAuthorName())
            .remark(request.getRemark())
            .contentBody(request.getContentBody())
            .attachmentResources(request.getAttachmentResources() == null ? List.of()
                : request.getAttachmentResources().stream()
                    .map(item -> SubmitAndStartManuscriptReviewCommand.SubmitAttachmentResourceCommand.builder()
                        .resourceType(item.getResourceType())
                        .displayName(item.getDisplayName())
                        .ossId(item.getOssId())
                        .build())
                    .toList())
            .externalLinks(request.getExternalLinks() == null ? List.of()
                : request.getExternalLinks().stream()
                    .map(item -> SubmitAndStartManuscriptReviewCommand.SubmitExternalLinkCommand.builder()
                        .displayName(item.getDisplayName())
                        .externalUrl(item.getExternalUrl())
                        .build())
                    .toList())
            .build());
        return R.ok(manuscriptReviewReadableService.getDetail(reviewId));
    }

    /**
     * 发起人重新提交流程。
     *
     * @param request 重新提交请求
     * @return 最新详情响应
     */
    @PostMapping("/resubmit")
    @SaCheckPermission("manuscript:review:resubmit")
    public R<ManuscriptReviewDetailResponse> resubmit(@RequestBody ReviewIdRequest request) {
        Long reviewId = requireReviewId(request.getId());
        manuscriptReviewService.resubmit(ResubmitManuscriptReviewCommand.builder().reviewId(reviewId).build());
        return R.ok(manuscriptReviewReadableService.getDetail(reviewId));
    }

    /**
     * 发起人撤销流程。
     *
     * @param request 撤销请求
     * @return 无返回值
     */
    @PutMapping("/cancelProcessApply")
    public R<Void> cancelProcessApply(@RequestBody CancelProcessRequest request) {
        manuscriptReviewService.cancelProcessApply(requireReviewId(request.getId()), request.getReason());
        return R.ok();
    }

    /**
     * 删除暂存资源。
     *
     * @param ossId OSS 主键
     * @return 无返回值
     */
    @DeleteMapping("/resource/pending/{ossId}")
    public R<Void> deletePendingResource(@PathVariable("ossId") Long ossId) {
        manuscriptReviewService.deletePendingResource(ossId);
        return R.ok();
    }

    /**
     * 兼容旧资源追加入口。
     *
     * @param request 资源新增请求
     * @return 新增资源读模型
     */
    @PostMapping("/resource")
    public R<ManuscriptReviewDetailResponse.ResourceItemVO> addResource(@RequestBody ResourceCreateRequest request) {
        Long reviewId = requireReviewId(request.getReviewId());
        Long resourceId = manuscriptReviewService.addResource(AddManuscriptReviewResourceCommand.builder()
            .reviewId(reviewId)
            .resourceType(request.getResourceType())
            .displayName(request.getDisplayName())
            .ossId(request.getOssId())
            .externalUrl(request.getExternalUrl())
            .build());
        return R.ok(manuscriptReviewReadableService.getResourceItem(reviewId, resourceId));
    }

    /**
     * 停用资源。
     *
     * @param request 资源停用请求
     * @return 无返回值
     */
    @PutMapping("/resource/disable")
    public R<Void> disableResource(@RequestBody ResourceDisableRequest request) {
        manuscriptReviewService.disableResource(DisableManuscriptReviewResourceCommand.builder()
            .resourceId(request.getResourceId())
            .disabledReason(request.getDisabledReason())
            .build());
        return R.ok();
    }

    @PutMapping("/resource/enable")
    public R<Void> enableResource(@RequestBody ResourceEnableRequest request) {
        manuscriptReviewService.enableResource(EnableManuscriptReviewResourceCommand.builder()
            .resourceId(request.getResourceId())
            .build());
        return R.ok();
    }

    /**
     * 新增视频标注。
     *
     * @param request 标注新增请求
     * @return 新增标注读模型
     */
    @PostMapping("/video-mark")
    public R<ManuscriptReviewDetailResponse.VideoMarkItemVO> addVideoMark(@RequestBody VideoMarkCreateRequest request) {
        Long reviewId = requireReviewId(request.getReviewId());
        Long markId = manuscriptReviewService.addVideoMark(AddManuscriptReviewVideoMarkCommand.builder()
            .reviewId(reviewId)
            .resourceId(request.getResourceId())
            .startTimeText(request.getStartTimeText())
            .endTimeText(request.getEndTimeText())
            .markContent(request.getMarkContent())
            .build());
        return R.ok(manuscriptReviewReadableService.getVideoMarkItem(reviewId, markId));
    }

    /**
     * 停用视频标注。
     *
     * @param request 标注停用请求
     * @return 无返回值
     */
    @PutMapping("/video-mark/disable")
    public R<Void> disableVideoMark(@RequestBody VideoMarkDisableRequest request) {
        manuscriptReviewService.disableVideoMark(DisableManuscriptReviewVideoMarkCommand.builder()
            .markId(request.getMarkId())
            .disabledReason(request.getDisabledReason())
            .build());
        return R.ok();
    }

    @PutMapping("/video-mark/enable")
    public R<Void> enableVideoMark(@RequestBody VideoMarkEnableRequest request) {
        manuscriptReviewService.enableVideoMark(EnableManuscriptReviewVideoMarkCommand.builder()
            .markId(request.getMarkId())
            .build());
        return R.ok();
    }

    /**
     * 解析流程类型。
     *
     * @param processType 流程类型编码
     * @return 流程类型枚举
     */
    private ManuscriptReviewProcessType resolveProcessType(String processType) {
        if (processType == null || processType.trim().isEmpty()) {
            throw new ServiceException("流程类型不能为空");
        }
        try {
            return ManuscriptReviewProcessType.valueOf(processType.trim());
        } catch (IllegalArgumentException ex) {
            throw new ServiceException("流程类型不支持");
        }
    }

    /**
     * 校验流程单主键。
     *
     * @param reviewId 流程单主键
     * @return 合法的流程单主键
     */
    private Long requireReviewId(Long reviewId) {
        if (reviewId == null) {
            throw new ServiceException("稿件审校流程不存在");
        }
        return reviewId;
    }

    /**
     * 判断当前请求是否仍走旧草稿提交兼容分支。
     *
     * @param request 提交请求
     * @return true 表示旧草稿提交，false 表示新增并提交一体化
     */
    private boolean isLegacyDraftSubmit(ManuscriptReviewSubmitRequest request) {
        return request.getId() != null
            && request.getProcessType() == null
            && request.getExternalManuscriptCode() == null
            && request.getTitle() == null
            && request.getMediaChannel() == null
            && request.getSubmitDepartment() == null
            && request.getAuthorName() == null
            && request.getRemark() == null
            && request.getContentBody() == null
            && (request.getAttachmentResources() == null || request.getAttachmentResources().isEmpty())
            && (request.getExternalLinks() == null || request.getExternalLinks().isEmpty());
    }

    @Getter
    @Setter
    public static class ReviewIdRequest {

        private Long id;
    }

    @Getter
    @Setter
    public static class CancelProcessRequest {

        private Long id;
        private String reason;
    }

    @Getter
    @Setter
    public static class ResourceCreateRequest {

        private Long reviewId;
        private String resourceType;
        private String displayName;
        private Long ossId;
        private String externalUrl;
    }

    @Getter
    @Setter
    public static class ResourceDisableRequest {

        private Long resourceId;
        private String disabledReason;
    }

    @Getter
    @Setter
    public static class ResourceEnableRequest {

        private Long resourceId;
    }

    @Getter
    @Setter
    public static class VideoMarkCreateRequest {

        private Long reviewId;
        private Long resourceId;
        private String startTimeText;
        private String endTimeText;
        private String markContent;
    }

    @Getter
    @Setter
    public static class VideoMarkDisableRequest {

        private Long markId;
        private String disabledReason;
    }

    @Getter
    @Setter
    public static class VideoMarkEnableRequest {

        private Long markId;
    }
}
