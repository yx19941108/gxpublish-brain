package com.gxpublish.brain.manuscript.review.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;
import java.util.function.LongSupplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gxpublish.brain.common.core.domain.dto.FlowInstanceBizExtDTO;
import com.gxpublish.brain.common.core.domain.dto.StartProcessDTO;
import com.gxpublish.brain.common.core.domain.event.ProcessEvent;
import com.gxpublish.brain.common.core.domain.event.ProcessTaskEvent;
import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.common.core.service.WorkflowService;
import com.gxpublish.brain.common.mybatis.utils.IdGeneratorUtil;
import com.gxpublish.brain.manuscript.review.domain.command.AddManuscriptReviewResourceCommand;
import com.gxpublish.brain.manuscript.review.domain.command.AddManuscriptReviewVideoMarkCommand;
import com.gxpublish.brain.manuscript.review.domain.command.CreateManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.command.DisableManuscriptReviewResourceCommand;
import com.gxpublish.brain.manuscript.review.domain.command.DisableManuscriptReviewVideoMarkCommand;
import com.gxpublish.brain.manuscript.review.domain.command.ResubmitManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.command.UpdateManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewAttachmentEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewExternalLinkEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewFlowConfigEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewHistoryEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewRecordEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewVideoMarkerEntity;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewCurrentUserGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewSerialGateway;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewAttachmentMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewExternalLinkMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewFlowConfigMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewHistoryMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewRecordMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemRoleMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserRoleMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewVideoMarkerMapper;
import com.gxpublish.brain.workflow.domain.bo.FlowCancelBo;
import com.gxpublish.brain.workflow.service.IFlwInstanceService;

@Service
public class ManuscriptReviewService {

    private static final String DEFAULT_TENANT_ID = "000000";
    private static final String ENABLED = "1";
    private static final String DISABLED = "0";
    private static final String CURRENT_USER_REQUIRED_MESSAGE = "当前登录用户不存在";
    private static final String REVIEW_NOT_FOUND_MESSAGE = "稿件审校流程不存在";
    private static final String RESOURCE_REQUIRED_MESSAGE = "稿件至少需要一种有效资源";
    private static final String RESOURCE_NOT_FOUND_MESSAGE = "资源不存在";
    private static final String VIDEO_MARK_NOT_FOUND_MESSAGE = "视频标注不存在";
    private static final String VIDEO_RESOURCE_INVALID_MESSAGE = "视频资源不存在或已停用";
    private static final String VIDEO_DURATION_MISSING_MESSAGE = "视频总时长未识别";
    private static final String TIME_RANGE_INVALID_MESSAGE = "标注结束时间不能早于开始时间";
    private static final String TIME_OUT_OF_DURATION_MESSAGE = "标注时间不能超出视频总时长";
    private static final String AUTHOR_DELIMITER_INVALID_MESSAGE = "作者仅允许使用中文顿号分隔";
    private static final String EXTERNAL_CODE_DUPLICATED_MESSAGE = "外部稿件编号已存在";
    private static final String SUBMIT_DEPARTMENT_IMMUTABLE_MESSAGE = "报送部门不允许修改";
    private static final String RESOURCE_TYPE_INVALID_MESSAGE = "资源类型不支持";
    private static final String EXTERNAL_LINK_PROTOCOL_INVALID_MESSAGE = "外链只允许http/https协议";
    private static final String EXTERNAL_LINK_DUPLICATE_MESSAGE = "同一流程内URL不允许重复";
    private static final String CANCEL_ONLY_INITIATOR_MESSAGE = "仅发起人本人可撤销";
    private static final String CANCEL_ONLY_WAITING_MESSAGE = "仅审批中的流程可撤销";
    private static final String RESUBMIT_ONLY_RETURNED_MESSAGE = "仅退回给发起人的流程可再次提交";
    private static final String UPDATE_PERMISSION_DENIED_MESSAGE = "当前用户无权修改该流程";
    private static final String FLOW_CONFIG_MISSING_MESSAGE = "审校流程审批链配置缺失";
    private static final String CERTIFIED_ROLE_RESOLUTION_MESSAGE = "持证发起人角色解析失败";
    private static final String FIRST_APPROVER_PERMISSION_VAR = "manuscriptReviewFirstLevelApprover";
    private static final String SECOND_APPROVER_PERMISSION_VAR = "manuscriptReviewSecondLevelApprover";
    private static final String THIRD_APPROVER_PERMISSION_VAR = "manuscriptReviewThirdLevelApprover";
    private static final String ROLE_STATUS_ACTIVE = "0";
    private static final String LEVEL_ONE_NODE = "待一级审批";
    private static final String LEVEL_TWO_NODE = "待二级审批";
    private static final String LEVEL_THREE_NODE = "待三级审批";
    private static final String ROLE_KEY_CERTIFIED_INITIATOR = "manuscript_review_certified_initiator";
    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");

    private final ManuscriptReviewRecordMapper recordMapper;
    private final ManuscriptReviewAttachmentMapper attachmentMapper;
    private final ManuscriptReviewExternalLinkMapper externalLinkMapper;
    private final ManuscriptReviewVideoMarkerMapper videoMarkerMapper;
    private final ManuscriptReviewHistoryMapper historyMapper;
    private final ManuscriptReviewFlowConfigMapper flowConfigMapper;
    private final ManuscriptReviewSystemRoleMapper roleMapper;
    private final ManuscriptReviewSystemUserRoleMapper userRoleMapper;
    private final ManuscriptReviewSystemUserMapper userMapper;
    private final ManuscriptReviewSerialGateway serialGateway;
    private final ManuscriptReviewCurrentUserGateway currentUserGateway;
    private final WorkflowService workflowService;
    private final IFlwInstanceService flwInstanceService;
    private final Clock clock;
    private final LongSupplier idGenerator;

    public ManuscriptReviewService(ManuscriptReviewRecordMapper recordMapper,
                                   ManuscriptReviewAttachmentMapper attachmentMapper,
                                   ManuscriptReviewExternalLinkMapper externalLinkMapper,
                                   ManuscriptReviewVideoMarkerMapper videoMarkerMapper,
                                   ManuscriptReviewHistoryMapper historyMapper,
                                   ManuscriptReviewFlowConfigMapper flowConfigMapper,
                                   ManuscriptReviewSystemRoleMapper roleMapper,
                                   ManuscriptReviewSystemUserRoleMapper userRoleMapper,
                                   ManuscriptReviewSystemUserMapper userMapper,
                                   ManuscriptReviewSerialGateway serialGateway,
                                   ManuscriptReviewCurrentUserGateway currentUserGateway,
                                   WorkflowService workflowService,
                                   IFlwInstanceService flwInstanceService,
                                   Clock clock,
                                   LongSupplier idGenerator) {
        this.recordMapper = recordMapper;
        this.attachmentMapper = attachmentMapper;
        this.externalLinkMapper = externalLinkMapper;
        this.videoMarkerMapper = videoMarkerMapper;
        this.historyMapper = historyMapper;
        this.flowConfigMapper = flowConfigMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.userMapper = userMapper;
        this.serialGateway = serialGateway;
        this.currentUserGateway = currentUserGateway;
        this.workflowService = workflowService;
        this.flwInstanceService = flwInstanceService;
        this.clock = clock;
        this.idGenerator = idGenerator;
    }

