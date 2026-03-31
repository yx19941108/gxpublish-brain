package com.gxpublish.brain.manuscript.review.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;
import java.util.function.LongSupplier;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gxpublish.brain.common.core.domain.dto.FlowInstanceBizExtDTO;
import com.gxpublish.brain.common.core.domain.dto.StartProcessDTO;
import com.gxpublish.brain.common.core.domain.event.ProcessEvent;
import com.gxpublish.brain.common.core.domain.event.ProcessTaskEvent;
import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.common.core.service.WorkflowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gxpublish.brain.common.oss.factory.OssFactory;
import com.gxpublish.brain.common.mybatis.utils.IdGeneratorUtil;
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
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewAttachmentEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewExternalLinkEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewFlowConfigEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewHistoryEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewRecordEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSysOssEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSysOssExt;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewVideoMarkerEntity;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewEnabledStatusEnum;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewFlowStatusEnum;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewHistoryActionTypeEnum;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewNodeStatusEnum;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewResourceTypeEnum;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewCurrentUserGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewSerialGateway;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewAttachmentMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewExternalLinkMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewFlowConfigMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewHistoryMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewRecordMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemRoleMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSysOssMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserRoleMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewVideoMarkerMapper;
import com.gxpublish.brain.workflow.domain.bo.FlowCancelBo;
import com.gxpublish.brain.workflow.service.IFlwInstanceService;

