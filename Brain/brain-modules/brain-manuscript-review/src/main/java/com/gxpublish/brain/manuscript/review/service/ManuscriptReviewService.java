package com.gxpublish.brain.manuscript.review.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.manuscript.review.domain.command.CreateManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.command.ResubmitManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;
import com.gxpublish.brain.manuscript.review.domain.result.ManuscriptReviewResult;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewConfigGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewCurrentUserGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewResubmitGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewSerialGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewUserRoleGateway;

public class ManuscriptReviewService {

    private static final String FLOW_STATUS_IN_APPROVAL = "审批中";
    private static final String NODE_LEVEL_ONE = "待一级审批";
    private static final String NODE_LEVEL_TWO = "待二级审批";
    private static final String CERTIFIED_SKIP_COMMENT = "系统判定发起人具备持证资格，自动跳过一级审批";
    private static final String TITLE_REQUIRED_MESSAGE = "标题不能为空";
    private static final String CONTENT_REQUIRED_MESSAGE = "正文不能为空";
    private static final String RESOURCE_REQUIRED_MESSAGE = "附件或外链至少提供一种";
    private static final String EXTERNAL_LINK_PROTOCOL_INVALID_MESSAGE = "外链只允许http/https协议";
    private static final String EXTERNAL_LINK_DUPLICATE_MESSAGE = "同一流程内URL不允许重复";
    private static final String RESUBMIT_NOT_RETURNED_MESSAGE = "当前流程未退回发起人，不能再次提交";
    private static final String CURRENT_USER_REQUIRED_MESSAGE = "当前登录用户不存在";
    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");

    private final ManuscriptReviewConfigGateway configGateway;
    private final ManuscriptReviewSerialGateway serialGateway;
    private final ManuscriptReviewUserRoleGateway userRoleGateway;
    private final ManuscriptReviewResubmitGateway resubmitGateway;
    private final ManuscriptReviewCurrentUserGateway currentUserGateway;
    private final Clock clock;

    public ManuscriptReviewService(ManuscriptReviewConfigGateway configGateway,
                                   ManuscriptReviewSerialGateway serialGateway,
                                   ManuscriptReviewUserRoleGateway userRoleGateway,
                                   ManuscriptReviewResubmitGateway resubmitGateway,
                                   ManuscriptReviewCurrentUserGateway currentUserGateway,
                                   Clock clock) {
        this.configGateway = configGateway;
        this.serialGateway = serialGateway;
        this.userRoleGateway = userRoleGateway;
        this.resubmitGateway = resubmitGateway;
        this.currentUserGateway = currentUserGateway;
        this.clock = clock;
    }

    public ManuscriptReviewService(ManuscriptReviewConfigGateway configGateway,
                                   ManuscriptReviewSerialGateway serialGateway,
                                   ManuscriptReviewUserRoleGateway userRoleGateway,
                                   ManuscriptReviewResubmitGateway resubmitGateway,
                                   ManuscriptReviewCurrentUserGateway currentUserGateway) {
        this(configGateway, serialGateway, userRoleGateway, resubmitGateway, currentUserGateway, Clock.system(BUSINESS_ZONE_ID));
    }

    public ManuscriptReviewResult createAndSubmit(CreateManuscriptReviewCommand command) {
        validateTitleRequired(command.getTitle());
        validateContentRequired(command.getContent());
        validateResourceRequired(command.getAttachmentCount(), command.getExternalLinkCount());
        validateExternalLinkUrls(command.getExternalLinkUrls());

        ManuscriptReviewProcessType processType = command.getProcessType();
        validateSubmitPreconditions(processType);

        String businessDate = LocalDate.now(clock).format(DateTimeFormatter.BASIC_ISO_DATE);
        String manuscriptCode = buildManuscriptCode(processType, businessDate);
        Long applicantUserId = requireCurrentUserId();
        boolean certifiedApplicant = userRoleGateway.hasCertifiedApplicantRole(applicantUserId);

        return ManuscriptReviewResult.builder()
            .manuscriptCode(manuscriptCode)
            .flowStatus(FLOW_STATUS_IN_APPROVAL)
            .currentNode(certifiedApplicant ? NODE_LEVEL_TWO : NODE_LEVEL_ONE)
            .routingComment(certifiedApplicant ? CERTIFIED_SKIP_COMMENT : null)
            .build();
    }