    @Autowired
    public ManuscriptReviewService(ManuscriptReviewRecordMapper recordMapper,
                                   ManuscriptReviewAttachmentMapper attachmentMapper,
                                   ManuscriptReviewExternalLinkMapper externalLinkMapper,
                                   ManuscriptReviewVideoMarkerMapper videoMarkerMapper,
                                   ManuscriptReviewHistoryMapper historyMapper,
                                   ManuscriptReviewFlowConfigMapper flowConfigMapper,
                                   ManuscriptReviewSystemRoleMapper roleMapper,
                                   ManuscriptReviewSystemUserRoleMapper userRoleMapper,
                                   ManuscriptReviewSystemUserMapper userMapper,
                                   ManuscriptReviewSerialGateway serialGateway,
                                   ManuscriptReviewCurrentUserGateway currentUserGateway,
                                   WorkflowService workflowService,
                                   IFlwInstanceService flwInstanceService) {
        this(
            recordMapper,
            attachmentMapper,
            externalLinkMapper,
            videoMarkerMapper,
            historyMapper,
            flowConfigMapper,
            roleMapper,
            userRoleMapper,
            userMapper,
            serialGateway,
            currentUserGateway,
            workflowService,
            flwInstanceService,
            Clock.system(BUSINESS_ZONE_ID),
            IdGeneratorUtil::nextLongId
        );
    }

    public Long create(CreateManuscriptReviewCommand command) {
        ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
        entity.setId(nextId());
        applyWriteFields(entity, command.getProcessType(), command.getExternalManuscriptCode(), command.getTitle(),
            command.getMediaChannel(), command.getSubmitDepartment(), command.getAuthorName(), command.getRemark(),
            command.getContentBody(), null);
        initializePreSubmitFlowFields(entity, command.getProcessType());
        fillCreateAuditFields(entity);
        recordMapper.insert(entity);
        insertActorHistory(entity.getId(), "CREATE", currentUsername() + "新增了审校流程单《" + entity.getTitle() + "》。");
        return entity.getId();
    }

    public void update(UpdateManuscriptReviewCommand command) {
        Long reviewId = requireReviewId(command.getId());
        ManuscriptReviewRecordEntity existing = requireRecord(reviewId);
        assertSubmitDepartmentReadonly(existing.getSubmitDepartment(), command.getSubmitDepartment());
        ensureCanModify(existing);

        ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
        entity.setId(reviewId);
        applyWriteFields(entity, command.getProcessType(), command.getExternalManuscriptCode(), command.getTitle(),
            command.getMediaChannel(), command.getSubmitDepartment(), command.getAuthorName(), command.getRemark(),
            command.getContentBody(), reviewId);
        entity.setFlowCode(resolveFlowCode(command.getProcessType()));
        fillUpdateAuditFields(entity);
        recordMapper.updateById(entity);
        insertActorHistory(reviewId, "UPDATE", buildUpdateHistoryText(existing, entity));
    }

    public String submitAndFlowStart(Long reviewId) {
        ManuscriptReviewRecordEntity existing = requireRecord(reviewId);
        ensureHasAtLeastOneEffectiveResource(reviewId);
        SubmissionRoute route = resolveSubmissionRoute(existing);
        String manuscriptCode = buildManuscriptCode(route.processType());
        Long flowInstanceId = startWorkflowOrThrow(reviewId, route, manuscriptCode, existing.getTitle());
        Date now = now();
        ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
        entity.setId(reviewId);
        entity.setFlowCode(route.flowCode());
        entity.setFlowInstanceId(flowInstanceId);
        entity.setFlowStatusLabel("审批中");
        entity.setCurrentNodeLabel(route.currentNodeLabel());
        entity.setManuscriptCode(manuscriptCode);
        entity.setFirstSubmitTime(existing.getFirstSubmitTime() == null ? now : existing.getFirstSubmitTime());
        entity.setLatestSubmitTime(now);
        fillUpdateAuditFields(entity);
        recordMapper.updateById(entity);
        insertActorHistory(reviewId, "SUBMIT", currentUsername() + "提交了审校流程单。");
        if (route.skipLevelOne()) {
            insertSystemHistory(reviewId, "SKIP_LEVEL_1", "系统判定发起人具备持证资格，自动跳过一级审批");
        }
        return entity.getManuscriptCode();
    }

    public void resubmit(ResubmitManuscriptReviewCommand command) {
        ManuscriptReviewRecordEntity existing = requireRecord(requireReviewId(command.getReviewId()));
        if (!isReturnedToInitiator(existing) || !Objects.equals(requireCurrentUserId(), existing.getInitiatorUserId())) {
            throw new ServiceException(RESUBMIT_ONLY_RETURNED_MESSAGE);
        }
        ensureHasAtLeastOneEffectiveResource(existing.getId());
        SubmissionRoute route = resolveSubmissionRoute(existing);
        String manuscriptCode = trimToNull(existing.getManuscriptCode()) == null ? buildManuscriptCode(route.processType()) : existing.getManuscriptCode();
        Long flowInstanceId = startWorkflowOrThrow(existing.getId(), route, manuscriptCode, existing.getTitle());
        Date now = now();
        ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
        entity.setId(existing.getId());
        entity.setFlowCode(route.flowCode());
        entity.setFlowInstanceId(flowInstanceId);
        entity.setFlowStatusLabel("审批中");
        entity.setCurrentNodeLabel(route.currentNodeLabel());
        entity.setManuscriptCode(manuscriptCode);
        entity.setFirstSubmitTime(existing.getFirstSubmitTime() == null ? now : existing.getFirstSubmitTime());
        entity.setLatestSubmitTime(now);
        fillUpdateAuditFields(entity);
        recordMapper.updateById(entity);
        insertActorHistory(existing.getId(), "RESUBMIT", currentUsername() + "再次提交了审校流程单。");
        if (route.skipLevelOne()) {
            insertSystemHistory(existing.getId(), "SKIP_LEVEL_1", "系统判定发起人具备持证资格，自动跳过一级审批");
        }
    }

    public void cancelProcessApply(Long reviewId, String reason) {
        ManuscriptReviewRecordEntity existing = requireRecord(requireReviewId(reviewId));
        Long currentUserId = requireCurrentUserId();
        if (!Objects.equals(currentUserId, existing.getInitiatorUserId())) {
            throw new ServiceException(CANCEL_ONLY_INITIATOR_MESSAGE);
        }
        if (!"审批中".equals(trimToNull(existing.getFlowStatusLabel()))) {
            throw new ServiceException(CANCEL_ONLY_WAITING_MESSAGE);
        }
        FlowCancelBo flowCancelBo = new FlowCancelBo();
        flowCancelBo.setBusinessId(reviewId.toString());
        flowCancelBo.setMessage(trimToNull(reason));
        flwInstanceService.cancelProcessApply(flowCancelBo);
        ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
        entity.setId(existing.getId());
        entity.setFlowInstanceId(existing.getFlowInstanceId());
        entity.setFlowStatusLabel("已取消");
        entity.setCurrentNodeLabel("流程已取消");
        entity.setRemark(trimToNull(reason));
        fillUpdateAuditFields(entity);
        recordMapper.updateById(entity);
        insertActorHistory(existing.getId(), "CANCEL", currentUsername() + "撤销了审校流程单。");
    }