@Service
public class ManuscriptReviewService implements IManuscriptReviewService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
    private static final String CANCEL_ONLY_WAITING_MESSAGE = "仅审批中或已退回的流程可撤销";
    private static final String RESUBMIT_ONLY_RETURNED_MESSAGE = "仅退回给发起人的流程可再次提交";
    private static final String PENDING_RESOURCE_NOT_FOUND_MESSAGE = "上传文件不存在或已被删除";
    private static final String PENDING_RESOURCE_PERMISSION_DENIED_MESSAGE = "当前用户无权删除该暂存资源";
    private static final String PENDING_RESOURCE_ALREADY_BOUND_MESSAGE = "该资源已正式入库，不能按暂存资源删除";
    private static final String PENDING_RESOURCE_INCOMPLETE_MESSAGE = "上传文件信息不完整，无法删除";
    private static final String UPDATE_PERMISSION_DENIED_MESSAGE = "当前用户无权修改该流程";
    private static final String INITIATOR_PERMISSION_DENIED_MESSAGE = "当前用户无权发起审校流程";
    private static final String FLOW_CONFIG_MISSING_MESSAGE = "审校流程审批链配置缺失";
    private static final String CERTIFIED_ROLE_RESOLUTION_MESSAGE = "持证发起人角色解析失败";
    private static final String FIRST_APPROVER_PERMISSION_VAR = "manuscriptReviewFirstLevelApprover";
    private static final String SECOND_APPROVER_PERMISSION_VAR = "manuscriptReviewSecondLevelApprover";
    private static final String THIRD_APPROVER_PERMISSION_VAR = "manuscriptReviewThirdLevelApprover";
    private static final String LEGACY_SUBMIT_HISTORY_ACTION = "SUBMIT";
    private static final String WORKFLOW_PENDING_APPROVAL_LABEL = "待审批";
    private static final String ROLE_STATUS_ACTIVE = "0";
    private static final String LEVEL_ONE_NODE = ManuscriptReviewNodeStatusEnum.LEVEL_1.getLabel();
    private static final String LEVEL_TWO_NODE = ManuscriptReviewNodeStatusEnum.LEVEL_2.getLabel();
    private static final String LEVEL_THREE_NODE = ManuscriptReviewNodeStatusEnum.LEVEL_3.getLabel();
    private static final String ROLE_KEY_CERTIFIED_INITIATOR = "manuscript_review_certified_initiator";
    private static final String ROLE_KEY_INITIATOR = "manuscript_review_initiator";
    private static final int UPDATE_HISTORY_BODY_PREVIEW_LIMIT = 60;
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
    private final ManuscriptReviewSysOssMapper sysOssMapper;
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
                                   ManuscriptReviewSysOssMapper sysOssMapper,
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
        this.sysOssMapper = sysOssMapper;
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
                                   IFlwInstanceService flwInstanceService,
                                   ManuscriptReviewSysOssMapper sysOssMapper) {
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
            sysOssMapper,
            Clock.system(BUSINESS_ZONE_ID),
            IdGeneratorUtil::nextLongId
        );
    }

    /**
     * ???????
     *
     * <p>v6.26 ?????????????????????
     * ??????????????????</p>
     *
     * @param command ??????
     * @return ???????
     */
    @Override
    public Long create(CreateManuscriptReviewCommand command) {
        ensureCurrentUserCanInitiate(normalizeTenantId(currentUserGateway.getCurrentTenantId()));
        ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
        entity.setId(nextId());
        applyWriteFields(entity, command.getProcessType(), command.getExternalManuscriptCode(), command.getTitle(),
            command.getMediaChannel(), command.getSubmitDepartment(), command.getAuthorName(), command.getRemark(),
            command.getContentBody(), null);
        initializePreSubmitFlowFields(entity, command.getProcessType());
        fillCreateAuditFields(entity);
        recordMapper.insert(entity);
        insertActorHistory(entity.getId(), ManuscriptReviewHistoryActionTypeEnum.CREATE.getCode(),
            currentUsername() + "新增了审校流程单《" + entity.getTitle() + "》。");
        return entity.getId();
    }

    /**
     * 修改现有流程单。
     *
     * @param command 主单修改命令
     * @return 无返回值
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
        SubmittedResourceSummary resourceSummary = saveSubmittedResources(
            reviewId,
            command.getAttachmentResources(),
            command.getExternalLinks(),
            false
        );
        insertActorHistory(reviewId, ManuscriptReviewHistoryActionTypeEnum.UPDATE.getCode(),
            buildUpdateHistoryText(existing, entity, resourceSummary));
    }

    /**
     * 兼容旧草稿记录的提交发起能力。
     *
     * <p>v6.26 追加改动：该方法仅保留边界内最小兼容能力，
     * 提交后仍需补写兼容链路使用的 SUBMIT 与 SKIP_LEVEL_1 历史。</p>
     *
     * @param reviewId 已存在流程单主键
     * @return 系统稿件号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitAndFlowStart(Long reviewId) {
        ManuscriptReviewRecordEntity existing = requireRecord(reviewId);
        ensureCurrentUserCanInitiate(normalizeTenantId(existing.getTenantId()));
        ensureHasAtLeastOneEffectiveResource(reviewId);
        SubmissionRoute route = resolveSubmissionRoute(existing);
        String manuscriptCode = buildManuscriptCode(route.processType());
        Long flowInstanceId = startWorkflowOrThrow(reviewId, route, manuscriptCode, existing.getTitle());
        Date now = now();
        ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
        entity.setId(reviewId);
        entity.setFlowCode(route.flowCode());
        entity.setFlowInstanceId(flowInstanceId);
        entity.setFlowStatusLabel(ManuscriptReviewFlowStatusEnum.WAITING.getLabel());
        applyCurrentNodeStatus(entity, route.currentNodeStatus());
        entity.setManuscriptCode(manuscriptCode);
        entity.setFirstSubmitTime(existing.getFirstSubmitTime() == null ? now : existing.getFirstSubmitTime());
        entity.setLatestSubmitTime(now);
        fillUpdateAuditFields(entity);
        recordMapper.updateById(entity);
        insertActorHistory(reviewId, LEGACY_SUBMIT_HISTORY_ACTION,
            currentUsername() + "提交了审校流程单。");
        if (route.skipLevelOne()) {
            insertSystemHistory(reviewId, ManuscriptReviewHistoryActionTypeEnum.SKIP_LEVEL_1.getCode(),
                buildSkipLevelOneHistoryText());
        }
        return entity.getManuscriptCode();
    }

    /**
     * 新增并提交一体化写入入口。
     *
     * <p>v6.26 追加改动：该方法在单个事务内完成主表落库、资源落库、
     * history 写入与 BPM 发起，作为新增页正式提交主入口。</p>
     *
     * @param command 新增并提交命令
     * @return 新建流程单主键
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitAndFlowStart(SubmitAndStartManuscriptReviewCommand command) {
        ManuscriptReviewProcessType processType = requireProcessType(command.getProcessType());
        String tenantId = normalizeTenantId(currentUserGateway.getCurrentTenantId());
        ensureCurrentUserCanInitiate(tenantId);
        SubmissionRoute route = resolveSubmissionRoute(buildSubmissionRouteRecord(tenantId, processType));
        Date submitBaseTime = now();
        Long reviewId = nextId();
        String manuscriptCode = buildManuscriptCode(processType);

        ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
        entity.setId(reviewId);
        applyWriteFields(entity, processType, command.getExternalManuscriptCode(), command.getTitle(),
            command.getMediaChannel(), command.getSubmitDepartment(), command.getAuthorName(), command.getRemark(),
            command.getContentBody(), null);
        entity.setFlowCode(route.flowCode());
        entity.setFlowStatusLabel(ManuscriptReviewFlowStatusEnum.WAITING.getLabel());
        applyCurrentNodeStatus(entity, route.currentNodeStatus());
        entity.setManuscriptCode(manuscriptCode);
        entity.setFirstSubmitTime(submitBaseTime);
        entity.setLatestSubmitTime(submitBaseTime);
        fillCreateAuditFields(entity);
        recordMapper.insert(entity);

        SubmittedResourceSummary resourceSummary = saveSubmittedResources(
            reviewId,
            command.getAttachmentResources(),
            command.getExternalLinks(),
            true
        );
        insertActorHistory(reviewId, ManuscriptReviewHistoryActionTypeEnum.CREATE.getCode(),
            buildCreateAndSubmitHistoryText(entity, resourceSummary), submitBaseTime);
        if (route.skipLevelOne()) {
            insertSystemHistory(reviewId, ManuscriptReviewHistoryActionTypeEnum.SKIP_LEVEL_1.getCode(),
                buildSkipLevelOneHistoryText(), submitBaseTime);
        }

        Long flowInstanceId = startWorkflowOrThrow(reviewId, route, manuscriptCode, entity.getTitle());
        ManuscriptReviewRecordEntity patch = new ManuscriptReviewRecordEntity();
        patch.setId(reviewId);
        patch.setFlowInstanceId(flowInstanceId);
        patch.setUpdateBy(requireCurrentUserId());
        patch.setUpdateTime(now());
        recordMapper.updateById(patch);
        return reviewId;
    }

    /**
     * 发起人修改后再次提交流程。
     *
     * <p>v6.26 追加改动：重新提交仍是独立后置动作，
     * 本方法只负责重提链路，不与修改保存混写。</p>
     *
     * @param command 再次提交命令
     * @return 无返回值
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
        entity.setFlowStatusLabel(ManuscriptReviewFlowStatusEnum.WAITING.getLabel());
        applyCurrentNodeStatus(entity, route.currentNodeStatus());
        entity.setManuscriptCode(manuscriptCode);
        entity.setFirstSubmitTime(existing.getFirstSubmitTime() == null ? now : existing.getFirstSubmitTime());
        entity.setLatestSubmitTime(now);
        fillUpdateAuditFields(entity);
        recordMapper.updateById(entity);
        insertActorHistory(existing.getId(), ManuscriptReviewHistoryActionTypeEnum.RESUBMIT.getCode(),
            currentUsername() + "再次提交了审校流程单。");
        if (route.skipLevelOne()) {
            insertSystemHistory(existing.getId(), ManuscriptReviewHistoryActionTypeEnum.SKIP_LEVEL_1.getCode(),
                buildSkipLevelOneHistoryText());
        }
    }

    /**
     * 发起人撤销流程。
     *
     * <p>v6.26 追加改动：撤销仍保持独立流程动作，
     * 只允许审批中且由发起人本人发起撤销。</p>
     *
     * @param reviewId 流程单主键
     * @param reason 撤销原因
     * @return 无返回值
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelProcessApply(Long reviewId, String reason) {
        ManuscriptReviewRecordEntity existing = requireRecord(requireReviewId(reviewId));
        Long currentUserId = requireCurrentUserId();
        if (!Objects.equals(currentUserId, existing.getInitiatorUserId())) {
            throw new ServiceException(CANCEL_ONLY_INITIATOR_MESSAGE);
        }
        String flowStatusLabel = trimToNull(existing.getFlowStatusLabel());
        if (!ManuscriptReviewFlowStatusEnum.WAITING.getLabel().equals(flowStatusLabel)
            && !ManuscriptReviewFlowStatusEnum.BACK.getLabel().equals(flowStatusLabel)) {
            throw new ServiceException(CANCEL_ONLY_WAITING_MESSAGE);
        }
        FlowCancelBo flowCancelBo = new FlowCancelBo();
        flowCancelBo.setBusinessId(reviewId.toString());
        flowCancelBo.setMessage(trimToNull(reason));
        flwInstanceService.cancelProcessApply(flowCancelBo);
        ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
        entity.setId(existing.getId());
        entity.setFlowInstanceId(existing.getFlowInstanceId());
        entity.setFlowStatusLabel(ManuscriptReviewFlowStatusEnum.CANCEL.getLabel());
        applyCurrentNodeStatus(entity, ManuscriptReviewNodeStatusEnum.FLOW_CANCELED);
        entity.setRemark(trimToNull(reason));
        fillUpdateAuditFields(entity);
        recordMapper.updateById(entity);
        insertActorHistory(existing.getId(), ManuscriptReviewHistoryActionTypeEnum.CANCEL.getCode(),
            currentUsername() + "撤销了审校流程单。");
    }

    /**
     * 构造用于提交路由解析的临时主单对象。
     *
     * <p>v6.26 追加改动：新增并提交一体化场景下，主单尚未持久化前，
     * 需要先根据租户和流程类型解析审批链与持证跳级规则。</p>
     *
     * @param tenantId 租户编号
     * @param processType 流程类型
     * @return 最小可用的临时主单对象
     */
    private ManuscriptReviewRecordEntity buildSubmissionRouteRecord(String tenantId, ManuscriptReviewProcessType processType) {
        ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
        entity.setTenantId(tenantId);
        entity.setProcessType(processType.name());
        return entity;
    }

    /**
     * 保存新增提交或修改保存时一并带入的资源参数。
     *
     * <p>v6.26 追加改动：该方法只负责事务内资源落表，
     * 是否单独写 RESOURCE_ADD history 由调用方通过 createAndSubmit 控制。</p>
     *
     * @param reviewId 流程单主键
     * @param attachmentResources 附件/视频参数列表
     * @param externalLinks 外链参数列表
     * @param createAndSubmit 是否为新增并提交一体化场景
     * @return 已保存资源摘要，供 history 文案组装使用
     */
    private SubmittedResourceSummary saveSubmittedResources(
        Long reviewId,
        List<SubmitAndStartManuscriptReviewCommand.SubmitAttachmentResourceCommand> attachmentResources,
        List<SubmitAndStartManuscriptReviewCommand.SubmitExternalLinkCommand> externalLinks,
        boolean createAndSubmit
    ) {
        List<SubmitAndStartManuscriptReviewCommand.SubmitAttachmentResourceCommand> normalizedAttachments =
            attachmentResources == null ? List.of() : attachmentResources;
        List<SubmitAndStartManuscriptReviewCommand.SubmitExternalLinkCommand> normalizedExternalLinks =
            externalLinks == null ? List.of() : externalLinks;
        if (createAndSubmit && normalizedAttachments.isEmpty() && normalizedExternalLinks.isEmpty()) {
            throw new ServiceException(RESOURCE_REQUIRED_MESSAGE);
        }

        List<String> attachmentNames = new ArrayList<>();
        List<String> videoNames = new ArrayList<>();
        List<String> externalLinkTitles = new ArrayList<>();

        for (SubmitAndStartManuscriptReviewCommand.SubmitAttachmentResourceCommand resource : normalizedAttachments) {
            ManuscriptReviewResourceTypeEnum resourceType = requireAttachmentSubmitType(resource.getResourceType());
            ManuscriptReviewAttachmentEntity entity = createSubmittedAttachmentEntity(
                reviewId,
                resourceType,
                resource.getDisplayName(),
                resource.getOssId()
            );
            attachmentMapper.insert(entity);
            if (resourceType.isVideo()) {
                videoNames.add(entity.getFileName());
            } else {
                attachmentNames.add(entity.getFileName());
            }
        }

        for (SubmitAndStartManuscriptReviewCommand.SubmitExternalLinkCommand externalLink : normalizedExternalLinks) {
            ManuscriptReviewExternalLinkEntity entity = createSubmittedExternalLinkEntity(
                reviewId,
                externalLink.getDisplayName(),
                externalLink.getExternalUrl()
            );
            externalLinkMapper.insert(entity);
            externalLinkTitles.add(entity.getLinkTitle());
        }
        return new SubmittedResourceSummary(attachmentNames, videoNames, externalLinkTitles);
    }

    /**
     * 校验并创建提交场景下的附件/视频实体。
     *
     * @param reviewId 流程单主键
     * @param resourceType 资源类型
     * @param displayName 显示名称
     * @param ossId OSS 主键
     * @return 已填充的附件实体
     */
    private ManuscriptReviewAttachmentEntity createSubmittedAttachmentEntity(
        Long reviewId,
        ManuscriptReviewResourceTypeEnum resourceType,
        String displayName,
        Long ossId
    ) {
        if (ossId == null) {
            throw new ServiceException("附件/视频资源必须提供ossId");
        }
        ManuscriptReviewSysOssEntity sysOss = sysOssMapper.selectById(ossId);
        if (sysOss == null) {
            throw new ServiceException("上传文件不存在或已被删除");
        }
        ManuscriptReviewSysOssExt ossExt = parseSysOssExt(sysOss.getExt1());
        ManuscriptReviewAttachmentEntity entity = new ManuscriptReviewAttachmentEntity();
        entity.setId(nextId());
        entity.setTenantId(normalizeTenantId(currentUserGateway.getCurrentTenantId()));
        entity.setReviewId(reviewId);
        entity.setOssId(ossId);
        entity.setFileName(requireLength(trimToNull(displayName), "资源名称不能为空", "资源名称长度不能超过255个字符", 255));
        populateAttachmentSnapshot(entity, sysOss, ossExt, resourceType.isVideo());
        entity.setEnabled(ManuscriptReviewEnabledStatusEnum.ENABLED.getCode());
        fillCreateAuditFields(entity);
        return entity;
    }

    /**
     * 校验并创建提交场景下的外链实体。
     *
     * @param reviewId 流程单主键
     * @param displayName 显示名称
     * @param externalUrl 外链地址
     * @return 已填充的外链实体
     */
    private ManuscriptReviewExternalLinkEntity createSubmittedExternalLinkEntity(Long reviewId, String displayName, String externalUrl) {
        String linkTitle = requireLength(trimToNull(displayName), "外链标题不能为空", "外链标题长度不能超过200个字符", 200);
        String validatedExternalUrl = validateExternalUrl(externalUrl);
        Long duplicateCount = externalLinkMapper.selectCount(
            new QueryWrapper<ManuscriptReviewExternalLinkEntity>()
                .eq("tenant_id", normalizeTenantId(currentUserGateway.getCurrentTenantId()))
                .eq("review_id", reviewId)
                .eq("link_url", validatedExternalUrl)
        );
        if (duplicateCount != null && duplicateCount > 0) {
            throw new ServiceException(EXTERNAL_LINK_DUPLICATE_MESSAGE);
        }
        ManuscriptReviewExternalLinkEntity entity = new ManuscriptReviewExternalLinkEntity();
        entity.setId(nextId());
        entity.setTenantId(normalizeTenantId(currentUserGateway.getCurrentTenantId()));
        entity.setReviewId(reviewId);
        entity.setLinkTitle(linkTitle);
        entity.setLinkUrl(validatedExternalUrl);
        entity.setEnabled(ManuscriptReviewEnabledStatusEnum.ENABLED.getCode());
        fillCreateAuditFields(entity);
        return entity;
    }

    /**
     * 校验提交场景下的附件资源类型。
     *
     * @param resourceType 资源类型编码
     * @return 合法的资源类型枚举
     */
    private ManuscriptReviewResourceTypeEnum requireAttachmentSubmitType(String resourceType) {
        ManuscriptReviewResourceTypeEnum typeEnum = ManuscriptReviewResourceTypeEnum.fromCode(trimToNull(resourceType));
        if (typeEnum == null || ManuscriptReviewResourceTypeEnum.EXTERNAL_LINK == typeEnum) {
            throw new ServiceException(RESOURCE_TYPE_INVALID_MESSAGE);
        }
        return typeEnum;
    }

    /**
     * 生成新增并提交场景下的 CREATE 历史文案。
     *
     * @param entity 主单实体
     * @param resourceSummary 提交资源摘要
     * @return 可读 CREATE 历史文案
     */
    private String buildCreateAndSubmitHistoryText(ManuscriptReviewRecordEntity entity, SubmittedResourceSummary resourceSummary) {
        List<String> summaryItems = new ArrayList<>();
        if (!resourceSummary.attachmentNames().isEmpty()) {
            summaryItems.add("附件" + joinResourceNames(resourceSummary.attachmentNames()));
        }
        if (!resourceSummary.videoNames().isEmpty()) {
            summaryItems.add("视频" + joinResourceNames(resourceSummary.videoNames()));
        }
        if (!resourceSummary.externalLinkTitles().isEmpty()) {
            summaryItems.add("外链" + joinResourceNames(resourceSummary.externalLinkTitles()));
        }
        if (summaryItems.isEmpty()) {
            return currentUsername() + "新增并提交了审校流程单《" + entity.getTitle() + "》。";
        }
        return currentUsername() + "新增并提交了审校流程单《" + entity.getTitle() + "》，并一并提交了"
            + String.join("、", summaryItems) + "。";
    }

    /**
     * 拼接资源名称列表，供 history 文案使用。
     *
     * @param resourceNames 资源名称集合
     * @return 拼接后的名称文案
     */
    private String joinResourceNames(Collection<String> resourceNames) {
        return "《" + String.join("》《", resourceNames) + "》";
    }

    /**
     * 提交资源摘要。
     *
     * @param attachmentNames 附件名称列表
     * @param videoNames 视频名称列表
     * @param externalLinkTitles 外链标题列表
     */
    private record SubmittedResourceSummary(
        List<String> attachmentNames,
        List<String> videoNames,
        List<String> externalLinkTitles
    ) {
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
            entity.setFlowStatusLabel(ManuscriptReviewFlowStatusEnum.WAITING.getLabel());
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
                entity.setFlowStatusLabel(WORKFLOW_PENDING_APPROVAL_LABEL);
                recordMapper.updateById(entity);
            }
            case "cancel" -> {
                entity.setFlowStatusLabel(ManuscriptReviewFlowStatusEnum.CANCEL.getLabel());
                applyCurrentNodeStatus(entity, ManuscriptReviewNodeStatusEnum.FLOW_CANCELED);
                recordMapper.updateById(entity);
            }
            case "back" -> {
                entity.setFlowStatusLabel(ManuscriptReviewFlowStatusEnum.BACK.getLabel());
                applyCurrentNodeStatus(entity, ManuscriptReviewNodeStatusEnum.RETURN_TO_INITIATOR);
                recordMapper.updateById(entity);
                insertWorkflowHistory(reviewId, processEvent.getTenantId(),
                    ManuscriptReviewHistoryActionTypeEnum.BACK.getCode(),
                    buildBackWorkflowHistoryText(processEvent),
                    processEvent.getParams());
            }
            case "finish" -> {
                entity.setFlowStatusLabel(ManuscriptReviewFlowStatusEnum.FINISH.getLabel());
                applyCurrentNodeStatus(entity, ManuscriptReviewNodeStatusEnum.FLOW_FINISHED);
                recordMapper.updateById(entity);
                insertWorkflowHistory(reviewId, processEvent.getTenantId(),
                    ManuscriptReviewHistoryActionTypeEnum.FINISH.getCode(),
                    buildFinishWorkflowHistoryText(processEvent),
                    processEvent.getParams());
            }
            case "termination" -> {
                entity.setFlowStatusLabel(ManuscriptReviewFlowStatusEnum.REJECT.getLabel());
                applyCurrentNodeStatus(entity, ManuscriptReviewNodeStatusEnum.FLOW_REJECTED);
                recordMapper.updateById(entity);
                insertWorkflowHistory(reviewId, processEvent.getTenantId(),
                    ManuscriptReviewHistoryActionTypeEnum.REJECT.getCode(),
                    buildRejectWorkflowHistoryText(processEvent),
                    processEvent.getParams());
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
        ManuscriptReviewNodeStatusEnum nodeStatus = resolveCurrentNodeStatus(
            trimToNull(processTaskEvent.getNodeCode()),
            trimToNull(processTaskEvent.getNodeName())
        );
        if (nodeStatus == null) {
            return;
        }
        ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
        entity.setId(reviewId);
        entity.setFlowInstanceId(processTaskEvent.getInstanceId());
        entity.setFlowStatusLabel(ManuscriptReviewFlowStatusEnum.WAITING.getLabel());
        applyCurrentNodeStatus(entity, nodeStatus);
        entity.setUpdateTime(now());
        recordMapper.updateById(entity);
        String approvalHistoryText = shouldWriteApprovalHistory(processTaskEvent.getNodeCode(), processTaskEvent.getParams())
            ? buildApprovalWorkflowHistoryText(processTaskEvent.getNodeCode(), processTaskEvent.getNodeName(), processTaskEvent.getParams())
            : null;
        if (approvalHistoryText != null) {
            insertWorkflowHistory(reviewId, processTaskEvent.getTenantId(),
                ManuscriptReviewHistoryActionTypeEnum.APPROVE.getCode(), approvalHistoryText, processTaskEvent.getParams());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePendingResource(Long ossId) {
        if (ossId == null) {
            throw new ServiceException(PENDING_RESOURCE_NOT_FOUND_MESSAGE);
        }
        ManuscriptReviewSysOssEntity sysOss = sysOssMapper.selectById(ossId);
        if (sysOss == null) {
            throw new ServiceException(PENDING_RESOURCE_NOT_FOUND_MESSAGE);
        }
        Long currentUserId = requireCurrentUserId();
        boolean ownerMatched = Objects.equals(currentUserId, sysOss.getCreateBy());
        boolean tenantMatched = Objects.equals(
            normalizeTenantId(currentUserGateway.getCurrentTenantId()),
            normalizeTenantId(sysOss.getTenantId())
        );
        if (!ownerMatched || !tenantMatched) {
            throw new ServiceException(PENDING_RESOURCE_PERMISSION_DENIED_MESSAGE);
        }
        Long boundAttachmentCount = attachmentMapper.selectCount(
            new QueryWrapper<ManuscriptReviewAttachmentEntity>().eq("oss_id", ossId)
        );
        if (boundAttachmentCount != null && boundAttachmentCount > 0) {
            throw new ServiceException(PENDING_RESOURCE_ALREADY_BOUND_MESSAGE);
        }
        String service = trimToNull(sysOss.getService());
        String url = trimToNull(sysOss.getUrl());
        if (service == null || url == null) {
            throw new ServiceException(PENDING_RESOURCE_INCOMPLETE_MESSAGE);
        }
        OssFactory.instance(service).delete(url);
        sysOssMapper.deleteById(ossId);
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

    public void enableResource(EnableManuscriptReviewResourceCommand command) {
        Long resourceId = command.getResourceId();
        if (resourceId == null) {
            throw new ServiceException(RESOURCE_NOT_FOUND_MESSAGE);
        }
        ManuscriptReviewAttachmentEntity attachment = attachmentMapper.selectById(resourceId);
        if (attachment != null) {
            ensureCanModify(requireRecord(attachment.getReviewId()));
            enableAttachment(attachment);
            return;
        }
        ManuscriptReviewExternalLinkEntity externalLink = externalLinkMapper.selectById(resourceId);
        if (externalLink != null) {
            ensureCanModify(requireRecord(externalLink.getReviewId()));
            enableExternalLink(externalLink);
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
        insertActorHistory(reviewId, ManuscriptReviewHistoryActionTypeEnum.VIDEO_MARK_ADD.getCode(),
            buildVideoMarkAddHistoryText(entity));
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
        insertActorHistory(marker.getReviewId(), ManuscriptReviewHistoryActionTypeEnum.VIDEO_MARK_DISABLE.getCode(),
            buildVideoMarkDisableHistoryText(marker));
    }

    public void enableVideoMark(EnableManuscriptReviewVideoMarkCommand command) {
        Long markId = command.getMarkId();
        if (markId == null) {
            throw new ServiceException(VIDEO_MARK_NOT_FOUND_MESSAGE);
        }
        ManuscriptReviewVideoMarkerEntity marker = videoMarkerMapper.selectById(markId);
        if (marker == null) {
            throw new ServiceException(VIDEO_MARK_NOT_FOUND_MESSAGE);
        }
        ensureCanModify(requireRecord(marker.getReviewId()));
        UpdateWrapper<ManuscriptReviewVideoMarkerEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", marker.getId())
            .set("enabled", ENABLED)
            .set("disabled_by", null)
            .set("disabled_time", null)
            .set("remark", null)
            .set("update_by", requireCurrentUserId())
            .set("update_time", now());
        videoMarkerMapper.update(null, updateWrapper);
        insertActorHistory(marker.getReviewId(), ManuscriptReviewHistoryActionTypeEnum.VIDEO_MARK_ENABLE.getCode(),
            buildVideoMarkEnableHistoryText(marker));
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
        entity.setSubmitDepartment(optionalLength(StrUtil.isEmpty(submitDepartment) ? StrUtil.EMPTY : StrUtil.trim(submitDepartment), "报送部门长度不能超过100个字符", 100));
        entity.setAuthorName(validateAuthorName(authorName));
        entity.setRemarkText(optionalLength(trimToNull(remark), "说明长度不能超过1000个字符", 1000));
        entity.setContentBody(requireLength(trimToNull(contentBody), "正文不能为空", "正文长度不能超过20000个字符", 20000));
    }

    private void initializePreSubmitFlowFields(ManuscriptReviewRecordEntity entity, ManuscriptReviewProcessType processType) {
        entity.setFlowCode(resolveFlowCode(processType));
        // Pre-submit records have not entered workflow yet; keep the runtime state semantically unset.
        entity.setFlowStatusLabel("");
        entity.setCurrentNodeStatus("");
        entity.setCurrentNodeLabel("");
    }

    private Long insertAttachmentResource(Long reviewId, AddManuscriptReviewResourceCommand command, boolean video) {
        if (command.getOssId() == null) {
            throw new ServiceException("附件/视频资源必须提供ossId");
        }
        ManuscriptReviewSysOssEntity sysOss = sysOssMapper.selectById(command.getOssId());
        if (sysOss == null) {
            throw new ServiceException("上传文件不存在或已被删除");
        }
        ManuscriptReviewSysOssExt ossExt = parseSysOssExt(sysOss.getExt1());
        ManuscriptReviewAttachmentEntity entity = new ManuscriptReviewAttachmentEntity();
        entity.setId(nextId());
        entity.setTenantId(normalizeTenantId(currentUserGateway.getCurrentTenantId()));
        entity.setReviewId(reviewId);
        entity.setOssId(command.getOssId());
        entity.setFileName(requireLength(trimToNull(command.getDisplayName()), "资源名称不能为空", "资源名称长度不能超过255个字符", 255));
        populateAttachmentSnapshot(entity, sysOss, ossExt, video);
        entity.setEnabled(ENABLED);
        fillCreateAuditFields(entity);
        attachmentMapper.insert(entity);
        insertActorHistory(reviewId, ManuscriptReviewHistoryActionTypeEnum.RESOURCE_ADD.getCode(),
            buildAttachmentAddHistoryText(entity));
        return entity.getId();
    }

    private void populateAttachmentSnapshot(ManuscriptReviewAttachmentEntity entity,
                                            ManuscriptReviewSysOssEntity sysOss,
                                            ManuscriptReviewSysOssExt ossExt,
                                            boolean video) {
        entity.setFileUrl(sysOss.getUrl());
        entity.setFileSize(ossExt == null ? null : ossExt.getFileSize());
        entity.setMimeType(ossExt == null ? null : trimToNull(ossExt.getContentType()));
        entity.setIsVideo(video);
        entity.setVideoDurationSeconds(video && ossExt != null ? ossExt.getVideoDurationSeconds() : null);
    }

    private ManuscriptReviewSysOssExt parseSysOssExt(String ext1Json) {
        String ext1 = trimToNull(ext1Json);
        if (ext1 == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(ext1, ManuscriptReviewSysOssExt.class);
        } catch (Exception exception) {
            throw new ServiceException("上传文件扩展信息解析失败");
        }
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
        insertActorHistory(reviewId, ManuscriptReviewHistoryActionTypeEnum.RESOURCE_ADD.getCode(),
            currentUsername() + "新增了外链《" + entity.getLinkTitle() + "》。");
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
        insertActorHistory(attachment.getReviewId(), ManuscriptReviewHistoryActionTypeEnum.RESOURCE_DISABLE.getCode(),
            buildAttachmentDisableHistoryText(attachment));
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
        insertActorHistory(externalLink.getReviewId(), ManuscriptReviewHistoryActionTypeEnum.RESOURCE_DISABLE.getCode(),
            currentUsername() + "停用了外链《" + externalLink.getLinkTitle() + "》。");
    }

    private void enableAttachment(ManuscriptReviewAttachmentEntity attachment) {
        UpdateWrapper<ManuscriptReviewAttachmentEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", attachment.getId())
            .set("enabled", ENABLED)
            .set("disabled_by", null)
            .set("disabled_time", null)
            .set("remark", null)
            .set("update_by", requireCurrentUserId())
            .set("update_time", now());
        attachmentMapper.update(null, updateWrapper);
        insertActorHistory(attachment.getReviewId(), ManuscriptReviewHistoryActionTypeEnum.RESOURCE_ENABLE.getCode(),
            buildAttachmentEnableHistoryText(attachment));
    }

    private void enableExternalLink(ManuscriptReviewExternalLinkEntity externalLink) {
        UpdateWrapper<ManuscriptReviewExternalLinkEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", externalLink.getId())
            .set("enabled", ENABLED)
            .set("disabled_by", null)
            .set("disabled_time", null)
            .set("remark", null)
            .set("update_by", requireCurrentUserId())
            .set("update_time", now());
        externalLinkMapper.update(null, updateWrapper);
        insertActorHistory(
            externalLink.getReviewId(),
            ManuscriptReviewHistoryActionTypeEnum.RESOURCE_ENABLE.getCode(),
            currentUsername() + "启用了外链《" + externalLink.getLinkTitle() + "》。"
        );
    }

    private void ensureCanModify(ManuscriptReviewRecordEntity existing) {
        Long currentUserId = requireCurrentUserId();
        if (Objects.equals(currentUserId, existing.getInitiatorUserId()) && (isDraft(existing) || isReturnedToInitiator(existing))) {
            return;
        }
        if (ManuscriptReviewFlowStatusEnum.WAITING.getLabel().equals(trimToNull(existing.getFlowStatusLabel()))
            && resolveCurrentApproverUserIds(existing).contains(currentUserId)) {
            return;
        }
        throw new ServiceException(UPDATE_PERMISSION_DENIED_MESSAGE);
    }

    private void ensureCurrentUserCanInitiate(String tenantId) {
        Long currentUserId = requireCurrentUserId();
        if (hasActiveRoleMember(tenantId, ROLE_KEY_INITIATOR, currentUserId) || isCertifiedInitiator(tenantId, currentUserId)) {
            return;
        }
        throw new ServiceException(INITIATOR_PERMISSION_DENIED_MESSAGE);
    }

    private boolean isDraft(ManuscriptReviewRecordEntity record) {
        return trimToNull(record.getFlowStatusLabel()) == null
            && trimToNull(record.getCurrentNodeLabel()) == null
            && trimToNull(record.getCurrentNodeStatus()) == null;
    }

    private boolean isReturnedToInitiator(ManuscriptReviewRecordEntity record) {
        return ManuscriptReviewFlowStatusEnum.BACK.getLabel().equals(trimToNull(record.getFlowStatusLabel()))
            || resolveCurrentNodeStatus(record) == ManuscriptReviewNodeStatusEnum.RETURN_TO_INITIATOR;
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
            skipLevelOne ? ManuscriptReviewNodeStatusEnum.LEVEL_2 : ManuscriptReviewNodeStatusEnum.LEVEL_1,
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

    private boolean hasActiveRoleMember(String tenantId, String roleKey, Long currentUserId) {
        return resolveActiveUserIdsByRoleKey(tenantId, roleKey).stream().anyMatch(currentUserId::equals);
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
        ManuscriptReviewNodeStatusEnum currentNodeStatus = resolveCurrentNodeStatus(record);
        if (currentNodeStatus == null) {
            return List.of();
        }
        String roleKey = switch (currentNodeStatus) {
            case LEVEL_1 -> trimToNull(flowConfig.getLevelOneRoleKey());
            case LEVEL_2 -> trimToNull(flowConfig.getLevelTwoRoleKey());
            case LEVEL_3 -> trimToNull(flowConfig.getLevelThreeRoleKey());
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
        insertActorHistory(reviewId, actionType, actionText, now());
    }

    /**
     * 写入带自定义时间的人工动作历史。
     *
     * @param reviewId 流程单主键
     * @param actionType 动作类型
     * @param actionText 动作文案
     * @param createTime 历史创建时间
     */
    private void insertActorHistory(Long reviewId, String actionType, String actionText, Date createTime) {
        ManuscriptReviewHistoryEntity entity = new ManuscriptReviewHistoryEntity();
        entity.setId(nextId());
        entity.setTenantId(normalizeTenantId(currentUserGateway.getCurrentTenantId()));
        entity.setReviewId(reviewId);
        entity.setActionType(actionType);
        entity.setActionText(actionText);
        entity.setActorUserId(requireCurrentUserId());
        entity.setActorName(currentUsername());
        entity.setCreateTime(createTime);
        entity.setSorted(nextHistorySorted(reviewId));
        historyMapper.insert(entity);
    }

    private void insertSystemHistory(Long reviewId, String actionType, String actionText) {
        insertSystemHistory(reviewId, actionType, actionText, now());
    }

    /**
     * 写入带自定义时间的系统历史。
     *
     * @param reviewId 流程单主键
     * @param actionType 动作类型
     * @param actionText 动作文案
     * @param createTime 历史创建时间
     */
    private void insertSystemHistory(Long reviewId, String actionType, String actionText, Date createTime) {
        ManuscriptReviewHistoryEntity entity = new ManuscriptReviewHistoryEntity();
        entity.setId(nextId());
        entity.setTenantId(normalizeTenantId(currentUserGateway.getCurrentTenantId()));
        entity.setReviewId(reviewId);
        entity.setActionType(actionType);
        entity.setActionText(actionText);
        entity.setActorUserId(requireCurrentUserId());
        entity.setActorName(currentUsername());
        entity.setCreateTime(createTime);
        entity.setSorted(nextHistorySorted(reviewId));
        historyMapper.insert(entity);
    }

    /**
     * 写入工作流侧同步的历史记录。
     *
     * <p>v6.26 追加改动：审批通过、退回、完成、驳回等工作流回写动作，
     * 统一通过该方法写入可读 history，保持业务时间线口径一致。</p>
     *
     * @param reviewId 流程单主键
     * @param tenantId 租户编号
     * @param actionType 动作类型编码
     * @param actionText 动作文案
     * @return 无返回值
     */
    private void insertWorkflowHistory(Long reviewId, String tenantId, String actionType, String actionText, Map<String, Object> params) {
        WorkflowActorInfo actorInfo = resolveWorkflowActorInfo(params);
        ManuscriptReviewHistoryEntity entity = new ManuscriptReviewHistoryEntity();
        entity.setId(nextId());
        entity.setTenantId(normalizeTenantId(tenantId));
        entity.setReviewId(reviewId);
        entity.setActionType(actionType);
        entity.setActionText(actionText);
        entity.setActorUserId(actorInfo.actorUserId());
        entity.setActorName(actorInfo.actorName());
        entity.setCreateTime(now());
        entity.setSorted(nextHistorySorted(reviewId));
        historyMapper.insert(entity);
    }

    private void applyCurrentNodeStatus(ManuscriptReviewRecordEntity entity, ManuscriptReviewNodeStatusEnum nodeStatus) {
        if (entity == null || nodeStatus == null) {
            return;
        }
        entity.setCurrentNodeStatus(nodeStatus.getCode());
        entity.setCurrentNodeLabel(nodeStatus.getLabel());
    }

    private ManuscriptReviewNodeStatusEnum resolveCurrentNodeStatus(ManuscriptReviewRecordEntity record) {
        if (record == null) {
            return null;
        }
        ManuscriptReviewNodeStatusEnum byCode = ManuscriptReviewNodeStatusEnum.fromCode(trimToNull(record.getCurrentNodeStatus()));
        return byCode != null ? byCode : ManuscriptReviewNodeStatusEnum.fromAnyLabel(trimToNull(record.getCurrentNodeLabel()));
    }

    private ManuscriptReviewNodeStatusEnum resolveCurrentNodeStatus(String nodeCode, String nodeName) {
        return ManuscriptReviewNodeStatusEnum.fromWorkflowTask(nodeCode, nodeName);
    }

    private int nextHistorySorted(Long reviewId) {
        List<ManuscriptReviewHistoryEntity> histories = historyMapper.selectList(
            new QueryWrapper<ManuscriptReviewHistoryEntity>()
                .eq("review_id", reviewId)
                .orderByDesc("sorted")
                .orderByDesc("create_time")
                .last("limit 1")
        );
        if (histories == null || histories.isEmpty() || histories.get(0) == null || histories.get(0).getSorted() == null) {
            return 1;
        }
        return histories.get(0).getSorted() + 1;
    }

    /**
     * 校验流程单至少存在一项有效资源。
     *
     * <p>v6.26 追加改动：新增并提交与修改后重新提交都要求资源随主单一并收敛，
     * 因此提交流程前必须统一校验附件、视频、外链不能同时为空。</p>
     *
     * @param reviewId 流程单主键
     * @return 无返回值
     */
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

    /**
     * 校验外部稿件编号并执行唯一性检查。
     *
     * <p>v6.26 追加改动：新增并提交与修改保存共用同一写入校验口径，
     * 外部稿件编号在当前租户下需保持可选但唯一。</p>
     *
     * @param externalManuscriptCode 外部稿件编号
     * @param reviewIdForDuplicateCheck 排重时需要排除的当前流程单主键
     * @return 规范化后的外部稿件编号
     */
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

    /**
     * 组装修改保存场景的 UPDATE 历史文案。
     *
     * <p>v6.26 追加改动：修改保存改为整表单一次性提交后，
     * 需要把主表字段变化与本次追加资源变化统一收敛成可读 history。</p>
     *
     * @param existing 变更前主单
     * @param incoming 变更后主单
     * @param resourceSummary 本次追加资源摘要
     * @return 可读的 UPDATE 历史文案
     */
    private String buildUpdateHistoryText(
        ManuscriptReviewRecordEntity existing,
        ManuscriptReviewRecordEntity incoming,
        SubmittedResourceSummary resourceSummary
    ) {
        List<String> diffItems = new ArrayList<>();
        appendTextDiff(diffItems, "外部稿件编号", existing.getExternalManuscriptCode(), incoming.getExternalManuscriptCode());
        appendTextDiff(diffItems, "标题", existing.getTitle(), incoming.getTitle());
        appendTextDiff(diffItems, "媒体/栏目", existing.getMediaChannel(), incoming.getMediaChannel());
        appendTextDiff(diffItems, "作者", existing.getAuthorName(), incoming.getAuthorName());
        appendOptionalTextDiff(diffItems, "说明", existing.getRemarkText(), incoming.getRemarkText());
        appendBodyDiff(diffItems, existing.getContentBody(), incoming.getContentBody());
        if (!resourceSummary.attachmentNames().isEmpty()) {
            diffItems.add("新增附件" + joinResourceNames(resourceSummary.attachmentNames()));
        }
        if (!resourceSummary.videoNames().isEmpty()) {
            diffItems.add("新增视频" + joinResourceNames(resourceSummary.videoNames()));
        }
        if (!resourceSummary.externalLinkTitles().isEmpty()) {
            diffItems.add("新增外链" + joinResourceNames(resourceSummary.externalLinkTitles()));
        }
        if (diffItems.isEmpty()) {
            diffItems.add("未识别到字段差异，已重新保存《" + incoming.getTitle() + "》");
        }
        return currentUsername() + "更新了审校流程单：" + String.join("；", diffItems) + "。";
    }

    /**
     * 追加必填字段的差异摘要。
     *
     * <p>v6.26 追加改动：为修改保存一体化场景输出可读 history，
     * 主表核心字段变化统一通过该方法转成摘要文案。</p>
     *
     * @param diffItems 差异摘要列表
     * @param fieldLabel 字段展示名称
     * @param oldValue 旧值
     * @param newValue 新值
     * @return 无返回值
     */
    private void appendTextDiff(List<String> diffItems, String fieldLabel, String oldValue, String newValue) {
        String normalizedOld = trimToNull(oldValue);
        String normalizedNew = trimToNull(newValue);
        if (!Objects.equals(normalizedOld, normalizedNew)) {
            diffItems.add(fieldLabel + "由“" + nullToPlaceholder(normalizedOld) + "”改为“" + nullToPlaceholder(normalizedNew) + "”");
        }
    }

    /**
     * 追加可选字段的差异摘要。
     *
     * <p>v6.26 追加改动：可选字段允许从空到有、从有到空，
     * 该方法统一处理补充、清空、改值三种摘要文案。</p>
     *
     * @param diffItems 差异摘要列表
     * @param fieldLabel 字段展示名称
     * @param oldValue 旧值
     * @param newValue 新值
     * @return 无返回值
     */
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

    private void appendBodyDiff(List<String> diffItems, String oldValue, String newValue) {
        String normalizedOld = trimToNull(oldValue);
        String normalizedNew = trimToNull(newValue);
        if (!Objects.equals(normalizedOld, normalizedNew)) {
            diffItems.add("正文由“" + toBodyHistoryPreview(normalizedOld) + "”改为“" + toBodyHistoryPreview(normalizedNew) + "”");
        }
    }

    private String toBodyHistoryPreview(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return "未填写";
        }
        String singleLine = normalized.replaceAll("\\s+", " ");
        if (singleLine.length() <= UPDATE_HISTORY_BODY_PREVIEW_LIMIT) {
            return singleLine;
        }
        return singleLine.substring(0, UPDATE_HISTORY_BODY_PREVIEW_LIMIT - 3) + "...";
    }

    private String buildAttachmentAddHistoryText(ManuscriptReviewAttachmentEntity entity) {
        return currentUsername() + (Boolean.TRUE.equals(entity.getIsVideo()) ? "上传了视频《" : "上传了附件《")
            + entity.getFileName() + "》。";
    }

    private String buildAttachmentDisableHistoryText(ManuscriptReviewAttachmentEntity entity) {
        return currentUsername() + (Boolean.TRUE.equals(entity.getIsVideo()) ? "停用了视频《" : "停用了附件《")
            + entity.getFileName() + "》。";
    }

    private String buildAttachmentEnableHistoryText(ManuscriptReviewAttachmentEntity entity) {
        return currentUsername() + (Boolean.TRUE.equals(entity.getIsVideo()) ? "启用了视频《" : "启用了附件《")
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

    private String buildVideoMarkEnableHistoryText(ManuscriptReviewVideoMarkerEntity entity) {
        return currentUsername() + "启用了视频标注《" + nullToPlaceholder(trimToNull(entity.getMarkerNote()))
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

    private WorkflowActorInfo resolveWorkflowActorInfo(Map<String, Object> params) {
        Long actorUserId = parseReviewId(params == null ? null : Objects.toString(params.get("handler"), null));
        if (actorUserId == null) {
            Long currentUserId = currentUserGateway.getCurrentUserId();
            return currentUserId == null
                ? new WorkflowActorInfo(0L, "系统")
                : new WorkflowActorInfo(currentUserId, currentUsername());
        }
        Long currentUserId = currentUserGateway.getCurrentUserId();
        if (Objects.equals(actorUserId, currentUserId)) {
            return new WorkflowActorInfo(actorUserId, currentUsername());
        }
        return new WorkflowActorInfo(actorUserId, "用户" + actorUserId);
    }

    private String buildSkipLevelOneHistoryText() {
        return currentUsername() + "提交流程时命中持证资格，系统自动跳过一级审批。";
    }

    private String buildBackWorkflowHistoryText(ProcessEvent processEvent) {
        WorkflowActorInfo actorInfo = resolveWorkflowActorInfo(processEvent.getParams());
        String nodeLabel = trimToNull(processEvent.getNodeName());
        String message = trimToNull(processEvent.getParams() == null ? null : Objects.toString(processEvent.getParams().get("message"), null));
        String action = actorInfo.actorName() + "在" + (nodeLabel == null ? "当前环节" : nodeLabel) + "退回了流程。";
        return message == null ? action : action + "审批意见：" + message + "。";
    }

    private String buildFinishWorkflowHistoryText(ProcessEvent processEvent) {
        return resolveWorkflowActorInfo(processEvent.getParams()).actorName() + "完成了流程审批。";
    }

    private String buildRejectWorkflowHistoryText(ProcessEvent processEvent) {
        WorkflowActorInfo actorInfo = resolveWorkflowActorInfo(processEvent.getParams());
        String nodeLabel = trimToNull(processEvent.getNodeName());
        String message = trimToNull(processEvent.getParams() == null ? null : Objects.toString(processEvent.getParams().get("message"), null));
        String action = actorInfo.actorName() + "在" + (nodeLabel == null ? "三级审批" : nodeLabel) + "驳回了流程，当前流程已终止。";
        return message == null ? action : action + "审批意见：" + message + "。";
    }

    private String buildApprovalWorkflowHistoryText(String nextNodeCode, String nodeName, Map<String, Object> params) {
        String normalizedNextNodeCode = trimToNull(nextNodeCode);
        if (normalizedNextNodeCode == null) {
            return null;
        }
        WorkflowActorInfo actorInfo = resolveWorkflowActorInfo(params);
        String previousNodeLabel = switch (normalizedNextNodeCode) {
            case "second-review-node" -> "一级审批";
            case "final-review-node" -> "二级审批";
            case "end-node" -> trimToNull(nodeName) == null ? "三级审批" : trimToNull(nodeName);
            default -> null;
        };
        if (previousNodeLabel == null) {
            return null;
        }
        String message = trimToNull(params == null ? null : Objects.toString(params.get("message"), null));
        String action = actorInfo.actorName() + "在" + previousNodeLabel + "通过了流程。";
        return message == null ? action : action + "审批意见：" + message + "。";
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

    private record WorkflowActorInfo(Long actorUserId, String actorName) {
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
        ManuscriptReviewNodeStatusEnum currentNodeStatus,
        boolean skipLevelOne,
        String firstApproverPermission,
        String secondApproverPermission,
        String thirdApproverPermission
    ) {
    }

    private record NormalizedTime(String text, int seconds) {
    }
}