    public ManuscriptReviewResult resubmit(ResubmitManuscriptReviewCommand command) {
        validateTitleRequired(command.getTitle());
        validateContentRequired(command.getContent());
        validateResourceRequired(command.getAttachmentCount(), command.getExternalLinkCount());
        validateExternalLinkUrls(command.getExternalLinkUrls());
        Long reviewId = command.getReviewId();
        Long applicantUserId = requireCurrentUserId();
        if (reviewId == null || !resubmitGateway.isReturnedToInitiator(reviewId, applicantUserId)) {
            throw new ServiceException(RESUBMIT_NOT_RETURNED_MESSAGE);
        }

        ManuscriptReviewProcessType processType = command.getProcessType();
        validateSubmitPreconditions(processType);
        boolean certifiedApplicant = userRoleGateway.hasCertifiedApplicantRole(applicantUserId);

        return ManuscriptReviewResult.builder()
            .flowStatus(FLOW_STATUS_IN_APPROVAL)
            .currentNode(certifiedApplicant ? NODE_LEVEL_TWO : NODE_LEVEL_ONE)
            .routingComment(certifiedApplicant ? CERTIFIED_SKIP_COMMENT : null)
            .build();
    }

    private void validateTitleRequired(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new ServiceException(TITLE_REQUIRED_MESSAGE);
        }
    }

    private void validateContentRequired(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new ServiceException(CONTENT_REQUIRED_MESSAGE);
        }
    }

    private void validateResourceRequired(int attachmentCount, int externalLinkCount) {
        if (attachmentCount < 1 && externalLinkCount < 1) {
            throw new ServiceException(RESOURCE_REQUIRED_MESSAGE);
        }
    }

    private void validateExternalLinkUrls(List<String> externalLinkUrls) {
        if (externalLinkUrls == null || externalLinkUrls.isEmpty()) {
            return;
        }
        Set<String> deduplicatedUrls = new HashSet<>();
        for (String externalLinkUrl : externalLinkUrls) {
            String normalizedUrl = externalLinkUrl == null ? "" : externalLinkUrl.trim();
            if (!normalizedUrl.startsWith("http://") && !normalizedUrl.startsWith("https://")) {
                throw new ServiceException(EXTERNAL_LINK_PROTOCOL_INVALID_MESSAGE);
            }
            if (!deduplicatedUrls.add(normalizedUrl)) {
                throw new ServiceException(EXTERNAL_LINK_DUPLICATE_MESSAGE);
            }
        }
    }

    private void validateSubmitPreconditions(ManuscriptReviewProcessType processType) {
        if (!configGateway.hasCompleteApprovalChain(processType)) {
            throw new ServiceException(processType.getDisplayName() + "缺少完整审批链配置");
        }
        if (!configGateway.hasActiveApproverMembers(processType)) {
            throw new ServiceException(processType.getDisplayName() + "审批角色缺少有效成员");
        }
    }

    private String buildManuscriptCode(ManuscriptReviewProcessType processType, String businessDate) {
        int serial = serialGateway.nextSerial(processType, businessDate);
        if (serial < 1) {
            serial = 1;
        }
        return processType.getManuscriptCodePrefix() + businessDate + String.format("%03d", serial);
    }

    private Long requireCurrentUserId() {
        Long currentUserId = currentUserGateway.getCurrentUserId();
        if (currentUserId == null) {
            throw new ServiceException(CURRENT_USER_REQUIRED_MESSAGE);
        }
        return currentUserId;
    }
}