    @EventListener(condition = "#processEvent.flowCode.startsWith('manuscript_review_')")
    public void processHandler(ProcessEvent processEvent) {
        Long reviewId = parseReviewId(processEvent.getBusinessId());
        if (reviewId == null || requireRecordIfPresent(reviewId) == null) {
            return;
        }
        if (Boolean.TRUE.equals(processEvent.getSubmit())) {
            ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
            entity.setId(reviewId);
            entity.setFlowInstanceId(processEvent.getInstanceId());
            entity.setFlowStatusLabel("审批中");
            entity.setUpdateTime(now());
            recordMapper.updateById(entity);
            return;
        }
        String status = trimToNull(processEvent.getStatus());
        if (status == null) {
            return;
        }
        ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
        entity.setId(reviewId);
        entity.setFlowInstanceId(processEvent.getInstanceId());
        entity.setUpdateTime(now());
        switch (status) {
            case "waiting" -> {
                entity.setFlowStatusLabel("待审批");
                recordMapper.updateById(entity);
            }
            case "cancel" -> {
                entity.setFlowStatusLabel("已取消");
                entity.setCurrentNodeLabel("流程已取消");
                recordMapper.updateById(entity);
            }
            case "back" -> {
                entity.setFlowStatusLabel("已退回");
                entity.setCurrentNodeLabel("待发起人处理");
                recordMapper.updateById(entity);
                insertWorkflowHistory(reviewId, processEvent.getTenantId(), "BACK", "流程已退回发起人处理。");
            }
            case "finish" -> {
                entity.setFlowStatusLabel("已完成");
                entity.setCurrentNodeLabel("流程完成");
                recordMapper.updateById(entity);
                insertWorkflowHistory(reviewId, processEvent.getTenantId(), "FINISH", "流程审批已完成。");
            }
            case "termination" -> {
                entity.setFlowStatusLabel("已驳回");
                entity.setCurrentNodeLabel("流程已驳回");
                recordMapper.updateById(entity);
                insertWorkflowHistory(reviewId, processEvent.getTenantId(), "REJECT", buildRejectWorkflowHistoryText(processEvent));
            }
            default -> {
            }
        }
    }

    @EventListener(condition = "#processTaskEvent.flowCode.startsWith('manuscript_review_')")
    public void processTaskHandler(ProcessTaskEvent processTaskEvent) {
        Long reviewId = parseReviewId(processTaskEvent.getBusinessId());
        if (reviewId == null || requireRecordIfPresent(reviewId) == null) {
            return;
        }
        String status = trimToNull(processTaskEvent.getStatus());
        if ("back".equals(status) || "cancel".equals(status) || "finish".equals(status) || "termination".equals(status)) {
            return;
        }
        String nodeName = trimToNull(processTaskEvent.getNodeName());
        if (nodeName == null) {
            return;
        }
        ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
        entity.setId(reviewId);
        entity.setFlowInstanceId(processTaskEvent.getInstanceId());
        entity.setFlowStatusLabel("审批中");
        entity.setCurrentNodeLabel(nodeName);
        entity.setUpdateTime(now());
        recordMapper.updateById(entity);
        String approvalHistoryText = shouldWriteApprovalHistory(processTaskEvent.getNodeCode(), processTaskEvent.getParams())
            ? buildApprovalWorkflowHistoryText(processTaskEvent.getNodeCode(), processTaskEvent.getParams())
            : null;
        if (approvalHistoryText != null) {
            insertWorkflowHistory(reviewId, processTaskEvent.getTenantId(), "APPROVE", approvalHistoryText);
        }
    }

    public Long addResource(AddManuscriptReviewResourceCommand command) {
        Long reviewId = requireReviewId(command.getReviewId());
        ensureCanModify(requireRecord(reviewId));
        String resourceType = normalizeResourceType(command.getResourceType());
        return switch (resourceType) {
            case "ATTACHMENT", "VIDEO" -> insertAttachmentResource(reviewId, command, "VIDEO".equals(resourceType));
            case "EXTERNAL_LINK" -> insertExternalLink(reviewId, command);
            default -> throw new ServiceException(RESOURCE_TYPE_INVALID_MESSAGE);
        };
    }

    public void disableResource(DisableManuscriptReviewResourceCommand command) {
        Long resourceId = command.getResourceId();
        if (resourceId == null) {
            throw new ServiceException(RESOURCE_NOT_FOUND_MESSAGE);
        }
        ManuscriptReviewAttachmentEntity attachment = attachmentMapper.selectById(resourceId);
        if (attachment != null) {
            ensureCanModify(requireRecord(attachment.getReviewId()));
            disableAttachment(attachment, command.getDisabledReason());
            return;
        }
        ManuscriptReviewExternalLinkEntity externalLink = externalLinkMapper.selectById(resourceId);
        if (externalLink != null) {
            ensureCanModify(requireRecord(externalLink.getReviewId()));
            disableExternalLink(externalLink, command.getDisabledReason());
            return;
        }
        throw new ServiceException(RESOURCE_NOT_FOUND_MESSAGE);
    }

    public Long addVideoMark(AddManuscriptReviewVideoMarkCommand command) {
        Long reviewId = requireReviewId(command.getReviewId());
        ensureCanModify(requireRecord(reviewId));
        ManuscriptReviewAttachmentEntity videoResource = attachmentMapper.selectById(command.getResourceId());
        if (videoResource == null || !Boolean.TRUE.equals(videoResource.getIsVideo()) || !ENABLED.equals(videoResource.getEnabled())) {
            throw new ServiceException(VIDEO_RESOURCE_INVALID_MESSAGE);
        }
        Integer durationSeconds = videoResource.getVideoDurationSeconds();
        if (durationSeconds == null || durationSeconds < 0) {
            throw new ServiceException(VIDEO_DURATION_MISSING_MESSAGE);
        }
        NormalizedTime start = parseTime(command.getStartTimeText(), "开始");
        NormalizedTime end = parseNullableTime(command.getEndTimeText(), "结束");
        if (end != null && end.seconds() < start.seconds()) {
            throw new ServiceException(TIME_RANGE_INVALID_MESSAGE);
        }
        if (start.seconds() > durationSeconds || (end != null && end.seconds() > durationSeconds)) {
            throw new ServiceException(TIME_OUT_OF_DURATION_MESSAGE);
        }

        ManuscriptReviewVideoMarkerEntity entity = new ManuscriptReviewVideoMarkerEntity();
        entity.setId(nextId());
        entity.setTenantId(normalizeTenantId(currentUserGateway.getCurrentTenantId()));
        entity.setReviewId(reviewId);
        entity.setVideoAttachmentId(videoResource.getId());
        entity.setStartTime(start.text());
        entity.setStartSeconds(start.seconds());
        entity.setEndTime(end == null ? null : end.text());
        entity.setEndSeconds(end == null ? null : end.seconds());
        entity.setMarkerNote(trimToNull(command.getMarkContent()));
        entity.setEnabled(ENABLED);
        fillCreateAuditFields(entity);
        videoMarkerMapper.insert(entity);
        insertActorHistory(reviewId, "VIDEO_MARK_ADD", buildVideoMarkAddHistoryText(entity));
        return entity.getId();
    }

