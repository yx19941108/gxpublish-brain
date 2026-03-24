package com.gxpublish.brain.manuscript.review.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gxpublish.brain.common.core.domain.R;
import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.manuscript.review.controller.request.ManuscriptReviewSubmitRequest;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewDetailResponse;
import com.gxpublish.brain.manuscript.review.domain.command.AddManuscriptReviewResourceCommand;
import com.gxpublish.brain.manuscript.review.domain.command.AddManuscriptReviewVideoMarkCommand;
import com.gxpublish.brain.manuscript.review.domain.command.CreateManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.command.DisableManuscriptReviewResourceCommand;
import com.gxpublish.brain.manuscript.review.domain.command.DisableManuscriptReviewVideoMarkCommand;
import com.gxpublish.brain.manuscript.review.domain.command.ResubmitManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.command.UpdateManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;
import com.gxpublish.brain.manuscript.review.service.ManuscriptReviewReadableService;
import com.gxpublish.brain.manuscript.review.service.ManuscriptReviewService;

import lombok.Getter;
import lombok.Setter;

@Validated
@RestController
@ConditionalOnBean(ManuscriptReviewService.class)
@RequestMapping("/workflow/manuscript-review")
public class ManuscriptReviewApiController {

    private ManuscriptReviewService manuscriptReviewService;
    private ManuscriptReviewReadableService manuscriptReviewReadableService;

    @Autowired
    public void setManuscriptReviewService(ManuscriptReviewService manuscriptReviewService) {
        this.manuscriptReviewService = manuscriptReviewService;
    }

    @Autowired
    public void setManuscriptReviewReadableService(ManuscriptReviewReadableService manuscriptReviewReadableService) {
        this.manuscriptReviewReadableService = manuscriptReviewReadableService;
    }

    @PostMapping
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

    @PutMapping
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
            .build());
        return R.ok();
    }

    @PostMapping("/submitAndFlowStart")
    public R<ManuscriptReviewDetailResponse> submitAndFlowStart(@RequestBody ReviewIdRequest request) {
        Long reviewId = requireReviewId(request.getId());
        manuscriptReviewService.submitAndFlowStart(reviewId);
        return R.ok(manuscriptReviewReadableService.getDetail(reviewId));
    }

    @PostMapping("/resubmit")
    public R<ManuscriptReviewDetailResponse> resubmit(@RequestBody ReviewIdRequest request) {
        Long reviewId = requireReviewId(request.getId());
        manuscriptReviewService.resubmit(ResubmitManuscriptReviewCommand.builder().reviewId(reviewId).build());
        return R.ok(manuscriptReviewReadableService.getDetail(reviewId));
    }

    @PutMapping("/cancelProcessApply")
    public R<Void> cancelProcessApply(@RequestBody CancelProcessRequest request) {
        manuscriptReviewService.cancelProcessApply(requireReviewId(request.getId()), request.getReason());
        return R.ok();
    }

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

    @PutMapping("/resource/disable")
    public R<Void> disableResource(@RequestBody ResourceDisableRequest request) {
        manuscriptReviewService.disableResource(DisableManuscriptReviewResourceCommand.builder()
            .resourceId(request.getResourceId())
            .disabledReason(request.getDisabledReason())
            .build());
        return R.ok();
    }

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

    @PutMapping("/video-mark/disable")
    public R<Void> disableVideoMark(@RequestBody VideoMarkDisableRequest request) {
        manuscriptReviewService.disableVideoMark(DisableManuscriptReviewVideoMarkCommand.builder()
            .markId(request.getMarkId())
            .disabledReason(request.getDisabledReason())
            .build());
        return R.ok();
    }

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

    private Long requireReviewId(Long reviewId) {
        if (reviewId == null) {
            throw new ServiceException("稿件审校流程不存在");
        }
        return reviewId;
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
}