    public void disableVideoMark(DisableManuscriptReviewVideoMarkCommand command) {
        Long markId = command.getMarkId();
        if (markId == null) {
            throw new ServiceException(VIDEO_MARK_NOT_FOUND_MESSAGE);
        }
        ManuscriptReviewVideoMarkerEntity marker = videoMarkerMapper.selectById(markId);
        if (marker == null) {
            throw new ServiceException(VIDEO_MARK_NOT_FOUND_MESSAGE);
        }
        ensureCanModify(requireRecord(marker.getReviewId()));
        ManuscriptReviewVideoMarkerEntity entity = new ManuscriptReviewVideoMarkerEntity();
        entity.setId(marker.getId());
        entity.setEnabled(DISABLED);
        entity.setDisabledBy(requireCurrentUserId());
        entity.setDisabledTime(now());
        entity.setRemark(trimToNull(command.getDisabledReason()));
        fillUpdateAuditFields(entity);
        videoMarkerMapper.updateById(entity);
        insertActorHistory(marker.getReviewId(), "VIDEO_MARK_DISABLE", buildVideoMarkDisableHistoryText(marker));
    }

    private void applyWriteFields(ManuscriptReviewRecordEntity entity,
                                  ManuscriptReviewProcessType processType,
                                  String externalManuscriptCode,
                                  String title,
                                  String mediaChannel,
                                  String submitDepartment,
                                  String authorName,
                                  String remark,
                                  String contentBody,
                                  Long reviewIdForDuplicateCheck) {
        entity.setProcessType(requireProcessType(processType).name());
        entity.setExternalManuscriptCode(validateExternalManuscriptCode(externalManuscriptCode, reviewIdForDuplicateCheck));
        entity.setTitle(requireLength(trimToNull(title), "标题不能为空", "标题长度不能超过200个字符", 200));
        entity.setMediaChannel(requireLength(trimToNull(mediaChannel), "媒体/栏目不能为空", "媒体/栏目长度不能超过100个字符", 100));
        entity.setSubmitDepartment(requireLength(trimToNull(submitDepartment), "报送部门不能为空", "报送部门长度不能超过100个字符", 100));
        entity.setAuthorName(validateAuthorName(authorName));
        entity.setRemarkText(optionalLength(trimToNull(remark), "说明长度不能超过1000个字符", 1000));
        entity.setContentBody(requireLength(trimToNull(contentBody), "正文不能为空", "正文长度不能超过20000个字符", 20000));
    }

    private void initializePreSubmitFlowFields(ManuscriptReviewRecordEntity entity, ManuscriptReviewProcessType processType) {
        entity.setFlowCode(resolveFlowCode(processType));
        // Pre-submit records have not entered workflow yet; keep the runtime state semantically unset.
        entity.setFlowStatusLabel("");
        entity.setCurrentNodeLabel("");
    }

    private Long insertAttachmentResource(Long reviewId, AddManuscriptReviewResourceCommand command, boolean video) {
        if (command.getOssId() == null) {
            throw new ServiceException("附件/视频资源必须提供ossId");
        }
        ManuscriptReviewAttachmentEntity entity = new ManuscriptReviewAttachmentEntity();
        entity.setId(nextId());
        entity.setTenantId(normalizeTenantId(currentUserGateway.getCurrentTenantId()));
        entity.setReviewId(reviewId);
        entity.setOssId(command.getOssId());
        entity.setFileName(requireLength(trimToNull(command.getDisplayName()), "资源名称不能为空", "资源名称长度不能超过255个字符", 255));
        entity.setIsVideo(video);
        entity.setEnabled(ENABLED);
        fillCreateAuditFields(entity);
        attachmentMapper.insert(entity);
        insertActorHistory(reviewId, "RESOURCE_ADD", buildAttachmentAddHistoryText(entity));
        return entity.getId();
    }

    private Long insertExternalLink(Long reviewId, AddManuscriptReviewResourceCommand command) {
        String linkTitle = requireLength(trimToNull(command.getDisplayName()), "外链标题不能为空", "外链标题长度不能超过200个字符", 200);
        String externalUrl = validateExternalUrl(command.getExternalUrl());
        Long duplicateCount = externalLinkMapper.selectCount(
            new QueryWrapper<ManuscriptReviewExternalLinkEntity>()
                .eq("tenant_id", normalizeTenantId(currentUserGateway.getCurrentTenantId()))
                .eq("review_id", reviewId)
                .eq("link_url", externalUrl)
        );
        if (duplicateCount != null && duplicateCount > 0) {
            throw new ServiceException(EXTERNAL_LINK_DUPLICATE_MESSAGE);
        }

        ManuscriptReviewExternalLinkEntity entity = new ManuscriptReviewExternalLinkEntity();
        entity.setId(nextId());
        entity.setTenantId(normalizeTenantId(currentUserGateway.getCurrentTenantId()));
        entity.setReviewId(reviewId);
        entity.setLinkTitle(linkTitle);
        entity.setLinkUrl(externalUrl);
        entity.setEnabled(ENABLED);
        fillCreateAuditFields(entity);
        externalLinkMapper.insert(entity);
        insertActorHistory(reviewId, "RESOURCE_ADD", currentUsername() + "新增了外链《" + entity.getLinkTitle() + "》。");
        return entity.getId();
    }

    private void disableAttachment(ManuscriptReviewAttachmentEntity attachment, String reason) {
        ManuscriptReviewAttachmentEntity entity = new ManuscriptReviewAttachmentEntity();
        entity.setId(attachment.getId());
        entity.setEnabled(DISABLED);
        entity.setDisabledBy(requireCurrentUserId());
        entity.setDisabledTime(now());
        entity.setRemark(trimToNull(reason));
        fillUpdateAuditFields(entity);
        attachmentMapper.updateById(entity);
        insertActorHistory(attachment.getReviewId(), "RESOURCE_DISABLE", buildAttachmentDisableHistoryText(attachment));
    }

    private void disableExternalLink(ManuscriptReviewExternalLinkEntity externalLink, String reason) {
        ManuscriptReviewExternalLinkEntity entity = new ManuscriptReviewExternalLinkEntity();
        entity.setId(externalLink.getId());
        entity.setEnabled(DISABLED);
        entity.setDisabledBy(requireCurrentUserId());
        entity.setDisabledTime(now());
        entity.setRemark(trimToNull(reason));
        fillUpdateAuditFields(entity);
        externalLinkMapper.updateById(entity);
        insertActorHistory(externalLink.getReviewId(), "RESOURCE_DISABLE",
            currentUsername() + "停用了外链《" + externalLink.getLinkTitle() + "》。");
    }

    private void ensureCanModify(ManuscriptReviewRecordEntity existing) {
        Long currentUserId = requireCurrentUserId();
        if (Objects.equals(currentUserId, existing.getInitiatorUserId()) && (isDraft(existing) || isReturnedToInitiator(existing))) {
            return;
        }
        if ("审批中".equals(trimToNull(existing.getFlowStatusLabel())) && resolveCurrentApproverUserIds(existing).contains(currentUserId)) {
            return;
        }
        throw new ServiceException(UPDATE_PERMISSION_DENIED_MESSAGE);
    }

    private boolean isDraft(ManuscriptReviewRecordEntity record) {
        return trimToNull(record.getFlowStatusLabel()) == null && trimToNull(record.getCurrentNodeLabel()) == null;
    }

    private boolean isReturnedToInitiator(ManuscriptReviewRecordEntity record) {
        return "已退回".equals(trimToNull(record.getFlowStatusLabel()))
            || "待发起人处理".equals(trimToNull(record.getCurrentNodeLabel()));
    }

    private SubmissionRoute resolveSubmissionRoute(ManuscriptReviewRecordEntity record) {
        ManuscriptReviewProcessType processType = requireProcessType(record.getProcessType());
        String tenantId = normalizeTenantId(record.getTenantId());
        ManuscriptReviewFlowConfigEntity flowConfig = requireFlowConfig(tenantId, processType);
        ManuscriptReviewSystemRoleEntity levelOneRole = requireActiveRole(tenantId, trimToNull(flowConfig.getLevelOneRoleKey()));
        ManuscriptReviewSystemRoleEntity levelTwoRole = requireActiveRole(tenantId, trimToNull(flowConfig.getLevelTwoRoleKey()));
        ManuscriptReviewSystemRoleEntity levelThreeRole = requireActiveRole(tenantId, trimToNull(flowConfig.getLevelThreeRoleKey()));
        ensureRoleHasActiveMembers(tenantId, LEVEL_ONE_NODE, levelOneRole);
        ensureRoleHasActiveMembers(tenantId, LEVEL_TWO_NODE, levelTwoRole);
        ensureRoleHasActiveMembers(tenantId, LEVEL_THREE_NODE, levelThreeRole);
        boolean skipLevelOne = isCertifiedInitiator(tenantId, requireCurrentUserId());
        return new SubmissionRoute(
            processType,
            trimToNull(flowConfig.getFlowCode()),
            skipLevelOne ? LEVEL_TWO_NODE : LEVEL_ONE_NODE,
            skipLevelOne,
            toPermissionFlag(levelOneRole),
            toPermissionFlag(levelTwoRole),
            toPermissionFlag(levelThreeRole)
        );
    }

    private ManuscriptReviewFlowConfigEntity requireFlowConfig(String tenantId, ManuscriptReviewProcessType processType) {
        ManuscriptReviewFlowConfigEntity flowConfig = flowConfigMapper.selectOne(
            new QueryWrapper<ManuscriptReviewFlowConfigEntity>()
                .eq("tenant_id", tenantId)
                .eq("process_type", processType.name())
                .eq("status", ROLE_STATUS_ACTIVE)
                .last("limit 1")
        );
        if (flowConfig == null
            || trimToNull(flowConfig.getFlowCode()) == null
            || trimToNull(flowConfig.getLevelOneRoleKey()) == null
            || trimToNull(flowConfig.getLevelTwoRoleKey()) == null
            || trimToNull(flowConfig.getLevelThreeRoleKey()) == null) {
            throw new ServiceException(FLOW_CONFIG_MISSING_MESSAGE);
        }
        return flowConfig;
    }

    private ManuscriptReviewSystemRoleEntity requireActiveRole(String tenantId, String roleKey) {
        return roleMapper.selectOne(
            new QueryWrapper<ManuscriptReviewSystemRoleEntity>()
                .eq("tenant_id", tenantId)
                .eq("role_key", roleKey)
                .eq("status", ROLE_STATUS_ACTIVE)
                .eq("del_flag", ROLE_STATUS_ACTIVE)
                .last("limit 1")
        );
    }

    private void ensureRoleHasActiveMembers(String tenantId, String nodeLabel, ManuscriptReviewSystemRoleEntity role) {
        if (role == null || role.getRoleId() == null || resolveActiveUserIdsByRoleId(tenantId, role.getRoleId()).isEmpty()) {
            throw new ServiceException(nodeLabel + "审批角色无有效成员");
        }
    }

    private boolean isCertifiedInitiator(String tenantId, Long currentUserId) {
        ManuscriptReviewSystemRoleEntity certifiedRole = roleMapper.selectOne(
            new QueryWrapper<ManuscriptReviewSystemRoleEntity>()
                .eq("tenant_id", tenantId)
                .eq("role_key", ROLE_KEY_CERTIFIED_INITIATOR)
                .eq("status", ROLE_STATUS_ACTIVE)
                .eq("del_flag", ROLE_STATUS_ACTIVE)
                .last("limit 1")
        );
        if (certifiedRole == null || certifiedRole.getRoleId() == null) {
            throw new ServiceException(CERTIFIED_ROLE_RESOLUTION_MESSAGE);
        }
        List<ManuscriptReviewSystemUserRoleEntity> userRoles = userRoleMapper.selectList(
            new QueryWrapper<ManuscriptReviewSystemUserRoleEntity>().eq("role_id", certifiedRole.getRoleId())
        );
        if (userRoles.isEmpty()) {
            return false;
        }
        List<Long> candidateUserIds = userRoles.stream()
            .map(ManuscriptReviewSystemUserRoleEntity::getUserId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (candidateUserIds.isEmpty()) {
            return false;
        }
        return userMapper.selectList(
            new QueryWrapper<ManuscriptReviewSystemUserEntity>()
                .in("user_id", candidateUserIds)
                .eq("tenant_id", tenantId)
                .eq("status", ROLE_STATUS_ACTIVE)
                .eq("del_flag", ROLE_STATUS_ACTIVE)
        ).stream().map(ManuscriptReviewSystemUserEntity::getUserId).anyMatch(currentUserId::equals);
    }

    private List<Long> resolveActiveUserIdsByRoleKey(String tenantId, String roleKey) {
        ManuscriptReviewSystemRoleEntity role = requireActiveRole(tenantId, roleKey);
        if (role == null || role.getRoleId() == null) {
            return List.of();
        }
        return resolveActiveUserIdsByRoleId(tenantId, role.getRoleId());
    }

    private List<Long> resolveActiveUserIdsByRoleId(String tenantId, Long roleId) {
        if (roleId == null) {
            return List.of();
        }
        List<Long> candidateUserIds = userRoleMapper.selectList(
            new QueryWrapper<ManuscriptReviewSystemUserRoleEntity>().eq("role_id", roleId)
        ).stream().map(ManuscriptReviewSystemUserRoleEntity::getUserId).filter(Objects::nonNull).distinct().toList();
        if (candidateUserIds.isEmpty()) {
            return List.of();
        }
        return userMapper.selectList(
            new QueryWrapper<ManuscriptReviewSystemUserEntity>()
                .in("user_id", candidateUserIds)
                .eq("tenant_id", tenantId)
                .eq("status", ROLE_STATUS_ACTIVE)
                .eq("del_flag", ROLE_STATUS_ACTIVE)
        ).stream().map(ManuscriptReviewSystemUserEntity::getUserId).filter(Objects::nonNull).distinct().toList();
    }

    private List<Long> resolveCurrentApproverUserIds(ManuscriptReviewRecordEntity record) {
        String tenantId = normalizeTenantId(record.getTenantId());
        ManuscriptReviewFlowConfigEntity flowConfig = flowConfigMapper.selectOne(
            new QueryWrapper<ManuscriptReviewFlowConfigEntity>()
                .eq("tenant_id", tenantId)
                .eq("process_type", record.getProcessType())
                .eq("status", ROLE_STATUS_ACTIVE)
                .last("limit 1")
        );
        if (flowConfig == null) {
            return List.of();
        }
        String currentNodeLabel = trimToNull(record.getCurrentNodeLabel());
        String roleKey = switch (currentNodeLabel) {
            case LEVEL_ONE_NODE -> trimToNull(flowConfig.getLevelOneRoleKey());
            case LEVEL_TWO_NODE -> trimToNull(flowConfig.getLevelTwoRoleKey());
            case LEVEL_THREE_NODE -> trimToNull(flowConfig.getLevelThreeRoleKey());
            default -> null;
        };
        return roleKey == null ? List.of() : resolveActiveUserIdsByRoleKey(tenantId, roleKey);
    }

    private Long startWorkflowOrThrow(Long reviewId, SubmissionRoute route, String manuscriptCode, String title) {
        StartProcessDTO startProcess = new StartProcessDTO();
        startProcess.setBusinessId(reviewId.toString());
        startProcess.setFlowCode(route.flowCode());
        Map<String, Object> variables = new HashMap<>();
        variables.put("ignore", true);
        variables.put("processType", route.processType().name());
        variables.put("skipLevelOne", route.skipLevelOne());
        variables.put("isCertified", route.skipLevelOne());
        variables.put(FIRST_APPROVER_PERMISSION_VAR, route.firstApproverPermission());
        variables.put(SECOND_APPROVER_PERMISSION_VAR, route.secondApproverPermission());
        variables.put(THIRD_APPROVER_PERMISSION_VAR, route.thirdApproverPermission());
        startProcess.setVariables(variables);

        FlowInstanceBizExtDTO bizExt = new FlowInstanceBizExtDTO();
        bizExt.setBusinessId(reviewId.toString());
        bizExt.setBusinessCode(manuscriptCode);
        bizExt.setBusinessTitle(title);
        startProcess.setBizExt(bizExt);

        if (!workflowService.startCompleteTask(startProcess)) {
            throw new ServiceException("流程发起异常");
        }
        Long flowInstanceId = workflowService.getInstanceIdByBusinessId(reviewId.toString());
        if (flowInstanceId == null) {
            throw new ServiceException("流程实例创建异常");
        }
        return flowInstanceId;
    }

    private void insertActorHistory(Long reviewId, String actionType, String actionText) {
        ManuscriptReviewHistoryEntity entity = new ManuscriptReviewHistoryEntity();
        entity.setId(nextId());
        entity.setTenantId(normalizeTenantId(currentUserGateway.getCurrentTenantId()));
        entity.setReviewId(reviewId);
        entity.setActionType(actionType);
        entity.setActionText(actionText);
        entity.setActorUserId(requireCurrentUserId());
        entity.setActorName(currentUsername());
        entity.setCreateTime(now());
        historyMapper.insert(entity);
    }

    private void insertSystemHistory(Long reviewId, String actionType, String actionText) {
        ManuscriptReviewHistoryEntity entity = new ManuscriptReviewHistoryEntity();
        entity.setId(nextId());
        entity.setTenantId(normalizeTenantId(currentUserGateway.getCurrentTenantId()));
        entity.setReviewId(reviewId);
        entity.setActionType(actionType);
        entity.setActionText(actionText);
        entity.setCreateTime(now());
        historyMapper.insert(entity);
    }

    private void insertWorkflowHistory(Long reviewId, String tenantId, String actionType, String actionText) {
        ManuscriptReviewHistoryEntity entity = new ManuscriptReviewHistoryEntity();
        entity.setId(nextId());
        entity.setTenantId(normalizeTenantId(tenantId));
        entity.setReviewId(reviewId);
        entity.setActionType(actionType);
        entity.setActionText(actionText);
        entity.setCreateTime(now());
        historyMapper.insert(entity);
    }

    private void ensureHasAtLeastOneEffectiveResource(Long reviewId) {
        List<ManuscriptReviewAttachmentEntity> attachments = attachmentMapper.selectList(
            new QueryWrapper<ManuscriptReviewAttachmentEntity>().eq("review_id", reviewId)
        );
        List<ManuscriptReviewExternalLinkEntity> externalLinks = externalLinkMapper.selectList(
            new QueryWrapper<ManuscriptReviewExternalLinkEntity>().eq("review_id", reviewId)
        );
        boolean hasAttachment = attachments.stream().anyMatch(item -> ENABLED.equals(item.getEnabled()) && !Boolean.TRUE.equals(item.getIsVideo()));
        boolean hasVideo = attachments.stream().anyMatch(item -> ENABLED.equals(item.getEnabled()) && Boolean.TRUE.equals(item.getIsVideo()));
        boolean hasExternalLink = externalLinks.stream().anyMatch(item -> ENABLED.equals(item.getEnabled()));
        if (!hasAttachment && !hasVideo && !hasExternalLink) {
            throw new ServiceException(RESOURCE_REQUIRED_MESSAGE);
        }
    }

    private String validateExternalManuscriptCode(String externalManuscriptCode, Long reviewIdForDuplicateCheck) {
        String normalized = trimToNull(externalManuscriptCode);
        if (normalized == null) {
            return null;
        }
        if (normalized.length() > 64) {
            throw new ServiceException("外部稿件编号长度不能超过64个字符");
        }
        QueryWrapper<ManuscriptReviewRecordEntity> wrapper = new QueryWrapper<ManuscriptReviewRecordEntity>()
            .eq("tenant_id", normalizeTenantId(currentUserGateway.getCurrentTenantId()))
            .eq("external_manuscript_code", normalized);
        if (reviewIdForDuplicateCheck != null) {
            wrapper.ne("id", reviewIdForDuplicateCheck);
        }
        Long duplicateCount = recordMapper.selectCount(wrapper);
        if (duplicateCount != null && duplicateCount > 0) {
            throw new ServiceException(EXTERNAL_CODE_DUPLICATED_MESSAGE);
        }
        return normalized;
    }

    private String validateAuthorName(String authorName) {
        String normalized = requireLength(trimToNull(authorName), "作者不能为空", "作者长度不能超过200个字符", 200);
        if (normalized.contains(",") || normalized.contains("，") || normalized.contains(";") || normalized.contains("；")) {
            throw new ServiceException(AUTHOR_DELIMITER_INVALID_MESSAGE);
        }
        String[] parts = normalized.split("、", -1);
        for (String part : parts) {
            if (part == null || part.trim().isEmpty()) {
                throw new ServiceException(AUTHOR_DELIMITER_INVALID_MESSAGE);
            }
        }
        return normalized;
    }

    private String validateExternalUrl(String externalUrl) {
        String normalized = requireLength(trimToNull(externalUrl), "外链地址不能为空", "外链地址长度不能超过500个字符", 500);
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            throw new ServiceException(EXTERNAL_LINK_PROTOCOL_INVALID_MESSAGE);
        }
        return normalized;
    }

    private void assertSubmitDepartmentReadonly(String existingSubmitDepartment, String incomingSubmitDepartment) {
        String existing = trimToNull(existingSubmitDepartment);
        String incoming = trimToNull(incomingSubmitDepartment);
        if (existing != null && incoming != null && !Objects.equals(existing, incoming)) {
            throw new ServiceException(SUBMIT_DEPARTMENT_IMMUTABLE_MESSAGE);
        }
    }

    private ManuscriptReviewRecordEntity requireRecord(Long reviewId) {
        ManuscriptReviewRecordEntity record = recordMapper.selectById(reviewId);
        if (record == null) {
            throw new ServiceException(REVIEW_NOT_FOUND_MESSAGE);
        }
        return record;
    }

    private Long requireReviewId(Long reviewId) {
        if (reviewId == null) {
            throw new ServiceException(REVIEW_NOT_FOUND_MESSAGE);
        }
        return reviewId;
    }

    private ManuscriptReviewProcessType requireProcessType(ManuscriptReviewProcessType processType) {
        if (processType == null) {
            throw new ServiceException("流程类型不能为空");
        }
        return processType;
    }

    private ManuscriptReviewProcessType requireProcessType(String processType) {
        String normalized = trimToNull(processType);
        if (normalized == null) {
            throw new ServiceException("流程类型不能为空");
        }
        try {
            return ManuscriptReviewProcessType.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new ServiceException("流程类型不支持");
        }
    }

    private String normalizeResourceType(String resourceType) {
        String normalized = trimToNull(resourceType);
        if (normalized == null) {
            throw new ServiceException(RESOURCE_TYPE_INVALID_MESSAGE);
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private String requireLength(String value, String requiredMessage, String lengthMessage, int maxLength) {
        if (value == null) {
            throw new ServiceException(requiredMessage);
        }
        if (value.length() > maxLength) {
            throw new ServiceException(lengthMessage);
        }
        return value;
    }

    private String optionalLength(String value, String lengthMessage, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new ServiceException(lengthMessage);
        }
        return value;
    }

    private NormalizedTime parseNullableTime(String raw, String label) {
        String normalized = trimToNull(raw);
        return normalized == null ? null : parseTime(normalized, label);
    }

    private NormalizedTime parseTime(String raw, String label) {
        String normalized = trimToNull(raw);
        if (normalized == null) {
            throw new ServiceException("标注" + label + "时间不能为空");
        }
        String[] segments = normalized.split(":");
        try {
            int seconds;
            if (segments.length == 1) {
                seconds = Integer.parseInt(segments[0]);
            } else if (segments.length == 2) {
                if (segments[0].length() != 2 || segments[1].length() != 2) {
                    throw new NumberFormatException("mm:ss width");
                }
                int minute = Integer.parseInt(segments[0]);
                int second = Integer.parseInt(segments[1]);
                if (second < 0 || second >= 60) {
                    throw new NumberFormatException("second");
                }
                seconds = minute * 60 + second;
            } else if (segments.length == 3) {
                if (segments[0].length() != 2 || segments[1].length() != 2 || segments[2].length() != 2) {
                    throw new NumberFormatException("HH:mm:ss width");
                }
                int hour = Integer.parseInt(segments[0]);
                int minute = Integer.parseInt(segments[1]);
                int second = Integer.parseInt(segments[2]);
                if (minute < 0 || minute >= 60 || second < 0 || second >= 60) {
                    throw new NumberFormatException("hh:mm:ss");
                }
                seconds = hour * 3600 + minute * 60 + second;
            } else {
                throw new NumberFormatException("segment");
            }
            if (seconds < 0) {
                throw new NumberFormatException("negative");
            }
            return new NormalizedTime(formatSeconds(seconds), seconds);
        } catch (NumberFormatException ex) {
            throw new ServiceException("标注" + label + "时间格式不合法");
        }
    }

    private String formatSeconds(int totalSeconds) {
        int hour = totalSeconds / 3600;
        int minute = (totalSeconds % 3600) / 60;
        int second = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    private String buildManuscriptCode(ManuscriptReviewProcessType processType) {
        String businessDate = LocalDate.now(clock).format(DateTimeFormatter.BASIC_ISO_DATE);
        int serial = serialGateway.nextSerial(processType, businessDate);
        if (serial < 1) {
            serial = 1;
        }
        return processType.getManuscriptCodePrefix() + businessDate + String.format("%03d", serial);
    }

    private String resolveFlowCode(ManuscriptReviewProcessType processType) {
        return switch (requireProcessType(processType)) {
            case AUDIT -> "manuscript_review_audit_flow";
            case PROOFREAD -> "manuscript_review_proofread_flow";
        };
    }

    private String toPermissionFlag(ManuscriptReviewSystemRoleEntity role) {
        if (role == null || role.getRoleId() == null) {
            throw new ServiceException(FLOW_CONFIG_MISSING_MESSAGE);
        }
        return "role:" + role.getRoleId();
    }

    private void fillCreateAuditFields(ManuscriptReviewRecordEntity entity) {
        Date now = now();
        Long currentUserId = requireCurrentUserId();
        entity.setTenantId(normalizeTenantId(currentUserGateway.getCurrentTenantId()));
        entity.setInitiatorUserId(currentUserId);
        entity.setInitiatorName(trimToNull(currentUserGateway.getCurrentUsername()));
        entity.setCreateDept(currentUserGateway.getCurrentDeptId());
        entity.setCreateBy(currentUserId);
        entity.setUpdateBy(currentUserId);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
    }

    private void fillCreateAuditFields(ManuscriptReviewAttachmentEntity entity) {
        Date now = now();
        Long currentUserId = requireCurrentUserId();
        entity.setCreateDept(currentUserGateway.getCurrentDeptId());
        entity.setCreateBy(currentUserId);
        entity.setUpdateBy(currentUserId);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
    }

    private void fillCreateAuditFields(ManuscriptReviewExternalLinkEntity entity) {
        Date now = now();
        Long currentUserId = requireCurrentUserId();
        entity.setCreateDept(currentUserGateway.getCurrentDeptId());
        entity.setCreateBy(currentUserId);
        entity.setUpdateBy(currentUserId);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
    }

    private void fillCreateAuditFields(ManuscriptReviewVideoMarkerEntity entity) {
        Date now = now();
        Long currentUserId = requireCurrentUserId();
        entity.setCreateDept(currentUserGateway.getCurrentDeptId());
        entity.setCreateBy(currentUserId);
        entity.setUpdateBy(currentUserId);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
    }

    private void fillUpdateAuditFields(ManuscriptReviewRecordEntity entity) {
        entity.setUpdateBy(requireCurrentUserId());
        entity.setUpdateTime(now());
    }

    private void fillUpdateAuditFields(ManuscriptReviewAttachmentEntity entity) {
        entity.setUpdateBy(requireCurrentUserId());
        entity.setUpdateTime(now());
    }

    private void fillUpdateAuditFields(ManuscriptReviewExternalLinkEntity entity) {
        entity.setUpdateBy(requireCurrentUserId());
        entity.setUpdateTime(now());
    }

    private void fillUpdateAuditFields(ManuscriptReviewVideoMarkerEntity entity) {
        entity.setUpdateBy(requireCurrentUserId());
        entity.setUpdateTime(now());
    }

    private String buildUpdateHistoryText(ManuscriptReviewRecordEntity existing, ManuscriptReviewRecordEntity incoming) {
        List<String> diffItems = new ArrayList<>();
        appendTextDiff(diffItems, "外部稿件编号", existing.getExternalManuscriptCode(), incoming.getExternalManuscriptCode());
        appendTextDiff(diffItems, "标题", existing.getTitle(), incoming.getTitle());
        appendTextDiff(diffItems, "媒体/栏目", existing.getMediaChannel(), incoming.getMediaChannel());
        appendTextDiff(diffItems, "作者", existing.getAuthorName(), incoming.getAuthorName());
        appendOptionalTextDiff(diffItems, "说明", existing.getRemarkText(), incoming.getRemarkText());
        if (!Objects.equals(trimToNull(existing.getContentBody()), trimToNull(incoming.getContentBody()))) {
            diffItems.add("正文已更新");
        }
        if (diffItems.isEmpty()) {
            diffItems.add("未识别到字段差异，已重新保存《" + incoming.getTitle() + "》");
        }
        return currentUsername() + "更新了审校流程单：" + String.join("；", diffItems) + "。";
    }

    private void appendTextDiff(List<String> diffItems, String fieldLabel, String oldValue, String newValue) {
        String normalizedOld = trimToNull(oldValue);
        String normalizedNew = trimToNull(newValue);
        if (!Objects.equals(normalizedOld, normalizedNew)) {
            diffItems.add(fieldLabel + "由“" + nullToPlaceholder(normalizedOld) + "”改为“" + nullToPlaceholder(normalizedNew) + "”");
        }
    }

    private void appendOptionalTextDiff(List<String> diffItems, String fieldLabel, String oldValue, String newValue) {
        String normalizedOld = trimToNull(oldValue);
        String normalizedNew = trimToNull(newValue);
        if (!Objects.equals(normalizedOld, normalizedNew)) {
            if (normalizedOld == null) {
                diffItems.add(fieldLabel + "补充为“" + normalizedNew + "”");
                return;
            }
            if (normalizedNew == null) {
                diffItems.add(fieldLabel + "已清空");
                return;
            }
            diffItems.add(fieldLabel + "由“" + normalizedOld + "”改为“" + normalizedNew + "”");
        }
    }

    private String buildAttachmentAddHistoryText(ManuscriptReviewAttachmentEntity entity) {
        return currentUsername() + (Boolean.TRUE.equals(entity.getIsVideo()) ? "上传了视频《" : "上传了附件《")
            + entity.getFileName() + "》。";
    }

    private String buildAttachmentDisableHistoryText(ManuscriptReviewAttachmentEntity entity) {
        return currentUsername() + (Boolean.TRUE.equals(entity.getIsVideo()) ? "停用了视频《" : "停用了附件《")
            + entity.getFileName() + "》。";
    }

    private String buildVideoMarkAddHistoryText(ManuscriptReviewVideoMarkerEntity entity) {
        return currentUsername() + "新增了视频标注《" + nullToPlaceholder(trimToNull(entity.getMarkerNote()))
            + "》（" + formatVideoMarkRange(entity.getStartTime(), entity.getEndTime()) + "）。";
    }

    private String buildVideoMarkDisableHistoryText(ManuscriptReviewVideoMarkerEntity entity) {
        return currentUsername() + "停用了视频标注《" + nullToPlaceholder(trimToNull(entity.getMarkerNote()))
            + "》（" + formatVideoMarkRange(entity.getStartTime(), entity.getEndTime()) + "）。";
    }

    private String formatVideoMarkRange(String startTime, String endTime) {
        String normalizedStart = nullToPlaceholder(trimToNull(startTime));
        String normalizedEnd = trimToNull(endTime);
        return normalizedEnd == null ? normalizedStart : normalizedStart + " - " + normalizedEnd;
    }

    private String nullToPlaceholder(String value) {
        return value == null ? "未填写" : value;
    }

    private Long requireCurrentUserId() {
        Long currentUserId = currentUserGateway.getCurrentUserId();
        if (currentUserId == null) {
            throw new ServiceException(CURRENT_USER_REQUIRED_MESSAGE);
        }
        return currentUserId;
    }

    private String currentUsername() {
        String username = trimToNull(currentUserGateway.getCurrentUsername());
        return username == null ? "当前用户" : username;
    }

    private ManuscriptReviewRecordEntity requireRecordIfPresent(Long reviewId) {
        return recordMapper.selectById(reviewId);
    }

    private Long parseReviewId(String businessId) {
        String normalized = trimToNull(businessId);
        if (normalized == null) {
            return null;
        }
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String buildRejectWorkflowHistoryText(ProcessEvent processEvent) {
        Map<String, Object> params = processEvent.getParams();
        String message = trimToNull(params == null ? null : Objects.toString(params.get("message"), null));
        return message == null ? "三级审批驳回，当前流程已终止。"
            : "三级审批驳回，当前流程已终止。审批意见：" + message + "。";
    }

    private String buildApprovalWorkflowHistoryText(String nextNodeCode, Map<String, Object> params) {
        String normalizedNextNodeCode = trimToNull(nextNodeCode);
        if (normalizedNextNodeCode == null) {
            return null;
        }
        String previousNodeLabel = switch (normalizedNextNodeCode) {
            case "second-review-node" -> "一级审批";
            case "final-review-node" -> "二级审批";
            case "end-node" -> "三级审批";
            default -> null;
        };
        if (previousNodeLabel == null) {
            return null;
        }
        String message = trimToNull(params == null ? null : Objects.toString(params.get("message"), null));
        return message == null ? previousNodeLabel + "审批通过。"
            : previousNodeLabel + "审批通过。审批意见：" + message + "。";
    }

    private boolean shouldWriteApprovalHistory(String nextNodeCode, Map<String, Object> params) {
        String normalizedNextNodeCode = trimToNull(nextNodeCode);
        if (normalizedNextNodeCode == null || isSubmitTriggeredTaskCreation(params)) {
            return false;
        }
        return switch (normalizedNextNodeCode) {
            case "second-review-node", "final-review-node", "end-node" -> true;
            default -> false;
        };
    }

    private boolean isSubmitTriggeredTaskCreation(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return false;
        }
        Object submit = params.get("submit");
        if (submit instanceof Boolean submitFlag) {
            return submitFlag;
        }
        return "true".equalsIgnoreCase(Objects.toString(submit, null));
    }

    private String normalizeTenantId(String tenantId) {
        String normalized = trimToNull(tenantId);
        return normalized == null ? DEFAULT_TENANT_ID : normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Date now() {
        return new Date(clock.millis());
    }

    private Long nextId() {
        return idGenerator.getAsLong();
    }

    private record SubmissionRoute(
        ManuscriptReviewProcessType processType,
        String flowCode,
        String currentNodeLabel,
        boolean skipLevelOne,
        String firstApproverPermission,
        String secondApproverPermission,
        String thirdApproverPermission
    ) {
    }

    private record NormalizedTime(String text, int seconds) {
    }
}
